package com.br.gabrielsilva.prismamc.commons.core.server;

import lombok.Getter;

@Getter
public enum ServerType {

	LOGIN("Login", null, 3),
	LOBBY("Lobby", new String[] {"hub"}, 3),
	KITPVP("KitPvP", new String[] {"pvp"}, 3),
	HG("HG", new String[] {"hungergames", "hardcoregames"}, 3), 
	EVENTO("Evento", null, 3),
	GLADIATOR("Gladiator", new String[] {"glad"}, 3),
	SCREENSHARE("ScreenShare", new String[] {"ss"}, 3),
	UNKNOWN("Unknown", null, 3);

	private String name;
	private String[] aliases;
	private int secondsToStabilize;
	
	ServerType(String name, String[] aliases, int secondsToStabilize) {
		this.name = name;
		this.aliases = aliases;
		this.secondsToStabilize = secondsToStabilize;
	}
	
	public boolean containsAlias(String serverName) {
		if (getAliases() == null) {
			return false;
		}
		boolean finded = false;
		for (String alias : getAliases()) {
			 if (alias.equalsIgnoreCase(serverName)) {
				 finded = true;
				 break;
			 }
		}
		return finded;
	}
	
	public static ServerType resolveServer(String serverName) {
		ServerType finded = ServerType.UNKNOWN;
		
		for (ServerType servers : ServerType.values()) {
			 if (servers.getName().equalsIgnoreCase(serverName)) {
				 finded = servers;
				 break;
			 }
			 if (servers.containsAlias(serverName)) {
				 finded = servers;
				 break;
			 } 
		}
		return finded;
	}
}