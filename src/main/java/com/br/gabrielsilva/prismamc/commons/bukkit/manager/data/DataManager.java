package com.br.gabrielsilva.prismamc.commons.bukkit.manager.data;

import java.util.HashMap;
import java.util.UUID;

import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;

public class DataManager {

	private HashMap<UUID, BukkitPlayer> bukkitPlayers;
	
	public DataManager() {
		this.bukkitPlayers = new HashMap<>();
	}
	
	public void addBukkitPlayer(UUID uuid, String nick) {
		this.bukkitPlayers.put(uuid, new BukkitPlayer(nick, uuid));
	}
	
	public boolean hasBukkitPlayer(UUID uuid) {
		return this.bukkitPlayers.containsKey(uuid);
	}
	
	public BukkitPlayer getBukkitPlayer(UUID uuid) {
		return this.bukkitPlayers.get(uuid);
	}
	
	public void removeBukkitPlayer(UUID uuid) {
		this.bukkitPlayers.remove(uuid);
	}
	
	public void clearAll() {
		this.bukkitPlayers.clear();
	}
	
	public void removeBukkitPlayerIfExists(UUID uuid) {
		if (hasBukkitPlayer(uuid)) {
			removeBukkitPlayer(uuid);
		}
	}
	
	public HashMap<UUID, BukkitPlayer> getBukkitPlayers() {
		return this.bukkitPlayers;
	}
}