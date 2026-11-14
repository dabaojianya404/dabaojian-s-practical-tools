package cn.autoforged.tree_clearing_tool_mod_1787384923.menu;

import cn.autoforged.tree_clearing_tool_mod_1787384923.item.BlacklistClearingToolItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/**
 * 黑名单清除工具的物品栏菜单（9x2 = 18 格）。
 *
 * 复用原版 {@link MenuType#GENERIC_9x2}，因此客户端会自动用原版箱子界面渲染，
 * 无需额外的 MenuType 注册与客户端 Screen 代码。
 *
 * 服务端打开菜单前会从手持物品 NBT 中读出已保存的 18 格内容装进 {@code container}；
 * 玩家关闭菜单时在 {@link #removed(Player)} 里把内容写回手持物品 NBT，供右键清除时读取。
 */
public class BlacklistClearingToolMenu extends ChestMenu {

	private final Container container;
	private final ItemStack toolStack;

	public BlacklistClearingToolMenu(int syncId, Inventory playerInventory, Container container, ItemStack toolStack) {
		super(MenuType.GENERIC_9x2, syncId, playerInventory, container, 2);
		this.container = container;
		this.toolStack = toolStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		BlacklistClearingToolItem.saveContainerToStack(this.toolStack, this.container);
	}
}
