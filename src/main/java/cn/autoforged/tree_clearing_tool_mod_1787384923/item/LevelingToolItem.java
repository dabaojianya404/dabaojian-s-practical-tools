package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import cn.autoforged.tree_clearing_tool_mod_1787384923.undo.UndoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 平整工具：右键以玩家视线命中的方块为水平中心，破坏一个可调长度（长）、宽度（宽）、
 * 高度（高）的区域，然后在玩家用 Shift+滚轮调节的基准高度上铺一块以该方块为中心的草方块平台。
 *
 * 对应修改要求：
 *  - Shift+滚轮调节平整的 Y 轴基准高度，存于物品 NBT（键 level_y）。
 *  - 手持平整工具时 Shift+左/右/上方向键分别调节破坏的 长/宽/高 范围：
 *      左方向键 -> 破坏长范围（level_length，默认 128）
 *      右方向键 -> 破坏宽范围（level_width，默认 128）
 *      上方向键 -> 破坏高范围（level_height，默认 50）
 *    均存于物品 NBT，由客户端发送 {@code LevelToolValuePacket} 同步到服务端。
 *  - 使用时只清理玩家面前所选定的区域（以视线命中方块为水平中心）。
 *  - 草坪（平台）高度为 1：只铺一层草方块。
 *  - 破坏完毕后自动更新光照（setBlock flag 3 重算光照）。
 *  - 不产生掉落物、不产生破坏粒子，只在完成后播放一次 STONE_BREAK 音效。
 *
 * 假设（需求未明示方向增减语义，采用每次按下 +1 的合理方案）：
 *  - Shift+方向键每次按下使对应维度范围 +1，无递减键，需配合持久叠加使用。
 */
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
			// 基准高度：缺省用选中方块的 Y（玩家可通过 Shift+滚轮调节）
			int surfaceY = readInt(stack, LEVEL_Y_KEY, center.getY());
			// 长/宽/高范围：玩家可通过 Shift+方向键调节（缺省 128/128/50）
			int length = Math.max(1, Math.min(MAX_LENGTH, readInt(stack, LEVEL_LENGTH_KEY, DEFAULT_LENGTH)));
			int width = Math.max(1, Math.min(MAX_WIDTH, readInt(stack, LEVEL_WIDTH_KEY, DEFAULT_WIDTH)));
			int height = Math.max(1, Math.min(MAX_HEIGHT, readInt(stack, LEVEL_HEIGHT_KEY, DEFAULT_HEIGHT)));

			java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
			destroyRegion(level, center, surfaceY, length, width, height, undo);
			placePlatform(level, center, surfaceY, length, width, undo);

			// 记录本次操作，供玩家按撤销键恢复
			UndoManager.push(player.getUUID(), level.dimension(), undo);

			playBreakSound(level, player, SoundEvents.STONE_BREAK);
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	/** 破坏：以 center 为水平中心，长宽 length x width，竖直从 surfaceY 向上 height 格全部清空。 */
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

	/** 放置：以 center 为水平中心，在 surfaceY 铺一层草方块（草坪高度为 1）。 */
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
					// 记录改动前状态（此处为刚被 destroyRegion 置空的空气）
					undo.add(new UndoManager.UndoBlock(pos, level.getBlockState(pos)));
					level.setBlock(pos, place, 3);
				}
			}
		}
	}
}
