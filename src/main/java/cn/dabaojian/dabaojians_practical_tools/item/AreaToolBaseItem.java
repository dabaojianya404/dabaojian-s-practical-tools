package cn.dabaojian.dabaojians_practical_tools.item;

import cn.dabaojian.dabaojians_practical_tools.undo.UndoManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Predicate;


public abstract class AreaToolBaseItem extends Item {

	public AreaToolBaseItem(Properties properties) {
		super(properties);
	}


	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		return use(context.getLevel(), player, context.getHand()).getResult();
	}

	public static int readInt(ItemStack stack, String key, int def) {
		CompoundTag tag = stack.getTag();
		return (tag != null && tag.contains(key)) ? tag.getInt(key) : def;
	}

	public static void setInt(ItemStack stack, String key, int value) {
		stack.getOrCreateTag().putInt(key, value);
	}


	protected static boolean isMapleBlock(BlockState state) {
		if (!FabricLoader.getInstance().isModLoaded("maple")) {
			return false;
		}
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return id != null && "maple".equals(id.getNamespace());
	}

	private static final String[] MAPLE_TREE_KEYWORDS = {"leaves", "log", "wood", "sapling", "trunk"};
	private static final String[] MAPLE_FLOWER_KEYWORDS = {"petal", "carpet", "flower", "grass", "bush", "sprout"};

	protected static boolean isMapleTreeBlock(BlockState state) {
		return isMapleBlock(state) && maplePathContains(state, MAPLE_TREE_KEYWORDS);
	}


	protected static boolean isMapleFlowerBlock(BlockState state) {
		return isMapleBlock(state) && maplePathContains(state, MAPLE_FLOWER_KEYWORDS);
	}

	private static boolean maplePathContains(BlockState state, String[] keywords) {
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		if (id == null) {
			return false;
		}
		String path = id.getPath();
		for (String keyword : keywords) {
			if (path.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

	protected static int clearBlocks(Level level, BlockPos center, int range, int dyMin, int dyMax, Predicate<BlockState> matcher) {
		return clearBlocks(level, center, range, dyMin, dyMax, matcher, null);
	}

	protected static int clearBlocks(Level level, BlockPos center, int range, int dyMin, int dyMax,
		Predicate<BlockState> matcher, java.util.List<UndoManager.UndoBlock> undoOut) {
		int cleared = 0;
		int half = range / 2;
		for (int dx = -half; dx < half; dx++) {
			for (int dz = -half; dz < half; dz++) {
				for (int dy = dyMin; dy <= dyMax; dy++) {
					BlockPos pos = center.offset(dx, dy, dz);
					BlockState state = level.getBlockState(pos);
					if (state.isAir()) {
						continue;
					}
					if (matcher.test(state)) {
						if (undoOut != null) {
							undoOut.add(new UndoManager.UndoBlock(pos, state));
						}
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
						cleared++;
					}
				}
			}
		}
		return cleared;
	}

	protected static void playBreakSound(Level level, Player player, SoundEvent sound) {
		level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	protected BlockPos findCenter(Player player) {
		HitResult hit = player.pick(64.0D, 1.0F, false);
		if (hit.getType() == HitResult.Type.BLOCK) {
			return ((BlockHitResult) hit).getBlockPos();
		}
		return player.blockPosition();
	}
}
