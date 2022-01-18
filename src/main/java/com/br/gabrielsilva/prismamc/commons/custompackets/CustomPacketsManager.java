package com.br.gabrielsilva.prismamc.commons.custompackets;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.PluginInstance;

public class CustomPacketsManager {

	public static int packetsReceiveds = 0, packetsSendeds = 0;
	
	
	public static String getSender() {
		if (Core.getPluginInstance() == PluginInstance.BUNGEECORD) {
			return "BungeeCord";
		} else {
			if (BukkitMain.getServerID() == 0) {
				return BukkitMain.getServerType().getName().toLowerCase();
			} else {
				return BukkitMain.getServerType().getName().toLowerCase() + BukkitMain.getServerID();
			}
		}
	}
	
	public static void addPacketsSended() {
		packetsSendeds++;
	}
	
	public static void addPacketsReceiveds() {
		packetsReceiveds++;
	}
}