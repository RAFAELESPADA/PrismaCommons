package com.br.gabrielsilva.prismamc.commons.bungee.manager.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.google.common.io.ByteStreams;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

@Getter @Setter
public class ConfigManager {

	private Configuration config;
	private HashMap<String, List<String>> permissions;
	
	public void setup() throws IOException {
		setPermissions(new HashMap<>());
		
		if (!BungeeMain.getInstance().getDataFolder().exists()) {
	        BungeeMain.getInstance().getDataFolder().mkdir();
		}
		
        File file = new File(BungeeMain.getInstance().getDataFolder(), "configBungee.yml");
        if (!file.exists()) {
            file.createNewFile();
            
            InputStream in = BungeeMain.getInstance().getResourceAsStream("configBungee.yml");
            OutputStream out = new FileOutputStream(file);
            try {
            	ByteStreams.copy(in, out);
            } catch (Exception ex) {
            	ex.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        }
        
        setConfig(YamlConfiguration.getProvider(YamlConfiguration.class).load(file));
		apply();
	}

	public void setupPermissions() {
        File filePermissions = new File(BungeeMain.getInstance().getDataFolder(), "permissionsBungee.yml");
        if (!filePermissions.exists()) {
        	try {
        		filePermissions.createNewFile();
        	} catch (Exception ex) {
            	return;
        	}
        }
		
		try {
			Configuration config = 
					ConfigurationProvider.getProvider(YamlConfiguration.class).load(filePermissions);
		
			boolean save = false;
			
			for (Groups tags : Groups.values()) {
				 if (tags.isExclusiva()) {
					 continue;
				 }
				 if (!tags.equals(Groups.DEV) && (!tags.equals(Groups.RANDOM))) {
					 if (config.get("permissions." + tags.getNome().toLowerCase()) == null) {
						 config.set("permissions." + tags.getNome().toLowerCase(), Arrays.asList("tag." + tags.getNome().toLowerCase()));
						 getPermissions().put(tags.getNome().toLowerCase(), 
								 Arrays.asList("a"));
						 save = true;

					 } else {
						 getPermissions().put(tags.getNome().toLowerCase(), config.getStringList("permissions." + tags.getNome().toLowerCase()));
					 }
            	 }
            }
        
			if (save) {
				ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, filePermissions);
			}
			
			config = null;
			
            getPermissions().put("dev", getPermissions().get("dono"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void apply() {
		Core.getRedis().setHostname(getConfig().getString("Redis.Host"));
		Core.getRedis().setSenha(getConfig().getString("Redis.Senha"));
		Core.getRedis().setPorta(getConfig().getInt("Redis.Porta"));
		
		Core.getMySQL().setHost(getConfig().getString("MySQL.Host"));
		Core.getMySQL().setDatabase(getConfig().getString("MySQL.Database"));
		Core.getMySQL().setUsuario(getConfig().getString("MySQL.Usuario"));
		Core.getMySQL().setSenha(getConfig().getString("MySQL.Senha"));
		Core.getMySQL().setPorta(getConfig().getString("MySQL.Porta"));
		
		BungeeMain.getManager().setMinutos(getConfig().getInt("AutoMSG.Minutos"));
		BungeeMain.getInstance().setLicenca(getConfig().getString("Licenca"));
		
		config = null;
	}
}