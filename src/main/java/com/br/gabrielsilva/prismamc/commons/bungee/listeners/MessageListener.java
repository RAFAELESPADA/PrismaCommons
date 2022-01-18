package com.br.gabrielsilva.prismamc.commons.bungee.listeners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.CustomPacketsManager;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener {

    private static final Map<Connection, Long> PACKET_USAGE = new ConcurrentHashMap<>();
    private static final Map<Connection, AtomicInteger> CHANNELS_REGISTERED = new ConcurrentHashMap<>();
    
	@EventHandler
	public void message(PluginMessageEvent event) {
		if (!event.getTag().equals("BungeeCord") && !event.getTag().equals(BungeeClient.getCHANNEL())) {
			return;
		}
        if ("WDL|INIT".equalsIgnoreCase(event.getTag()) && event.getSender() instanceof ProxiedPlayer) {
        	event.getSender().disconnect((BaseComponent)new TextComponent("§c§lERRO\n§fVocê não pode baixar nossos mapas."));
            return;
        }
        if ("PERMISSIONSREPL".equalsIgnoreCase(event.getTag()) && new String(event.getData()).contains("mod.worlddownloader")) {
        	event.getSender().disconnect((BaseComponent)new TextComponent("§c§lERRO\n§fVocê não pode baixar nossos mapas."));
        	return;
        }
        
        if (!event.getTag().equals(BungeeClient.getCHANNEL())) {
        	return;
        }
        
        final ByteArrayDataInput data = ByteStreams.newDataInput(event.getData());
		final String packetName = data.readUTF();
		
		final BungeePackets packet = BungeePackets.getPacket(packetName);
		
		
		if (packet != null) {
			CustomPacketsManager.addPacketsReceiveds();
			
			packet.read(data);
			
			try {
				packet.handle(BungeeClient.getHandler());
			} catch (HandlePacketException ex) {
				BukkitMain.console("Ocorreu um erro ao tentar lidar com o pacote '"+packet.getPacketName() + "' -> " + ex.getLocalizedMessage());
			}
		}
	}
	
	private List<String> banned = new ArrayList<>();
	
    @EventHandler
    public void onPacket(PluginMessageEvent event) {
        String name = event.getTag();
        if (!"MC|BSign".equals(name) && !"MC|BEdit".equals(name) && !"REGISTER".equals(name))
            return;

        Connection connection = event.getSender();
        if (!(connection instanceof ProxiedPlayer))
            return;

        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) connection;
        
        try {
            if ("REGISTER".equals(name)) {
                if (!CHANNELS_REGISTERED.containsKey(connection)) {
                    CHANNELS_REGISTERED.put(connection, new AtomicInteger());
                }
                
                for (int i = 0; i < new String(event.getData(), Charsets.UTF_8).split("\0").length; i++) {
                     if (CHANNELS_REGISTERED.get(connection).incrementAndGet() > 124) {
                    	 if (!banned.contains(proxiedPlayer.getName())) {
                    		 banned.add(proxiedPlayer.getName());
                     		 ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), 
                     				 "kick " + proxiedPlayer.getName() + " Nosso servidor é completamente protegido contra isto, tente novamente.");
                        	 BungeeMain.console(proxiedPlayer.getName() + " registrou muitos canais e foi kikado automaticamente.");
                    	 }
                         throw new IOException("Too many channels");
                     }
                }
            } else {
                if (elapsed(PACKET_USAGE.getOrDefault(connection, -1L), 100L)) {
                    PACKET_USAGE.put(connection, System.currentTimeMillis());
                } else {
                	if (!banned.contains(proxiedPlayer.getName())) {
            		    banned.add(proxiedPlayer.getName());
             		    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), 
             		    		"kick " + proxiedPlayer.getName() + " Nosso servidor é completamente protegido contra isto, tente novamente.");
                    	BungeeMain.console(proxiedPlayer.getName() + " tentou enviar muitos pacote e foi kikado automaticamente.");
                	}
                	throw new IOException("Packet flood");
                }
            }
        } catch (Throwable ex) {
            BungeeMain.console(connection.getAddress() + " tried to exploit CustomPayload: " + ex.getMessage());
            
            event.getSender().disconnect(TextComponent.fromLegacyText(
            "§cVocê não pode enviar muitos pacotes para o servidor."));
	    
            event.getReceiver().disconnect(TextComponent.fromLegacyText(
            "§cVocê não pode enviar muitos pacotes para o servidor."));
            
        	if (!banned.contains(proxiedPlayer.getName())) {
    		    banned.add(proxiedPlayer.getName());
     		    ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), 
     		    		"kick " + proxiedPlayer.getName() + " Nosso servidor é completamente protegido contra isto, tente novamente.");
        	}
            
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        CHANNELS_REGISTERED.remove(event.getPlayer());
        PACKET_USAGE.remove(event.getPlayer());
    }
    
    private boolean elapsed(long from, long required) {
        return from == -1L || System.currentTimeMillis() - from > required;
    }
}