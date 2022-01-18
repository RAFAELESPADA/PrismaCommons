package com.br.gabrielsilva.prismamc.commons.core.clan;

import java.util.ArrayList;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.PluginInstance;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Clan {

	private String nome, dono, tag;
	private int elo, participantes;
	private List<String> membros, admins;
	private List<Object> onlines;
	private long timestamp;
	
	public Clan(String nome, String dono, String tag, int elo, int participantes, List<String> membros, List<String> admins) {
		setNome(nome);
		setDono(dono);
		setTag(tag);
		setElo(elo);
		
		setParticipantes(participantes);
		
		setMembros(new ArrayList<>());
		setAdmins(new ArrayList<>());
		setOnlines(new ArrayList<>());

		for (String m : membros) {
			 getMembros().add(m);
		}
		for (String a : admins) {
			 getAdmins().add(a);
		}
		
		setTimestamp(System.currentTimeMillis());
	}
	
	public List<String> getAllParticipantes() {
		List<String> participantes = new ArrayList<>();
		
		for (String membros : getMembros())
			participantes.add(membros);
		
		for (String admins : getAdmins())
			participantes.add(admins);
		
		participantes.add(getDono());
		return participantes;
	}

	public void removeOnline(Object p) {
		if (getOnlines().contains(p)) {
		    getOnlines().remove(p);
		}
	}
	
	public void addMembro(String nome) {
		if (!getMembros().contains(nome)) {
			getMembros().add(nome);
			
			setParticipantes(getAllParticipantes().size());
		}
	}
	
	public void addAdmin(String nome) {
		if (!getAdmins().contains(nome)) {
			getAdmins().add(nome);
			
			setParticipantes(getAllParticipantes().size());
		}
	}
	
	public void removeMembro(String nome) {
		if (getMembros().contains(nome)) {
			getMembros().remove(nome);
			
			setParticipantes(getAllParticipantes().size());
		}
	}
	
	public void removeAdmin(String nome) {
		if (getAdmins().contains(nome)) {
			getAdmins().remove(nome);
			
			setParticipantes(getAllParticipantes().size());
		}
	}
	
	public void addOnline(Object p) {
		if (!getOnlines().contains(p)) {
		    getOnlines().add(p);
		}
	}
	
	public String getMembrosFormatado() {
		return StringUtils.formatArrayToStringWithoutSpace(this.membros, false);
	}
	
	public String getAdminsFormatado() {
		return StringUtils.formatArrayToStringWithoutSpace(this.admins, false);
	}
	
	public boolean hasPerm(String nick) {
		if (getDono().equalsIgnoreCase(nick)) {
			return true;
		}
		if (getAdmins().contains(nick)) {
			return true;
		}
		return false;
	}
	
	public void sendMessage(String message) {
		if (Core.getPluginInstance() == PluginInstance.SPIGOT) {
			for (Object players : onlines) {
				 org.bukkit.entity.Player player = (org.bukkit.entity.Player) players;
				 player.sendMessage(message);
			}
		} else {
			for (Object players : onlines) {
				 net.md_5.bungee.api.connection.ProxiedPlayer player = (net.md_5.bungee.api.connection.ProxiedPlayer) players;
				 player.sendMessage(message);
			}
		}
	}
	
	public void removeELO() {
		removeELO(1);
	}
	
	public void removeELO(int valor) {
		if (getElo() - valor <= 0) {
			setElo(0);
			return;
		}
		
		setElo(getElo() - valor);
		
		BungeeMain.runAsync(() -> {
			Core.getClanManager().saveClan(getNome());
		});
	}
	
	public void addELO(int valor) {
		setElo(getElo() + valor);
		
		BungeeMain.runAsync(() -> {
			Core.getClanManager().saveClan(getNome());
		});
	}
}