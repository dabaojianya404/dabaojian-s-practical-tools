package cn.dabaojian.dabaojians_practical_tools.client;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import cn.dabaojian.dabaojians_practical_tools.item.AreaToolBaseItem;
import cn.dabaojian.dabaojians_practical_tools.item.LevelingToolItem;
import cn.dabaojian.dabaojians_practical_tools.item.ModItems;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.LevelToolValuePacket;
import cn.dabaojian.dabaojians_practical_tools.networking.packet.UndoPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;


public class ExampleModClient implements ClientModInitializer {

	public static final String CATEGORY = "key.categories." + ExampleMod.RESOURCE_NAMESPACE;
	public static final KeyMapping UNDO_KEY = KeyBindingHelper.registerKeyBinding(
		new KeyMapping("key." + ExampleMod.RESOURCE_NAMESPACE + ".undo",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, CATEGORY));

	// 方向键上一次的按下状态，用于边沿检测（避免按住时每 tick 触发一次调节）
	private static boolean prevLeft;
	private static boolean prevRight;
	private static boolean prevUp;

	@Override
	public void onInitializeClient() {
		TabStatsClient.register();
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (UNDO_KEY.consumeClick()) {
				ClientPlayNetworking.send(new UndoPacket());
			}
			if (mc.player != null) {
				handleLevelingArrows(mc);
			}
		});
	}


	private static void handleLevelingArrows(Minecraft mc) {
		long window = mc.getWindow().getWindow();
		boolean left = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT);
		boolean right = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT);
		boolean up = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_UP);

		if (Screen.hasShiftDown()) {
			ItemStack stack = mc.player.getMainHandItem();
			if (!stack.is(ModItems.LEVELING_TOOL)) {
				stack = mc.player.getOffhandItem();
			}
			if (stack.is(ModItems.LEVELING_TOOL)) {
				if (left && !prevLeft) {
					adjustLevel(stack, LevelToolValuePacket.KIND_LENGTH, LevelingToolItem.LEVEL_LENGTH_KEY,
						LevelingToolItem.DEFAULT_LENGTH, LevelingToolItem.MAX_LENGTH, "长");
				}
				if (right && !prevRight) {
					adjustLevel(stack, LevelToolValuePacket.KIND_WIDTH, LevelingToolItem.LEVEL_WIDTH_KEY,
						LevelingToolItem.DEFAULT_WIDTH, LevelingToolItem.MAX_WIDTH, "宽");
				}
				if (up && !prevUp) {
					adjustLevel(stack, LevelToolValuePacket.KIND_HEIGHT, LevelingToolItem.LEVEL_HEIGHT_KEY,
						LevelingToolItem.DEFAULT_HEIGHT, LevelingToolItem.MAX_HEIGHT, "高");
				}
			}
		}

		prevLeft = left;
		prevRight = right;
		prevUp = up;
	}

	private static void adjustLevel(ItemStack stack, int kind, String key, int def, int max, String label) {
		int value = Math.min(max, AreaToolBaseItem.readInt(stack, key, def) + 1);
		AreaToolBaseItem.setInt(stack, key, value);
		ClientPlayNetworking.send(new LevelToolValuePacket(kind, value));
		if (Minecraft.getInstance().player != null) {
			Minecraft.getInstance().player.displayClientMessage(
				Component.literal("平整 " + label + "范围: " + value), true);
		}
	}
}
