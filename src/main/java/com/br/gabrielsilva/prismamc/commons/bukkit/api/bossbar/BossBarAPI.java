package com.br.gabrielsilva.prismamc.commons.bukkit.api.bossbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;

public class BossBarAPI {

	private static HashMap<UUID, BossBar> bossBars = new HashMap<>();
	private static boolean initialized = false;
	
	private static void init() {
		if (initialized) {
			return;
		}
		
		initialized = true;
		
		Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
			
			@EventHandler
			public void onQuit(PlayerQuitEvent event) {
				if (getBossBars().containsKey(event.getPlayer().getUniqueId())) {
					getBossBars().get(event.getPlayer().getUniqueId()).remover();
					getBossBars().remove(event.getPlayer().getUniqueId());
				}
			}
			
			@EventHandler
			public void onDeath(PlayerDeathEvent event) {
				if (getBossBars().containsKey(event.getEntity().getUniqueId())) {
					getBossBars().get(event.getEntity().getUniqueId()).remover();
					getBossBars().remove(event.getEntity().getUniqueId());
				}
				if (event.getEntity().getKiller() != null) {
					if (getBossBars().containsKey(event.getEntity().getKiller().getUniqueId())) {
						getBossBars().get(event.getEntity().getKiller().getUniqueId()).remover();
						getBossBars().remove(event.getEntity().getKiller().getUniqueId());
					}
				}
			}
			
			@EventHandler
			public void onUpdate(UpdateEvent event) {
				if (event.getType() != UpdateType.SEGUNDO) {
					return;
				}
				
				if (bossBars.size() != 0) {
					
					List<UUID> toRemove = new ArrayList<>();
					
					for (BossBar boss : bossBars.values()) {
						 if (boss.isCancelar()) {
							 boss.remover();
							 toRemove.add(boss.getPlayer().getUniqueId());
						 } else {
							 boss.onSecond();
						 }
					}
					
					for (UUID uuids : toRemove) {
						 bossBars.remove(uuids);
					}
						
					toRemove.clear();
					toRemove = null;
				}	
			}
			
		}, BukkitMain.getInstance());
	}
	
	public static void sendBossBar(Player player, String name, int tempo) {
		if (!initialized) {
			init();
			sendBossBar(player, name, tempo);
			return;
		}
		
		if (getBossBars().containsKey(player.getUniqueId())) {
			getBossBars().get(player.getUniqueId()).remover();
		}
		
		BossBar bossBar = new BossBar(player, name, true);
		bossBar.setSegundos(tempo);
		
		getBossBars().put(player.getUniqueId(), bossBar);
	}

	public static void sendBossBar(Player player, String name) {
		if (!initialized) {
			init();
			sendBossBar(player, name);
			return;
		}
		
		
		if (getBossBars().containsKey(player.getUniqueId())) {
			getBossBars().get(player.getUniqueId()).remover();
		}
		
		BossBar bossBar = new BossBar(player, name, false);
		getBossBars().put(player.getUniqueId(), bossBar);
	}
	
	public static HashMap<UUID, BossBar> getBossBars() {
		return bossBars;
	}
}