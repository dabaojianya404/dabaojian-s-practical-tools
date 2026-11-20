package cn.dabaojian.dabaojians_practical_tools.client;

import cn.dabaojian.dabaojians_practical_tools.item.AreaToolBaseItem;
import cn.dabaojian.dabaojians_practical_tools.item.BlacklistClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.FlowerClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.LevelingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.MixedClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.ModItems;
import cn.dabaojian.dabaojians_practical_tools.item.TreeClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.SyncToolValuePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public final class ToolScrollHandler {

	private ToolScrollHandler() {
	}

	public static boolean handleScroll(Player player, double scrollY) {
		if (scrollY == 0) {
			return false;
		}
		int step = scrollY > 0 ? 1 : -1;

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(ModItems.TREE_CLEARING_TOOL)) {
				int range = clamp(
					AreaToolBaseItem.readInt(stack, TreeClearingToolItem.RANGE_KEY, TreeClearingToolItem.DEFAULT_RANGE) + step,
					1, TreeClearingToolItem.MAX_RANGE);
				AreaToolBaseItem.setInt(stack, TreeClearingToolItem.RANGE_KEY, range);
				ClientPlayNetworking.send(new SyncToolValuePacket(range));
				showActionBar(player, "清树范围: " + range + "x" + range);
				return true;
			}
			if (stack.is(ModItems.FLOWER_CLEARING_TOOL)) {
				int range = clamp(
					AreaToolBaseItem.readInt(stack, FlowerClearingToolItem.RANGE_KEY, FlowerClearingToolItem.DEFAULT_RANGE) + step,
					1, FlowerClearingToolItem.MAX_RANGE);
				AreaToolBaseItem.setInt(stack, FlowerClearingToolItem.RANGE_KEY, range);
				ClientPlayNetworking.send(new SyncToolValuePacket(range));
				showActionBar(player, "清花范围: " + range + "x" + range);
				return true;
			}
			if (stack.is(ModItems.LEVELING_TOOL)) {
				int base = AreaToolBaseItem.readInt(stack, LevelingToolItem.LEVEL_Y_KEY, player.getBlockY());
				int y = base + step;
				AreaToolBaseItem.setInt(stack, LevelingToolItem.LEVEL_Y_KEY, y);
				ClientPlayNetworking.send(new SyncToolValuePacket(y));
				showActionBar(player, "平整 Y 基准: " + y);
				return true;
			}
			if (stack.is(ModItems.MIXED_CLEARING_TOOL)) {
				int range = clamp(
					AreaToolBaseItem.readInt(stack, MixedClearingToolItem.RANGE_KEY, MixedClearingToolItem.DEFAULT_RANGE) + step,
					1, MixedClearingToolItem.MAX_RANGE);
				AreaToolBaseItem.setInt(stack, MixedClearingToolItem.RANGE_KEY, range);
				ClientPlayNetworking.send(new SyncToolValuePacket(range));
				showActionBar(player, "混合清理范围: " + range + "x" + range);
				return true;
			}
			if (stack.is(ModItems.BLACKLIST_CLEARING_TOOL)) {
				int range = clamp(
					AreaToolBaseItem.readInt(stack, BlacklistClearingToolItem.RANGE_KEY, BlacklistClearingToolItem.DEFAULT_RANGE) + step,
					1, BlacklistClearingToolItem.MAX_RANGE);
				AreaToolBaseItem.setInt(stack, BlacklistClearingToolItem.RANGE_KEY, range);
				ClientPlayNetworking.send(new SyncToolValuePacket(range));
				showActionBar(player, "黑名单清除范围: " + range + "x" + range);
				return true;
			}
		}
		return false;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static void showActionBar(Player player, String message) {
		player.displayClientMessage(Component.literal(message), true);
	}
}
