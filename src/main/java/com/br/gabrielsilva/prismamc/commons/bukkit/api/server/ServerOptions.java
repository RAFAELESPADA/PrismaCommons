package com.br.gabrielsilva.prismamc.commons.bukkit.api.server;

import lombok.Getter;
import lombok.Setter;

public class ServerOptions {

	@Getter @Setter
	public static boolean chat, PvP, dano, debug, doubleCoins, doubleXP;

	public static void init() {
		chat = true;
		PvP = true;
		dano = true;
		debug = false;
		doubleCoins = false;
		doubleXP = false;
	}
}