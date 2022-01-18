package com.br.gabrielsilva.prismamc.commons.bungee.manager.permissions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PermissionsManager {

	public static HashMap<UUID, List<String>> playerPermissions = new HashMap<>();
	
	public void addPermission(ProxiedPlayer proxiedPlayer, String permission) {
		proxiedPlayer.setPermission(permission, true);
		
		List<String> permissions = 
				(playerPermissions.containsKey(proxiedPlayer.getUniqueId()) ? playerPermissions.get(proxiedPlayer.getUniqueId()) : new ArrayList<>());
		permissions.add(permission);
		
		playerPermissions.put(proxiedPlayer.getUniqueId(), permissions);
	}
	
	public static void injectPermissions(ProxiedPlayer proxiedPlayer, String grupo) {
		List<String> perms = BungeeMain.getManager().getConfigManager().getPermissions().get(grupo.toLowerCase());
		
		if (perms.size() == 0) {
			return;
		}
		
		for (int i = 0; i < perms.size(); i++) {
			 proxiedPlayer.setPermission(perms.get(i), true);
		}
		playerPermissions.put(proxiedPlayer.getUniqueId(), perms);
	}
	
	public static void clearPermissions(ProxiedPlayer proxiedPlayer) {
		if (!playerPermissions.containsKey(proxiedPlayer.getUniqueId())) {
			return;
		}
		
		List<String> perms = playerPermissions.get(proxiedPlayer.getUniqueId());
		for (int i = 0; i < perms.size(); i++) {
			 proxiedPlayer.setPermission(perms.get(i), false);
		}
		
		playerPermissions.remove(proxiedPlayer.getUniqueId());
	}
	
	public static void updatePermissionsAll() {
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 if (BungeeMain.isValid(proxiedPlayer)) {
				 clearPermissions(proxiedPlayer);
				 
				 injectPermissions(proxiedPlayer, BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer).getGrupo().getNome());
			 }
		}
	}
}