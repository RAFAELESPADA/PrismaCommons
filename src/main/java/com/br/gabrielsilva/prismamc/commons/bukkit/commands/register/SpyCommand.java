package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Completer;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;

public class SpyCommand implements CommandClass {

	private final String COMMAND_USAGE = "§a§lSPY §fUtilize: /spy <Nick/All>",
			SPYING_ALL = "§a§lSPY §fVocê está espiando todos os §a§lTELLS!",
			NOT_SPYING_ALL = "§a§lSPY §fVocê não está espionando mais!",
			NOT_SPYING_PLAYER = "§a§lSPY §fVocê não está espiando o jogador §a%nick%",
			SPYING_PLAYER = "§a§lSPY §fVocê está espiando o jogador §a%nick%";
	
	public static HashMap<UUID, List<String>> spying = new HashMap<>();
	
	@Command(name = "spy", aliases= {"espiar"}, groupsToUse = {Groups.MOD})
	public void spy(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		Player player = commandSender.getPlayer();
		
		if (args.length != 1) {
			player.sendMessage(COMMAND_USAGE);
			return;
		}
		
		String nick = args[0];
		if ((nick.equalsIgnoreCase("all")) || (nick.equalsIgnoreCase("todos")) || (nick.equalsIgnoreCase("*"))) {
			if (!spying.containsKey(player.getUniqueId())) {
				List<String> lista = new ArrayList<>();
				lista.add("all");
				spying.put(player.getUniqueId(), lista);
				player.sendMessage(SPYING_ALL);
			} else {
				spying.clear();
				player.sendMessage(NOT_SPYING_ALL);
			}
		} else {	
			Player target = Bukkit.getPlayer(nick);
			if (target == null) {
				player.sendMessage(PluginMessages.JOGADOR_OFFLINE);
				return;
			}
			
			if (!spying.containsKey(player.getUniqueId())) {
				List<String> lista = new ArrayList<>();
				lista.add(BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId()).getNick());
				spying.put(player.getUniqueId(), lista);
				player.sendMessage(SPYING_ALL);
			} else {
				String realNick = BukkitMain.getManager().getDataManager().getBukkitPlayer(target.getUniqueId()).getNick();
				
				if (realNick.equalsIgnoreCase("BiielBr")) {
					player.sendMessage("§cVocê não pode espiar o pai.");
					return;
				}
				
				List<String> lista =  spying.get(player.getUniqueId());
				if (lista.contains("all")) {
					player.sendMessage("§cVocê ja está espiando todos os jogadores.");
					return;
				}
				
				if (lista.contains(realNick)) {
					lista.remove(realNick);
					
					player.sendMessage(NOT_SPYING_PLAYER.replace("%nick%", realNick));
					
					if (lista.size() == 0) {
						spying.remove(player.getUniqueId());
					} else {
						spying.put(player.getUniqueId(), lista);
					}
				} else {
					lista.add(realNick);
					
					
					player.sendMessage(SPYING_PLAYER.replace("%nick%", realNick));
					spying.put(player.getUniqueId(), lista);
				}
			}
		}
	}
	
	public static List<Player> getSpys() {
		List<Player> list = new ArrayList<>();
		
		for (UUID uuids : spying.keySet()) {
			 Player target = Bukkit.getPlayer(uuids);
			 if (target != null && target.isOnline()) {
				 list.add(target);
			 }
		}
		
		return list;
	}
	
	public static boolean isSpyingPlayer(Player player, String nick) {
		if (!spying.containsKey(player.getUniqueId())) {
			return false;
		}
		
		return spying.get(player.getUniqueId()).contains(nick);
	}
	
	public static boolean isSpyingAll(Player player) {
		if (!spying.containsKey(player.getUniqueId())) {
			return false;
		}
		
		return spying.get(player.getUniqueId()).contains("all");
	}
	
	@Completer(name = "spy", aliases = {"espiar"})
	public List<String> tagcompleter(BukkitCommandSender sender, String label, String[] args) {
		if (sender.isPlayer()) {
			Player p = sender.getPlayer();
			
			if (args.length == 1) {
				List<String> list = new ArrayList<>();
				list.add("all");
				
	            for (Player ons : Bukkit.getOnlinePlayers()) {
	            	 if (ons.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
	                     list.add(ons.getName());
	                  }
	             }
				
				return list;
			}
		}
		
		return new ArrayList<>();
	}
}