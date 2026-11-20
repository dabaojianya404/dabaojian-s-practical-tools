package cn.dabaojian.dabaojians_practical_tools.networking.packet;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record UndoPacket() implements FabricPacket {

	public static final PacketType<UndoPacket> TYPE =
		PacketType.create(new ResourceLocation(ExampleMod.MOD_ID, "undo"), UndoPacket::new);

	public UndoPacket(FriendlyByteBuf buf) {
		this();
	}

	@Override
	public void write(FriendlyByteBuf buf) {
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
