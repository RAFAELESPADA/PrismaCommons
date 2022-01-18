package com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import lombok.Getter;

@Getter
public class PacketBungeeUpdateField extends BungeePackets {

	private String nick, type, field, fieldValue, extraValue;
	
	public PacketBungeeUpdateField() {}
	
	public PacketBungeeUpdateField(String nick, String type, String field, String fieldValue, String extraValue) {
		this.nick = nick;
		this.type = type;
		this.field = field;
		this.fieldValue = fieldValue;
		this.extraValue = extraValue;
	}
	
	public PacketBungeeUpdateField(String nick, String type, String field, String fieldValue) {
		this.nick = nick;
		this.type = type;
		this.field = field;
		this.fieldValue = fieldValue;
		this.extraValue = "";
	}
	
	@Override
	public void read(ByteArrayDataInput in) {
		nick = in.readUTF();
		type = in.readUTF();
		field = in.readUTF();
		fieldValue = in.readUTF();
		extraValue = in.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(getPacketName());
		out.writeUTF(nick);
		out.writeUTF(type);
		out.writeUTF(field);
		out.writeUTF(fieldValue);
		out.writeUTF(extraValue);
	}

	public boolean hasExtraValue() {
		return !getExtraValue().isEmpty() && !getExtraValue().equals("");
	}
	
	@Override
	public void handle(BungeePacketsHandler handler) throws HandlePacketException {
		handler.handleUpdateField(this);
	}

	@Override
	public String getPacketName() {
		return getClass().getSimpleName();
	}
}