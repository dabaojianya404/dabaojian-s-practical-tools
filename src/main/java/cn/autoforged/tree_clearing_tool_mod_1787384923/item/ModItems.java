package cn.autoforged.tree_clearing_tool_mod_1787384923.item;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

	// 清树工具：耐久 500，堆叠 1（durability 隐式把堆叠设为 1，与规格 stack_size=1 一致）
	public static final Item TREE_CLEARING_TOOL = register("tree_clearing_tool",
		new TreeClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	// 清除花的工具：清除周围花朵，Shift+滚轮调节范围，最大 128x128
	public static final Item FLOWER_CLEARING_TOOL = register("flower_clearing_tool",
		new FlowerClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	// 平整工具：平整 128x128 区域并铺一层草方块，Shift+滚轮调节 Y 轴基准高度
	public static final Item LEVELING_TOOL = register("leveling_tool",
		new LevelingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	// 混合清理工具：同时清理树/花/草/蘑菇等，Shift+滚轮调节范围，最大 256x256
	public static final Item MIXED_CLEARING_TOOL = register("mixed_clearing_tool",
		new MixedClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	// 光照更新工具：右键更新周围光照，Shift+右键循环调节范围，最大 64
	public static final Item LIGHT_UPDATE_TOOL = register("light_update_tool",
		new LightUpdateToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	// 黑名单清除工具：Shift+右键指定方块或打开 18 格物品栏，右键只清除这些方块，Shift+滚轮调节范围
	public static final Item BLACKLIST_CLEARING_TOOL = register("blacklist_clearing_tool",
		new BlacklistClearingToolItem(new Item.Properties()
			.stacksTo(1)
			.durability(500)));

	private static Item register(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM,
			new ResourceLocation(ExampleMod.MOD_ID, name), item);
	}

	// 在 onInitialize 期间调用以触发本类静态字段初始化（Fabric 需显式触碰类）
	public static void register() {
	}
}
