package com.br.gabrielsilva.prismamc.commons.core.group;

import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;

import lombok.Getter;

@Getter
public enum Groups {
	
	RANDOM(new Tag("random", "", "§7§k", true)),
	
	//default tag
	MEMBRO(new Tag("MEMBRO", "", "§7", false)),
	EVENTO(new Tag("EVENTO", "EVENTO", "§e", true)),
	
	SAPPHIRE(new Tag("SAPPHIRE", "SAPPHIRE", "§3")),
	LEGEND(new Tag("LEGEND", "LEGEND", "§6")),
	RUBY(new Tag("RUBY", "RUBY", "§c")),
	STREAMER(new Tag("STREAMER", "STREAMER", "§3")),
	BETA(new Tag("BETA", "BETA", "§1")),
	DESIGNER(new Tag("DESIGNER", "DESIGNER", "§2", "DZN")),
	YOUTUBER(new Tag("YOUTUBER", "YT", "§b", "YT")),
	YOUTUBER_PLUS(new Tag("YOUTUBER+", "YT+", "§3", "YT+")),
	BUILDER(new Tag("BUILDER", "BUILDER", "§e", "CONSTRUTOR")),
	INVESTIDOR(new Tag("INVESTIDOR", "INVEST", "§a", "INVEST")),
	TRIAL(new Tag("TRIAL", "TRIAL", "§d", "TRIAL-MOD")),
	MOD(new Tag("MOD", "MOD", "§5", "MODERADOR")),
	INVESTIDOR_PLUS(new Tag("INVESTIDOR+", "INVEST+", "§2", "INVEST+", "INVST+")),
	MOD_GC(new Tag("MODGC", "MODGC", "§5", "MODGC")),
	MOD_PLUS(new Tag("MOD+", "MOD+", "§5", "MODPLUS")),
	GERENTE(new Tag("GERENTE", "GERENTE", "§c", "GER")),
	ADMIN(new Tag("ADMIN", "ADMIN", "§c", "ADMINISTRADOR")),
	DIRETOR(new Tag("DIRETOR", "DIRETOR", "§4", "DIR")),
	DONO(new Tag("DONO", "DONO", "§4", "owner")),
	DEV(new Tag("DEV", "DEV", "§3", "DEVELOPER"));

	private Tag tag;
	
	Groups(Tag tag) {
		this.tag = tag;
	}
	
	public int getNivel() {
		return getTag().getNivel();
	}
	
	public String getCor() {
		return getTag().getColor();
	}
	
	public String getNome() {
		return getTag().getNome();
	}
	
	public boolean isExclusiva() {
		return getTag().isExclusiva();
	}

	public static Groups getFromString(String name) {
		Groups groupFinded = MEMBRO;
		
		for (Groups groups : values()) {
			 if (groups.getTag().getNome().equalsIgnoreCase(name)) {
				 groupFinded = groups;
				 break;
			 }
			 for (String aliases : groups.getTag().getAliases()) {
				  if (aliases.equalsIgnoreCase(name)) {
					  groupFinded = groups;
					  break;
				  }
			 }
		}
		return groupFinded;
	}
	
	public static Groups getFromNivel(int nivel) {
		Groups groupFinded = MEMBRO;
		
		for (Groups groups : values()) {
			 if (groups.getTag().getNivel() == nivel) {
				 groupFinded = groups;
				 break;
			 }
		}
		
		return groupFinded;
	}
	
	public static Boolean existGrupo(String grupo) {
		boolean existe = false;
		
		for (Groups groups : values()) {
			 if (groups.getTag().getNome().toLowerCase().equals(grupo.toLowerCase())) {
				 existe = true;
				 break;
			 }
			 for (String aliases : groups.getTag().getAliases()) {
				  if (aliases.equalsIgnoreCase(grupo)) {
					  existe = true;
					  break;
				  }
			 }
		}
		return existe;
	}
}