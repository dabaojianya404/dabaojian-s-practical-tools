package cn.dabaojian.dabaojians_practical_tools.item;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

	public static final Item TREE_CLEARING_TOOL = register("tree_clearing_tool",
		new TreeClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	public static final Item FLOWER_CLEARING_TOOL = register("flower_clearing_tool",
		new FlowerClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	public static final Item LEVELING_TOOL = register("leveling_tool",
		new LevelingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	public static final Item MIXED_CLEARING_TOOL = register("mixed_clearing_tool",
		new MixedClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	public static final Item LIGHT_UPDATE_TOOL = register("light_update_tool",
		new LightUpdateToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	public static final Item BLACKLIST_CLEARING_TOOL = register("blacklist_clearing_tool",
		new BlacklistClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	private static Item register(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM,
			new ResourceLocation(ExampleMod.RESOURCE_NAMESPACE, name), item);
	}

	public static void register() {
	}
}
