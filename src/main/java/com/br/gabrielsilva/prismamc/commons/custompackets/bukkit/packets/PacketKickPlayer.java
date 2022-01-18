package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketKickPlayer extends BukkitPackets {

	private String nick, motivo, sendedBy;

	public PacketKickPlayer() {}
	
	public PacketKickPlayer(String nick, String motivo) {
		this.nick = nick;
		this.motivo = motivo;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nick = in.readUTF();
		this.motivo = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNick());
		out.writeUTF(getMotivo());
	}

	@Override
	public void handle(BukkitPacketsHandler handler) throws HandlePacketException {
		handler.handleKickPlayer(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}