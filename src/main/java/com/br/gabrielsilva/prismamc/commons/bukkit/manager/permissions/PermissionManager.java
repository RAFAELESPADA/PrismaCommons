package com.br.gabrielsilva.prismamc.commons.bukkit.manager.permissions;

import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PermissionManager {
	
	private HashMap<String, List<String>> permissions;
	
	public PermissionManager() {
		setPermissions(new HashMap<>());
	}
	
	public void setup() {
		ConfigurationSection score = 
				BukkitMain.getManager().getConfigManager().getPermsConfig().getConfigurationSection("permissions");
		
        if (score == null) {
        	return;
        }
        
        for (String grupos : score.getKeys(false)) {
        	 getPermissions().put(grupos.toLowerCase(), BukkitMain.getManager().getConfigManager().getPermsConfig().
        			 getStringList("permissions." + grupos));
        }

        getPermissions().put("dev", getPermissions().get("dono"));
	}
	
	public void injectPermissions(Player player) {
		if (!BukkitMain.getManager().getDataManager().hasBukkitPlayer(player.getUniqueId())) {
			return;
		}
		
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
	 	if (bukkitPlayer.getAttachment() != null) {
	 		bukkitPlayer.getAttachment().removePermissions(bukkitPlayer.getAttachment().getPermissions());
	 	}
		
		bukkitPlayer.setAttachment(new AttachmentManager(player, BukkitMain.getInstance()));
		
		bukkitPlayer.getAttachment().removePermissions(bukkitPlayer.getAttachment().getPermissions());	
	
		bukkitPlayer.getAttachment().addPermissions(
				getPermsFromGroup(bukkitPlayer.getDataHandler().getData(DataType.GRUPO).getString().toLowerCase()));
		
		List<String> permsPlayer = bukkitPlayer.getDataHandler().getData(DataType.PERMS).getList();
		
		if (permsPlayer.size() != 0) {
			bukkitPlayer.getAttachment().addPermissions(permsPlayer);
		}
	}
	
	public void removeForAllPlayers() {
		for (Player on : Bukkit.getOnlinePlayers()) {
			 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(on.getUniqueId());
		 	 if (bukkitPlayer.getAttachment() != null) {
		 		 bukkitPlayer.getAttachment().removePermissions(bukkitPlayer.getAttachment().getPermissions());
		 	 }
		}
	}
	
	public void updatePermissionsAll() {
		for (Player on : Bukkit.getOnlinePlayers()) {
			 BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(on.getUniqueId());
		 	 if (bukkitPlayer.getAttachment() != null) {
		 		 bukkitPlayer.getAttachment().addPermissions(
						getPermsFromGroup(bukkitPlayer.getDataHandler().getData(DataType.GRUPO).getString().toLowerCase()));
				
				List<String> permsPlayer = bukkitPlayer.getDataHandler().getData(DataType.PERMS).getList();
				
				 if (permsPlayer.size() != 0) {
					 bukkitPlayer.getAttachment().addPermissions(permsPlayer);
				 }
				 
				 permsPlayer.clear();
				 permsPlayer = null;
		 	 }
		}
	}

	public List<String> getPermsFromGroup(String group) {
		return getPermissions().get(group);
	}	
}