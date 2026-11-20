package cn.dabaojian.dabaojians_practical_tools.networking.packet;

import cn.dabaojian.dabaojians_practical_tools.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record LevelToolValuePacket(int kind, int value) implements FabricPacket {

	public static final int KIND_LENGTH = 0;
	public static final int KIND_WIDTH = 1;
	public static final int KIND_HEIGHT = 2;

	public static final PacketType<LevelToolValuePacket> TYPE =
		PacketType.create(new ResourceLocation(ExampleMod.MOD_ID, "level_tool_value"), LevelToolValuePacket::new);

	public LevelToolValuePacket(FriendlyByteBuf buf) {
		this(buf.readInt(), buf.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeInt(kind);
		buf.writeInt(value);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}
}
