package com.br.gabrielsilva.prismamc.commons.custompackets.bungee;

import java.util.HashMap;
import java.util.Map;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketCheckPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketFindPlayerByReport;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketLoadClan;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketUpdateSkin;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public abstract class BungeePackets {

	private static final Map<String, Class<?>> MAP_CLASS = new HashMap<String, Class<?>>();
	
	public static void registerPackets() {
		register(PacketBungeeUpdateField.class);
		register(PacketCheckPlayer.class);
		register(PacketFindPlayerByReport.class);
		register(PacketLoadClan.class);
		register(PacketUpdateSkin.class);
	}
	
	private static void register(Class<? extends BungeePackets> packetClass) {
		MAP_CLASS.put(packetClass.getSimpleName(), packetClass);
	}
	
	public static BungeePackets getPacket(String packetName) {
		try {
			return (BungeePackets) MAP_CLASS.get(packetName).newInstance();
		} catch (Exception e) {
			Core.console("Erro no pacote -> " + packetName);
			return null;
		}
	}
	
	public abstract void read(ByteArrayDataInput in);

	public abstract void write(ByteArrayDataOutput out);

	public abstract void handle(BungeePacketsHandler handler) throws HandlePacketException;
	
	public abstract String getPacketName();
}