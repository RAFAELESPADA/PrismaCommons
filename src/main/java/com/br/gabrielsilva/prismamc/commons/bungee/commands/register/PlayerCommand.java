package com.br.gabrielsilva.prismamc.commons.bungee.commands.register;

import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.server.types.HungerGamesServer;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerCommand implements CommandClass {
	
	@Command(name = "hg", aliases= {"hungergames", "hardcoregames"})
	public void hg(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		
		if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê não pode ir para outro servidor estando em uma sessão de ScreenShare.");
			return;
		}
		
		if (args.length == 1) {
			if (!StringUtils.isInteger(args[0])) {
				proxiedPlayer.sendMessage("");
				proxiedPlayer.sendMessage("§cUtilize: /hg - para entrar na melhor sala.");
				proxiedPlayer.sendMessage("§cUtilize: /hg <1-5> -  para entrar em uma sala especifica.");
				proxiedPlayer.sendMessage("");
				return;
			}
			int ip = Integer.valueOf(args[0]);
			if (ip <= 0 || ip > 5) {
				proxiedPlayer.sendMessage("§cUtilize: /hg <1-5>");
				return;
			}
			
			HungerGamesServer hungerGames = Core.getServersHandler().getHungerGamesServer("hg" + ip);
			if (!hungerGames.isOnline()) {
				proxiedPlayer.sendMessage("§cEste servidor está offline.");
				return;
			}
			if (hungerGames.getVivos() >= 80) {
				proxiedPlayer.sendMessage("§cSala lotada.");
				return;
			}
			
			ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo("HG" + ip);
			if (serverInfo == null) {
				proxiedPlayer.sendMessage("§cEste servidor está offline.");
				return;
			}
			proxiedPlayer.sendMessage("§aConectando...");
			BungeeMain.runLater(() -> {
				proxiedPlayer.connect(serverInfo);
			}, 333, TimeUnit.MILLISECONDS);
		} else if (args.length == 0) {
			HungerGamesServer hungerGames = Core.getServersHandler().getBestHungerGames();
			if (hungerGames == null) {
				proxiedPlayer.sendMessage("§cNenhuma sala disponível.");
				return;
			}
			
			ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo("HG" + hungerGames.getServerID());
			if (serverInfo == null) {
				proxiedPlayer.sendMessage("§cNenhuma sala disponível.");
				return;
			}
			proxiedPlayer.sendMessage("§aConectando...");
			BungeeMain.runLater(() -> {
				proxiedPlayer.connect(serverInfo);
			}, 333, TimeUnit.MILLISECONDS);
		} else {
			proxiedPlayer.sendMessage("");
			proxiedPlayer.sendMessage("§cUtilize: /hg - para entrar na melhor sala.");
			proxiedPlayer.sendMessage("§cUtilize: /hg <1-5> -  para entrar em uma sala especifica.");
			proxiedPlayer.sendMessage("");
		}
	}
	
	@Command(name = "go", aliases= {"ir"})
	public void go(BungeeCommandSender commandSender, String label, String[] args) {
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
			proxiedPlayer.sendMessage("§cUse: /go <nick>");
			return;
		} 
		
		if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê não pode ir para outro servidor estando em uma sessão de ScreenShare.");
			return;
		}
		
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
		if ((player == null) || (player.getServer() == null)) {
			proxiedPlayer.sendMessage("§cO jogador não está em nenhum servidor.");
		} else {
			proxiedPlayer.sendMessage("§aConectando...");
			proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo(player.getServer().getInfo().getName()));
		}
	}
	
	@Command(name = "lobby")
	public void lobby(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê não pode ir para outro servidor estando em uma sessão de ScreenShare.");
			return;
		}
		
		String serverToConnect = "Lobby",
				serverPlayer = proxiedPlayer.getServer().getInfo().getName().toLowerCase();
		
		if (serverPlayer.equalsIgnoreCase(serverToConnect)) {
			proxiedPlayer.sendMessage("§cVocê ja está neste servidor.");
			return;
		}
		
		proxiedPlayer.sendMessage("§aConectando...");
		final String server = serverToConnect;
		BungeeMain.runLater(() -> {
			proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo(server));
		}, 333, TimeUnit.MILLISECONDS);
	}
	
	@Command(name = "staffchat", aliases= {"sc"})
	public void staffchat(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}

		// sabe como funciona os dados do redis?
		// mais ou menos

		
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName());
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				if (proxyPlayer.isViewStaffChat()) {
					proxiedPlayer.sendMessage("§cVocê ja está vendo o StaffChat.");
					return;
				}
				proxyPlayer.setViewStaffChat(true);
				proxiedPlayer.sendMessage("§aAgora você está vendo o StaffChat!");
			} else if (args[0].equalsIgnoreCase("off")) {
				if (!proxyPlayer.isViewStaffChat()) {
					proxiedPlayer.sendMessage("§cVocê não está vendo o StaffChat.");
					return;
				}
				proxyPlayer.setViewStaffChat(false);
				proxiedPlayer.sendMessage("§aAgora você não verá o StaffChat!");
			} else {
				proxiedPlayer.sendMessage("");
				proxiedPlayer.sendMessage("§cUse: /staffchat");
				proxiedPlayer.sendMessage("§cUse: /staffchat <On/Off>");
				proxiedPlayer.sendMessage("");
			}
			return;
		}
		
		if (proxyPlayer.isInStaffChat()) {
			proxyPlayer.setInStaffChat(false);
			proxiedPlayer.sendMessage("§aVocê saiu do chat da Staff.");
			
			MySQLManager.atualizarStatus(DataCategory.PREFERENCIAS.getTableName(), 
					DataType.STAFFCHAT.getField(), "nick", proxiedPlayer.getName(), "false");
			
			BungeeClient.sendPacketToServer(proxiedPlayer, 
					new PacketUpdateField(proxiedPlayer.getName(), "CustomPlayer", "Preferencias", "staffchat", "false"));

		} else {
			proxyPlayer.setInStaffChat(true);
			proxiedPlayer.sendMessage("§aVocê entrou no chat da Staff.");
			
			MySQLManager.atualizarStatus(DataCategory.PREFERENCIAS.getTableName(), 
					DataType.STAFFCHAT.getField(), "nick", proxiedPlayer.getName(), "true");
			
			BungeeClient.sendPacketToServer(proxiedPlayer, 
					new PacketUpdateField(proxiedPlayer.getName(), "CustomPlayer", "Preferencias", "staffchat", "true"));
		}
	}
	
	@Command(name = "evento")
	public void evento(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		
		if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).inScreenShare()) {
			proxiedPlayer.sendMessage("§cVocê não pode ir para outro servidor estando em uma sessão de ScreenShare.");
			return;
		}
		
		if (!StaffCommand.hasEvento && !BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).isStaffer()) {
			commandSender.sendMessage("§cNenhum evento disponível.");
			return;
		}
		
		proxiedPlayer.sendMessage("§aConectando...");
		BungeeMain.runLater(() -> {
			proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo("Evento"));
		}, 333, TimeUnit.MILLISECONDS);
	}
	
	@Command(name = "connect", aliases= {"server", "play", "conectar"})
	public void server(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (args.length == 0) {
			proxiedPlayer.sendMessage("§cUse: /connect <Servidor>");
			return;
		}
		if (!proxiedPlayer.getServer().getInfo().getName().equals(args[0])) {
			if (ProxyServer.getInstance().getServers().containsKey(args[0])) {
				if (args[0].toLowerCase().equalsIgnoreCase("Login")) {
					proxiedPlayer.sendMessage("§cVocê não pode se conectar no servidor de Login.");
					return;
				}
				if (args[0].equalsIgnoreCase("torneio")) {
					proxiedPlayer.sendMessage("§cUtilize: /torneio");
					return;
				}
				if (args[0].equalsIgnoreCase("evento")) {
					proxiedPlayer.sendMessage("§cUtilize: /evento");
					return;
				}
				String serverInstance = Core.getServersHandler().getServerInstance(args[0].toLowerCase());
				if (serverInstance.equalsIgnoreCase("Unknown")) {
					proxiedPlayer.sendMessage("§cEste servidor não existe.");
					return;
				} else if (serverInstance.equalsIgnoreCase("NetworkServer")) {
					if (!Core.getServersHandler().getNetworkServer(args[0].toLowerCase()).isOnline()) {
						proxiedPlayer.sendMessage("§cEste servidor está offline.");
						return;
					}
				} else if (serverInstance.equalsIgnoreCase("HungerGamesServer")) {
					if (!Core.getServersHandler().getHungerGamesServer(args[0].toLowerCase()).isOnline()) {
						proxiedPlayer.sendMessage("§cEste servidor está offline.");
						return;
					}
				} 
				
				if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).inScreenShare()) {
					proxiedPlayer.sendMessage("§cVocê não pode ir para outro servidor estando em uma sessão de ScreenShare.");
					return;
				}
				
				proxiedPlayer.sendMessage("§aConectando...");
				proxiedPlayer.connect(ProxyServer.getInstance().getServerInfo(args[0]));
			} else {
				proxiedPlayer.sendMessage("§cO servidor não existe.");
			}
		} else {
			proxiedPlayer.sendMessage("§cVocê já está conectado a este servidor.");
		}
	}
}