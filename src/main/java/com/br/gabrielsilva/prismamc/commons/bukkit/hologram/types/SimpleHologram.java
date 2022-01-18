package com.br.gabrielsilva.prismamc.commons.bukkit.hologram.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.Hologram;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.HologramAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.PlayerTop;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.PluginConfig;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleHologram {

	private String name, tableName, keyToFind;
	private List<PlayerTop> top;
	private boolean created;
	private Plugin instance;
	
	public SimpleHologram(Plugin instance, String name, String tableName, String keyToFind) {
		setInstance(instance);
		
		setName(name);
		setCreated(false);
		setTop(new ArrayList<>());
		setTableName(tableName);
		setKeyToFind(keyToFind);
	}
	
	public void create() {
		create("world");
	}
	
	public void create(String worldName) {
		if (isCreated()) {
			return;
		}
		
		PluginConfig.createLoc(getInstance(), "hologramas." + getName().toLowerCase(), worldName);

		Location location = PluginConfig.getNewLoc(getInstance(), "hologramas." + getName().toLowerCase());
		World world = location.getWorld();
		
		double y = location.getY();
		
		Hologram titulo = 
				HologramAPI.createHologram("top" + getName().toLowerCase(), location, "§6§lTOP 10 "+ getName().toUpperCase());
		titulo.spawn();
		
		y = y-=0.25;
		
		Hologram space = titulo.addLineBelow("", "");
		
		y = y-=0.25;
		
		Hologram top1 = 
				HologramAPI.createHologram("top1-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top1.spawn();
		y = y-=0.25;
		
		Hologram top2 = 
				HologramAPI.createHologram("top2-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top2.spawn();
		y = y-=0.25;
		
		Hologram top3 = 
				HologramAPI.createHologram("top3-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top3.spawn();
		y = y-=0.25;
		
		Hologram top4 = 
				HologramAPI.createHologram("top4-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top4.spawn();
		y = y-=0.25;
		
		Hologram top5 = 
				HologramAPI.createHologram("top5-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top5.spawn();
		y = y-=0.25;
		
		Hologram top6 = 
				HologramAPI.createHologram("top6-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top6.spawn();
		y = y-=0.25;
		
		Hologram top7 = 
				HologramAPI.createHologram("top7-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top7.spawn();
		y = y-=0.25;
		
		Hologram top8 = 
				HologramAPI.createHologram("top8-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top8.spawn();
		y = y-=0.25;
		
		Hologram top9 = 
				HologramAPI.createHologram("top9-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top9.spawn();
		y = y-=0.25;
		
		Hologram top10 = 
				HologramAPI.createHologram("top10-" + getName(), new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top10.spawn();
		setCreated(true);
		
		updateValues();
	}
	
	private String getFullString(int id) {
		final PlayerTop playerTop = getTop().get(id - 1);
		return "§a" + id + " §7- " + playerTop.getNome() + " §f: §e" + playerTop.getValor();
	}
	
	public void updateHolograms() {
		for (Hologram hologram : HologramAPI.getHolograms()) {
			 if (hologram.isSpawned()) {
				 String name = hologram.getName();
				 if ((name.equalsIgnoreCase("")) || (name.equalsIgnoreCase("name"))) {
					 continue;
				 }
				 if (name.equalsIgnoreCase("jogandoAgora")) {
					 continue;
				 }
				 
				 if (name.endsWith("-" + getName())) {
					 name = name.replace("top", "");
					 int pos = Integer.valueOf(name.split("-" + getName())[0]);
					 
					 hologram.setText(getFullString(pos));
				 }
			 }
		}
	}
	
	public void updateValues() {
		BukkitMain.runAsync(() -> {
			
			List<PlayerTop> updated = new ArrayList<>();
			
			try {
				PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
				"SELECT * FROM "+getTableName()+" ORDER BY "+getKeyToFind()+" DESC LIMIT 10");
				ResultSet result = preparedStatament.executeQuery();
				
				int id = 0;
				while (result.next()) {
					id++;
					
					final String nick = result.getString("nick"),
							group = MySQLManager.getString("accounts", "nick", nick, "grupo");
				    
					final Groups tag = Groups.getFromString(group);
					
				    updated.add(new PlayerTop(String.valueOf(id), tag.getCor() + nick, String.valueOf(result.getInt(getKeyToFind()))));
				}
				
				while (id != 10) {
					id++;
					updated.add(new PlayerTop(String.valueOf(id), "§7Ninguém", "0"));
				}
				result.close();
				preparedStatament.close();
				
				top.clear();
				for (PlayerTop tops : updated) {
					 top.add(tops);
				}
				
				updated.clear();
				updated = null;
				
				updateHolograms();
			} catch (SQLException ex) {
				BukkitMain.console("§cOcorreu um erro ao tentar atualizar o TOP RANK -> " + ex.getLocalizedMessage());
			}
		});
	}
}