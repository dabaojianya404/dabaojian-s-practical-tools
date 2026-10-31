package cn.autoforged.tree_clearing_tool_mod_1787384923.client;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.AreaToolBaseItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.LevelingToolItem;
import cn.autoforged.tree_clearing_tool_mod_1787384923.item.ModItems;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.LevelToolValuePacket;
import cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet.UndoPacket;
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

/**
 * 客户端入口点：
 *  - 注册「撤销」按键（默认 F7），玩家可在按键绑定界面修改，按下时撤销自己上一步操作。
 *  - Shift+滚轮调节工具数值：Shift 键为硬编码默认键，不暴露给按键绑定界面（玩家不可调整）。
 *  - 手持平整工具时，Shift+左/右/上方向键分别调节破坏的 长/宽/高 范围。
 */
public class ExampleModClient implements ClientModInitializer {

	public static final String CATEGORY = "key.categories." + ExampleMod.MOD_ID;
	public static final KeyMapping UNDO_KEY = KeyBindingHelper.registerKeyBinding(
		new KeyMapping("key." + ExampleMod.MOD_ID + ".undo",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F7, CATEGORY));

	// 方向键上一次的按下状态，用于边沿检测（避免按住时每 tick 触发一次调节）
	private static boolean prevLeft;
	private static boolean prevRight;
	private static boolean prevUp;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (UNDO_KEY.consumeClick()) {
				ClientPlayNetworking.send(new UndoPacket());
			}
			if (mc.player != null) {
				handleLevelingArrows(mc);
			}
		});
	}

	/**
	 * 手持平整工具 + Shift 时，按左/右/上方向键分别把破坏的 长/宽/高 范围 +1，
	 * 写入手持物品 NBT 并同步给服务端（供 use() 读取）。
	 */
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
