package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketTeleportPlayerByReport extends BukkitPackets {

	private String nickStaffer, nickTarget;
	
	public PacketTeleportPlayerByReport() {}
	
	public PacketTeleportPlayerByReport(String nickStaffer, String nickTarget) {
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
	public void handle(BukkitPacketsHandler handler) throws HandlePacketException {
		handler.handleTeleportPlayerByReport(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}