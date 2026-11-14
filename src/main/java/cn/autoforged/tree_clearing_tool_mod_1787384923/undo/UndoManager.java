package cn.autoforged.tree_clearing_tool_mod_1787384923.undo;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 撤销管理器（服务端）。
 *
 * 每个工具在 use() 破坏方块时，会把「被改动前的方块状态」收集起来交给本管理器，
 * 与玩家 UUID 绑定压入该玩家的撤销栈。客户端按 F7 时发送 {@code UndoPacket}，
 * 服务端在收包回调里调用 {@link #undo} 恢复上一步被破坏的方块（setBlock flag 3 会随
 * 方块变化重算光照，避免阴影残留）。
 *
 * 设计取舍：
 *  - 快照含维度信息，跨维度时不撤销（防错位），只弹出丢弃并报告无操作。
 *  - 撤销按快照内倒序应用，同一位置以「最早记录」的状态为准——例如平整工具先清空再铺
 *    草方块，同一位被记录两次时，倒序应用会正确恢复到工具使用前的原样。
 *  - 每个玩家只保留最近若干步（MAX_DEPTH），防止大面积清理时占用内存过多。
 */
public final class UndoManager {

	/** 单个被改动方块的记录：位置 + 改动前的方块状态。 */
	public record UndoBlock(BlockPos pos, BlockState state) {
	}

	/** 一次工具操作对应的完整撤销快照。 */
	public record Snapshot(ResourceKey<Level> dimension, List<UndoBlock> blocks) {
	}

	/** 每个玩家最多保留的撤销步数。 */
	private static final int MAX_DEPTH = 10;

	private static final Map<UUID, ArrayDeque<Snapshot>> STACKS = new HashMap<>();

	private UndoManager() {
	}

	/** 记录一次工具破坏操作，供该玩家后续撤销。 */
	public static void push(UUID player, ResourceKey<Level> dimension, List<UndoBlock> blocks) {
		if (blocks == null || blocks.isEmpty()) {
			return;
		}
		ArrayDeque<Snapshot> stack = STACKS.computeIfAbsent(player, k -> new ArrayDeque<>());
		stack.addFirst(new Snapshot(dimension, new ArrayList<>(blocks)));
		while (stack.size() > MAX_DEPTH) {
			stack.removeLast();
		}
	}

	/**
	 * 撤销该玩家最近一步操作。只处理与 {@code level} 相同维度的快照；
	 * 跨维度的旧快照被弹出丢弃。
	 *
	 * @return true 表示成功恢复了方块；false 表示没有可撤销的操作（或不在同一维度）。
	 */
	public static boolean undo(UUID player, ServerLevel level) {
		ArrayDeque<Snapshot> stack = STACKS.get(player);
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		while (!stack.isEmpty()) {
			Snapshot snap = stack.peekFirst();
			if (!snap.dimension().location().equals(level.dimension().location())) {
				stack.removeFirst();
				continue;
			}
			stack.removeFirst();
			List<UndoBlock> blocks = snap.blocks();
			// 倒序应用：同一位置以最早记录的状态为准（见类注释）
			for (int i = blocks.size() - 1; i >= 0; i--) {
				UndoBlock b = blocks.get(i);
				level.setBlock(b.pos(), b.state(), 3);
			}
			return true;
		}
		return false;
	}
}
