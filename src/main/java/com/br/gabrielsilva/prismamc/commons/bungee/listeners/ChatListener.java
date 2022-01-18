package com.br.gabrielsilva.prismamc.commons.bungee.listeners;

import java.util.HashMap;
import java.util.UUID;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.register.PunishCommand;
import com.br.gabrielsilva.prismamc.commons.bungee.utils.BlockMessages;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ChatListener implements Listener {

	public static HashMap<UUID, Integer> warns = new HashMap<>();
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChatMessage(ChatEvent event) {
		if (event.isCommand()) {
			return;
		}
		
		String mensagem = event.getMessage();
		
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if (!BungeeMain.getManager().getSessionManager().hasSession(player)) {
			player.disconnect(TextComponent.fromLegacyText("§4§lERRO\n\n§fVocê não possuí uma sessão no servidor."));
			event.setCancelled(true);
			return;
		}
		
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(player);
		final Groups grupo = proxyPlayer.getGrupo();
		
		if (grupo.getNivel() < Groups.YOUTUBER.getNivel()) {
			if (BlockMessages.hasDivulgation(player.getName(), mensagem)) {
				event.setCancelled(true);
				player.sendMessage("");
				player.sendMessage("§cQualquer tipo de divulgação não é permitido em nosso servidor.");
				addWarn(player);
				return;
			}
		}
		
		if (mensagem.startsWith("/")) {
			return;
		}
		
		if (mensagem.startsWith(" /")) {
			return;
		}
		
		if (proxyPlayer.getMute().isAplicado()) {
			if (proxyPlayer.getMute().isExpired()) {
				proxyPlayer.getMute().unmute();
				proxyPlayer.resetPunishment();
			} else if (proxyPlayer.getMute().isPermanent()) {
				event.setCancelled(true);
				player.sendMessage(PluginMessages.MUTADO_PERMANENTEMENTE);
			} else {
				event.setCancelled(true);
				player.sendMessage(PluginMessages.MUTADO_TEMPORARIAMENTE.replace("%tempo%", 
						DateUtils.formatDifference(proxyPlayer.getMute().getPunishmentTime())));
			}
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		if (mensagem.contains("%r")) {
			mensagem = mensagem.replaceAll("%r", "r");
		}
		if (mensagem.contains("&")) {
			if (grupo.getNivel() >= Groups.SAPPHIRE.getNivel()) {
				mensagem = mensagem.replaceAll("&", "§");
			}
		}
		
		if (proxyPlayer.isInClanChat()) {
			event.setCancelled(true);
			if (!Core.getClanManager().hasClanData(proxyPlayer.getClan())) {
				Core.getClanManager().loadClan(proxyPlayer.getClan());
			}
			
			final Clan clan = Core.getClanManager().getClan(proxyPlayer.getClan());
			
			final String colorNick = (clan.getDono().equals(player.getName()) ? "§4" : 
				clan.getAdmins().contains(player.getName()) ? "§c" : "§7");
			
			final String formatado = "§6§lCLAN " + colorNick + player.getName() + ": §f" + mensagem;
			
			for (Object object : clan.getOnlines()) {
				 ProxiedPlayer onlines = (ProxiedPlayer) object;
				 onlines.sendMessage(formatado);
			}
			return;
		}
		
		if (proxyPlayer.isInStaffChat()) {
			event.setCancelled(true);
			if (!proxyPlayer.isViewStaffChat()) {
				player.sendMessage("§cVocê está no StaffChat, porém, não está vendo as mensagens do StaffChat, ative-as.");
				return;
			}
			
			final String formatado = "§6§l[STAFFCHAT] " + 
			proxyPlayer.getGrupo().getCor() + "§l" + proxyPlayer.getGrupo().getNome() + " " + proxyPlayer.getGrupo().getCor() 
			+  player.getName() + ": §f" + mensagem;
			
			for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
				 if (proxiedPlayer.hasPermission("prismamc.staff")) {
					 if (BungeeMain.isValid(proxiedPlayer)) {
						 if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).isViewStaffChat()) {
							 proxiedPlayer.sendMessage(formatado);
						 }
					 }
				 }
			}
			return;
		}
	}

	private void addWarn(ProxiedPlayer player) {
		int avisos = warns.containsKey(player.getUniqueId()) ? warns.get(player.getUniqueId()) : 0;
		if (avisos == 0) {
			player.sendMessage("§cVocê recebeu seu primeiro aviso. (1/3)");
		} else if (avisos == 1) {
			player.sendMessage("§cVocê recebeu seu segundo aviso. (2/3)");
		} else if (avisos == 3) {
			player.sendMessage("§cVocê recebeu seu terceiro aviso. (3/3)");
		} else {
			long tempo = 0;
			try {
				tempo = DateUtils.parseDateDiff("3d", true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			PunishCommand.handleMute(player.getName(), "Divulgação", "CONSOLE", tempo);
			player.sendMessage("");
			warns.remove(player.getUniqueId());
			return;
		}
		player.sendMessage("");
		warns.put(player.getUniqueId(), avisos + 1);
	}
}