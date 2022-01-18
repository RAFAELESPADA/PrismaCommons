package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketSetClanTag extends BukkitPackets {

	private String clan, tag;

	public PacketSetClanTag() {}
	
	public PacketSetClanTag(String clan, String tag) {
		this.clan = clan;
		this.tag = tag;
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		this.clan = in.readUTF();
		this.tag = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(getClan());
		out.writeUTF(getTag());
	}

	@Override
	public void handle(BukkitPacketsHandler handler) throws HandlePacketException {
		handler.handleSetClanTag(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}