package com.br.gabrielsilva.prismamc.commons.bungee.commands;

import java.util.Collection;
import java.util.UUID;

import com.br.gabrielsilva.prismamc.commons.core.command.CommandSender;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RequiredArgsConstructor
public class BungeeCommandSender implements CommandSender, net.md_5.bungee.api.CommandSender {

	@NonNull
	private net.md_5.bungee.api.CommandSender sender;

	@Override
	public void addGroups(String... arg0) {
		sender.addGroups(arg0);
	}

	@Override
	public Collection<String> getGroups() {
		return sender.getGroups();
	}

	@Override
	public String getName() {
		return sender.getName();
	}

	@Override
	public Collection<String> getPermissions() {
		return sender.getPermissions();
	}

	@Override
	public boolean hasPermission(String arg0) {
		if (sender.hasPermission("prismamc.cmd.all")) {
			return true;
		}
		if (sender.hasPermission("prismamc.cmd." + arg0)) {
			return true;
		}
		sender.sendMessage(PluginMessages.SEM_PERMISSÃO);
		return false;
	}

	@Override
	public void removeGroups(String... arg0) {
		sender.removeGroups(arg0);
	}

	@Override
	public void sendMessage(String arg0) {
		sender.sendMessage(TextComponent.fromLegacyText(arg0));
	}

	@Override
	public void sendMessage(BaseComponent... arg0) {
		sender.sendMessage(arg0);
	}

	@Override
	public void sendMessage(BaseComponent arg0) {
		sender.sendMessage(arg0);
	}

	@Override
	public void sendMessages(String... arg0) {
		sender.sendMessages(arg0);
	}

	@Override
	public void setPermission(String arg0, boolean arg1) {
		sender.setPermission(arg0, arg1);
	}

	@Override
	public UUID getUniqueId() {
		return sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : UUID.randomUUID();
	}

	public String getNick() {
		if (sender instanceof ProxiedPlayer) {
			return ((ProxiedPlayer)sender).getName();
		}
		return "CONSOLE";
	}
	
	@Override
	public boolean isPlayer() {
		if (sender instanceof ProxiedPlayer) {
			return true;
		}
		sender.sendMessage("§cComando disponível apenas para Jogadores.");
		return false;
	}
	
	public ProxiedPlayer getPlayer() {
		return (ProxiedPlayer) sender;
	}
}