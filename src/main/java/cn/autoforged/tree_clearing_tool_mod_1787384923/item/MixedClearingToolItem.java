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
import net.minecraft.world.level.block.Blocks;

/**
 * 混合清理工具：右键瞄准一块区域（以视线命中的方块为中心），清除水平 range x range
 * 范围内的树（原木/树叶）、花、草、蘑菇等。
 *
 * 对应修改要求：
 *  - 同时清理树、花、草、蘑菇等（匹配 LOGS / LEAVES / FLOWERS / REPLACEABLE 标签）。
 *  - 不清理水：REPLACEABLE 标签含水，故用 !is(Blocks.WATER) 排除水方块。
 *  - 清理完成后自动更新光照（setBlock flag 3 重算光照）。
 *  - Shift+滚轮调节破坏范围，存于物品 NBT（键 mixed_range），最大 256x256。
 *  - 只用于创造模式：不产生掉落物、不产生破坏粒子，只在完成后播放一次 STONE_BREAK 音效。
 *
 * 假设（需求未明示，采用最合理方案）：
 *  - range 为水平边长，中心为视线命中方块；竖直扫描范围取命中块 Y-4 到 Y+128，
 *    足以覆盖绝大多数树木高度（与清树工具一致）。
 */
public class MixedClearingToolItem extends AreaToolBaseItem {

	public static final String RANGE_KEY = "mixed_range";
	public static final int MAX_RANGE = 256;
	public static final int DEFAULT_RANGE = 8;
	private static final int VERTICAL_MIN = -4;
	private static final int VERTICAL_MAX = 128;

	public MixedClearingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			BlockPos center = findCenter(player);
			int range = Math.max(1, Math.min(MAX_RANGE, readInt(stack, RANGE_KEY, DEFAULT_RANGE)));

			java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
			// 安装了「枫」模组时，树与花草一并清理（isMapleBlock 覆盖全部 maple 命名空间方块）
			int cleared = clearBlocks(level, center, range, VERTICAL_MIN, VERTICAL_MAX,
				s -> !s.is(Blocks.WATER) && (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)
					|| s.is(BlockTags.FLOWERS) || s.is(BlockTags.REPLACEABLE) || isMapleBlock(s)), undo);

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
