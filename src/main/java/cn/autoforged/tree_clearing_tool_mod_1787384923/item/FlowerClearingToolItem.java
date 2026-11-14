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
 * 清除花的工具：右键瞄准一块区域（以视线命中的方块为中心），清除水平 range x range
 * 范围内的所有花朵与草。
 *
 * 对应修改要求：
 *  - 清理花的同时也清理草（FLOWERS 标签 + REPLACEABLE 标签覆盖短草/高草丛等）。
 *  - 不清理水：REPLACEABLE 标签含水，故用 !is(Blocks.WATER) 排除水方块。
 *  - Shift+滚轮调节破坏范围，存于物品 NBT（键 flower_range），最大 128x128。
 *  - 清除完自动更新光照（setBlock flag 3 重算光照，避免阴影残留）。
 *  - 取消逐方块的破坏音效，只保留一次 STONE_BREAK 音效避免声音过大。
 *  - 不产生掉落物、不产生破坏粒子（只用 setBlock 置空气）。
 *
 * 假设：竖直扫描范围取命中块 Y-4 到 Y+4，覆盖地面花朵及较高植物。
 */
public class FlowerClearingToolItem extends AreaToolBaseItem {

	public static final String RANGE_KEY = "flower_range";
	public static final int MAX_RANGE = 128;
	public static final int DEFAULT_RANGE = 8;
	private static final int VERTICAL_MIN = -4;
	private static final int VERTICAL_MAX = 4;

	public FlowerClearingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			BlockPos center = findCenter(player);
			int range = Math.max(1, Math.min(MAX_RANGE, readInt(stack, RANGE_KEY, DEFAULT_RANGE)));

			java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
			// 安装了「枫」模组时，额外清理其「花草」方块（花瓣/地毯/花草），但不清理其树木
			int cleared = clearBlocks(level, center, range, VERTICAL_MIN, VERTICAL_MAX,
				s -> !s.is(Blocks.WATER) && (s.is(BlockTags.FLOWERS) || s.is(BlockTags.REPLACEABLE) || isMapleFlowerBlock(s)), undo);

			if (cleared > 0) {
				// 记录本次破坏，供玩家按撤销键恢复
				UndoManager.push(player.getUUID(), level.dimension(), undo);
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				// 只保留一个破坏音效，避免声音过大
				playBreakSound(level, player, SoundEvents.STONE_BREAK);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
