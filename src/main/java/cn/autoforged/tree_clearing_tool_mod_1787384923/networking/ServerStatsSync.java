package cn.autoforged.tree_clearing_tool_mod_1787384923.networking;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayDeque;
import java.util.Deque;

/** Sends a rolling, server-measured TPS estimate to connected clients once per second. */
public final class ServerStatsSync {
    public static final ResourceLocation CHANNEL = new ResourceLocation(ExampleMod.MOD_ID, "server_stats");
    private static final Deque<Long> TICK_DURATIONS = new ArrayDeque<>();
    private static long lastTickNanos;
    private static int tickCounter;
    private static volatile float currentTps = 20.0F;

    private ServerStatsSync() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(ServerStatsSync::onEndTick);
    }

    private static void onEndTick(MinecraftServer server) {
        long now = System.nanoTime();
        if (lastTickNanos != 0L) {
            TICK_DURATIONS.addLast(now - lastTickNanos);
            while (TICK_DURATIONS.size() > 100) TICK_DURATIONS.removeFirst();
            long total = 0L;
            for (long duration : TICK_DURATIONS) total += duration;
            if (!TICK_DURATIONS.isEmpty()) {
                double averageMs = total / (double) TICK_DURATIONS.size() / 1_000_000.0;
                currentTps = (float) Math.min(20.0, 1000.0 / Math.max(averageMs, 1.0));
            }
        }
        lastTickNanos = now;

        if (++tickCounter >= 20) {
            tickCounter = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeFloat(currentTps);
                ServerPlayNetworking.send(player, CHANNEL, buf);
            }
        }
    }
}
