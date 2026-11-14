package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import cn.autoforged.tree_clearing_tool_mod_1787384923.undo.UndoManager;
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

/**
 * 区域内批量工具（清树 / 清花 / 平整）的公共基类。
 *
 * 设计取舍（对应修改要求）：
 *  - 只用在创造模式：用 level.setBlock 直接置空气，不调用 destroyBlock，
 *    从而【不生成掉落物、不产生破坏粒子特效、不播放逐方块破坏音效】。
 *  - flag 3（= 方块更新 + 通知客户端）会让光照随方块变化自动重算，避免阴影残留；
 *    破坏完成后由各工具统一播放一次破坏音效，避免大面积破坏时音量过大。
 *  - 范围 / 基准高度等可调数值存在手持物品的 NBT 中，供客户端滚轮调节、服务端读取。
 */
public abstract class AreaToolBaseItem extends Item {

	public AreaToolBaseItem(Properties properties) {
		super(properties);
	}

	/**
	 * 右键方块时原版只调用 {@link Item#useOn}，不会调用 {@link Item#use}。
	 * 因此把「对着方块右键」也委托给 {@link Item#use}，让各工具在瞄准方块时同样生效
	 * （{@link #findCenter} 会取到被瞄准的方块作为中心）。
	 */
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

	/**
	 * 该方块是否属于「枫」模组（命名空间 {@code maple}）。
	 *
	 * 仅当检测到安装了 maple 模组时才返回 true；未安装时直接返回 false，
	 * 因此不会引用任何 maple 专有类或方块，未安装时也不会报错。
	 * 通过方块注册名的命名空间判断，可同时覆盖 pink_petales / maple_carpet /
	 * red_maple_carpet / red_maple_leaves 等所有枫模组方块。
	 */
	protected static boolean isMapleBlock(BlockState state) {
		if (!FabricLoader.getInstance().isModLoaded("maple")) {
			return false;
		}
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		return id != null && "maple".equals(id.getNamespace());
	}

	// 枫模组方块按注册名路径关键词区分为「树」与「花草」两类。
	// 未安装 maple 时 isMapleBlock 返回 false，下面的判定也随之为 false，不会误清原版方块。
	private static final String[] MAPLE_TREE_KEYWORDS = {"leaves", "log", "wood", "sapling", "trunk"};
	private static final String[] MAPLE_FLOWER_KEYWORDS = {"petal", "carpet", "flower", "grass", "bush", "sprout"};

	/**
	 * 是否属于枫模组的「树木」类方块（树叶 / 原木 / 木头 / 树苗 / 树干）。
	 * 清树工具用它，从而不会误清枫模组的花草。
	 */
	protected static boolean isMapleTreeBlock(BlockState state) {
		return isMapleBlock(state) && maplePathContains(state, MAPLE_TREE_KEYWORDS);
	}

	/**
	 * 是否属于枫模组的「花草 / 地面植被」类方块（花瓣 / 地毯 / 花 / 草 / 灌木 / 嫩芽）。
	 * 清花工具用它，从而不会误清枫模组的树木。
	 */
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

	/**
	 * 以 center 为中心，水平扫描一块 range x range 的区域，竖直从 dyMin 到 dyMax（相对 center），
	 * 将满足 matcher 的方块直接置为空气（无掉落、无粒子、无逐方块音效）。
	 *
	 * @return 清除的方块数量
	 */
	protected static int clearBlocks(Level level, BlockPos center, int range, int dyMin, int dyMax, Predicate<BlockState> matcher) {
		return clearBlocks(level, center, range, dyMin, dyMax, matcher, null);
	}

	/**
	 * 同 {@link #clearBlocks(Level, BlockPos, int, int, int, Predicate)}，但会把每个被清除
	 * 方块改动前的状态记录进 {@code undoOut}（非空时），供撤销功能使用。
	 *
	 * @return 清除的方块数量
	 */
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

	/** 整个破坏过程结束后只播放一次音效，避免大面积破坏时音量过大。 */
	protected static void playBreakSound(Level level, Player player, SoundEvent sound) {
		level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	/** 以玩家视线命中的方块作为区域中心；视线落空时回退到玩家所在方块。 */
	protected BlockPos findCenter(Player player) {
		HitResult hit = player.pick(64.0D, 1.0F, false);
		if (hit.getType() == HitResult.Type.BLOCK) {
			return ((BlockHitResult) hit).getBlockPos();
		}
		return player.blockPosition();
	}
}
