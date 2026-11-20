package cn.dabaojian.dabaojians_practical_tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LightUpdateToolItem extends Item {

	public static final String RANGE_KEY = "light_range";
	public static final int DEFAULT_RANGE = 16;
	public static final int MAX_RANGE = 64;
	private static final int[] PRESETS = { 8, 16, 32, 64 };

	public LightUpdateToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		int range = Math.max(1, Math.min(MAX_RANGE, AreaToolBaseItem.readInt(stack, RANGE_KEY, DEFAULT_RANGE)));
		if (player.isShiftKeyDown()) {
			int next = nextPreset(range);
			AreaToolBaseItem.setInt(stack, RANGE_KEY, next);
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("光照更新范围: " + next), true);
		} else {
			updateLight(level, player.blockPosition(), range);
			player.displayClientMessage(net.minecraft.network.chat.Component.literal("已更新 " + range + " 范围内光照"), true);
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
		}
		return InteractionResultHolder.sidedSuccess(stack, true);
	}

	private static void updateLight(Level level, BlockPos center, int range) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				for (int dz = -range; dz <= range; dz++) {
					mutable.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					level.getLightEngine().checkBlock(mutable);
				}
			}
		}
	}

	private static int nextPreset(int current) {
		for (int i = 0; i < PRESETS.length; i++) {
			if (current <= PRESETS[i]) {
				return PRESETS[(i + 1) % PRESETS.length];
			}
		}
		return PRESETS[0];
	}
}
