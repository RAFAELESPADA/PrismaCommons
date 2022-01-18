package com.br.gabrielsilva.prismamc.commons.bungee.listeners;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.register.StaffCommand;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.permissions.PermissionsManager;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.SkinApplier;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.base.BasePunishment;
import com.br.gabrielsilva.prismamc.commons.core.base.PunishmentType;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AccountListener implements Listener {
	
	private ArrayList<String> playersNotLoged = new ArrayList();
	
	@EventHandler
	public void preLogin(PreLoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (!BungeeMain.isLoaded()) {
			event.setCancelled(true);
			event.setCancelReason("§b§lKOMBO\n\n§fO servidor está carregando.");
			return;
		}
		
		PendingConnection connection = event.getConnection();
		if (!connection.isConnected()) {
			return;
		}
		
	    if (!BungeeMain.getManager().getPremiumMapManager().containsMap(connection.getName())) {
			BungeeMain.getManager().getPremiumMapManager().load(connection.getName());
			
			connection.setOnlineMode(BungeeMain.getManager().getPremiumMapManager().getPremiumMap(connection.getName()).isPremium());
	    	if (BungeeMain.getManager().getPremiumMapManager().getChangedNicks().contains(connection.getName())) {
	    		BungeeMain.getManager().getPremiumMapManager().getChangedNicks().remove(connection.getName());
	    		
				event.setCancelled(true);
			    event.setCancelReason("§c§lPREMIUM-MAP\n\n"
			     + "§fFoi detectado uma mudança no seu Nick.\n"
			     + "§fAtualizando seus dados...");
	    	}
	    } else {
	    	connection.setOnlineMode(BungeeMain.getManager().getPremiumMapManager().getPremiumMap(connection.getName()).isPremium());
	    }
	    
		if (!this.playersNotLoged.contains(connection.getName())) {
		    this.playersNotLoged.add(connection.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(LoginEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		PendingConnection pendingConnection = event.getConnection();
		if (pendingConnection == null) {
			return;
		}
		
	    InetSocketAddress inetSocketAddress = pendingConnection.getAddress();
	    if (inetSocketAddress == null) {
	        return;
	    }
	    
	    InetAddress inetAddress = inetSocketAddress.getAddress(); 
	    if (inetAddress == null) {
	        return;
	    }
	    
		if (!pendingConnection.isConnected()) {
			return;
		}
	    
	    final String nick = pendingConnection.getName(), 
	    		address = pendingConnection.getAddress().getAddress().getHostAddress().toString().replace("/", "");
	    
		if (!StringUtils.validUsername(nick)) {
			event.setCancelled(true);
			event.setCancelReason("§cNick inválido.");
			return;
		}
		
		if (!BungeeMain.getManager().getPremiumMapManager().containsMap(nick)) {
			event.setCancelled(true);
			event.setCancelReason("§cTente entrar novamente.");
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		event.registerIntent(BungeeMain.getInstance());
		handleCheckBan(nick, address, event);
		
		if (event.isCancelled()) {
			return;
		}
		
		if (BungeeMain.isGlobalWhitelist()) {
			if (!BungeeMain.getWhitelistPlayers().contains(nick.toLowerCase())) {
				event.setCancelled(true);
				event.setCancelReason("§cServidor em manutenção...");
			}
		}
		if (event.isCancelled()) {
			return;
		}
		
		event.registerIntent(BungeeMain.getInstance());
		BungeeMain.runAsync(() -> {
			try {
				SkinApplier.handleSkin(pendingConnection, event);
			} catch (Exception ex) {
				event.setCancelled(true);
				event.setCancelReason("§cFalha ao processar o seu Login");
				event.completeIntent(BungeeMain.getInstance());
			}
		});
	}
	
	@EventHandler
	public void deslogar(PlayerDisconnectEvent event) {
		ProxiedPlayer proxiedPlayer = event.getPlayer();
		
		if (BungeeMain.getManager().getSessionManager().hasSession(proxiedPlayer)) {
			BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer);
			if (!bungeePlayer.getClan().equalsIgnoreCase("Nenhum")) {
				Clan clan = Core.getClanManager().getClan(bungeePlayer.getClan());
				clan.removeOnline(proxiedPlayer);
				
				if (clan.getOnlines().size() == 0) {
					Core.getClanManager().removeClanData(bungeePlayer.getClan());
				}
			}
			
			if (bungeePlayer.inScreenShare()) {
				ProxiedPlayer other = ProxyServer.getInstance().getPlayer(bungeePlayer.getScreenShareWith());
				if (bungeePlayer.isPuxou()) {
					other.sendMessage("§cO Staffer que estava na sessão de compartilhamento de tela deslogou.");
					bungeePlayer.quitScreenShare();
				} else {
					bungeePlayer.quitScreenShare();
					other.sendMessage("§cO jogador deslogou da sessão.");
				}
				
				BungeeMain.getManager().getSessionManager().getSession(other).quitScreenShare();
				other.connect(ProxyServer.getInstance().getServerInfo("Lobby"));
			}
		
			bungeePlayer.quit();
			bungeePlayer.resetPunishment();
			PermissionsManager.clearPermissions(proxiedPlayer);
		}
	}
	
	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer proxiedPlayer = event.getPlayer();
		
		final String address = proxiedPlayer.getAddress().getAddress().getHostAddress().toString().replace("/", "");
		BungeeMain.runLater(() -> {
			if (proxiedPlayer == null || proxiedPlayer.getServer() == null) {
	    	    return;
			}
			
			if (BungeeMain.getManager().getSessionManager().hasSession(proxiedPlayer.getName())) {
				BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).handleLogin(proxiedPlayer);
			}
			
			if (BungeeMain.getManager().getPremiumMapManager().getPremiumMap(proxiedPlayer.getName()).isPremium()) {
				proxiedPlayer.sendMessage("§aAutenticado como jogador Original.");
				MySQLManager.atualizarStatus("accounts", "last_ip", proxiedPlayer.getName(), address);
			} else {
				proxiedPlayer.sendMessage("§cAutenticado como jogador Pirata.");
			}
			
			if (StaffCommand.hasEvento) {
				proxiedPlayer.sendMessage("");
				proxiedPlayer.sendMessage(PluginMessages.EVENTO_DISPONIVEL);
				proxiedPlayer.sendMessage("");
			}
		}, 1, TimeUnit.SECONDS);
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void connect(ServerConnectEvent event) {
		ProxiedPlayer proxiedPlayer = event.getPlayer();
		
		if (this.playersNotLoged.contains(proxiedPlayer.getName())) {
			boolean premium = BungeeMain.getManager().getPremiumMapManager().getPremiumMap(proxiedPlayer.getName()).isPremium();
			
			final String address = proxiedPlayer.getAddress().getAddress().getHostAddress().toString().replace("/", "");
			
			this.playersNotLoged.remove(proxiedPlayer.getName());
			
			if (!BungeeMain.getManager().getSessionManager().hasSession(proxiedPlayer.getName())) {
				if (premium) {
					if (!event.getTarget().getName().equalsIgnoreCase("Lobby")) {
					    event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Lobby"));
					}
					BungeeMain.getManager().getSessionManager().addSession(proxiedPlayer);
					
					BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).setAddress(address);
				} else {
					if (!event.getTarget().getName().equalsIgnoreCase("Login")) {
						event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Login"));
					}
				}
			} else if (BungeeMain.getManager().getSessionManager().hasSession(proxiedPlayer.getName())) {
				BungeePlayer playerSession = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName());

				if (event.getTarget().getName().equalsIgnoreCase("Login")) {
					if (premium) {
						event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Lobby"));
						return;
					}
					if (playerSession.isValidSession()) {
						if (playerSession.getAddress().equals(address)) {
							event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Lobby"));
						}
					}
				} else if (event.getTarget().getName().equalsIgnoreCase("Lobby")) {
					if (premium) {
						return;
					}
					if (playerSession.isValidSession()) {
						if (!playerSession.getAddress().equals(address)) {
							event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Login"));
						}
					} else {
						event.setTarget(BungeeMain.getInstance().getProxy().getServerInfo("Login"));
						proxiedPlayer.sendMessage(TextComponent.fromLegacyText("§cSua sessão expirou, logue novamente."));
					}
				}
			}
			return;
		}
	}
	
	private void handleCheckBan(String nick, String address, LoginEvent event) {
		BasePunishment punish = new BasePunishment(nick, address, PunishmentType.BAN);
	    
		try {
			punish.load();
		} catch (Exception ex) {
			event.setCancelled(true);
			event.setCancelReason("§cOcorreu um erro ao tentar verificar sua conta...");
			BungeeMain.console("Ocorreu um erro ao tentar verificar uma punicao -> " + ex.getLocalizedMessage());
			event.completeIntent(BungeeMain.getInstance());
			return;
		}
		
		if (punish.isAplicado()) {
			if (punish.isPermanent()) {
				event.setCancelled(true);
				event.setCancelReason(PluginMessages.VOCÊ_ESTA_PERMANENTEMENTE_BANIDO.
				replace("%motivo%", punish.getMotivo()).replace("%baniu%", punish.getPunidoPor()));
				event.completeIntent(BungeeMain.getInstance());
			} else {
				if (punish.isExpired()) {
					BungeeMain.console("banimento de '"+nick+ "[" + address + "]' expirou, removendo o ban.");
					punish.unban();
					event.completeIntent(BungeeMain.getInstance());
				} else {
					event.setCancelled(true);
					event.setCancelReason(PluginMessages.VOCÊ_ESTA_TEMPORARIAMENTE_BANIDO.
							replace("%motivo%", punish.getMotivo()).replace("%baniu%", punish.getPunidoPor()).
							replace("%tempo%", DateUtils.formatDifference(punish.getPunishmentTime())));
					event.completeIntent(BungeeMain.getInstance());
				}
			}
		} else {
			event.completeIntent(BungeeMain.getInstance());
		}
	}
}