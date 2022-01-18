package com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketUpdateSkin extends BungeePackets {

	private String nick, skinToApply;

	public PacketUpdateSkin() {}
	
	public PacketUpdateSkin(String nick, String skinToApply) {
		this.nick = nick;
		this.skinToApply = skinToApply;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nick = in.readUTF();
		this.skinToApply = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNick());
		out.writeUTF(getSkinToApply());
	}

	@Override
	public void handle(BungeePacketsHandler handler) throws HandlePacketException {
		handler.handleLoadUpdateSkin(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}