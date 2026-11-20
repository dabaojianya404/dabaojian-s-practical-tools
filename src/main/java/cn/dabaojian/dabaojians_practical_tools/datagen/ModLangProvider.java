package cn.dabaojian.dabaojians_practical_tools.datagen;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import cn.dabaojian.dabaojians_practical_tools.item.ModItems;
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
		translationBuilder.add("container." + ExampleMod.RESOURCE_NAMESPACE + ".blacklist", "Blacklist Filter");
		translationBuilder.add("message." + ExampleMod.RESOURCE_NAMESPACE + ".blacklist.selected", "Target set: %s");
		translationBuilder.add("message." + ExampleMod.RESOURCE_NAMESPACE + ".blacklist.empty",
			"No target set: Shift+Right-click a block, or Shift+Right-click the air to open the filter");
		translationBuilder.add("key.categories." + ExampleMod.RESOURCE_NAMESPACE, "dabaojians practical tools");
		translationBuilder.add("key." + ExampleMod.RESOURCE_NAMESPACE + ".undo", "Undo Last Clear");
		translationBuilder.add("message." + ExampleMod.RESOURCE_NAMESPACE + ".undo.success", "Undo successful");
		translationBuilder.add("message." + ExampleMod.RESOURCE_NAMESPACE + ".undo.nothing", "Nothing to undo");
	}
}
