package com.br.gabrielsilva.prismamc.commons.bukkit;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.CooldownAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuListener;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandFramework;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.UpdateScheduler;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.packets.CustomKnockbackInjector;
import com.br.gabrielsilva.prismamc.commons.bukkit.listeners.CoreListeners;
import com.br.gabrielsilva.prismamc.commons.bukkit.listeners.DamageListener;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.Manager;
import com.br.gabrielsilva.prismamc.commons.bukkit.utils.LogFilter;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.PluginInstance;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.CommandLoader;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.ListenerLoader;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BukkitMain extends JavaPlugin {

	@Getter
	@Setter
	private static BukkitMain instance;

	@Getter
	@Setter
	private static int serverID;

	@Getter
	@Setter
	private static ServerType serverType;

	@Getter
	@Setter
	private static Manager manager;

	@Getter
	@Setter
	private static boolean loaded;

	@Getter
	@Setter
	private static String serverAddress;

	public void onLoad() {
		setLoaded(false);

		setInstance(this);

		new LogFilter().registerFilter();

		new Core(PluginInstance.SPIGOT);

		setManager(new Manager());
		saveDefaultConfig();

		getManager().init();

		setServerType(ServerType.resolveServer(getConfig().getString("Servidor")));
		setServerID(getConfig().getInt("ServidorID"));

		Core.getMySQL().openConnection();
		Core.getRedis().openConnection3();
	}

	public void onEnable() {
		if (!Core.correctlyStarted()) {
			desligar();
			return;
		}


		if (getServerType() == ServerType.UNKNOWN) {
			console("§cConfigure o tipo de servidor na config.");
			desligar();
			return;
		}
		BukkitClient.initChannel(getInstance());
		ServerOptions.init();

		ServerAPI.unregisterCommands(
				"op", "deop", "kill", "about", "ver", "?", "scoreboard", "me", "say", "pl", "plugins",
				"reload", "rl", "stop", "ban", "ban-ip", "msg", "ban-list", "help", "pardon", "pardon-ip", "tp", "xp", "gamemode",
				"minecraft", "minecraft:tell", "tell", "r", "whisper");

		new CommandLoader(new BukkitCommandFramework(getInstance())).
				loadCommandsFromPackage("com.br.gabrielsilva.prismamc.commons.bukkit.commands.register");

		ListenerLoader.loadListenersBukkit(getInstance(), "com.br.gabrielsilva.prismamc.commons.bukkit.listeners");

		Bukkit.getServer().getPluginManager().registerEvents(new MenuListener(), getInstance());
		if (getServerType() == ServerType.HG || getServerType() == ServerType.EVENTO || getServerType() == ServerType.KITPVP) {
			Bukkit.getServer().getPluginManager().registerEvents(new CooldownAPI(), getInstance());
		}

		Bukkit.getServer().getScheduler().runTaskTimer(getInstance(), new UpdateScheduler(), 20, 20);
		getManager().getPermissionManager().setup();

		new DamageListener().setup();
		new CoreListeners();

		runLater(() -> {
			getManager().getConfigManager().unloadGlobalConfig();
			getManager().getConfigManager().unloadPermissoes();
			getManager().getConfigManager().unloadDano();
		}, 40);

		runLater(() -> {
			console("--------------------------------------------------");
			console("Servidor iniciando no tipo: " + getServerType().getName());
			console("Servidor com ID #" + getServerID());
			console("Servidor totalmente carregado e estabilizado.");
			console("---------------------------------------------------");

			setLoaded(true);
		}, 20 * getServerType().getSecondsToStabilize());
	}
	
	public void onDisable() {
		ServerAPI.sendLogs();
		
		if (getManager() != null) {
			getManager().disable();
		}
		
		Core.getMySQL().closeConnection();
		Core.getRedis().closeConnection();
		
		if (getServerType() != ServerType.HG && getServerType() != ServerType.EVENTO) {
			File playerFilesDir = new File("world/playerdata");
			if (playerFilesDir.isDirectory()) {
				String[] playerDats = playerFilesDir.list();
				for (int i = 0; i < playerDats.length; i++) {
		             File datFile = new File(playerFilesDir, playerDats[i]);
		             datFile.delete();
				}
			}
		
			File statsFileDir = new File("world/stats");
			if (statsFileDir.isDirectory()) {
				String[] statsDats = statsFileDir.list();
		        for (int i = 0; i < statsDats.length; i++) {
		             File datFile = new File(statsFileDir, statsDats[i]);
		             datFile.delete();
		        }
			}
		}
		
		HandlerList.unregisterAll();
		Bukkit.getScheduler().cancelAllTasks();
		
		for (Player on : Bukkit.getOnlinePlayers()) {
			 on.kickPlayer(PluginMessages.SERVER_RESTARTING);
		}
	}
	
	public void injectCustomKnockback() {
		new CustomKnockbackInjector(getInstance());
	}
	
	public static void desligar() {
		Bukkit.shutdown();
	}
	
	public static void console(String msg) {
		Bukkit.getConsoleSender().sendMessage("[Commons] " + msg);
	}
	
	public static void runAsync(Runnable runnable) {
		Bukkit.getScheduler().runTaskAsynchronously(getInstance(), runnable);	
	}
	
	public static void runLater(Runnable runnable) {
		runLater(runnable, 5);	
	}
	
	public static void runLater(Runnable runnable, long ticks) {
		Bukkit.getScheduler().runTaskLater(getInstance(), runnable, ticks);	
	}
}