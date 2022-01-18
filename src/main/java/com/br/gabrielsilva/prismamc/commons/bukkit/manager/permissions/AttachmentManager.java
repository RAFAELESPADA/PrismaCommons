package com.br.gabrielsilva.prismamc.commons.bukkit.manager.permissions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

public class AttachmentManager {

	private PermissionAttachment attachment;

	public AttachmentManager(Player player, Plugin plugin) {
		this.attachment = player.addAttachment(plugin);
	}

	public void addPermission(String permission) {
		this.attachment.setPermission(permission, true);
	}

	public void addPermissions(List<String> permissions) {
		if (permissions == null) {
			return;
		}
		
		for (String permission : permissions) {
			this.attachment.setPermission(permission, true);
		}
	}

	public void removePermissions(List<String> permissions) {
		if (permissions == null) {
			return;
		}
		
		for (String permission : permissions) {
			this.attachment.unsetPermission(permission);
		}
	}
	
	public void resetPermissions() {
		if (getPermissions().size() != 0) {
			for (String permissions : getPermissions()) {
				 this.attachment.unsetPermission(permissions);
			}
		}
	}
	
	public void removePermission(String permission) {
		this.attachment.unsetPermission(permission);
	}
	
	public List<String> getPermissions() {
		return new ArrayList<String>(attachment.getPermissions().keySet());
	}
}