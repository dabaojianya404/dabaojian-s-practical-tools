package cn.dabaojian.dabaojians_practical_tools;

import cn.dabaojian.dabaojians_practical_tools.networking.ServerStatsSync;
import cn.dabaojian.dabaojians_practical_tools.item.BlacklistClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.FlowerClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.LevelingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.MixedClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.ModItems;
import cn.dabaojian.dabaojians_practical_tools.item.TreeClearingToolItem;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.LevelToolValuePacket;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.SyncToolValuePacket;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.UndoPacket;
import cn.dabaojian.dabaojians_practical_tools.undo.UndoManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	public static final String MOD_ID = "dabaojians_practical_tools";
	public static final String RESOURCE_NAMESPACE = "dabaojians_practical_tools";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerStatsSync.register();

		ModItems.register();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(entries -> {
				entries.accept(ModItems.TREE_CLEARING_TOOL);
				entries.accept(ModItems.FLOWER_CLEARING_TOOL);
				entries.accept(ModItems.LEVELING_TOOL);
				entries.accept(ModItems.MIXED_CLEARING_TOOL);
				entries.accept(ModItems.LIGHT_UPDATE_TOOL);
				entries.accept(ModItems.BLACKLIST_CLEARING_TOOL);
			});

		ServerPlayNetworking.registerGlobalReceiver(SyncToolValuePacket.TYPE, (packet, player, responseSender) -> {
			int value = packet.value();
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = player.getItemInHand(hand);
				if (stack.is(ModItems.TREE_CLEARING_TOOL)) {
					stack.getOrCreateTag().putInt(TreeClearingToolItem.RANGE_KEY, value);
				} else if (stack.is(ModItems.FLOWER_CLEARING_TOOL)) {
					stack.getOrCreateTag().putInt(FlowerClearingToolItem.RANGE_KEY, value);
				} else if (stack.is(ModItems.LEVELING_TOOL)) {
					stack.getOrCreateTag().putInt(LevelingToolItem.LEVEL_Y_KEY, value);
				} else if (stack.is(ModItems.MIXED_CLEARING_TOOL)) {
					stack.getOrCreateTag().putInt(MixedClearingToolItem.RANGE_KEY, value);
				} else if (stack.is(ModItems.BLACKLIST_CLEARING_TOOL)) {
					stack.getOrCreateTag().putInt(BlacklistClearingToolItem.RANGE_KEY, value);
				}
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(UndoPacket.TYPE, (packet, player, responseSender) -> {
			if (UndoManager.undo(player.getUUID(), player.serverLevel())) {
				player.displayClientMessage(Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.success"), true);
			} else {
				player.displayClientMessage(Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.nothing"), true);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(LevelToolValuePacket.TYPE, (packet, player, responseSender) -> {
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = player.getItemInHand(hand);
				if (!stack.is(ModItems.LEVELING_TOOL)) {
					continue;
				}
				CompoundTag tag = stack.getOrCreateTag();
				switch (packet.kind()) {
					case LevelToolValuePacket.KIND_LENGTH -> tag.putInt(LevelingToolItem.LEVEL_LENGTH_KEY, packet.value());
					case LevelToolValuePacket.KIND_WIDTH -> tag.putInt(LevelingToolItem.LEVEL_WIDTH_KEY, packet.value());
					case LevelToolValuePacket.KIND_HEIGHT -> tag.putInt(LevelingToolItem.LEVEL_HEIGHT_KEY, packet.value());
					default -> { }
				}
			}
		});

		registerUndoCommand();

		LOGGER.info("Hello Fabric world!");
	}

	private static void registerUndoCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("dbjtools")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("undo")
					.executes(ctx -> undoSelf(ctx.getSource())))
				.then(Commands.argument("player", EntityArgument.player())
					.then(Commands.literal("undo")
						.executes(ctx -> undoOther(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))))));
		});
	}

	private static int undoSelf(CommandSourceStack source) throws CommandSyntaxException {
		ServerPlayer admin = source.getPlayerOrException();
		if (UndoManager.undo(admin.getUUID(), admin.serverLevel())) {
			source.sendSuccess(() -> Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.success"), false);
		} else {
			source.sendSuccess(() -> Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.nothing"), false);
		}
		return 1;
	}

	private static int undoOther(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
		if (UndoManager.undo(target.getUUID(), target.serverLevel())) {
			source.sendSuccess(() -> Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.success"), false);
		} else {
			source.sendSuccess(() -> Component.translatable("message." + RESOURCE_NAMESPACE + ".undo.nothing"), false);
		}
		return 1;
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
