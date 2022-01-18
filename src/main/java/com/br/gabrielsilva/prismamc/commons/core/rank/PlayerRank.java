package com.br.gabrielsilva.prismamc.commons.core.rank;

import lombok.Getter;

@Getter
public enum PlayerRank {

	UNRANKED("§f", "-", "Unranked", 500),
    PRIMARY("§a", "\u2630", "Primary", 1500),
    ADVANCED("§e", "\u2632", "Advanced", 5500),
    EXPERT("§1", "\u2637", "Expert", 12000),
    SILVER("§7", "✶", "Silver", 18000),
    GOLD("§6", "\u2729", "Gold", 25000),
    DIAMOND("§b", "\u2726", "Diamond", 35000),
    EMERALD("§2", "\u2725", "Emerald", 50000),
    CRYSTAL("§9", "\u2749", "Crystal", 65000),
    SAPPHIRE("§3", "\u2741", "Sapphire", 80000),
    ELITE("§5", "\u2739", "Elite", 90000),
    MASTER("§c", "\u272B", "Master", 120000),
    LEGENDARY("§4", "\u272A", "Legendary", 150000);
	
	private String simbolo, nome, cor;
	private int max;
	
	private PlayerRank(String cor, String simbolo, String nome, int max) {
		this.nome = nome;
		this.cor = cor;
		this.simbolo = simbolo;
		this.max = max;
	}
	
	public int getMin() {
		int min = 0;
		if (this.ordinal() > 0) {
			min = PlayerRank.values()[this.ordinal() - 1].getMax();
		}
		return min;
	}
	
	public static PlayerRank getRanking(String ligaNome) {
		PlayerRank liga = PlayerRank.UNRANKED;
		for (PlayerRank rank : values()) {
			 if (rank.getNome().toLowerCase().equals(ligaNome.toLowerCase())) {
				 liga = rank;
				 break;
			 }
		}
		return liga;
	}
	
	public PlayerRank getPreviousLeague() {
		return this == UNRANKED ? UNRANKED : PlayerRank.values()[ordinal() - 1];	
	}
	
	public PlayerRank getNextLeague() {
		return this == LEGENDARY ? LEGENDARY : PlayerRank.values()[ordinal() + 1];	
	}
	
	public static PlayerRank getRanking(int xp) {
		if (xp >= LEGENDARY.getMax()) {
			return LEGENDARY;
		}
		PlayerRank liga = UNRANKED;
		for (PlayerRank rank : values()) {
			 if (xp <= rank.max && xp > rank.getMin()) {
				 liga = rank;
			 }
		}
		return liga;
	}
}