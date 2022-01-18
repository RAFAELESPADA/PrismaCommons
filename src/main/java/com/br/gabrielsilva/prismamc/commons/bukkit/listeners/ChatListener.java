package com.br.gabrielsilva.prismamc.commons.bukkit.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;

public class ChatListener implements Listener {

	public static HashMap<UUID, Long> chatCooldown = new HashMap<>();
	public static HashMap<String, String> clanTag = new HashMap<>();
	
	@EventHandler(priority = EventPriority.LOW)
	public void chat(AsyncPlayerChatEvent event) {
		if (event.getMessage().startsWith("/")) {
			return;
		}
		
		if (event.getMessage().startsWith(" /")) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		event.setCancelled(true);
		
		Player player = event.getPlayer();
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		
		Groups grupo = bukkitPlayer.getData(DataType.GRUPO).getGrupo();
		
		if (!ServerOptions.isChat() && 
				(grupo.getNivel() < Groups.YOUTUBER_PLUS.getNivel())) {
			player.sendMessage(PluginMessages.CHAT_ESTA_DESATIVADO);
			player = null;
			bukkitPlayer = null;
			grupo = null;
			return;
		}
		
		boolean continuar = true;
		if (grupo.getNivel() < Groups.TRIAL.getNivel()) {
			if (chatCooldown.containsKey(player.getUniqueId()) && (chatCooldown.get(player.getUniqueId()) >= System.currentTimeMillis())) {
				player.sendMessage(PluginMessages.CHAT_COOLDOWN);
				continuar = false;
				return;
			}
			chatCooldown.put(player.getUniqueId(), System.currentTimeMillis() + 2000L);
		}
		
		if (!continuar) {
			player = null;
			bukkitPlayer = null;
			grupo = null;
			return;
		}
		
		String message = event.getMessage();
		if (message.contains("%")) {
			message = message.replaceAll("%", "%%");
		}
		
		if (message.contains("&")) {
			if (grupo.getNivel() >= Groups.SAPPHIRE.getNivel()) {
				message = message.replaceAll("&", "§");
			}
		}
		
		for (Player onlines : event.getRecipients()) {
			 onlines.sendMessage(player.getDisplayName() + "§f: " + message);
		}
		
		player = null;
		bukkitPlayer = null;
		grupo = null;
		message = null;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		if (!e.isCancelled()) {
			Player p = e.getPlayer();
			String cmd = e.getMessage().split(" ")[0];
			HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(cmd);
			if (topic == null) {
				e.setCancelled(true);
				p.sendMessage(PluginMessages.COMANDO_INEXISTENTE);
				return;
			}
		}
	}
	
	String[] cmdsBlockeds = {"/?", "/bukkit:", "/say", "/kill", "/msg", "/me", "/w", "/help"};
	
	@EventHandler
    public void blockCMDS(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String msg = e.getMessage().toLowerCase();
		if (msg.equals("stop")) {
			e.setCancelled(true);
			p.chat("/parar");
			return;
		}
		if ((msg.equals("/pl"))  || (msg.equals("/plugin")) || (msg.startsWith("ver")) || (msg.startsWith("icanhasbukkit")) || (msg.equals("/plugins")) || (msg.startsWith("/help")) || (msg.startsWith("/bukkit:"))) {
			e.setCancelled(true);
			p.sendMessage("§cComando bloqueado.");
			return;
		}
		boolean block = false;
		for (String cmds : cmdsBlockeds) {
			 if (msg.startsWith(cmds)) {
				 if ((cmds.equalsIgnoreCase("/w")) || (msg.equalsIgnoreCase("/killmobs")) ||(msg.equalsIgnoreCase("/whitelist")) || (msg.equalsIgnoreCase("/wand")))
					 continue;
				 block = true;
				 break;
			 }
		}
		if (block) {
			e.setCancelled(true);
		    p.sendMessage("§cComando bloqueado.");
		}
	}
}