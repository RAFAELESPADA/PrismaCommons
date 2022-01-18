package com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketLoadClan extends BungeePackets {

	private String nick, clan;

	public PacketLoadClan() {}
	
	public PacketLoadClan(String nick, String clan) {
		this.nick = nick;
		this.clan = clan;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nick = in.readUTF();
		this.clan = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNick());
		out.writeUTF(getClan());
	}

	@Override
	public void handle(BungeePacketsHandler handler) throws HandlePacketException {
		handler.handleLoadClan(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}