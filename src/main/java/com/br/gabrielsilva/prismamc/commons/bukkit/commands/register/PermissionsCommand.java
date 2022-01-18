package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

public class PermissionsCommand implements CommandClass {

	@Command(name = "permissionsbukkit", aliases = {"permsbukkit", "permbukkit", "pmbukkit"}, groupsToUse = {Groups.DONO})
	public void permissions(BukkitCommandSender commandSender, String label, String[] args) {
		if (args.length == 0) {
			sendHelp(commandSender);
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("refreshall")) {
				boolean hasLoaded = loadConfigIfNotLoaded();
				BukkitMain.getManager().getPermissionManager().setup();
				commandSender.sendMessage("�aCarregando permiss�es atualizadas...");
				
				BukkitMain.runLater(() -> {
					commandSender.sendMessage("�aAplicando permiss�es para todos os jogadores online...");
					BukkitMain.getManager().getPermissionManager().updatePermissionsAll();
					
					commandSender.sendMessage("�aAs permiss�es de todos os grupos foram atualizadas e aplicadas!");
					
					if (hasLoaded) {
						unload();
					}
				}, 60);
			} else {
				sendHelp(commandSender);
			}
		} else if (args.length == 2) {
			if ((args[0].equalsIgnoreCase("view")) || (args[0].equalsIgnoreCase("check"))) {
				String groupName = args[1];
				if (!Groups.existGrupo(groupName)) {
					commandSender.sendMessage("�cEste grupo n�o existe.");
					return;
				}
				
				boolean hasLoaded = loadConfigIfNotLoaded();
				
				final Groups group = Groups.getFromString(groupName);
				
				int amount = 0;
				
		        commandSender.sendMessage("");
		        commandSender.sendMessage("�7Permiss�es de " + group.getCor() + "�l" + group.getNome());
		        commandSender.sendMessage("");
		        for (String perms : BukkitMain.getManager().getConfigManager().getPermsConfig().getStringList("permissions." + groupName.toLowerCase())) {
		        	 commandSender.sendMessage("�7- �a" + perms);
		        	 amount++;
		        }
		        commandSender.sendMessage("");
		        commandSender.sendMessage("�7Total de permiss�es: �a" + amount);
		        commandSender.sendMessage("");
		        
				if (hasLoaded) {
					unload();
				}
			} else if (args.length == 3) {
				if (args[0].equalsIgnoreCase("add")) {
					String groupName = args[1];
					if (!Groups.existGrupo(groupName)) {
						commandSender.sendMessage("�cEste grupo n�o existe.");
						return;
					}
					boolean hasLoaded = loadConfigIfNotLoaded();
					final Groups group = Groups.getFromString(groupName);
					
					final String permToAdd = args[2];
					List<String> perms = 
							BukkitMain.getManager().getConfigManager().getPermsConfig().getStringList("permissions." + groupName.toLowerCase());
					
					if (perms.contains(permToAdd)) {
						commandSender.sendMessage("�cO grupo " + group.getCor() + group.getNome() + " �cj� possu� est� permiss�o.");
					} else {
						perms.add(permToAdd);
						commandSender.sendMessage("�aO grupo " + group.getCor() + group.getNome() + " �arecebeu a permiss�o '"+permToAdd+"'.");
						commandSender.sendMessage("�aUtilize: /" + label + " refreshall - para aplicar as novas permiss�es em todos os jogadores.");
						BukkitMain.getManager().getConfigManager().getPermsConfig().set(
								"permissions." + group.getNome().toLowerCase(), perms);
						BukkitMain.getManager().getConfigManager().savePermsConfig();
						BukkitMain.getManager().getPermissionManager().setup();
					}
					
					perms.clear();
					perms = null;
					if (hasLoaded) {
						unload();
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					String groupName = args[1];
					if (!Groups.existGrupo(groupName)) {
						commandSender.sendMessage("�cEste grupo n�o existe.");
						return;
					}
					boolean hasLoaded = loadConfigIfNotLoaded();
					final Groups group = Groups.getFromString(groupName);
					
					final String permToAdd = args[2];
					List<String> perms = 
							BukkitMain.getManager().getConfigManager().getPermsConfig().getStringList("permissions." + groupName.toLowerCase());
					
					if (!perms.contains(permToAdd)) {
						commandSender.sendMessage("�cO grupo " + group.getCor() + group.getNome() + " �cn�o possu� est� permiss�o.");
					} else {
						perms.remove(permToAdd);
						commandSender.sendMessage("�aO grupo " + group.getCor() + group.getNome() + " �ateve a permiss�o '"+permToAdd+"' retirada.");
						commandSender.sendMessage("�aUtilize: /" + label + " refreshall - para aplicar as novas permiss�es em todos os jogadores.");
						BukkitMain.getManager().getConfigManager().getPermsConfig().set(
								"permissions." + group.getNome().toLowerCase(), perms);
						BukkitMain.getManager().getConfigManager().savePermsConfig();
						BukkitMain.getManager().getPermissionManager().setup();
					}
					
					perms.clear();
					perms = null;
					if (hasLoaded) {
						unload();
					}
				}
			} else {
				sendHelp(commandSender);
			}
		}
	}
	
	private void unload() {
		BukkitMain.runLater(() -> {
			BukkitMain.getManager().getConfigManager().unloadPermissoes();
		}, 2);
	}
	
	private boolean loadConfigIfNotLoaded() {
		if (!BukkitMain.getManager().getConfigManager().isPermiss�esLoaded()) {
			BukkitMain.getManager().getConfigManager().loadPermissoes();
			return true;
		}
		return false;
	}
	
	private void sendHelp(BukkitCommandSender commandSender) {
		commandSender.sendMessage("");
		commandSender.sendMessage("�cUtilize: /permissionsbukkit view <Grupo>");
		commandSender.sendMessage("�cUtilize: /permissionsbukkit <Add/Remove> <Grupo> <Permiss�o>");
		commandSender.sendMessage("�cUtilize: /permissionsbukkit refreshall");
		commandSender.sendMessage("");
	}
}