package cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：手持平整工具按 Shift+方向键调节破坏 长/宽/高 范围时，
 * 把新数值（kind 区分维度）同步给服务端，写入手持物品的 NBT，供 use() 读取。
 *
 * kind：0 = 长范围（level_length），1 = 宽范围（level_width），2 = 高范围（level_height）。
 */
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
