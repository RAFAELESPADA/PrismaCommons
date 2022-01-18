package com.br.gabrielsilva.prismamc.commons.core.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BasePunishment {

	private String conta, punidoPor, motivo, IP;
	private Long punishmentTime;
	private boolean aplicado;
	private PunishmentType punishmentType;
	
	public BasePunishment(String nick, String IP, PunishmentType punishmentType) {
		this.conta = nick;
		this.aplicado = false;
		this.IP = IP;
		this.punidoPor = "";
		this.motivo = "";
		this.punishmentTime = 0L;
		this.punishmentType = punishmentType;
	}
	
	public void banir() {
		if (getPunishmentType() != PunishmentType.BAN) {
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"INSERT INTO bans(nick, IP, motivo, baniu, tempo) VALUES (?, ?, ?, ?, ?)");
				
			preparedStatament.setString(1, getConta());
			preparedStatament.setString(2, getIP());
			preparedStatament.setString(3, getMotivo());
			preparedStatament.setString(4, getPunidoPor());
			preparedStatament.setString(5, String.valueOf(getPunishmentTime()));
			
			preparedStatament.executeUpdate();
			preparedStatament.close();
		} catch (SQLException ex) {
			Core.console(PluginMessages.OCORREU_UM_ERRO2.replace("{0}", "banir um jogador").replace("{1}", ex.getLocalizedMessage()));
			return;
		}
	}
	
	public void mute() {
		if (getPunishmentType() != PunishmentType.MUTE) {
			return;
		}
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"INSERT INTO mutes(nick, motivo, mutou, tempo) VALUES (?, ?, ?, ?)");
				
			preparedStatament.setString(1, getConta());
			preparedStatament.setString(2, getMotivo());
			preparedStatament.setString(3, getPunidoPor());
			preparedStatament.setString(4, String.valueOf(getPunishmentTime()));
			
			preparedStatament.executeUpdate();
			preparedStatament.close();
		} catch (SQLException ex) {
			Core.console("Ocorreu um erro ao tentar mutar um jogador. > " + ex.getLocalizedMessage());
			return;
		}
	}
	
	public void unban() {
		if (getPunishmentType() != PunishmentType.BAN) {
			return;
		}
		
		MySQLManager.executeUpdateAsync("DELETE FROM bans WHERE nick='" + getConta() + "';");
		
		if (!getIP().isEmpty()) {
			MySQLManager.executeUpdateAsync("DELETE FROM bans WHERE IP='" + getIP() + "';");
		}
	}
	
	public void unmute() {
		if (getPunishmentType() != PunishmentType.MUTE) {
			return;
		}
		MySQLManager.executeUpdate("DELETE FROM mutes WHERE nick='" + getConta() + "';");
	}
	
	public void load() throws Exception {
		if (punishmentType.equals(PunishmentType.BAN)) {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM bans WHERE ip='"+this.IP+"'");
				
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				preparedStatament.close();
				result.close();
				
				preparedStatament = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM bans WHERE nick='"+conta+"'");
				result = preparedStatament.executeQuery();
				if (!result.next()) {
					preparedStatament.close();
					result.close();
					return;
				}
				this.IP = result.getString("IP");
				this.motivo = result.getString("motivo");
				this.punidoPor = result.getString("baniu");
				this.punishmentTime = Long.valueOf(result.getString("tempo"));
				this.aplicado = true;
				preparedStatament.close();
				result.close();
				return;
			}
			this.IP = result.getString("IP");
			this.motivo = result.getString("motivo");
			this.punidoPor = result.getString("baniu");
			this.punishmentTime = Long.valueOf(result.getString("tempo"));
			this.aplicado = true;
			preparedStatament.close();
			result.close();
		} else if (punishmentType.equals(PunishmentType.MUTE)) {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM mutes WHERE nick='"+conta+"';");
			
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				preparedStatament.close();
				result.close();
				return;
			}
			this.motivo = result.getString("motivo");
			this.punidoPor = result.getString("mutou");
			this.punishmentTime = Long.valueOf(result.getString("tempo"));
			this.aplicado = true;
			preparedStatament.close();
			result.close();
		} 
	}

	public boolean isExpired() {
		return !isPermanent() && punishmentTime < System.currentTimeMillis();
	}

	public boolean isPermanent() {
		return punishmentTime == 0;
	}
	
	public boolean isApplied() {
		return aplicado;
	}
}