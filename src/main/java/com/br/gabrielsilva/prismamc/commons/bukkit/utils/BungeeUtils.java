package com.br.gabrielsilva.prismamc.commons.bukkit.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;

public class BungeeUtils {

    public static void redirecionar(Player p, String servidor) {
    	ByteArrayOutputStream b = new ByteArrayOutputStream();
    	DataOutputStream out = new DataOutputStream(b);
    	try {
    		out.writeUTF("Connect");
    		out.writeUTF(servidor);
    	} catch (IOException eee) {
			p.kickPlayer("§cOcorreu um erro ao tentar conectar-se ao servidor: " + servidor);
    	}
    	p.sendPluginMessage(BukkitMain.getInstance(), "BungeeCord", b.toByteArray());
    }
    
    public static void redirecionarWithKick(Player p, String servidor) {
    	redirecionar(p, servidor);
		BukkitMain.runLater(() -> {
			if (p.isOnline()) {
				p.kickPlayer("§cOcorreu um erro ao tentar conectar-se ao servidor: " + servidor);
			}
		}, 30);
    }
    
    private static boolean redirectingAll = false;
    
    public static void redirecionarTodosAsync(String servidor, boolean stop) {
    	if (redirectingAll) {
    		return;
    	}
    	
    	redirectingAll = true;
    	
		int delay = 0;
		
		int elapsed = (Bukkit.getOnlinePlayers().size() * 4) + 60;
		
		for (Player onlines : Bukkit.getOnlinePlayers()) {
			 BukkitMain.getManager().getDataManager().getBukkitPlayer(onlines.getUniqueId()).getDataHandler().saveLoadeds();
			 
			 new BukkitRunnable() {
				 public void run() {
					 redirecionar(onlines, servidor);
				 }
			 }.runTaskLater(BukkitMain.getInstance(), delay);
			 
			 delay+=2;
		}
		
		BukkitMain.runLater(() -> {
			Bukkit.shutdown();
		}, elapsed);
    }
}