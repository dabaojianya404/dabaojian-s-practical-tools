package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import cn.autoforged.tree_clearing_tool_mod_1787384923.menu.BlacklistClearingToolMenu;
import cn.autoforged.tree_clearing_tool_mod_1787384923.undo.UndoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.HashSet;
import java.util.Set;

/**
 * 黑名单清除工具（名称「黑名单清除工具」）：
 *
 *  - Shift+右键对准某个方块：把该方块指定为唯一清除目标（存入手持物品 NBT 键
 *    {@link #SELECTED_KEY}，存的是方块注册名）。
 *  - Shift+右键对准空气：打开一个 9x2（18 格）的物品栏。玩家往里面放置的物品/方块
 *    会成为清除目标；关闭界面时写回手持物品 NBT（键 {@link #ITEMS_KEY}）。
 *  - Shift+滚轮：调节清除范围（NBT 键 {@link #RANGE_KEY}，默认 8，最大 128）。
 *  - 普通右键：以视线命中方块为中心，清除 range x range x range 立方体范围内
 *    「已指定方块」或「物品栏中物品对应的方块」。
 *
 * 假设（需求未明示，采用最合理方案）：
 *  - 因为是清除「某一种方块」，竖直方向不做地面假设，直接取与水平相同的 range 做立方体扫描。
 *  - 指定方块与物品栏内容同时存在时取并集；两者都为空时右键不清理任何方块并提示。
 *  - 只用于创造模式：不产生掉落物/粒子，只在完成后播放一次 STONE_BREAK 音效。
 */
public class BlacklistClearingToolItem extends AreaToolBaseItem {

	public static final String RANGE_KEY = "blacklist_range";
	public static final String SELECTED_KEY = "blacklist_selected";
	public static final String ITEMS_KEY = "blacklist_items";
	public static final int MAX_RANGE = 128;
	public static final int DEFAULT_RANGE = 8;
	public static final int SLOT_COUNT = 18;

	public BlacklistClearingToolItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide) {
			if (player.isShiftKeyDown()) {
				handleShiftUse(level, player, stack);
			} else {
				handleClear(level, player, stack, hand);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

	/** Shift+右键：命中方块则指定该方块；命中空气则打开 18 格物品栏。 */
	private void handleShiftUse(Level level, Player player, ItemStack stack) {
		HitResult hit = player.pick(64.0D, 1.0F, false);
		if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos pos = ((BlockHitResult) hit).getBlockPos();
			BlockState state = level.getBlockState(pos);
			if (!state.isAir()) {
				ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
				if (id != null) {
					stack.getOrCreateTag().putString(SELECTED_KEY, id.toString());
					player.displayClientMessage(Component.translatable(
						"message." + ExampleMod.MOD_ID + ".blacklist.selected", state.getBlock().getName()), true);
				}
			}
		} else if (player instanceof ServerPlayer serverPlayer) {
			openBlacklistMenu(serverPlayer, stack);
		}
	}

	private void openBlacklistMenu(ServerPlayer player, ItemStack stack) {
		SimpleContainer container = loadContainerFromStack(stack);
		player.openMenu(new SimpleMenuProvider(
			(syncId, playerInventory, ignored) -> new BlacklistClearingToolMenu(syncId, playerInventory, container, stack),
			Component.translatable("container." + ExampleMod.MOD_ID + ".blacklist")));
	}

	/** 普通右键：清除范围内与已指定方块/物品栏内容匹配的方块。 */
	private void handleClear(Level level, Player player, ItemStack stack, InteractionHand hand) {
		Set<Block> targets = collectTargetBlocks(stack);
		if (targets.isEmpty()) {
			player.displayClientMessage(Component.translatable(
				"message." + ExampleMod.MOD_ID + ".blacklist.empty"), true);
			return;
		}

		BlockPos center = findCenter(player);
		int range = Math.max(1, Math.min(MAX_RANGE, readInt(stack, RANGE_KEY, DEFAULT_RANGE)));
		int half = range / 2;

		java.util.List<UndoManager.UndoBlock> undo = new java.util.ArrayList<>();
		int cleared = clearBlocks(level, center, range, -half, half - 1,
			s -> targets.contains(s.getBlock()), undo);

		if (cleared > 0) {
			UndoManager.push(player.getUUID(), level.dimension(), undo);
			stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
			playBreakSound(level, player, SoundEvents.STONE_BREAK);
		}
	}

	/** 收集当前清除目标：已指定方块 + 物品栏内容对应的方块。 */
	public static Set<Block> collectTargetBlocks(ItemStack stack) {
		Set<Block> targets = new HashSet<>();
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(SELECTED_KEY, Tag.TAG_STRING)) {
			Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(tag.getString(SELECTED_KEY)));
			if (block != null && block != Blocks.AIR) {
				targets.add(block);
			}
		}
		SimpleContainer container = loadContainerFromStack(stack);
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack item = container.getItem(i);
			if (item.isEmpty()) {
				continue;
			}
			Block block = item.getItem() instanceof BlockItem blockItem
				? blockItem.getBlock()
				: Block.byItem(item.getItem());
			if (block != Blocks.AIR) {
				targets.add(block);
			}
		}
		return targets;
	}

	public static SimpleContainer loadContainerFromStack(ItemStack stack) {
		SimpleContainer container = new SimpleContainer(SLOT_COUNT);
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(ITEMS_KEY, Tag.TAG_COMPOUND)) {
			NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
			ContainerHelper.loadAllItems(tag.getCompound(ITEMS_KEY), items);
			for (int i = 0; i < SLOT_COUNT; i++) {
				container.setItem(i, items.get(i));
			}
		}
		return container;
	}

	public static void saveContainerToStack(ItemStack stack, Container container) {
		NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		for (int i = 0; i < SLOT_COUNT && i < container.getContainerSize(); i++) {
			items.set(i, container.getItem(i));
		}
		CompoundTag itemsTag = new CompoundTag();
		ContainerHelper.saveAllItems(itemsTag, items);
		stack.getOrCreateTag().put(ITEMS_KEY, itemsTag);
	}
}
