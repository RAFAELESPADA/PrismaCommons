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
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PlayerRankingHologram {

	private String name;
	private List<PlayerTop> top;
	private boolean created;
	private Plugin instance;
	
	public PlayerRankingHologram(Plugin instance, String name) {
		setInstance(instance);
		
		setName(name);
		setCreated(false);
		setTop(new ArrayList<>());
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
				HologramAPI.createHologram("top1-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top1.spawn();
		y = y-=0.25;
		
		Hologram top2 = 
				HologramAPI.createHologram("top2-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top2.spawn();
		y = y-=0.25;
		
		Hologram top3 = 
				HologramAPI.createHologram("top3-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top3.spawn();
		y = y-=0.25;
		
		Hologram top4 = 
				HologramAPI.createHologram("top4-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top4.spawn();
		y = y-=0.25;
		
		Hologram top5 = 
				HologramAPI.createHologram("top5-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top5.spawn();
		y = y-=0.25;
		
		Hologram top6 = 
				HologramAPI.createHologram("top6-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top6.spawn();
		y = y-=0.25;
		
		Hologram top7 = 
				HologramAPI.createHologram("top7-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top7.spawn();
		y = y-=0.25;
		
		Hologram top8 = 
				HologramAPI.createHologram("top8-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top8.spawn();
		y = y-=0.25;
		
		Hologram top9 = 
				HologramAPI.createHologram("top9-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top9.spawn();
		y = y-=0.25;
		
		Hologram top10 = 
				HologramAPI.createHologram("top10-ranking", new Location(world, location.getX(), y, location.getZ()), "§7Carregando...");
		top10.spawn();
		setCreated(true);
		
		updateValues();
	}
	
	private String getFullString(int id) {
		final PlayerTop playerTop = getTop().get(id - 1);
		
		final PlayerRank rank = PlayerRank.getRanking(playerTop.getValorNotReformuled());
		
		return "§a" + id + " §7- " + playerTop.getNome() + " §f: §e" + playerTop.getValor() + 
				" §7[" + rank.getCor() + rank.getSimbolo() + "§7] " + rank.getCor() + rank.getNome();
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
				 
				 if (name.contains("-ranking")) {
					 name = name.replace("top", "");
					 int pos = Integer.valueOf(name.split("-ranking")[0]);
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
				"SELECT * FROM accounts ORDER BY xp DESC LIMIT 10");
				ResultSet result = preparedStatament.executeQuery();
				
				int id = 0;
				while (result.next()) {
					id++;
					
					final String nick = result.getString("nick"),
							grupo = result.getString("grupo");
				    
				    final Groups tag = Groups.getFromString(grupo);
				    final String nome = tag.getCor() + nick;
				    
				    updated.add(new PlayerTop(String.valueOf(id), nome, String.valueOf(result.getInt("xp"))));
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