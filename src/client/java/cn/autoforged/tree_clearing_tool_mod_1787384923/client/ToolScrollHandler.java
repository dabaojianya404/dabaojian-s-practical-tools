package cn.autoforged.tree_clearing_tool_mod_1787384923.client;

import cn.autoforged.tree_clearing_tool_mod_1787384923.item.AreaToolBaseItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.BlacklistClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.FlowerClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.LevelingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.MixedClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.TreeClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.SyncToolValuePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Shift+滚轮调节工具数值的客户端逻辑。由 {@code MouseHandlerMixin} 在滚轮事件里调用。
 *
 * 作用：
 *  - 根据手持的模组工具，把滚轮步进应用到对应数值（清树/清花为破坏范围，平整为 Y 轴基准高度）；
 *  - 把新值写入手持物品 NBT（供本端显示），并同步给服务端（写服务端手持物品 NBT，供 use() 读取）；
 *  - 通过 action bar 显示当前数值。
 */
public final class ToolScrollHandler {

	private ToolScrollHandler() {
	}

	/** @return true 表示本次滚轮已被本模组消费（应取消原版滚轮行为）。 */
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
