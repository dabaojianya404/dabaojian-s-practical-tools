package cn.autoforged.tree_clearing_tool_mod_1787384923.datagen;

import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;

public class ModModelProvider extends FabricModelProvider {

	public ModModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerators) {
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerators) {
		// 六个工具均使用手持模型 item/handheld
		itemModelGenerators.generateFlatItem(ModItems.TREE_CLEARING_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
		itemModelGenerators.generateFlatItem(ModItems.FLOWER_CLEARING_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
		itemModelGenerators.generateFlatItem(ModItems.LEVELING_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
		itemModelGenerators.generateFlatItem(ModItems.MIXED_CLEARING_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
		itemModelGenerators.generateFlatItem(ModItems.LIGHT_UPDATE_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
		itemModelGenerators.generateFlatItem(ModItems.BLACKLIST_CLEARING_TOOL, ModelTemplates.FLAT_HANDHELD_ITEM);
	}
}
