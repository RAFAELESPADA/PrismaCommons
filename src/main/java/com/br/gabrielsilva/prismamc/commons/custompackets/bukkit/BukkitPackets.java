package com.br.gabrielsilva.prismamc.commons.custompackets.bukkit;

import java.util.HashMap;
import java.util.Map;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketKickPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketRespawnPlayer;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketSetClanTag;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketTeleportPlayerByReport;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public abstract class BukkitPackets {

	private static final Map<String, Class<?>> MAP_CLASS = new HashMap<String, Class<?>>();
	
	public static void registerPackets() {
		register(PacketKickPlayer.class);
		register(PacketRespawnPlayer.class);
		register(PacketSetClanTag.class);
		register(PacketTeleportPlayerByReport.class);
		register(PacketUpdateField.class);
	}
	
	private static void register(Class<? extends BukkitPackets> packetClass) {
		MAP_CLASS.put(packetClass.getSimpleName(), packetClass);
	}
	
	public static BukkitPackets getPacket(String packetName) {
		try {
			return (BukkitPackets) MAP_CLASS.get(packetName).newInstance();
		} catch (Exception e) {
			Core.console("Erro no pacote -> " + packetName);
			return null;
		}
	}
	
	public abstract void read(ByteArrayDataInput in);

	public abstract void write(ByteArrayDataOutput out);

	public abstract void handle(BukkitPacketsHandler handler) throws HandlePacketException;
	
	public abstract String getPacketName();
}