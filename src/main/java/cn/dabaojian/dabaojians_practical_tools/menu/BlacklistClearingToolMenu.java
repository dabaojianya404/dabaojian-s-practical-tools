package cn.dabaojian.dabaojians_practical_tools.menu;

import cn.dabaojian.dabaojians_practical_tools.item.BlacklistClearingToolItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

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
