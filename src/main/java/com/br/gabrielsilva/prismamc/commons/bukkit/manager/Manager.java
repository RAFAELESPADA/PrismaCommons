package com.br.gabrielsilva.prismamc.commons.bukkit.manager;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.HologramPlugin;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.ConfigManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.data.DataManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.permissions.PermissionManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.WorldEditManager;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Manager {
	
	private DataManager dataManager;
	private PermissionManager permissionManager;
	private ConfigManager configManager;
	private WorldEditManager worldEditManager;
	
	private boolean hologramEnabled;
	
	public Manager() {
		setDataManager(new DataManager());
		setConfigManager(new ConfigManager());
		
		setPermissionManager(new PermissionManager());
		setWorldEditManager(new WorldEditManager());
		setHologramEnabled(false);
	}
	
	public void init() {
		getConfigManager().loadPermissoes();
	}
	
	public void enableHologram() {
		if (isHologramEnabled()) {
			return;
		}
		setHologramEnabled(true);
		
		HologramPlugin.onEnable();
		
		BukkitMain.console("Sistema de Hologramas ativados.");
	}
	
	public void disable() {
		if (isHologramEnabled()) {
			HologramPlugin.onDisable();
		}
	}
}