package cn.autoforged.tree_clearing_tool_mod_1787384923;

import cn.autoforged.tree_clearing_tool_mod_1787384923.item.BlacklistClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.FlowerClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.LevelingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.MixedClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.TreeClearingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.LevelToolValuePacket;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.SyncToolValuePacket;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.UndoPacket;
import cn.autoforged.tree_clearing_tool_mod_1787384923.undo.UndoManager;
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
	public static final String MOD_ID = "tree_clearing_tool_mod_1787384923";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.register();

		// 六个工具加入创造栏「工具与实用物品」页
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
			.register(entries -> {
				entries.accept(ModItems.TREE_CLEARING_TOOL);
				entries.accept(ModItems.FLOWER_CLEARING_TOOL);
				entries.accept(ModItems.LEVELING_TOOL);
				entries.accept(ModItems.MIXED_CLEARING_TOOL);
				entries.accept(ModItems.LIGHT_UPDATE_TOOL);
				entries.accept(ModItems.BLACKLIST_CLEARING_TOOL);
			});

		// 客户端 Shift+滚轮调节数值时，把新值写入手持工具的 NBT（服务端 use() 读取）
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

		// 客户端按「撤销」键（默认 F7）时，恢复上一步工具破坏的方块并重算光照
		ServerPlayNetworking.registerGlobalReceiver(UndoPacket.TYPE, (packet, player, responseSender) -> {
			if (UndoManager.undo(player.getUUID(), player.serverLevel())) {
				player.displayClientMessage(Component.translatable("message." + MOD_ID + ".undo.success"), true);
			} else {
				player.displayClientMessage(Component.translatable("message." + MOD_ID + ".undo.nothing"), true);
			}
		});

		// 客户端手持平整工具按 Shift+方向键调节破坏长/宽/高时，把新值写入手持物品 NBT（服务端 use() 读取）
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

	/**
	 * 注册 OP 指令 /dbjtools [玩家] undo。
	 *
	 * 拥有 OP 权限（level 2）的管理员可：
	 *  - /dbjtools undo             撤销自己最近一步工具操作
	 *  - /dbjtools &lt;玩家&gt; undo     撤销指定服务器成员的最近一步工具操作
	 *
	 * 保留客户端按 F7 撤销自己操作的功能（走 {@link UndoPacket}），本指令为管理员扩展入口。
	 */
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
			source.sendSuccess(() -> Component.translatable("message." + MOD_ID + ".undo.success"), false);
		} else {
			source.sendSuccess(() -> Component.translatable("message." + MOD_ID + ".undo.nothing"), false);
		}
		return 1;
	}

	private static int undoOther(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
		if (UndoManager.undo(target.getUUID(), target.serverLevel())) {
			source.sendSuccess(() -> Component.translatable("message." + MOD_ID + ".undo.success"), false);
		} else {
			source.sendSuccess(() -> Component.translatable("message." + MOD_ID + ".undo.nothing"), false);
		}
		return 1;
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
