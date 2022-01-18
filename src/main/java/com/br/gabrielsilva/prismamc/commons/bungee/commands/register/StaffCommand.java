package com.br.gabrielsilva.prismamc.commons.bungee.commands.register;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandFramework;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandSender;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.permissions.PermissionsManager;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Completer;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.connections.redis.RedisAPI;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.server.types.HungerGamesEventServer;
import com.br.gabrielsilva.prismamc.commons.core.server.types.HungerGamesServer;
import com.br.gabrielsilva.prismamc.commons.core.server.types.NetworkServer;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class StaffCommand implements CommandClass {

	public static boolean hasEvento = false;
	
	@Command(name = "globalwhitelist", aliases = { "gwhitelist", "gw"}, groupsToUse= {Groups.DONO}, runAsync = true)
	public void globalWhitelist(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUtilize: /" + label + " <On/Off>");
			commandSender.sendMessage("§cUtilize: /" + label + " <Lista>");
			commandSender.sendMessage("§cUtilize: /" + label + " <add/remove> <Nick>");
			commandSender.sendMessage("");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				changeGlobalWhite(commandSender, true);
			} else if (args[0].equalsIgnoreCase("off")) {
				changeGlobalWhite(commandSender, false);
			} else if (args[0].equalsIgnoreCase("lista")) {
				commandSender.sendMessage("");
				String formated = StringUtils.formatArrayToString(BungeeMain.getWhitelistPlayers());
				commandSender.sendMessage("§aJogadores na WhiteList: " + formated);
			} else {
				commandSender.sendMessage("");
				commandSender.sendMessage("§cUtilize: /" + label + " <Lista>");
				commandSender.sendMessage("§cUtilize: /" + label + " <add/remove> <Nick>");
				commandSender.sendMessage("");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("add")) {
				String nick = args[1];
				if (BungeeMain.getWhitelistPlayers().contains(nick.toLowerCase())) {
					commandSender.sendMessage("§cEste jcogador ja está na WhiteList!");
					return;
				}
				BungeeMain.getWhitelistPlayers().add(nick.toLowerCase());
				commandSender.sendMessage("§7" + nick + " §afoi adicionado na WhiteList!");
				
				try (Jedis jedis = Core.getRedis().getPool().getResource()) {
					if (jedis.exists("globalWhitelistPlayers")) {
						Map<String, String> hash = jedis.hgetAll("globalWhitelistPlayers");		
						hash.put(nick.toLowerCase(), ".");	
						jedis.hmset("globalWhitelistPlayers", hash);
						
						hash.clear();
						hash = null;
					}
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				String nick = args[1];
				
				if (nick.equalsIgnoreCase("biielbr")) {
					commandSender.sendMessage("§cEste jogador não pode ser removido.");
					return;
				}
				
				if (!BungeeMain.getWhitelistPlayers().contains(nick.toLowerCase())) {
					commandSender.sendMessage("§cEste jogador não está na WhiteList!");
					return;
				}
				BungeeMain.getWhitelistPlayers().remove(nick.toLowerCase());
				
				commandSender.sendMessage("§7" + nick + " §afoi removido da WhiteList!");
				
				try (Jedis jedis = Core.getRedis().getPool().getResource()) {
					if (jedis.exists("globalWhitelistPlayers")) {
						Map<String, String> hash = jedis.hgetAll("globalWhitelistPlayers");		
						hash.remove(nick.toLowerCase());	
						jedis.hmset("globalWhitelistPlayers", hash);
						
						hash.clear();
						hash = null;
					}
				}
			} else {
				commandSender.sendMessage("");
				commandSender.sendMessage("§cUtilize: /" + label + " <On/Off>");
				commandSender.sendMessage("§cUtilize: /" + label + " <Lista>");
				commandSender.sendMessage("§cUtilize: /" + label + " <add/remove> <Nick>");
				commandSender.sendMessage("");
			}
		}
	}
	
	private void changeGlobalWhite(BungeeCommandSender commandSender, boolean ativar) {
		boolean ativada = BungeeMain.isGlobalWhitelist();
		
		if (ativada) {
			if (ativar) {
				commandSender.sendMessage("§cA WhiteList global ja está ativada!");
				return;
			}
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				jedis.set("globalWhitelist", "false");
			}
			commandSender.sendMessage("§aWhiteList global desativada!");
			BungeeMain.setGlobalWhitelist(false);
			BungeeMain.getManager().warnStaff("§aA WhiteList global foi desativada!", Groups.GERENTE);
		} else {
			if (!ativar) {
				commandSender.sendMessage("§cA WhiteList global ja está desativada!");
				return;
			}
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				jedis.set("globalWhitelist", "true");
			}
			BungeeMain.setGlobalWhitelist(true);
			commandSender.sendMessage("§aWhiteList global ativada!");
			BungeeMain.getManager().warnStaff("§aA WhiteList global foi ativada!", Groups.GERENTE);
		}
	}
	
	@Command(name="sskick", runAsync=true)
	public void sskick(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (args.length != 1) {
			proxiedPlayer.sendMessage("§cUtilize: /" + label + " <Nick>");
			return;
		}
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
		if ((target == null) || (target.getServer() == null)) {
			proxiedPlayer.sendMessage("§cO jogador não está em nenhum servidor.");
			return;
		}
		
		BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer);
		if (!bungeePlayer.inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê não está em uma sessão de compartilhamento de tela.");
			return;
		}
		
		if (bungeePlayer.getScreenShareWith() != target.getUniqueId()) {
			proxiedPlayer.sendMessage("§cVocê não está em uma sessão com este jogador.");
			return;
		}
		
		bungeePlayer.setScreenShareWith(null);
		bungeePlayer.setPuxou(false);
		
		BungeePlayer bungeePlayerTarget = BungeeMain.getManager().getSessionManager().getSession(target);
		bungeePlayerTarget.setScreenShareWith(null);
		bungeePlayerTarget.setPuxou(false);
		
		target.sendMessage("§cSessão encerrada.");
		proxiedPlayer.sendMessage("§cSessão encerrada.");
		
		BungeeMain.runLater(() -> {
			proxiedPlayer.sendMessage("§aConectando...");
			target.sendMessage("§aConectando...");
			
			proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo("Lobby"));
		    target.connect(ProxyServer.getInstance().getServerInfo("Lobby"));
		}, 1, TimeUnit.SECONDS);
	}
	
	@Command(name="ss", aliases= {"screenshare", "puxar", "telar"}, runAsync=true)
	public void ss(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (args.length != 1) {
			proxiedPlayer.sendMessage("§cUtilize: /" + label + " <Nick>");
			return;
		}
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
		if (!BungeeMain.isValid(target)) {
			proxiedPlayer.sendMessage("§cO jogador precisa ter uma sessão válida.");
			return;
		}
		if (target.hasPermission("screenshare.bypass")) {
			proxiedPlayer.sendMessage("§cEste jogador não pode ser puxado para uma sessão de compartilhamento de tela.");
			return;
		}
		
		if (target == proxiedPlayer) {
			proxiedPlayer.sendMessage("§cVocê não pode se puxar para a ScreenShare.");
			return;
		}
		
		BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer);
		if (bungeePlayer.inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê ja está em uma sessão de ScreenShare.");
			return;
		}
		
		BungeePlayer bungeePlayerTarget = BungeeMain.getManager().getSessionManager().getSession(target);
		if (bungeePlayerTarget.inScreenShare()) {
			proxiedPlayer.sendMessage("§cEste jogador ja está em uma sessão de ScreenShare.");
			return;
		}
		
		bungeePlayerTarget.setScreenShareWith(proxiedPlayer.getUniqueId());
		bungeePlayerTarget.setPuxou(false);
		
		bungeePlayer.setScreenShareWith(target.getUniqueId());
		bungeePlayer.setPuxou(true);
		
		proxiedPlayer.sendMessage("");
		proxiedPlayer.sendMessage("§aVocê puxou o player §7" + target.getName() + " §apara screenshare.");
		proxiedPlayer.sendMessage("§aPara liberar o jogador, utilize: /sskick <Nick>");
		proxiedPlayer.sendMessage("");
		
		target.sendMessage("");
		target.sendMessage("§cVocê foi puxado para sessão de screenshare.");
		target.sendMessage("");
		
		BungeeMain.runLater(() -> {
			proxiedPlayer.sendMessage("§aConectando...");
			target.sendMessage("§aConectando...");
			
			proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo("ScreenShare"));
		    target.connect(ProxyServer.getInstance().getServerInfo("ScreenShare"));
		}, 1, TimeUnit.SECONDS);
	}
	
	@Command(name="serverInfo", aliases = {"svinfo", "si", "svsinfo"}, groupsToUse= {Groups.ADMIN})
	public void serverInfo(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUtilize: /serverinfo <Servidor>");
			commandSender.sendMessage("§cUtilize: /serverinfo list");
			commandSender.sendMessage("");
			return;
		}
		
		if (args[0].equalsIgnoreCase("list")) {
			if (!commandSender.isPlayer()) {
				return;
			}
			
			ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
			proxiedPlayer.sendMessage("");
			
			for (NetworkServer networkServer : Core.getServersHandler().getNetworkServers().values()) {
				 TextComponent component = new TextComponent("§e[" + networkServer.getServerName().toUpperCase() + "]");
				 
				 component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(
				 "Online: " + (networkServer.isOnline() ? "§aSim" : "§cNão") + "\n" +
				 "Onlines: §7" + networkServer.getOnlines() + "/" + networkServer.getMaxPlayers()
				 )).create()));
				 
				proxiedPlayer.sendMessage((BaseComponent)component);
			}
			
			for (HungerGamesServer hungerGamesServer : Core.getServersHandler().getHungerGamesServers().values()) {
				 TextComponent component = new TextComponent("§e[HG" + hungerGamesServer.getServerID() + "]");
				 
				 component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(
				 "Online: " + (hungerGamesServer.isOnline() ? "§aSim" : "§cNão") + "\n" +
				 "Onlines: §7" + hungerGamesServer.getVivos() + "/" + hungerGamesServer.getMaxPlayers() + "\n" +
				 "Tempo: §7" + DateUtils.formatarSegundos2(hungerGamesServer.getTempo()) + "\n" +
				 "Estágio: §7" + hungerGamesServer.getEstagio().getNome())).create()));
				 
				proxiedPlayer.sendMessage((BaseComponent)component);
			}
			
			for (HungerGamesEventServer hungerGamesServer : Core.getServersHandler().getHungerGamesEventServer().values()) {
				 TextComponent component = new TextComponent("§e[EVENTO" + hungerGamesServer.getServerID() + "]");
				 
				 component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(
				 "Online: " + (hungerGamesServer.isOnline() ? "§aSim" : "§cNão") + "\n" +
				 "Onlines: §7" + hungerGamesServer.getVivos() + "/" + hungerGamesServer.getMaxPlayers() + "\n" +
				 "Tempo: §7" + DateUtils.formatarSegundos2(hungerGamesServer.getTempo()) + "\n" +
				 "Estágio: §7" + hungerGamesServer.getEstagio().getNome())).create()));
				 
				proxiedPlayer.sendMessage((BaseComponent)component);
			}
			
			proxiedPlayer.sendMessage("");
			return;
		}
		
		String serverInstance = Core.getServersHandler().getServerInstance(args[0]);
		if (serverInstance.equalsIgnoreCase("Unknown")) {
			commandSender.sendMessage("§cTipo de servidor não encontrado.");
			return;
		}
		commandSender.sendMessage("");
		if (serverInstance.equalsIgnoreCase("NetworkServer")) {
			NetworkServer networkServer = Core.getServersHandler().getNetworkServer(args[0].toLowerCase());
			commandSender.sendMessage("");
			commandSender.sendMessage("§a§l" + networkServer.getServerName().toUpperCase());
			commandSender.sendMessage("");
			commandSender.sendMessage("Online: " + (networkServer.isOnline() ? "§aSim" : "§cNão"));
			commandSender.sendMessage("Onlines: §7" + networkServer.getOnlines() + "/" + networkServer.getMaxPlayers());
			
			networkServer = null;
		} else if (serverInstance.equalsIgnoreCase("HungerGamesServer")) {
			HungerGamesServer hungerGamesServer = Core.getServersHandler().getHungerGamesServer(args[0].toLowerCase());
			commandSender.sendMessage("");
			commandSender.sendMessage("§a§lHG - " + hungerGamesServer.getServerID());
			commandSender.sendMessage("");
			commandSender.sendMessage("Online: " + (hungerGamesServer.isOnline() ? "§aSim" : "§cNão"));
			commandSender.sendMessage("Vivos: §7" + hungerGamesServer.getVivos() + "/" + hungerGamesServer.getMaxPlayers());
			commandSender.sendMessage("Tempo: §7" + DateUtils.formatarSegundos2(hungerGamesServer.getTempo()));
			commandSender.sendMessage("Estágio: §7" + hungerGamesServer.getEstagio().getNome());
			commandSender.sendMessage("");
			
			hungerGamesServer = null;
		} else if (serverInstance.equalsIgnoreCase("HungerGamesEventServer")) {
			HungerGamesEventServer hungerGamesServer = Core.getServersHandler().getHungerGamesEventServer(args[0].toLowerCase());
			commandSender.sendMessage("");
			commandSender.sendMessage("§a§lEVENTO - " + hungerGamesServer.getServerID());
			commandSender.sendMessage("");
			commandSender.sendMessage("Online: " + (hungerGamesServer.isOnline() ? "§aSim" : "§cNão"));
			commandSender.sendMessage("Vivos: §7" + hungerGamesServer.getVivos() + "/" + hungerGamesServer.getMaxPlayers());
			commandSender.sendMessage("Tempo: §7" + DateUtils.formatarSegundos2(hungerGamesServer.getTempo()));
			commandSender.sendMessage("Estágio: §7" + hungerGamesServer.getEstagio().getNome());
			commandSender.sendMessage("");
			
			hungerGamesServer = null;
		}
		commandSender.sendMessage("");
	}
	
	@Command(name = "eventomanager")
	public void eventomanager(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage("§cUse: /eventomanager <On/Off>");
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.mod")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				if (hasEvento) {
					commandSender.sendMessage("§cJá existe um evento disponível.");
					return;
				}
				hasEvento = true;
				commandSender.sendMessage("§aVocê iniciou o evento.");
				
				TextComponent message = new TextComponent("§b§lEVENTO\n\n§fUm Evento acabou de ser liberado!\n§aClique para conectar-se.");
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/evento"));
				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] { new TextComponent("Clique para conectar")}));
				
				BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(""));
				BungeeCord.getInstance().broadcast(message);
				BungeeCord.getInstance().broadcast(TextComponent.fromLegacyText(""));
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!hasEvento) {
					commandSender.sendMessage("§cNenhum evento disponível.");
					return;
				}
				hasEvento = false;
				commandSender.sendMessage("§cVocê cancelou o evento.");
			} else {
				commandSender.sendMessage("§cUse: /evento <On/Off>");
			}
		}
	}
	
	@Command(name = "addcash", groupsToUse= {Groups.DONO}, runAsync = true)
	public void addcash(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 2) {
			commandSender.sendMessage(PluginMessages.ADDCASH);
			return;
		}
		if (!StringUtils.isInteger(args[1])) {
			commandSender.sendMessage(PluginMessages.ADDCASH);
			return;
		}
		int toAdd = Integer.valueOf(args[1]);
		String nick = args[0];
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (target != null) {
			if (!BungeeMain.isValid(target)) {
				commandSender.sendMessage(PluginMessages.NAO_POSSUI_SESSAO);
				return;
			}
			
			BungeeClient.sendPacketToServer(target, 
					new PacketUpdateField(target.getName(), "CustomPlayer", "Cash", "" + toAdd));
			
			target.sendMessage(PluginMessages.CASH_RECEBIDO.replace("%quantia%", args[1]));
			commandSender.sendMessage(PluginMessages.CASH_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", target.getName()));
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM accounts WHERE nick='"+nick+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
			    result.close();
			    preparedStatament.close();
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
			nick = result.getString("nick");
			int atual = result.getInt("cash");
			
		    result.close();
		    preparedStatament.close();
		    
		    MySQLManager.atualizarStatus("accounts", "cash", nick, String.valueOf(toAdd + atual));
		    try {
				RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.CASH, "" + toAdd + atual);
			} catch (Exception e) {}
		    
		    commandSender.sendMessage(PluginMessages.CASH_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", nick));
		} catch (SQLException ex) {
			BungeeMain.console(PluginMessages.OCORREU_UM_ERRO2.replace("{0}", "adicionar Cash para um jogador").replace("{1}", ex.getLocalizedMessage()));
			commandSender.sendMessage(PluginMessages.OCORREU_UM_ERRO);
		}
	}
	
	@Command(name = "addcoins", groupsToUse= {Groups.DONO}, runAsync = true)
	public void addcoins(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 2) {
			commandSender.sendMessage(PluginMessages.ADDCOINS);
			return;
		}
		if (!StringUtils.isInteger(args[1])) {
			commandSender.sendMessage(PluginMessages.ADDCOINS);
			return;
		}
		int toAdd = Integer.valueOf(args[1]);
		String nick = args[0];
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		
		if (target != null) {
			if (!BungeeMain.isValid(target)) {
				commandSender.sendMessage(PluginMessages.NAO_POSSUI_SESSAO);
				return;
			}
			
			BungeeClient.sendPacketToServer(target, 
					new PacketUpdateField(target.getName(), "CustomPlayer", "Coins", "" + toAdd));
			
			target.sendMessage(PluginMessages.COINS_RECEBIDOS.replace("%quantia%", args[1]));
			commandSender.sendMessage(PluginMessages.COINS_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", target.getName()));
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM accounts WHERE nick='"+nick+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
			    result.close();
			    preparedStatament.close();
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
			nick = result.getString("nick");
			int atual = result.getInt("coins");
			
		    result.close();
		    preparedStatament.close();
		    
		    MySQLManager.atualizarStatus("accounts", "coins", nick, String.valueOf(toAdd + atual));
		    try {
				RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.COINS, "" + toAdd + atual);
			} catch (Exception e) {}
		    
		    commandSender.sendMessage(PluginMessages.COINS_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", nick));
		} catch (SQLException ex) {
			BungeeMain.console(PluginMessages.OCORREU_UM_ERRO2.replace("{0}", "adicionar Coins para um jogador").replace("{1}", ex.getLocalizedMessage()));
			commandSender.sendMessage(PluginMessages.OCORREU_UM_ERRO);
		}
	}
	
	@Command(name = "addperm", groupsToUse= {Groups.DONO}, runAsync = true)
	public void addperm(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 2) {
			commandSender.sendMessage("§cUse: /addperm <Nick> <Permissao>");
			return;
		}
		String nick = args[0];
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM accounts WHERE nick='"+nick+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
			    result.close();
			    preparedStatament.close();
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
			result.close();
			preparedStatament.close();
			
			handleAddPerm(nick, args[1]);
			commandSender.sendMessage("§fPermissão '§a" + args[1] + "§f' foi adicionada para o §a" + nick);
		} catch (SQLException ex) {
			BungeeMain.console(PluginMessages.OCORREU_UM_ERRO2.replace("{0}", "adicionar uma permissao para um jogador").replace("{1}", ex.getLocalizedMessage()));
			commandSender.sendMessage(PluginMessages.OCORREU_UM_ERRO);
		} 
	}
	
	@Command(name = "addxp", groupsToUse= {Groups.DONO}, runAsync = true)
	public void addxp(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 2) {
			commandSender.sendMessage(PluginMessages.ADDXP);
			return;
		}
		if (!StringUtils.isInteger(args[1])) {
			commandSender.sendMessage(PluginMessages.ADDXP);
			return;
		}
		int toAdd = Integer.valueOf(args[1]);
		String nick = args[0];
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		
		if (target != null) {
			if (!BungeeMain.isValid(target)) {
				commandSender.sendMessage(PluginMessages.NAO_POSSUI_SESSAO);
				return;
			}
			
    		BungeeClient.sendPacketToServer(target, 
					new PacketUpdateField(target.getName(), "CustomPlayer", "XP", "" + toAdd));
			
			target.sendMessage(PluginMessages.XP_RECEBIDOS.replace("%quantia%", args[1]));
			commandSender.sendMessage(PluginMessages.XP_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", target.getName()));
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM accounts WHERE nick='"+nick+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
			    result.close();
			    preparedStatament.close();
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
			nick = result.getString("nick");
			int atual = result.getInt("xp");
			
		    result.close();
		    preparedStatament.close();
		    
		    MySQLManager.atualizarStatus("accounts", "xp", nick, String.valueOf(toAdd + atual));
		    try {
				RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.XP, "" + toAdd + atual);
			} catch (Exception e) {}
		    
		    commandSender.sendMessage(PluginMessages.XP_SUCESSO.replace("%quantia%", args[1]).replace("%nick%", nick));
		} catch (SQLException ex) {
			BungeeMain.console(PluginMessages.OCORREU_UM_ERRO2.replace("{0}", "adicionar XP para um jogador").replace("{1}", ex.getLocalizedMessage()));
			commandSender.sendMessage(PluginMessages.OCORREU_UM_ERRO);
		}
	}
	
	@Command(name = "alert", aliases = { "aviso" }, runAsync = true)
	public void alert(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
		    commandSender.sendMessage(PluginMessages.ALERT);
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.admin")) {
			p.sendMessage("Sem permissao");
			return;
		}
		StringBuilder builder = new StringBuilder();
		if (args[0].startsWith("&h")) {
			args[0] = args[0].substring(2, args[0].length());
		} else {
			builder.append(PluginMessages.ALERT_PREFIX);
		}
		for (String s : args) {
			 builder.append(ChatColor.translateAlternateColorCodes('&', s));
			 builder.append(" ");
		}
		String message = builder.substring(0, builder.length() - 1);
		ProxyServer.getInstance().broadcast(message);
	}
	
	@Command(name = "bungeeinfo", runAsync = true)
	public void bungeeinfo(BungeeCommandSender commandSender, String label, String[] args) {
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.dono")) {
			p.sendMessage("Sem permissao");
			return;
		}
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		double usedPercentage = (used * 100) / total;
		
		commandSender.sendMessage("");
		commandSender.sendMessage("§e§lBUNGEECORD CENTRAL - INFOS");
		commandSender.sendMessage("");
		commandSender.sendMessage("Sessões no Cache -> " + BungeeMain.getManager().getSessionManager().getSessions().size());
		commandSender.sendMessage("Clans no Cache -> " + Core.getClanManager().getClans().size());
		commandSender.sendMessage("Total de comandos executados: §c" + BungeeCommandFramework.COMMANDS_EXECUTEDS);
		commandSender.sendMessage("");
		commandSender.sendMessage("§e§lRAM");
		commandSender.sendMessage(" Memória total: §e" + total / 1048576L + " MB");
		commandSender.sendMessage(" Memória usada: §c" + (used / 1048576L) + " MB");
		commandSender.sendMessage(" Memória livre: §a" + (free / 1048576L) + " MB");
		commandSender.sendMessage(" Memória em uso: " + ramQuality(usedPercentage) + "%");
		commandSender.sendMessage("");
	}
	
	@Command(name = "globallist", aliases = {"glist", "gl"}, groupsToUse= {Groups.GERENTE}, runAsync = true)
	public void glist(BungeeCommandSender commandSender, String label, String[] args) {
		
		int crackeds = 0,
				premiums = 0;
		
		commandSender.sendMessage("");
		
		for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
	         List<String> players = new ArrayList<>();
	         
	         for (ProxiedPlayer player : server.getPlayers()) {
	        	  boolean premium = BungeeMain.getManager().getPremiumMapManager().getPremiumMap(player.getName()).isPremium();
	        	  
	        	  if (premium) {
	        		  premiums++;
	        	  } else {
	        		  crackeds++;
	        	  }
	        	  
	        	  if (BungeeMain.getManager().getSessionManager().hasSession(player)) {
	        		  final BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(player);
	        		  if (premium) {
		        		  players.add(proxyPlayer.getGrupo().getCor() + player.getDisplayName());
	        		  } else {
		        		  players.add(proxyPlayer.getGrupo().getCor() + player.getDisplayName() + "§c(P)");
	        		  }
	        	  } else {
	        		  players.add("§7" + player.getDisplayName());
	        	  }
	         }
	         
	         Collections.sort(players, String.CASE_INSENSITIVE_ORDER);
	         
	         commandSender.sendMessage("§e" + server.getName() + " [" + server.getPlayers().size() + "] " + 
	         Util.format(players, ChatColor.RESET + ", "));
		}
		
		commandSender.sendMessage("");
		commandSender.sendMessage("§fNo momento há §a" + ProxyServer.getInstance().getPlayers().size() + " §fjogadores em toda rede!");
		commandSender.sendMessage("");
		commandSender.sendMessage("§fNo momento há §6" + premiums + " §fjogadores §6§lORIGINAIS §fonline.");
		commandSender.sendMessage("§fNo momento há §c" + crackeds + " §fjogadores §c§lPIRATAS §fonline.");
		commandSender.sendMessage("");
	}
	
	@Command(name = "find", aliases= {"procurar"},groupsToUse= {Groups.YOUTUBER_PLUS}, runAsync = true)
	public void find(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage("§cUse: /find <Nick>");
			return;
		}
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
		if ((player == null) || (player.getServer() == null)) {
			commandSender.sendMessage("§cO jogador não está em nenhum servidor.");
		} else {
			commandSender.sendMessage("§aJogador encontrado.\n§aOnline no servidor: §f" + player.getServer().getInfo().getName());
		}
	}
	
	@Command(name = "kickall", aliases = { "kall" }, groupsToUse= {Groups.DONO}, runAsync = true)
	public void kickAll(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage("§cUtilize: /kickall <Motivo>");
			return;
		}
		final String motivo = StringUtils.createArgs(0, args);
		
		commandSender.sendMessage("§aExpulsando todos os jogadores...");
		
		int delay = 333;
		
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 BungeeMain.runLater(() -> {
				 if (BungeeMain.isValid(proxiedPlayer)) {
					 if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).getGrupo().getNivel() >= 
							 Groups.ADMIN.getNivel()) {
						 return;
					 }
				 }
				 proxiedPlayer.disconnect(PluginMessages.VOCÊ_FOI_EXPULSO.replace("%expulsou%", "CONSOLE").replace("%motivo%", motivo));
			 }, delay, TimeUnit.MILLISECONDS);
			 delay+=333;
		}
	}
	
	@Command(name = "kick", aliases = { "expulsar" })
	public void kick(BungeeCommandSender commandSender, String label, String[] args) {
		String expulsou = commandSender.getNick();
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length == 0) {
			commandSender.sendMessage(PluginMessages.KICK);
			return;
		}
		String nick = args[0];
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (target == null) {
			commandSender.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		
		boolean kick = true;
		if (!expulsou.equalsIgnoreCase("CONSOLE")) {
			if (BungeeMain.isValid(target)) {
				BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(target),
						proxyExpulsou = BungeeMain.getManager().getSessionManager().getSession(commandSender.getPlayer());
				
				if (proxyPlayer.getGrupo().getNivel() > proxyExpulsou.getGrupo().getNivel()) {
					commandSender.sendMessage("§cVocê não pode expulsar alguém com o grupo superior ao seu.");
					kick = false;
				}
				
			}
		}
		
		if (!kick) {
			return;
		}
		String motivo = "Não especificado";
		if (args.length >= 2) {
			motivo = StringUtils.createArgs(1, args);
		}
		
		commandSender.sendMessage("§aJogador expulso.");
		
		final String m = motivo;
		BungeeMain.runLater(() -> {
			target.disconnect(PluginMessages.VOCÊ_FOI_EXPULSO.replace("%expulsou%", expulsou).replace("%motivo%", m));
		}, 100);
	}
	
	@Command(name = "premium", aliases = { "checkpremium" }, groupsToUse= {Groups.TRIAL}, runAsync = true)
	public void premium(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			commandSender.sendMessage("");
			commandSender.sendMessage("Total de PremiumMap: §a" + BungeeMain.getManager().getPremiumMapManager().getPremiumMaps().size());
			commandSender.sendMessage("Jogadores Piratas: §c" + BungeeMain.getManager().getPremiumMapManager().getCrackedAmount());
			commandSender.sendMessage("Jogadores Originais: §a" + BungeeMain.getManager().getPremiumMapManager().getPremiumAmount());
			commandSender.sendMessage("");
			return;
		}
		if (args.length == 1) {
			String nick = args[0];
			
			if (!BungeeMain.getManager().getPremiumMapManager().containsMap(nick)) {
				BungeeMain.getManager().getPremiumMapManager().load(nick);
			}
			
			if (!BungeeMain.getManager().getPremiumMapManager().containsMap(nick)) {
				commandSender.sendMessage("§cConta ainda não carregada...");
				return;
			}
			commandSender.sendMessage("Jogador: " + 
			     (BungeeMain.getManager().getPremiumMapManager().getPremiumMap(nick).isPremium() ? "§aOriginal" : "§cPirata"));
		}
	}
	
	@Command(name = "removesession", groupsToUse= {Groups.DONO}, runAsync = true)
	public void removeSession(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage("§cUse: /removesession <Nick>");
			return;
		}
		String nick = args[0];
		if (!BungeeMain.getManager().getSessionManager().hasSession(nick)) {
			commandSender.sendMessage("§cEsta sessão não existe.");
			return;
		}
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (target != null) {
			target.disconnect("§cSua sessão foi removida.");
		}
		
		BungeeMain.runLater(() -> {
			BungeeMain.getManager().getSessionManager().removeSession(nick);
		}, 1, TimeUnit.SECONDS);
	}
	
	@Command(name = "proxyplayer", aliases = {"pp", "bungeeplayer"}, runAsync = true)
	public void proxyplayer(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage("§cUse: /proxyplayer <Nick>");
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.dono")) {
			p.sendMessage("Sem permissao");
			return;
		}
		String nick = args[0];
		if (!BungeeMain.getManager().getSessionManager().hasSession(nick)) {
			commandSender.sendMessage("§cEste nick não possuí uma sessão.");
			return;
		}
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(nick);
		commandSender.sendMessage("");
		commandSender.sendMessage("§a§lBUNGEE PLAYER INFO:");
		commandSender.sendMessage("");
		commandSender.sendMessage("Nick: §7" + proxyPlayer.getNick());
		commandSender.sendMessage("IP: §7" + proxyPlayer.getAddress());
		commandSender.sendMessage("Grupo: §7" + proxyPlayer.getGrupo().getCor() + proxyPlayer.getGrupo().getNome());
		commandSender.sendMessage("");
		commandSender.sendMessage("");
	}
	
	@Command(name = "staff", aliases = { "stafflist", "sl", "slist"}, runAsync = true)
	public void stafflist(BungeeCommandSender commandSender, String label, String[] args) {
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		commandSender.sendMessage("");
		commandSender.sendMessage("§aStaffers online:");
		commandSender.sendMessage("");
		
		int id = 1;
		
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 if (!BungeeMain.isValid(proxiedPlayer)) {
				 continue;
			 }
			 if (proxiedPlayer.hasPermission("prismamc.staff")) {
				 final BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer);
				 final Tag tag = bungeePlayer.getGrupo().getTag();
				 
				 String prefix = "§a" + id + "º - " + tag.getColor() + "§l" + tag.getTag() +
						 " " + tag.getColor() + proxiedPlayer.getName() + " §f(§a" + proxiedPlayer.getServer().getInfo().getName() + "§f)";
				 
				 TextComponent message = new TextComponent(prefix);
				 message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/connect " + proxiedPlayer.getServer().getInfo().getName().toLowerCase()));
				 message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] { new TextComponent("§aClique para conectar")}));
				 
				 commandSender.sendMessage(message);
				 id++;
			 }
		}
		
		commandSender.sendMessage("");
		commandSender.sendMessage("§aStaffers online: §7" + (id - 1));
		commandSender.sendMessage("");
	}
	
	@Command(name = "setgroup", aliases = { "group" }, groupsToUse = Groups.GERENTE, runAsync = true)
	public void setgroup(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length >= 2) {
			String grupo = args[1];
			if (!Groups.existGrupo(grupo)) {
				commandSender.sendMessage(PluginMessages.GRUPO_INEXISTENTE);
				return;
			}
			
			Groups tag = Groups.getFromString(grupo);
			
			if (!commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				if (!BungeeMain.isValid(commandSender.getPlayer())) {
					return;
				}
				Groups tagPlayer = 
						BungeeMain.getManager().getSessionManager().getSession((ProxiedPlayer) commandSender.getPlayer()).getGrupo();
				
				if (tag.getNivel() > tagPlayer.getNivel()) {
					commandSender.sendMessage(PluginMessages.SETGROUP_ACIMA);
				   	return;
				}
			}
			
			String nick = MySQLManager.getString("accounts", "nick", args[0], "nick");
			if (nick.equalsIgnoreCase("N/A")) {
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				return;
			}
			if (tag == Groups.INVESTIDOR || tag == Groups.INVESTIDOR_PLUS  || tag == Groups.YOUTUBER_PLUS || tag == Groups.TRIAL || tag == Groups.MOD || tag == Groups.MOD_GC || tag == Groups.MOD_PLUS || tag == Groups.GERENTE || tag == Groups.ADMIN || tag == Groups.DIRETOR || tag == Groups.DONO) {
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set prismamc.staff");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set prismamc.receivewarn");
			}
			if (tag == Groups.ADMIN || tag == Groups.GERENTE || tag == Groups.DIRETOR || tag == Groups.DONO) {
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set prismamc.admin");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set prismamc.mod");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set prismamc.receivewarn");
			}
			if (tag == Groups.DONO) {
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission set *");
			}
			if (tag == Groups.MEMBRO || tag == Groups.SAPPHIRE || tag == Groups.LEGEND || tag == Groups.RUBY || tag == Groups.BETA || tag == Groups.YOUTUBER || tag == Groups.STREAMER || tag == Groups.DESIGNER) {
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission unset prismamc.staff");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission unset prismamc.admin");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission unset prismamc.mod");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission unset *");
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), "lpb user " + nick + " permission unset prismamc.receivewarn");
			}
			if (tag == Groups.DEV) {
				if (!nick.equalsIgnoreCase("BiielBr")) {
					commandSender.sendMessage("§cApenas o BiielBr tem direito a esta TAG.");
					return;
				}
			}
			
			commandSender.sendMessage(PluginMessages.SETGROUP_SUCESSO.replace("%nick%", nick).
			replace("%grupo%", tag.getCor() + "§l" + tag.getNome()));
			
			long tempo = 0;
			if (args.length == 3) {
				try {
					tempo = DateUtils.parseDateDiff(args[2], true);
				} catch (Exception ex) {
					commandSender.sendMessage(PluginMessages.TEMPO_INVALIDO);
					return;
				}
			}
			
			handleSetGroup(nick, commandSender.getNick(), grupo, tempo);
		} else {
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUse: /setgroup <Nick> <Grupo>");
			commandSender.sendMessage("§cUse: /setgroup <Nick> <Grupo> <Tempo>");
			commandSender.sendMessage("");
		}
	}
	
	@Completer(name = "setgroup", aliases = {"group"})
    public List<String> groupCompleter(ProxiedPlayer player, String label, String[] args) {
		if (args.length == 1) {
            ArrayList<String> players = new ArrayList<>();
            
            for (ProxiedPlayer p : BungeeCord.getInstance().getPlayers()) {
            	 if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                     players.add(p.getName());
                  }
             }
            
            return players;
		} else if (args.length == 2) {
            ArrayList<String> grupos = new ArrayList<>();
            for (Groups group : Groups.values()) {
                 if (group.getNome().toLowerCase().startsWith(args[1].toLowerCase())) {
                     grupos.add(group.toString());
                 }
             }
             return grupos;
		}
        return new ArrayList<>();
    }
	
	public static void handleSetGroup(String nick, String setou, String grupo, long tempo) {
		final Groups tag = Groups.getFromString(grupo);
		
		BungeeMain.console(nick + " foi setado no Grupo: " + tag.getNome() + " pelo " + setou);
		BungeeMain.getManager().warnStaff(PluginMessages.JOGADOR_FOI_SETADO_NO_GRUPO.replace("%nick%", nick).replace("%setou%", setou).
				replace("%grupo%", tag.getCor() + "§l" + tag.getNome()).replace("%duração%", (tempo == 0L ? "§6§lETERNA" : "de " + DateUtils.formatDifference(tempo + 100))));
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		
		if (!BungeeMain.isValid(target)) {
			MySQLManager.atualizarStatus("accounts", "grupo", nick, tag.getNome());
			MySQLManager.atualizarStatus("accounts", "grupo_time", nick, String.valueOf(tempo));
			
		    try {
				RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.GRUPO, tag.getNome());
				RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.GRUPO_TIME, String.valueOf(tempo));
			} catch (Exception e) {}
		} else {
			BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(target);
			bungeePlayer.setGrupo(Groups.getFromString(grupo));
			bungeePlayer.setInStaffChat(false);
			
    		BungeeClient.sendPacketToServer(target, 
					new PacketUpdateField(target.getName(), "CustomPlayer", "Grupo", grupo, String.valueOf(tempo)));
    		
    		PermissionsManager.clearPermissions(target);
    		PermissionsManager.injectPermissions(target, grupo);
		}
	}
	
	@Command(name = "send", aliases = {"enviar"}, runAsync = true)
	public void send(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 2) {
			if (!commandSender.hasPermission("prismamc.admin")) {
				commandSender.sendMessage("Sem permissao");
				return;
			}
			commandSender.sendMessage("");
			commandSender.sendMessage("§cUse: /send <Nick> <Servidor>");
			commandSender.sendMessage("§cUse: /send todos <Servidor>");
			commandSender.sendMessage("§cUse: /send local <Servidor>");
			commandSender.sendMessage("§cUse: /send <Servidor> <Servidor>");
			commandSender.sendMessage("");
			return;
		}
		ServerInfo target = ProxyServer.getInstance().getServerInfo(args[1]);
		if (target == null) {
			commandSender.sendMessage("§cEste servidor não existe.");
			return;
		}
		if ((target.getName().equalsIgnoreCase("Login")) || (target.getName().equalsIgnoreCase("ScreenShare"))) {
			commandSender.sendMessage("§cVocê não pode enviar alguém para este servidor.");
			return;
		}
		if (ProxyServer.getInstance().getServerInfo(args[0]) != null) {
			if ((args[0].equalsIgnoreCase("Login")) || (args[0].equalsIgnoreCase("ScreenShare"))) {
				commandSender.sendMessage("§cVocê não pode puxar alguém deste servidor.");
				return;
			}
			ServerInfo from = ProxyServer.getInstance().getServerInfo(args[0]);
			commandSender.sendMessage("§aEnviando todos os jogadores de " + from.getName() + " para: §7" + target.getName());
			
			int delay = 333;
			
			for (ProxiedPlayer inServer : from.getPlayers()) {
				 BungeeMain.runLater(() -> {
					 enviar(inServer, target);
				 }, delay, TimeUnit.MILLISECONDS);
				 delay+=333;
			}
			return;
		}
		if (args[0].equalsIgnoreCase("local")) {
			if (commandSender.getNick().equalsIgnoreCase("CONSOLE")) {
				commandSender.sendMessage("§cVocê não está em um servidor.");
				return;
			}
			ProxiedPlayer p = (ProxiedPlayer) commandSender;
			commandSender.sendMessage("§aEnviando todos os jogadores locais para: §7" + target.getName());
			
			int delay = 333;
			
			for (ProxiedPlayer inLocal : p.getServer().getInfo().getPlayers()) {
				 BungeeMain.runLater(() -> {
					 enviar(inLocal, target);
				 }, delay, TimeUnit.MILLISECONDS);
				 delay+=333;
			}
		} else if (args[0].equalsIgnoreCase("todos") || (args[0].equalsIgnoreCase("*"))) {
			commandSender.sendMessage("§aEnviando todos os jogadores para: §7" + target.getName());
			
			int delay = 333;
			
			for (ProxiedPlayer p1 : ProxyServer.getInstance().getPlayers()) {
				 if (p1.getServer().getInfo().getName().equals("Login")) {
					 continue;
				 }
				 BungeeMain.runLater(() -> {
					 enviar(p1, target);
				 }, delay, TimeUnit.MILLISECONDS);
				delay+=333;
			}
		} else {
			ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
			if (player == null) {
				commandSender.sendMessage("§cJogador offline");
				return;
			}
			commandSender.sendMessage("§aTentando enviar §7" + player.getName() + " §apara: §7" + target.getName());
			enviar(player, target);
		}
	}
	
	@Completer(name = "ban", aliases = {"mute", "find", "kick", "premium", "setgroup", "unmute", "report", "go", "addcoins",
			"addcash", "addxp", "addperm"})
	public List<String> completer(ProxiedPlayer p, String label, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 0) {
			for (ProxiedPlayer o : BungeeCord.getInstance().getPlayers()) {
				 list.add(o.getName());
			}
		} else if (args.length == 1) {
			for (ProxiedPlayer o : BungeeCord.getInstance().getPlayers()) {
				if (args[0].toLowerCase().startsWith(o.getName().toLowerCase().substring(0, 1))) {
					list.add(o.getName());
				} else {
					list.add(o.getName());
				}
			}
		}
		return list;
	}
	
	private void enviar(ProxiedPlayer player, ServerInfo target) {
		if ((player.getServer() != null) && (!player.getServer().getInfo().getName().contains("login")) && 
				(!player.getServer().getInfo().getName().toLowerCase().contains("screenshare")) && 
				(!player.getServer().getInfo().equals(target))) {
			player.connect(target);
			player.sendMessage("§aVocê foi enviado para outro servidor.");
		}
	}
	
	private String ramQuality(double percentage) {
		if (percentage <= 60)
			return "§a" + percentage;
		else if (percentage > 60 && percentage < 90)
			return "§e" + percentage;
		else
			return "§c" + percentage;
	}
	
	public static void handleAddPerm(String nick, String perm) {
		String perms = MySQLManager.getString("accounts", "nick", nick, "perms");
		
    	ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
    	
		if (perms.equalsIgnoreCase("N/A")) {
	    	if (target != null) {
	    		BungeeClient.sendPacketToServer(target, 
						new PacketUpdateField(target.getName(), "CustomPlayer", "SetPrivatePerms", perm));
	    	} else {
	    		MySQLManager.atualizarStatus("accounts", "perms", nick, perm);
	    		
	    		try {
					RedisAPI.removeCacheIfExist(nick, DataCategory.PRISMA_PLAYER);
				} catch (Exception ex) {}
	    	}
		} else {
			List<String> permissões = StringUtils.reformuleFormattedWithoutSpace(perms);
			
			if (!permissões.contains(perm)) {
		    	if (target != null) {
		    		BungeeClient.sendPacketToServer(target, 
							new PacketUpdateField(target.getName(), "CustomPlayer", "SetPrivatePerms", perms + "," + perm));
		    	} else {
		    		MySQLManager.atualizarStatus("accounts", "perms", nick, perms + "," + perm);
		    		
		    		try {
						RedisAPI.removeCacheIfExist(nick, DataCategory.PRISMA_PLAYER);
					} catch (Exception ex) {}
		    	}
			}
		}
	}
}