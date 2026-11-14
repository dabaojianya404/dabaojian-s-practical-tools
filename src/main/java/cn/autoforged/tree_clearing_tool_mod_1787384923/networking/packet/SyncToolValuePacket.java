package cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：Shift+滚轮调节工具数值（破坏范围 / 平整基准 Y）时，
 * 把新数值同步给服务端，写入手持物品的 NBT，供 use() 读取。
 */
public record SyncToolValuePacket(int value) implements FabricPacket {

	public static final PacketType<SyncToolValuePacket> TYPE =
		PacketType.create(new ResourceLocation(ExampleMod.MOD_ID, "sync_tool_value"), SyncToolValuePacket::new);

	public SyncToolValuePacket(FriendlyByteBuf buf) {
		this(buf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(value);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
