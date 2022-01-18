package com.br.gabrielsilva.prismamc.commons.core.clan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import lombok.Getter;

public class ClanManager {

	@Getter
	private HashMap<String, Clan> clans;

	public ClanManager() {
		this.clans = new HashMap<>();
	}
	
	public void loadClan(String nome) {
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM clans WHERE (nome='" + nome + "');");
			ResultSet result = preparedStatament.executeQuery();
			boolean existe = result.next();
			
			if (existe) {
				String dono = result.getString("dono"), tag = result.getString("tag"), 
						membrosFormatted = result.getString("membros"), adminsFormatted = result.getString("admins");
				
				int elo = result.getInt("elo"),
						participantes = result.getInt("participantes");
				
				List<String> membros = StringUtils.reformuleFormatted(membrosFormatted),
						admins = StringUtils.reformuleFormatted(adminsFormatted);
				
				if (!hasClanData(nome)) {
					putClan(nome.toLowerCase(), new Clan(nome, dono, tag, elo, participantes, membros, admins));
				}
			}
			result.close();
			preparedStatament.close();
		} catch (SQLException ex) {
			for (int i = 0; i < 3; i++) {
				 Core.console("§cOcorreu um erro ao tentar carregar um clan... -> " + ex.getLocalizedMessage());
			}
		}
	}
	
	public void saveClan(String name) {
		if (!hasClanData(name)) {
			return;
		}
		
		BungeeMain.runAsync(() -> {
			final Clan clan = getClan(name);
			
			try {
				PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
					"UPDATE clans SET elo=?, membros=?, admins=?, dono=?, participantes=? WHERE nome='"+name+"'");
					
				preparedStatament.setInt(1, clan.getElo());
				preparedStatament.setString(2, clan.getMembrosFormatado());
				preparedStatament.setString(3, clan.getAdminsFormatado());
				preparedStatament.setString(4, clan.getDono());
				preparedStatament.setInt(5, clan.getParticipantes());
				preparedStatament.execute();
					
				preparedStatament.close();
			} catch (SQLException ex) {
				Core.console("Ocorreu um erro ao tentar salvar as estatistícas do clan " + name + " > " + ex.getLocalizedMessage());
			}
		});
	}
	
	public void removeClanData(String nome) {
		this.clans.remove(nome);
	}
	
	public Clan getClan(String name) {
		return this.clans.get(name.toLowerCase());
	}
	
	public void putClan(String name, Clan clan) {
		this.clans.put(name.toLowerCase(), clan);
	}
	
	public void unloadClan(String name) {
		this.clans.remove(name.toLowerCase());
	}
	
	public boolean hasClanData(String nome) {
		return this.clans.containsKey(nome.toLowerCase());
	}
}