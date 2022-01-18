package com.br.gabrielsilva.prismamc.commons.bukkit.manager.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.values.ConfigValues;
import com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.values.ConfigValues.CONFIGS;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.MachineOS;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ConfigManager {
	
	private String diretorioFiles;
	private FileConfiguration kits, config, permissões, baus, dano;
	private File fkits, fconfig, fpermissões, fbaus, fdano;
	
	@Getter @Setter
	private boolean kitsLoaded, configLoaded, permissõesLoaded, bausLoaded, danoLoaded;
	
	public ConfigManager() {
		setKitsLoaded(false);
		setConfigLoaded(false);
		setPermissõesLoaded(false);
		setBausLoaded(false);
		setDanoLoaded(false);
		setDiretorioFiles(MachineOS.getDiretorio());
		
		File dir = new File(getDiretorioFiles());
		if (!dir.exists()) {
			dir.mkdir();
		}
	}
	
	public void loadDanoConfig() {
		if (isDanoLoaded()) {
			console("Dano ja está carregada...");
		} else {
			setDanoLoaded(true);
			
			long started = System.currentTimeMillis();
			
			this.fdano = new File(getDiretorioFiles(), "damages.yml");
			createIfNotExists(this.fdano);
			
			this.dano = YamlConfiguration.loadConfiguration(this.fdano);
			
			ConfigValues.checkAndApply(CONFIGS.DANO);
			
			console("Dano carregada em -> " + DateUtils.getElapsed(started));
		}
	}
	
	public void loadGlobalConfig() {
		if (isConfigLoaded()) {
			console("GlobalConfig ja está carregada...");
		} else {
			setConfigLoaded(true);
			
			long started = System.currentTimeMillis();
			
			this.fconfig = new File(getDiretorioFiles(), "globalConfig.yml");
			createIfNotExists(this.fconfig);
			
			this.config = YamlConfiguration.loadConfiguration(this.fconfig);
			
			ConfigValues.checkAndApply(CONFIGS.GLOBAL_CONFIG);
			
			console("GlobalConfig carregada em -> " + DateUtils.getElapsed(started));
		}
	}
	
	public void loadKits() {
		if (isKitsLoaded()) {
			console("Kits ja está carregada...");
		} else {
			setKitsLoaded(true);
			long started = System.currentTimeMillis();
			
			this.fkits = new File(getDiretorioFiles(), "kits.yml");
			createIfNotExists(this.fkits);
			
			this.kits = YamlConfiguration.loadConfiguration(this.fkits);
			
			console("Kits carregada em -> " + DateUtils.getElapsed(started));
		}
	}
	
	public void loadPermissoes() {
		if (isPermissõesLoaded()) {
			console("Permissoes ja está carregada...");
		} else {
			setPermissõesLoaded(true);
			long started = System.currentTimeMillis();
			
			this.fpermissões = new File(getDiretorioFiles(), "permissionsBukkit.yml");
			createIfNotExists(this.fpermissões);
			
			this.permissões = YamlConfiguration.loadConfiguration(this.fpermissões);
			
			ConfigValues.checkAndApply(CONFIGS.PERMISSOES);
			
			console("Permissoes carregada em -> " + DateUtils.getElapsed(started));
		}
	}
	
	public void loadBaus() {
		if (isBausLoaded()) {
			console("Baus ja está carregada...");
		} else {
			setBausLoaded(true);
					
			long started = System.currentTimeMillis();
			
			this.fbaus = new File(getDiretorioFiles(), "itensBaus.yml");
			createIfNotExists(this.fpermissões);
			
			this.baus = YamlConfiguration.loadConfiguration(this.fbaus);
			
			ConfigValues.checkAndApply(CONFIGS.BAUS);
			
			console("Baus carregada em -> " + DateUtils.getElapsed(started));
		}
	}

	private void createIfNotExists(File fconfig) {
		if (!fconfig.exists()) {
		     try {
		    	 fconfig.createNewFile();
		     } catch (IOException e) {
		    	 e.printStackTrace();
		     }
		}
	}
	
	public FileConfiguration getGlobalConfig() {
		if (!isConfigLoaded()) {
			loadGlobalConfig();
		}
		return config;
	}
	
	public FileConfiguration getDanoConfig() {
		if (!isDanoLoaded()) {
			loadDanoConfig();
		}
		return dano;
	}
	
	public FileConfiguration getPermsConfig() {
		if (!isPermissõesLoaded()) {
			loadPermissoes();
		}
		return permissões;
	}
	
	public FileConfiguration getKitsConfig() {
		if (!isKitsLoaded()) {
			loadKits();
		}
		return kits;
	}
	
	public FileConfiguration getBausConfig() {
		if (!isBausLoaded()) {
			loadBaus();
		}
		return baus;
	}
	
	public void unloadDano() {
		if (isDanoLoaded()) {
			setDanoLoaded(false);
			this.dano = null;
			this.fdano = null;
		}
	}
	
	public void unloadBaus() {
		if (isBausLoaded()) {
			setBausLoaded(false);
			this.baus = null;
			this.fbaus = null;
		}
	}
	
	public void unloadPermissoes() {
		if (isPermissõesLoaded()) {
			setPermissõesLoaded(false);
			this.permissões = null;
			this.fpermissões = null;
		}
	}
	
	public void unloadKits() {
		if (isKitsLoaded()) {
			setKitsLoaded(false);
			this.kits = null;
			this.fkits = null;
		}
	}
	
	public void unloadGlobalConfig() {
		if (isConfigLoaded()) {
			setConfigLoaded(false);
			this.config = null;
			this.fconfig = null;
		}
	}
	
	public void saveBausConfig() {
		try {
			baus.save(fbaus);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void saveDanoConfig() {
		try {
			dano.save(fdano);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void saveKitsConfig() {
		try {
			kits.save(fkits);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void savePermsConfig() {
		try {
			permissões.save(fpermissões);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public void saveGlobalConfig() {
		try {
			config.save(fconfig);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private void console(String message) {
		BukkitMain.console("[ConfigManager] " + message);
	}
}