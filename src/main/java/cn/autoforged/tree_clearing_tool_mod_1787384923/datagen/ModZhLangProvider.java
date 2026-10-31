package cn.autoforged.tree_clearing_tool_mod_1787384923.datagen;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class ModZhLangProvider extends FabricLanguageProvider {

	public ModZhLangProvider(FabricDataOutput output) {
		super(output, "zh_cn");
	}

	@Override
	public void generateTranslations(TranslationBuilder translationBuilder) {
		translationBuilder.add(ModItems.TREE_CLEARING_TOOL, "清树工具");
		translationBuilder.add(ModItems.FLOWER_CLEARING_TOOL, "清除花工具");
		translationBuilder.add(ModItems.LEVELING_TOOL, "平整工具");
		translationBuilder.add(ModItems.MIXED_CLEARING_TOOL, "混合清理工具");
		translationBuilder.add(ModItems.LIGHT_UPDATE_TOOL, "光照更新工具");
		translationBuilder.add(ModItems.BLACKLIST_CLEARING_TOOL, "黑名单清除工具");
		translationBuilder.add("container." + ExampleMod.MOD_ID + ".blacklist", "黑名单过滤栏");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".blacklist.selected", "已指定清除目标: %s");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".blacklist.empty",
			"未指定清除目标：Shift+右键方块指定，或 Shift+右键空气打开过滤栏");
		translationBuilder.add("key.categories." + ExampleMod.MOD_ID, "大宝剑的实用工具");
		translationBuilder.add("key." + ExampleMod.MOD_ID + ".undo", "撤销上一次清理");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".undo.success", "撤销成功");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".undo.nothing", "没有可撤销的操作");
	}
}
