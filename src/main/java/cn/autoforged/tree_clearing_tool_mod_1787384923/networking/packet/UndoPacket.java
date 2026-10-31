package cn.autoforged.tree_clearing_tool_mod_1787384923.networking.packet;

import cn.autoforged.tree_clearing_tool_mod_1787384923.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 客户端 -> 服务端：玩家按「撤销」键（默认 F7）时发送，请求撤销上一步工具破坏操作。
 * 不带任何数据，服务端在收包回调里调用 {@code UndoManager.undo} 恢复方块。
 */
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
