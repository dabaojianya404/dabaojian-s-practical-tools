package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import cn.autoforged.tree_clearing_tool_mod_1787384923.undo.UndoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 清树工具：右键瞄准一块区域（以视线命中的方块为中心），清除水平 range x range
 * 范围内的所有原木与树叶。
 *
 * 修改要求对应的行为：
 *  - 已取消两秒冷却（不再调用 addCooldown）。
 *  - 破坏方块时不再播放逐方块音效/粒子/掉落，只在破坏完成后播放一次 STONE_BREAK 音效。
 *  - 破坏范围可由 Shift+滚轮在客户端调节，存于物品 NBT（键 tree_range），最大 256x256。
 *  - 只用于创造模式：不产生掉落物、不产生破坏粒子，减轻大面积破坏时的卡顿。
 *  - 用 setBlock 置空气（flag 3）自动重算光照，避免阴影残留。
 *
 * 假设（需求未明示，采用最合理方案）：
 *  - range 为水平边长，中心为视线命中方块；竖直扫描范围取命中块 Y-4 到 Y+128，
 *    足以覆盖绝大多数树木高度。
 */
public class TreeClearingToolItem extends AreaToolBaseItem {

	public static final String RANGE_KEY = "tree_range";
	public static final int MAX_RANGE = 256;
	public static final int DEFAULT_RANGE = 12;
	private static final int VERTICAL_MIN = -4;
	private static final int VERTICAL_MAX = 128;

	public TreeClearingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			BlockPos center = findCenter(player);
			int range = Math.max(1, Math.min(MAX_RANGE, readInt(stack, RANGE_KEY, DEFAULT_RANGE)));

			java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
			// 安装了「枫」模组时，额外清理其「树木」方块（树叶/原木/木头/树苗），但不清理其花草
			int cleared = clearBlocks(level, center, range, VERTICAL_MIN, VERTICAL_MAX,
				s -> s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES) || isMapleTreeBlock(s), undo);

			if (cleared > 0) {
				// 记录本次破坏，供玩家按撤销键恢复
				UndoManager.push(player.getUUID(), level.dimension(), undo);
				// 创造模式下 hurtAndBreak 不扣耐久（见 ItemStack.hurtAndBreak 的 instabuild 判断）
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				playBreakSound(level, player, SoundEvents.STONE_BREAK);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
