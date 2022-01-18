package com.br.gabrielsilva.prismamc.commons.bukkit.manager.config;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

public class PluginConfig {

	public static HashMap<String, Location> hashLoc = new HashMap<>();
	
	public static void putNewLoc(Plugin instance, String nome, Player p) {
        instance.getConfig().set("locs."+nome+".world", p.getLocation().getWorld().getName());
        instance.getConfig().set("locs."+nome+".x", p.getLocation().getBlockX() + 0.500);
        instance.getConfig().set("locs."+nome+".y", Double.valueOf(p.getLocation().getY()));
        instance.getConfig().set("locs."+nome+".z", p.getLocation().getBlockZ() + 0.500);
        instance.getConfig().set("locs."+nome+".yaw", Float.valueOf(p.getLocation().getYaw()));
        instance.getConfig().set("locs."+nome+".pitch", Float.valueOf(p.getLocation().getPitch()));
        instance.saveConfig();
        if (hashLoc.containsKey(nome)) {
        	hashLoc.remove(nome);
        }
        getNewLoc(instance, nome);
	}
	
	public static Location getNewLoc(Plugin instance, String nome) {
		if (hashLoc.containsKey(nome)) {
			return hashLoc.get(nome);
		}
		
	    if (!instance.getConfig().contains("locs."+nome+".world")) {
	    	BukkitMain.console("Local inválido.");
	    	return null;
	    }
		
		double x = instance.getConfig().getDouble("locs."+nome+".x"),
				y = instance.getConfig().getDouble("locs."+nome+".y"),
				z = instance.getConfig().getDouble("locs."+nome+".z");
		
		Location loc = new Location(Bukkit.getWorld(instance.getConfig().getString("locs." + nome + ".world")), x, y, z,
		(float)instance.getConfig().getLong("locs."+nome+".yaw"),
		(float)instance.getConfig().getLong("locs."+nome+".pitch"));
		hashLoc.put(nome, loc);
		
		return loc;
	}
	
	public static void createLoc(Plugin instance, String nome) {
		createLoc(instance, nome, "world");
	}
	
	public static void createLoc(Plugin instance, String nome, String world) {
	    if (!instance.getConfig().contains("locs."+nome+".world")) {
	    	instance.getConfig().set("locs."+nome+".world", world);
	    	instance.getConfig().set("locs."+nome+".x", 0.5);
	    	instance.getConfig().set("locs."+nome+".y", 80.5);
	    	instance.getConfig().set("locs."+nome+".z", 0.5);
	    	instance.getConfig().set("locs."+nome+".yaw", 0);
	    	instance.getConfig().set("locs."+nome+".pitch", 0);
	    	instance.saveConfig();
	    }
	}
}