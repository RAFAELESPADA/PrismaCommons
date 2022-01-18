package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;

public class CustomTagCommand implements CommandClass {
	
	@Command(name = "customtag", aliases = {"ctag"}, groupsToUse = {Groups.DONO})
	public void permissions(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		if (args.length != 2) {
			commandSender.sendMessage("§cUtilize: /ctag <Cor> <Tag>");
			return;
		}
		
		String cor = args[0],
				tag = args[1];
			
		if (cor.length() > 2) {
			commandSender.sendMessage("§cUse no máximo 2 caractéres (1 cor).");
			return;
		}
		if (tag.length() > 9) {
			commandSender.sendMessage("§cTag muito grande.");
			return;
		}
		if ((cor.equalsIgnoreCase("&l")) || (cor.equalsIgnoreCase("&k")) || (cor.equalsIgnoreCase("&o"))) {
			commandSender.sendMessage("§cVocê não pode usar esta 'cor'.");
			return;
		}
		
		if (!cor.contains("&")) {
			commandSender.sendMessage("§cUtilize '&' para especificar a cor.");
			return;
		}
		
		if (cor.equalsIgnoreCase("&&")) {
			commandSender.sendMessage("§cCor inválida");
			return;
		}
		
		cor = cor.replaceAll("&", "§");
		
		Player player = commandSender.getPlayer();
		Tag customTag = new Tag("ctag-#", tag, cor);
		
		TagAPI.update(player, customTag);
		player.sendMessage("§aCustomTag aplicada!");
	}
}
