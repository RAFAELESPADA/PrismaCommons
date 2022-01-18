package com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketFindPlayerByReport extends BungeePackets {

	private String nickStaffer, nickTarget;
	
	public PacketFindPlayerByReport() {}
	
	public PacketFindPlayerByReport(String nickStaffer, String nickTarget) {
		this.nickStaffer = nickStaffer;
		this.nickTarget = nickTarget;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.nickStaffer = in.readUTF();
		this.nickTarget = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getNickStaffer());
		out.writeUTF(getNickTarget());
	}

	@Override
	public void handle(BungeePacketsHandler handler) throws HandlePacketException {
		handler.handleFindPlayerByReport(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}