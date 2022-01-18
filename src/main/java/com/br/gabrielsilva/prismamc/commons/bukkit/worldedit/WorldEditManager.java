package com.br.gabrielsilva.prismamc.commons.bukkit.worldedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldEditManager {

	private HashMap<UUID, Location> pos1, pos2;
	private HashMap<UUID, Constructions> construções;
	
	public WorldEditManager() {
		this.pos1 = new HashMap<>();
		this.pos2 = new HashMap<>();
		this.construções = new HashMap<>();
	}
	
	public void addConstructionByUUID(Player owner, List<Location> locations) {
		this.construções.put(owner.getUniqueId(), new Constructions(owner, locations));
	}
	
	public void removeConstructionByUUID(UUID uuid) {
		this.construções.remove(uuid);
	}
	
	public boolean hasRollingConstructionByUUID(UUID uuid) {
		return this.construções.containsKey(uuid);
	}
	
	public Constructions getConstructionByUUID(UUID uuid) {
		return this.construções.get(uuid);
	}
	
	public Location getPos1(Player player) {
		return this.pos1.get(player.getUniqueId());
	}
	
	public Location getPos2(Player player) {
		return this.pos2.get(player.getUniqueId());
	}
	
	public boolean continueEdit(Player player) {
		if (!this.pos1.containsKey(player.getUniqueId())) {
			player.sendMessage("§e§lWORLDEDIT §fA primeira localização não foi setada.");
			return false;
		}
		if (!this.pos2.containsKey(player.getUniqueId())) {
			player.sendMessage("§e§lWORLDEDIT §fA segunda localização não foi setada.");
			return false;
		}
		return true;
	}
	
	public void setPos1(Player player, Location loc) {
		this.pos1.put(player.getUniqueId(), loc);
		player.sendMessage("§e§lWORLDEDIT §fPrimeira localização setada em: (§7" + loc.getBlockX() + "§f,§7" + loc.getBlockY() + "§f,§7" + loc.getBlockZ() + "§f).");
	}
	
	public void setPos2(Player player, Location loc) {
		this.pos2.put(player.getUniqueId(), loc);
		player.sendMessage("§e§lWORLDEDIT §fSegunda localização setada em: (§7" + loc.getBlockX() + "§f,§7" + loc.getBlockY() + "§f,§7" + loc.getBlockZ() + "§f).");
	}
	
	public List<Location> getLocationsFromTwoPoints(Location location1, Location location2) {
		List<Location> locations = new ArrayList<>();
		int topBlockX = (location1.getBlockX() < location2.getBlockX() ? location2.getBlockX() : location1.getBlockX()),
			bottomBlockX = (location1.getBlockX() > location2.getBlockX() ? location2.getBlockX() : location1.getBlockX()),
			    topBlockY = (location1.getBlockY() < location2.getBlockY() ? location2.getBlockY() : location1.getBlockY()),
				    bottomBlockY = (location1.getBlockY() > location2.getBlockY() ? location2.getBlockY() : location1.getBlockY()),
				        topBlockZ = (location1.getBlockZ() < location2.getBlockZ() ? location2.getBlockZ() : location1.getBlockZ()),
				            bottomBlockZ = (location1.getBlockZ() > location2.getBlockZ() ? location2.getBlockZ() : location1.getBlockZ());
		
		for (int x = bottomBlockX; x <= topBlockX; x++) {
			 for (int z = bottomBlockZ; z <= topBlockZ; z++) {
				  for (int y = bottomBlockY; y <= topBlockY; y++) {
					   locations.add(new Location(location1.getWorld(), x, y, z));
				  }
			 }
		}
		
		return locations;
	}
}