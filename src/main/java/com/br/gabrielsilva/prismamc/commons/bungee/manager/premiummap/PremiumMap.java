package com.br.gabrielsilva.prismamc.commons.bungee.manager.premiummap;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PremiumMap {

	private boolean premium;
	private String nick;
	private UUID UUID;

	public PremiumMap(UUID uuid, String nick, boolean premium) {
		this.nick = nick;
		this.premium = premium;
		this.UUID = uuid;
	}
}