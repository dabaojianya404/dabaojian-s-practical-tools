package cn.dabaojian.dabaojians_practical_tools.item;

import cn.dabaojian.dabaojians_practical_tools.undo.UndoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LevelingToolItem extends AreaToolBaseItem {

	public static final String LEVEL_Y_KEY = "level_y";
	public static final String LEVEL_LENGTH_KEY = "level_length";
	public static final String LEVEL_WIDTH_KEY = "level_width";
	public static final String LEVEL_HEIGHT_KEY = "level_height";
	public static final int DEFAULT_LENGTH = 128;
	public static final int DEFAULT_WIDTH = 128;
	public static final int DEFAULT_HEIGHT = 50;
	public static final int MAX_LENGTH = 256;
	public static final int MAX_WIDTH = 256;
	public static final int MAX_HEIGHT = 200;
	public static final int PLATFORM_THICKNESS = 1;

	public LevelingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			BlockPos center = findCenter(player);
			int surfaceY = readInt(stack, LEVEL_Y_KEY, center.getY());
			int length = Math.max(1, Math.min(MAX_LENGTH, readInt(stack, LEVEL_LENGTH_KEY, DEFAULT_LENGTH)));
			int width = Math.max(1, Math.min(MAX_WIDTH, readInt(stack, LEVEL_WIDTH_KEY, DEFAULT_WIDTH)));
			int height = Math.max(1, Math.min(MAX_HEIGHT, readInt(stack, LEVEL_HEIGHT_KEY, DEFAULT_HEIGHT)));

			java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
			destroyRegion(level, center, surfaceY, length, width, height, undo);
			placePlatform(level, center, surfaceY, length, width, undo);
			UndoManager.push(player.getUUID(), level.dimension(), undo);

			playBreakSound(level, player, SoundEvents.STONE_BREAK);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	private void destroyRegion(Level level, BlockPos center, int surfaceY, int length, int width, int height,
		java.util.List<UndoManager.UndoBlock> undo) {
		int halfX = length / 2;
		int halfZ = width / 2;
		for (int dx = -halfX; dx < halfX; dx++) {
			for (int dz = -halfZ; dz < halfZ; dz++) {
				for (int dy = 0; dy < height; dy++) {
					BlockPos pos = new BlockPos(center.getX() + dx, surfaceY + dy, center.getZ() + dz);
					BlockState state = level.getBlockState(pos);
					if (!state.isAir()) {
						undo.add(new UndoManager.UndoBlock(pos, state));
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		}
	}

	private void placePlatform(Level level, BlockPos center, int surfaceY, int length, int width,
		java.util.List<UndoManager.UndoBlock> undo) {
		int halfX = length / 2;
		int halfZ = width / 2;
		for (int dx = -halfX; dx < halfX; dx++) {
			for (int dz = -halfZ; dz < halfZ; dz++) {
				for (int k = 0; k < PLATFORM_THICKNESS; k++) {
					BlockPos pos = new BlockPos(center.getX() + dx, surfaceY - k, center.getZ() + dz);
					BlockState place = (k == 0)
						? Blocks.GRASS_BLOCK.defaultBlockState()
						: Blocks.DIRT.defaultBlockState();
					undo.add(new UndoManager.UndoBlock(pos, level.getBlockState(pos)));
					level.setBlock(pos, place, 3);
				}
			}
		}
	}
}
