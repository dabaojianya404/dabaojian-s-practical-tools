package cn.autoforged.tree_clearing_tool_mod_1787384923.client.mixin;

import cn.autoforged.tree_clearing_tool_mod_1787384923.client.ToolScrollHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截客户端滚轮事件：当玩家按住 Shift 且手持本模组工具时，把滚轮步进交给
 * {@link ToolScrollHandler} 调节工具数值，并取消原版滚轮（切快捷栏）行为。
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void treeclearingmod$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
		if (!Screen.hasShiftDown()) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null) {
			return;
		}
		if (ToolScrollHandler.handleScroll(mc.player, yOffset)) {
			ci.cancel();
		}
	}
}
