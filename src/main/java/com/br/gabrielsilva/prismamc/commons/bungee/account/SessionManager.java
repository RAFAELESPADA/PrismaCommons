package com.br.gabrielsilva.prismamc.commons.bungee.account;

import java.util.HashMap;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class SessionManager {

	private HashMap<String, BungeePlayer> sessions;
	
	public SessionManager() {
		this.sessions = new HashMap<>();
	}
	
	public void removeSession(ProxiedPlayer proxiedPlayer) {
		sessions.remove(proxiedPlayer.getName().toLowerCase());
	}
	
	public void removeSession(String nick) {
		sessions.remove(nick.toLowerCase());
	}
	
	public void addSession(ProxiedPlayer proxiedPlayer) {
		if (hasSession(proxiedPlayer)) {
			return;
		}
		sessions.put(proxiedPlayer.getName().toLowerCase(), new BungeePlayer(
				proxiedPlayer.getName(), proxiedPlayer.getUniqueId(), 
				proxiedPlayer.getAddress().getAddress().getHostAddress().toString().replace("/", "")));
	}
	
	public BungeePlayer getSession(ProxiedPlayer proxiedPlayer) {
		return sessions.get(proxiedPlayer.getName().toLowerCase());
	}

	public boolean hasSession(ProxiedPlayer proxiedPlayer) {
		return sessions.containsKey(proxiedPlayer.getName().toLowerCase());
	}
	
	public boolean hasSession(String nick) {
		return getSessions().containsKey(nick.toLowerCase());
	}
	
	public BungeePlayer getSession(String nick) {
		return sessions.get(nick.toLowerCase());
	}
	
	public HashMap<String, BungeePlayer> getSessions() {
		return sessions;
	}
}