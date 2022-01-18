package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketRespawnPlayer extends BukkitPackets {

	private String nick;

	public PacketRespawnPlayer() {}
	
	public PacketRespawnPlayer(String nick) {
		this.nick = nick;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nick = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNick());
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