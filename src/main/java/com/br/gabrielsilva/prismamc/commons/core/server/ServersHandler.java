package com.br.gabrielsilva.prismamc.commons.core.server;

import java.util.HashMap;
import java.util.Map;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.server.types.HungerGamesEventServer;
import com.br.gabrielsilva.prismamc.commons.core.server.types.HungerGamesServer;
import com.br.gabrielsilva.prismamc.commons.core.server.types.NetworkServer;
import com.br.gabrielsilva.prismamc.commons.core.server.types.Stages;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

@Getter @Setter
public class ServersHandler {

	private final int salasHG = 8;
	private int globalCount;
	private HashMap<String, HungerGamesServer> hungerGamesServers;
	private HashMap<String, NetworkServer> networkServers;
	private HashMap<String, HungerGamesEventServer> hungerGamesEventServer;
	private Jedis jedis;
	
	public ServersHandler() {
		setGlobalCount(0);
	}
	
	public void init() {
		initJedis();
		
		setHungerGamesServers(new HashMap<>());
		setNetworkServers(new HashMap<>());
		setHungerGamesEventServer(new HashMap<>());
		
		for (int i = 1; i <= salasHG; i++) {
			 getHungerGamesServers().put("hg" + i, new HungerGamesServer(i));
		}
		
		getHungerGamesEventServer().put("evento1", new HungerGamesEventServer(1));		
		getNetworkServers().put("gladiator", new NetworkServer("gladiator"));
		getNetworkServers().put("kitpvp", new NetworkServer("kitpvp"));
	}
	
	private void initJedis() {
		setJedis(Core.getRedis().getPool().getResource());
	}
	
	public void updateAllServers() {
		try {
			if (getJedis() != null) {
				updateGlobalCount(getJedis());
				
				for (HungerGamesServer hungerGamesServers : getHungerGamesServers().values()) {
					 hungerGamesServers.update(getJedis());
				}
				
				for (HungerGamesEventServer hungerGamesServers : getHungerGamesEventServer().values()) {
					 hungerGamesServers.update(getJedis());
				}
				
				for (NetworkServer networkServers : getNetworkServers().values()) {
					 networkServers.update(getJedis());
				}
			}
		} catch (JedisException ex) {
			Core.console("Ocorreu um erro ao tentar atualizar todos os servidores. -> " + ex.getLocalizedMessage());
			setGlobalCount(0);
		} 
	}
	
	public void updateGlobalCount(Jedis jedis) {
		if (!jedis.exists("globalPlayers")) {
			setGlobalCount(0);
			return;
		}
		setGlobalCount(Integer.valueOf(jedis.get("globalPlayers")));
	}
	
	public void sendUpdateNetworkServer(String serverName, int onlines, int maxPlayers) {
		if (!Core.getRedis().isConnected()) {
			return;
		}
		
		if (getJedis() == null) {
			initJedis();
			sendUpdateNetworkServer(serverName, onlines, maxPlayers);
			return;
		}
		
		try {
			Map<String, String> hash =  new HashMap<>();
			
			hash.put("onlines", "" + onlines);
			hash.put("maxPlayers", "" + maxPlayers);
			hash.put("lastTime", "" + System.currentTimeMillis());
			
			getJedis().hmset("serverInfo:" + serverName, hash);
			
			hash.clear();
			hash = null;
		} catch (JedisException ex) {
			Core.console("Ocorreu um erro ao enviar um UpdateNetworkServer para o cache.");
		}
	}
	
	public void sendUpdateHungerGamesServer(int serverID, int vivos, int tempo, String estagio, int maxPlayers) {
		if (!Core.getRedis().isConnected()) {
			return;
		}
		
		if (getJedis() == null) {
			initJedis();
			sendUpdateHungerGamesServer(serverID, vivos, tempo, estagio, maxPlayers);
			return;
		}
		
		try {
			Map<String, String> hash =  new HashMap<>();
			
			hash.put("vivos", "" + vivos);
			hash.put("tempo", "" + tempo);
			hash.put("estagio", estagio);
			hash.put("maxPlayers", "" + maxPlayers);
			hash.put("lastTime", "" + System.currentTimeMillis());
			
			getJedis().hmset("serverInfo:hg" + serverID, hash);
			
			hash.clear();
			hash = null;
		} catch (JedisException ex) {
			Core.console("Ocorreu um erro ao enviar um UpdateHungerGamesServer para o cache.");
		}
	}
	
	public void sendUpdateHungerGamesEvent(int serverID, int vivos, int tempo, String estagio, int maxPlayers) {
		if (!Core.getRedis().isConnected()) {
			return;
		}
		
		if (getJedis() == null) {
			initJedis();
			sendUpdateHungerGamesEvent(serverID, vivos, tempo, estagio, maxPlayers);
			return;
		}
		
		try {
			Map<String, String> hash =  new HashMap<>();
			
			hash.put("vivos", "" + vivos);
			hash.put("tempo", "" + tempo);
			hash.put("estagio", estagio);
			hash.put("maxPlayers", "" + maxPlayers);
			hash.put("lastTime", "" + System.currentTimeMillis());
			
			getJedis().hmset("serverInfo:evento" + serverID, hash);
			
			hash.clear();
			hash = null;
		} catch (JedisException ex) {
			Core.console("Ocorreu um erro ao enviar um UpdateHungerGamesEventServer para o cache.");
		}
	}
	
	public int getAmountHungerGames() {
		int onlines = 0;
		
		for (HungerGamesServer hgs : hungerGamesServers.values()) {
			 onlines+=hgs.getVivos();
		}
		
		for (HungerGamesEventServer hgs : hungerGamesEventServer.values()) {
			 onlines+=hgs.getVivos();
		}
		return onlines;
	}
	
	public HungerGamesServer getBestHungerGames() {
		HungerGamesServer finded = null;
		
		int maxPlayers = -1;
		
		for (int i = salasHG; i >= 1; i--) {
			 HungerGamesServer hungerGamesServer = getHungerGamesServer("hg" + i);
			 if (hungerGamesServer.isOnline()) {
				 if (hungerGamesServer.getEstagio() == Stages.PREJOGO) {
					 int onlines = hungerGamesServer.getVivos();
					 if (onlines < hungerGamesServer.getMaxPlayers()) {
						 if (onlines >= maxPlayers) {
							 maxPlayers = onlines;
							 finded = hungerGamesServer;
						 }
					 }
				 }
			 }
		}
		return finded;
	}
	
	public String getServerInstance(String nome) {
		if (this.networkServers.containsKey(nome)) {
			return "NetworkServer";
		} else if (this.hungerGamesServers.containsKey(nome)) {
			return "HungerGamesServer";
		} else if (this.hungerGamesServers.containsKey(nome)) {
			return "HungerGamesEventServer";
		}
		return "Unknown";
	}
	
	public NetworkServer getNetworkServer(String name) {
		return this.networkServers.get(name);
	}
	
	public HungerGamesServer getHungerGamesServer(String name) {
		return this.hungerGamesServers.get(name);
	}
	
	public HungerGamesEventServer getHungerGamesEventServer(String name) {
		return this.hungerGamesEventServer.get(name);
	}
}