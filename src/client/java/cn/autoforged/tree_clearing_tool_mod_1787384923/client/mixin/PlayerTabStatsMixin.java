
package cn.autoforged.tree_clearing_tool_mod_1787384923.client.mixin;

import cn.autoforged.tree_clearing_tool_mod_1787384923.client.TabStatsClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabStatsMixin {

    @Shadow
    private Component header;

    @Unique
    private Component practicalTools$originalHeader;

    /**
     * 在玩家列表渲染前，将 TPS 和 Ping 添加到原有 Header。
     */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("HEAD")
    )
    private void practicalTools$addStatsToHeader(
            GuiGraphics guiGraphics,
            int screenWidth,
            Scoreboard scoreboard,
            Objective objective,
            CallbackInfo ci
    ) {
        practicalTools$originalHeader = this.header;

        float tps = TabStatsClient.getServerTps();
        int ping = TabStatsClient.getPing();

        MutableComponent line = Component.empty();

        // TPS：红色
        line.append(
                Component.literal("TPS: ")
                        .withStyle(ChatFormatting.RED)
        );

        line.append(
                Component.literal(
                        tps < 0
                                ? "--"
                                : String.format(java.util.Locale.ROOT, "%.1f", tps)
                ).withStyle(ChatFormatting.RED)
        );

        line.append(Component.literal("   "));

        // Ping 标签：黄色
        line.append(
                Component.literal("ping: ")
                        .withStyle(ChatFormatting.YELLOW)
        );

        // Ping 数值颜色
        ChatFormatting pingColor;

        if (ping < 0) {
            pingColor = ChatFormatting.GRAY;
        } else if (ping < 100) {
            pingColor = ChatFormatting.GREEN;
        } else if (ping <= 500) {
            pingColor = ChatFormatting.YELLOW;
        } else {
            pingColor = ChatFormatting.RED;
        }

        line.append(
                Component.literal(ping < 0 ? "--" : ping + "ms")
                        .withStyle(pingColor)
        );

        // 保留服务器原本设置的 Header
        if (this.header != null && !this.header.getString().isEmpty()) {
            line.append(Component.literal("   "));
            line.append(this.header);
        }

        this.header = line;
    }

    /**
     * 玩家列表渲染结束后，恢复原有 Header。
     */
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V",
            at = @At("RETURN")
    )
    private void practicalTools$restoreHeader(
            GuiGraphics guiGraphics,
            int screenWidth,
            Scoreboard scoreboard,
            Objective objective,
            CallbackInfo ci
    ) {
        this.header = practicalTools$originalHeader;
        practicalTools$originalHeader = null;
    }
}

