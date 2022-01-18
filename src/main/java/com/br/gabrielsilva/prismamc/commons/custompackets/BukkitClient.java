package com.br.gabrielsilva.prismamc.commons.custompackets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPacketsHandler;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.exception.HandlePacketException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.Getter;

public class BukkitClient {

	@Getter
	private static final String CHANNEL = "CustomPacketsHandler";
	private static boolean initialized = false;
	
	@Getter
	private static BukkitPacketsHandler handler;
	
	public static void initChannel(Plugin plugin) {
		if (initialized) {
			return;
		}
		initialized = true;

		BukkitPackets.registerPackets();
		
		handler = new BukkitPacketsHandler();
		
		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
	    Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
	    Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, new PluginMessageListener() {
	    	
			public void onPluginMessageReceived(String channel, Player player, byte[] message) {
				if (!channel.equalsIgnoreCase(CHANNEL)) {
					return;
				}
				final ByteArrayDataInput data = ByteStreams.newDataInput(message);
				final String packetName = data.readUTF();
				
				final BukkitPackets packet = BukkitPackets.getPacket(packetName);

				if (packet != null) {
					CustomPacketsManager.addPacketsReceiveds();
					
					packet.read(data);
					
					try {
						packet.handle(getHandler());
					} catch (HandlePacketException ex) {
						BukkitMain.console("Ocorreu um erro ao tentar lidar com o pacote '"+packet.getPacketName() + "' -> " + ex.getLocalizedMessage());
					}
				}
			}
		});
	}
	
	public static void sendPacket(BungeePackets packet) {
		sendPacket(getFirstPlayer(), packet);
	}

	public static void sendPacket(Player sender, BungeePackets packet) {
		if (sender == null) {
			return;
		}
		ByteArrayDataOutput	packetData = ByteStreams.newDataOutput();
		packet.write(packetData);
		sender.sendPluginMessage(BukkitMain.getInstance(), CHANNEL, packetData.toByteArray());
		CustomPacketsManager.addPacketsSended();
	}
	
	private static Player getFirstPlayer() {
		Player finded = null;
		for (Player onlines : Bukkit.getOnlinePlayers()) {
			 if (onlines.isOnline()) {
				 finded = onlines;
				 break;
			 }
		}
		return finded;
	}
}