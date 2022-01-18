package com.br.gabrielsilva.prismamc.commons.core;

import java.util.ArrayList;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.clan.ClanManager;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQL;
import com.br.gabrielsilva.prismamc.commons.core.connections.redis.Redis;
import com.br.gabrielsilva.prismamc.commons.core.server.ServersHandler;
import com.br.gabrielsilva.prismamc.commons.core.utils.fetcher.UUIDFetcher;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.MachineOS;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Core {

	@Getter
	private static final Gson gson = new Gson();
	
	@Getter
	private static final JsonParser parser = new JsonParser();
	
	@Getter @Setter
	private static PluginInstance pluginInstance;
	
	@Getter @Setter
	private static Redis redis;
	
	@Getter @Setter
	private static ClanManager clanManager;
	
	@Getter @Setter
	private static ServersHandler serversHandler;
	
	@Getter @Setter
	private static UUIDFetcher uuidFetcher;
	
	@Getter @Setter
	private static MySQL mySQL;
	
	@Getter @Setter
	private static List<String> logsCommandBungee, logsCommandBukkit;
	
	public Core(PluginInstance instancia) {
		setPluginInstance(instancia);
		
		new MachineOS();
		
		if (getPluginInstance() == PluginInstance.BUNGEECORD) {
		    setLogsCommandBungee(new ArrayList<>());
		} else {
			setLogsCommandBukkit(new ArrayList<>());
		}

		setRedis(new Redis(new JedisPool(
				new JedisPoolConfig(),
				"localhost", 6379, 0, ""
		)));
		setMySQL(new MySQL());
		
		setClanManager(new ClanManager());
		setServersHandler(new ServersHandler());
		setUuidFetcher(new UUIDFetcher());
		
		getUuidFetcher().init();
	}
	
	public static boolean correctlyStarted() {
		if (!getRedis().isConnected()) {
			return false;
		}
		if (!getMySQL().isConnected()) {
			return false;
		}
		return true;
	}
	
	public static void desligar() {
		if (getPluginInstance() == PluginInstance.BUNGEECORD) {
			BungeeMain.desligar();
		} else {
			BukkitMain.desligar();
		}
	}
	
	public static void console(String mensagem) {
		if (getPluginInstance() == PluginInstance.BUNGEECORD) {
			BungeeMain.console(mensagem);
		} else {
			BukkitMain.console(mensagem);
		}
	}
}