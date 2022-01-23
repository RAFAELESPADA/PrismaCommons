package com.br.gabrielsilva.prismamc.commons.bungee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandFramework;
import com.br.gabrielsilva.prismamc.commons.bungee.listeners.MotdListener;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.Manager;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.SkinApplier;
import com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter.LogFilterBungee;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.PluginInstance;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.CommandLoader;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.FileLoader;
import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.listeners.BungeeListeners;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.Jedis;

@Getter @Setter
public class BungeeMain extends Plugin {

	@Getter @Setter
	private static BungeeMain instance;
	
	@Getter @Setter
	private static Manager manager;
	
	@Getter @Setter
	private String licenca;
	
	@Getter @Setter
	private static boolean globalWhitelist;
	
	@Getter @Setter
	private static List<String> whitelistPlayers;
	
	@Getter @Setter
	private static boolean loaded = false;
	
	public void onLoad() {
		setInstance(this);
		
		LogFilterBungee.load(getInstance());
		
		new Core(PluginInstance.BUNGEECORD);
		setManager(new Manager());

		try {
			getManager().getConfigManager().setup();
			getManager().getConfigManager().setupPermissions();
		} catch (IOException e) {
			console("Ocorreu um algum erro ao tentar carregar as configs -> " + e.getLocalizedMessage());
			desligar();
			return;
		}
		Core.getMySQL().openConnection();
		Core.getRedis().openConnection3();
	}
	
	public void onEnable() {
		if (!Core.correctlyStarted()) {
			desligar();
			return;
		}

		{
			setGlobalWhitelist(false);
			setWhitelistPlayers(new ArrayList<>());

			BungeeClient.initChannel(getInstance());

			getProxy().registerChannel("WDL|INIT");
			getProxy().registerChannel("PERMISSIONREPL");

			Core.getServersHandler().init();
			SkinApplier.init();

			new CommandLoader(new BungeeCommandFramework(getInstance())).loadCommandsFromPackage("com.br.gabrielsilva.prismamc.commons.bungee.commands.register");

			getManager().getMessages().add("§6§lKOMBOPVP &fEncontrou algum §4§lCHEATER? §fUtilize /report <nick> <motivo>");
			getManager().getMessages().add("§6§lKOMBO§f§lPVP §fEntre no nosso §9§lDISCORD: §bhttps://discord.gg/DZsabJSKdB");
			getManager().getMessages().add("§6§lKOMBO§f§lPVP §fAplique-se para §d§lTRIAL §fEntre no discord: §bhttps://discord.gg/yArGeWpFdE");
			getManager().getMessages().add("§6§lKOMBO§f§lPVP §fCompre §aVip §fno nosso discord: §bhttps://discord.gg/yArGeWpFdE");
			MotdListener.linha1 = "§6§lKOMBO§f§lPVP §bKITPVP, HG, GLADIATOR";

			MotdListener.linha2.add("§bVenha conhecer nosso KITPVP!");
			MotdListener.linha2.add("§aVenha jogar HG!");
			MotdListener.linha2.add("§eNovas atualizações!");
			MotdListener.linha2.add("§fDiversão garantida!");
			MotdListener.linha2.add("§fTreine seu PvP com Sopa!");
			MotdListener.manutenção = "§cServidor em manutenção";

			BungeeListeners.loadListeners(getInstance(), "com.br.gabrielsilva.prismamc.commons.bungee.listeners");
			getManager().init();

	}}
	
	public void onDisable() {
		if (getManager() != null) {
			getManager().sendLogs();
		}
		
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			 jedis.set("globalPlayers", "0");
		}
		getProxy().unregisterChannel("WDL|INIT");
		getProxy().unregisterChannel("PERMISSIONREPL");
		
		Core.getRedis().closeConnection();
		Core.getMySQL().closeConnection();

		LogFilterBungee.unload();
	}
	
	public static void desligar() {
		ProxyServer.getInstance().stop();
	}
	
	public static void console(String msg) {
		ProxyServer.getInstance().getConsole().sendMessage("[BungeeCord] " + msg);
	}
	
	public static void runAsync(Runnable runnable) {
		ProxyServer.getInstance().getScheduler().runAsync(getInstance(), runnable);
	}
	
	public static void runLater(Runnable runnable) {
		ProxyServer.getInstance().getScheduler().schedule(getInstance(), runnable, 500, TimeUnit.MILLISECONDS);
	}
	
	public static void runLater(Runnable runnable, long ms) {
		ProxyServer.getInstance().getScheduler().schedule(getInstance(), runnable, ms, TimeUnit.MILLISECONDS);
	}
	
	public static void runLater(Runnable runnable, int tempo, TimeUnit timeUnit) {
		ProxyServer.getInstance().getScheduler().schedule(getInstance(), runnable, tempo, timeUnit);
	}
	
	public static boolean isValid(ProxiedPlayer proxiedPlayer) {
		if (proxiedPlayer == null) {
			return false;
		}
		if (proxiedPlayer.getServer() == null) {
			return false;
		}
		if (proxiedPlayer.getServer().getInfo().getName().equalsIgnoreCase("Login")) {
			return false;
		}
		if (!getManager().getSessionManager().hasSession(proxiedPlayer)) {
			return false;
		}
		return true;
	}
}
