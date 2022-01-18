package com.br.gabrielsilva.prismamc.commons.bungee.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.account.SessionManager;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.config.ConfigManager;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.premiummap.PremiumMapManager;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

@Getter @Setter
public class Manager {

	private int index, minutos;
	private ArrayList<String> messages;
	private PremiumMapManager premiumMapManager;
	private SessionManager sessionManager;
	private ConfigManager configManager;
	
	public Manager() {
		setIndex(0);
		setMinutos(2);
		setMessages(new ArrayList<>());
		
		setPremiumMapManager(new PremiumMapManager());
		setSessionManager(new SessionManager());
		setConfigManager(new ConfigManager());
		
		getPremiumMapManager().init();
	}
	
	public void warnStaff(String message) {
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 if (proxiedPlayer.hasPermission("prismamc.receivewarn")) {
				 if (BungeeMain.isValid(proxiedPlayer)) {
				     proxiedPlayer.sendMessage(message);
				 }
			 }
		}
	}
	
	public void warnStaff(String message, Groups tag) {
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 if (proxiedPlayer.hasPermission("prismamc.receivewarn")) {
				 if (BungeeMain.isValid(proxiedPlayer)) {
					 if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).getGrupo().getNivel() >= tag.getNivel()) {
						 proxiedPlayer.sendMessage(message);
					 }
				 }
			 }
		}
	}
	
	public void init() {
		getPremiumMapManager().loadAll();
		
		handleDefaultSkin();
		handleGlobalWhitelist();
		
		BungeeMain.getInstance().getProxy().getScheduler().schedule(BungeeMain.getInstance(), () -> {
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				 jedis.set("globalPlayers", "" + ProxyServer.getInstance().getOnlineCount());
			}
			Core.getServersHandler().updateAllServers();
		}, 2, 2, TimeUnit.SECONDS);
		
		BungeeMain.getInstance().getProxy().getScheduler().schedule(BungeeMain.getInstance(), () -> {
			if (index >= messages.size()) {
				index = 0;
			}

			final String message = messages.get(index);
			
			for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
				 proxiedPlayers.sendMessage(message);
			}
			index++;
			
			MySQLManager.contains("accounts", "nick", "biielbr");
		}, minutos, minutos, TimeUnit.MINUTES);
		
		BungeeMain.getInstance().getProxy().getScheduler().schedule(BungeeMain.getInstance(), () -> {
			List<String> toRemove = new ArrayList<>();
			
			for (BungeePlayer proxyPlayers : getSessionManager().getSessions().values()) {
				 if (proxyPlayers != null) {
					 if (!proxyPlayers.isValidSession()) {
						 toRemove.add(proxyPlayers.getNick().toLowerCase());
					 }
				 }
			}
			
			if (toRemove.size() != 0) {
				BungeeMain.console("Foram removidas -> " + toRemove.size() + " sessoes expiradas.");
				
				for (String nicks : toRemove) {
					 ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(nicks);
					 if (proxiedPlayer == null) {
						 getSessionManager().getSessions().remove(nicks);
					 } else {
						 if (proxiedPlayer.getServer() != null) {
							 if (proxiedPlayer.getServer().getInfo().getName().equalsIgnoreCase("Login")) {
								 proxiedPlayer.disconnect("§cSua sessão expirou.");
								 getSessionManager().removeSession(nicks);
							 } else if (proxiedPlayer.getServer().getInfo().getName().equalsIgnoreCase("lobby")) {
								 proxiedPlayer.disconnect("§cSua sessão expirou.");
								 getSessionManager().removeSession(nicks);
							 }
						 }
					 }
				}
			}
			
			toRemove.clear();
			toRemove = null;
		}, 10, 1, TimeUnit.HOURS);
		
		BungeeMain.getInstance().getProxy().getScheduler().schedule(BungeeMain.getInstance(), () -> {
			sendLogs();
			
			System.gc();
		}, 2, 2, TimeUnit.MINUTES);
	}
	
	private void handleDefaultSkin() {
		if (!MySQLManager.contains("skins", "nick", "0171")) {
			
			try {
				PreparedStatement set = Core.getMySQL().getConexão().prepareStatement(
				"INSERT INTO skins (nick, value, signature, timestamp) VALUES (?, ?, ?, ?)");
				
				set.setString(1, "0171");
				set.setString(2, "eyJ0aW1lc3RhbXAiOjE1NzUxNTE1Mjg5OTcsInByb2ZpbGVJZCI6IjM2NTBlMzZhZmU0ZjRmMGRiYTQ4NDAxY2VkYjE1MGUxIiwicHJvZmlsZU5hbWUiOiIwMTcxIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kYWFmOWZhZGQ3ZWQyMDAxZmUwYTlmZjI3ZmQxYmY3YjBhNjMyZjJlZmY3MGYxMjkzMGIzNGIzNWJlYWE4NjFkIn19fQ==");
				set.setString(3, "iEPj+++vKgJsMDXvgqjMZpag/Wzz/LLOjEK+OlUlf1Fn4vRFrfylMYlH+KopzX1TcdoY5vt1fEbgradTJEFUuFOXL7AI+RZYUoZ9mMbPpXn3Xbkhcw+Q5D9+EDV1WHVLXpnM3sMZPApMAGNMzOAUUChxQbz0HVrB3OjHtbbmkns2hecABaomRC9Gd4b8mWK/5u1gYUQEwF2I+VmJNuwkzOCUhMhMp4Z4RKg8vfePEXqi+cXD4p+phUU+CGxorHr3VakOI2RuGig8JQAI9L92q6C6yFL1j61nCUn1CVokHtGNFtLxE1ow6PuofLsWjR8+F0ksv/qk1jzYY3tucaXGuuz+QRiWTQiXGd+jp5oHFMx5IF77zU7naRJ4fNunhn3Z/AWQpV4v5huRemHe2QLSfPaICNe6cs9P11R/+EsEJs1R7cO9k3rfulMQPlKLHntI2vxVRnl3VqarD4r3o0sZJWfOp9g3xiKg3KTwx5d28zd7uZqmx+i2ien8Vz8J42/II8bGNP9GIxgbWAPRT2bERJca+lRwDnkS5CGA6QZB3euuWubBLPVV/oG7Q28Ij/MCMTOLoVrBIqM39punuEtULK57u/ERS4YkTIh7+j55tAl0lB9Vpo7Uu3yAPjOsG6OPMrZMUNgzi/PtC3KhWyYgFfGVUBNoIAdSuKs5C2JH1ps=");
				set.setString(4, String.valueOf(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(80)));
    		
				set.executeUpdate();
				set.close();
			} catch (SQLException ex) {
				BungeeMain.console("Ocorreu um erro ao tentar adicionar a Skin padrao!");
			}
		}
	}

	private void handleGlobalWhitelist() {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			 if (!jedis.exists("globalWhitelistPlayers")) {
				 HashMap<String, String> hash = new HashMap<>();
				 hash.put("biielbr", ".");
				 jedis.hmset("globalWhitelistPlayers", hash);
				 BungeeMain.getWhitelistPlayers().add("biielbr");
			 } else {
				 Map<String, String> hash = jedis.hgetAll("globalWhitelistPlayers");
				 for (String nicks : hash.keySet()) {
					  BungeeMain.getWhitelistPlayers().add(nicks.toLowerCase());
				 }
			 }
			 if (jedis.exists("globalWhitelist")) {
				 BungeeMain.setGlobalWhitelist(Boolean.valueOf(jedis.get("globalWhitelist")));
			 } else {
				 BungeeMain.setGlobalWhitelist(false);
				 jedis.set("globalWhitelist", "false");
			 }
		}
	}

	public void sendLogs() {
		if (Core.getLogsCommandBungee().size() == 0) {
			return;
		}
		
		BungeeMain.runAsync(() -> {
	    	try {
	            File saveTo = new File(
	            		BungeeMain.getInstance().getDataFolder(), "logsCommandBungee.log");
	            
	            if (!saveTo.exists()) {
	                 saveTo.createNewFile();
	            }
	            FileWriter fw = new FileWriter(saveTo, true);
	            PrintWriter pw = new PrintWriter(fw);
	            
	            for (String log : Core.getLogsCommandBungee()) {
	            	 pw.println(log);
	            }
	            
	            pw.flush();
	            pw.close();
	            
	            Core.getLogsCommandBungee().clear();
	         } catch (IOException e) {
	        	 BukkitMain.console("Ocorreu um erro ao tentar enviar os Logs de Comandos -> " + e.getLocalizedMessage());
	         }
		});
	}
}