package cn.dabaojian.dabaojians_practical_tools.item;

import cn.dabaojian.dabaojians_practical_tools.undo.UndoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

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
			int cleared = clearBlocks(level, center, range, VERTICAL_MIN, VERTICAL_MAX,
				s -> !s.is(Blocks.WATER) && (s.is(BlockTags.FLOWERS) || s.is(BlockTags.REPLACEABLE) || isMapleFlowerBlock(s)), undo);

			if (cleared > 0) {
				UndoManager.push(player.getUUID(), level.dimension(), undo);
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
				playBreakSound(level, player, SoundEvents.STONE_BREAK);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}
}
