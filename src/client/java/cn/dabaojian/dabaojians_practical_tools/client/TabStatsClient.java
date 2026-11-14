package cn.dabaojian.dabaojians_practical_tools.client;

import cn.dabaojian.dabaojians_practical_tools.networking.ServerStatsSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class TabStatsClient {
    private static volatile float serverTps = -1.0F;

    private TabStatsClient() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ServerStatsSync.CHANNEL,
            (client, handler, buf, responseSender) -> {
                float receivedTps = buf.readFloat();
                client.execute(() -> serverTps = receivedTps);
            });
    }

    public static float getServerTps() {
        return serverTps;
    }

    public static int getPing() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return -1;
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }
}
