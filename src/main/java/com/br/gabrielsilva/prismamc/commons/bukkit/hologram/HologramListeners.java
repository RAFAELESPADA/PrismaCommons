package com.br.gabrielsilva.prismamc.commons.bukkit.hologram;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

public class HologramListeners implements Listener {

	@EventHandler(priority=EventPriority.MONITOR)
	public void join(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if (player.isOnline()) {
			handleHolograms(player);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		for (Hologram hologram : HologramAPI.getHolograms()) {
			 if (hologram.isSpawned()) {
				 hologram.clean(event.getPlayer());
			 }
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		
		handleHolograms(player);
	}
	
	public static void handleHolograms(Player player) {
		for (Hologram hologram : HologramAPI.getHolograms()) {
			 if (hologram.isSpawned()) {
				 if (hologram.getLocation().getWorld().getName().equals(player.getWorld().getName())) {
					 
					 if (hologram.getLocation().distance(player.getLocation()) <= 50) {
						 //spawn
						 
						 boolean spawn = true;
						 if (hologram.getName().equalsIgnoreCase("name")) {
							 if (!BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).is1_8()) {
								 spawn = false;
							 }
						 }
						 
						 if (spawn) {
							 try {
								 HologramAPI.spawnSingle(hologram, player);
							 } catch (Exception ex) {}
						 }
					 } else {
						 try {
							HologramAPI.despawnSingle(hologram, player);
						 } catch (Exception ex) {}
					 }
				 }
			 }
		}
	}
}