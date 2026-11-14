package cn.autoforged.tree_clearing_tool_mod_1787384923.datagen;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class ModLangProvider extends FabricLanguageProvider {

	public ModLangProvider(FabricDataOutput output) {
		super(output, "en_us");
	}

	@Override
	public void generateTranslations(TranslationBuilder translationBuilder) {
		translationBuilder.add(ModItems.TREE_CLEARING_TOOL, "Tree Clearing Tool");
		translationBuilder.add(ModItems.FLOWER_CLEARING_TOOL, "Flower Clearing Tool");
		translationBuilder.add(ModItems.LEVELING_TOOL, "Leveling Tool");
		translationBuilder.add(ModItems.MIXED_CLEARING_TOOL, "Mixed Clearing Tool");
		translationBuilder.add(ModItems.LIGHT_UPDATE_TOOL, "Light Update Tool");
		translationBuilder.add(ModItems.BLACKLIST_CLEARING_TOOL, "Blacklist Clearing Tool");
		translationBuilder.add("container." + ExampleMod.MOD_ID + ".blacklist", "Blacklist Filter");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".blacklist.selected", "Target set: %s");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".blacklist.empty",
			"No target set: Shift+Right-click a block, or Shift+Right-click the air to open the filter");
		translationBuilder.add("key.categories." + ExampleMod.MOD_ID, "dabaojians practical tools");
		translationBuilder.add("key." + ExampleMod.MOD_ID + ".undo", "Undo Last Clear");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".undo.success", "Undo successful");
		translationBuilder.add("message." + ExampleMod.MOD_ID + ".undo.nothing", "Nothing to undo");
	}
}
