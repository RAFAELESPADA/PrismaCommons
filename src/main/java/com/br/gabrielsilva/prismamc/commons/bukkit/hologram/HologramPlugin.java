package com.br.gabrielsilva.prismamc.commons.bukkit.hologram;

import org.bukkit.Bukkit;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

public class HologramPlugin {
	
	public static void onEnable() {
		HologramAPI.enableProtocolSupport();
		
		Bukkit.getPluginManager().registerEvents(new HologramListeners(), BukkitMain.getInstance());

		HologramAPI.packetsEnabled = true;
	}
	
	public static void onDisable() {
		for (Hologram h : HologramAPI.getHolograms()) {
			 HologramAPI.removeHologram(h);
		}
	}
}