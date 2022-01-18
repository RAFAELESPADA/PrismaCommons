package com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.CustomPacketsManager;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketCheckPlayer extends BungeePackets {

	private String nick, sendedBy;

	public PacketCheckPlayer() {}
	
	public PacketCheckPlayer(String nick) {
		this.nick = nick;
		
		this.sendedBy = CustomPacketsManager.getSender();
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nick = in.readUTF();
		this.sendedBy = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNick());
		out.writeUTF(getSendedBy());
	}

	@Override
	public void handle(BungeePacketsHandler handler) throws HandlePacketException {
		handler.handleCheckPlayer(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}