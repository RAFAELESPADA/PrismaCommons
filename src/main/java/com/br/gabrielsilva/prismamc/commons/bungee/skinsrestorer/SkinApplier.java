package com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer;

import java.lang.reflect.Field;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketRespawnPlayer;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.connection.LoginResult.Property;

public class SkinApplier {

    private static Class<?> LoginResult;

	public static void handleSkin(PendingConnection pendingConnection, LoginEvent loginEvent) throws Exception {
		Property textures = (Property) SkinStorage.getOrCreateSkinForPlayer(pendingConnection.getName());
		LoginResult loginProfile = ((InitialHandler)pendingConnection).getLoginProfile();

		if (loginProfile == null) {
            try {
            	// NEW BUNGEECORD
            	loginProfile = (net.md_5.bungee.connection.LoginResult) ReflectionUtil.invokeConstructor(LoginResult,
                new Class<?>[]{String.class, String.class, Property[].class},
                pendingConnection.getUniqueId().toString(), pendingConnection.getName(), new Property[]{textures});
            } catch (Exception e) {
                // FALL BACK TO OLD
            	loginProfile = (net.md_5.bungee.connection.LoginResult) ReflectionUtil.invokeConstructor(LoginResult,
                new Class<?>[]{String.class, Property[].class}, pendingConnection.getUniqueId().toString(),
                new Property[]{textures});
            }
		}
		    
		LoginResult.Property[] properties = { textures };
		loginProfile.setProperties(properties);
		
		Field field = pendingConnection.getClass().getDeclaredField("loginProfile");
		field.setAccessible(true);
		field.set(pendingConnection, loginProfile);
		
		loginEvent.completeIntent(BungeeMain.getInstance());
	}
    
    public static void applySkin(ProxiedPlayer proxiedPlayer, String skinToApply) {
    	if (proxiedPlayer == null || proxiedPlayer.getServer() == null) {
    		return;
    	}
    	
    	BungeeMain.runAsync(() -> {
    		try {
       			Property textures = (Property) SkinStorage.getOrCreateSkinForPlayer(skinToApply);
                InitialHandler handler = (InitialHandler) proxiedPlayer.getPendingConnection();
                LoginResult profile = null;
                
                try {
                	// NEW BUNGEECORD
                    profile = (net.md_5.bungee.connection.LoginResult) ReflectionUtil.invokeConstructor(LoginResult,
                    new Class<?>[]{String.class, String.class, Property[].class},
                    proxiedPlayer.getUniqueId().toString(), proxiedPlayer.getName(), new Property[]{textures});
                } catch (Exception e) {
                    // FALL BACK TO OLD
                    profile = (net.md_5.bungee.connection.LoginResult) ReflectionUtil.invokeConstructor(LoginResult,
                    new Class<?>[]{String.class, Property[].class}, proxiedPlayer.getUniqueId().toString(),
                    new Property[]{textures});
                }
                
                Property[] newprops = new Property[]{textures};

                profile.setProperties(newprops);
                ReflectionUtil.setObject(InitialHandler.class, handler, "loginProfile", profile);
                
    	    	if (proxiedPlayer == null || proxiedPlayer.getServer() == null) {
    	    		return;
    	    	}
    	    	
    	    	BungeeClient.sendPacketToServer(proxiedPlayer.getServer().getInfo(), 
    	    			new PacketRespawnPlayer(proxiedPlayer.getName()));
    	    	
    		} catch (Exception e) {}
    	});
    }

    public static void init() {
        try {
            LoginResult = ReflectionUtil.getBungeeClass("connection", "LoginResult");
        } catch (Exception e) {}
    }

	public static void applySkin(String name) {
		ProxiedPlayer proxied = ProxyServer.getInstance().getPlayer(name);
		if (proxied != null) {
			applySkin(proxied, name);
		}
	}
}