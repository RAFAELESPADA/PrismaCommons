package com.br.gabrielsilva.prismamc.commons.bukkit.custom.packets.utils;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Hit {

	private Vector direction;
	private boolean sprintKb;
	private int kbEnchantLevel;
	private Player attacker;
  
	public Hit(Vector direction, boolean sprintKb, int kbEnchantLevel, Player attacker) {
		this.direction = direction;
		this.sprintKb = sprintKb;
		this.kbEnchantLevel = kbEnchantLevel;
		this.attacker = attacker;
	}
}