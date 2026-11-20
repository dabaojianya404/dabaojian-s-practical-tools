package cn.dabaojian.dabaojians_practical_tools.networking.packet;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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
