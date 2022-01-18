package com.br.gabrielsilva.prismamc.commons.custompackets;

import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.BukkitPackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePackets;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.BungeePacketsHandler;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeClient {

	@Getter
	private static final String CHANNEL = "CustomPacketsHandler";
	
	@Getter
	private static BungeePacketsHandler handler;
	
	private static boolean initialized = false;
	
	public static void initChannel(Plugin plugin) {
		if (initialized) {
			return;
		}
		initialized = true;
		
		BungeePackets.registerPackets();
		
		handler = new BungeePacketsHandler();
		
		plugin.getProxy().registerChannel(CHANNEL);
	}
	
	public static void sendPacketToServer(ServerInfo serverInfo, BukkitPackets packet) {
		if (serverInfo == null) {
			return;
		}
		ByteArrayDataOutput	packetData = ByteStreams.newDataOutput();
		packet.write(packetData);
		serverInfo.sendData(CHANNEL, packetData.toByteArray());
		CustomPacketsManager.addPacketsSended();
	}
	
	public static void sendPacketToServer(ProxiedPlayer target, BukkitPackets packet) {
		if (target == null) {
			return;
		}
		if (target.getServer() == null) {
			return;
		}
		
		ByteArrayDataOutput	packetData = ByteStreams.newDataOutput();
		packet.write(packetData);
		target.getServer().sendData(CHANNEL, packetData.toByteArray());
		CustomPacketsManager.addPacketsSended();
	}
	
	public static void sendPacketToAllServers(BukkitPackets packet) {
		for (ServerInfo servers : ProxyServer.getInstance().getServers().values()) {
			 if (servers == null) {
				 continue;
			 }
			 if (servers.getPlayers().size() < 1) {
				 continue;
			 }
			 ByteArrayDataOutput packetData = ByteStreams.newDataOutput();
			 packet.write(packetData);
			 servers.sendData(CHANNEL, packetData.toByteArray());
			 CustomPacketsManager.addPacketsSended();
		}
	}
	
	public static ServerInfo getServerByName(String name) {
		if (name.contains("0")) {
			name = name.replaceAll("0", "");
		}
		
		ServerInfo server = null;
		
		for (ServerInfo servers : ProxyServer.getInstance().getServers().values()) {
			 if (servers.getName().equalsIgnoreCase(name)) {
				 server = servers;
				 break;
			 }
		}
		
		return server;
	}
}