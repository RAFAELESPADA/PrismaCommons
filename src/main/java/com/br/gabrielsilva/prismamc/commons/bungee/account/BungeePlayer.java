package com.br.gabrielsilva.prismamc.commons.bungee.account;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.manager.permissions.PermissionsManager;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.base.BasePunishment;
import com.br.gabrielsilva.prismamc.commons.core.base.PunishmentType;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@Getter @Setter
public class BungeePlayer {

	private String nick, address, clan;
	private UUID uniqueId;
	private long timestamp, lastReport;
	private Groups grupo;
	private boolean inStaffChat, inClanChat, viewReports, viewStaffChat, online;
	private BasePunishment mute;
	
	//screenshare
	private UUID screenShareWith;
	private boolean puxou;
	
	public BungeePlayer(String nick, UUID uniqueId, String address) {
		setNick(nick);
		setAddress(address);
		
		setUniqueId(uniqueId);
		setScreenShareWith(null);
		
		setGrupo(Groups.MEMBRO);
		setClan("Nenhum");
		
		setInClanChat(false);
		setInStaffChat(false);
		setViewReports(false);
		setOnline(false);
		setPuxou(false);
		setViewStaffChat(true);
		
		setTimestamp(System.currentTimeMillis());
		setMute(new BasePunishment(nick, "", PunishmentType.MUTE));
	}
	
	public boolean inScreenShare() {
		return getScreenShareWith() != null;
	}
	
	public boolean isStaffer() {
		return getGrupo().getNivel() >= Groups.TRIAL.getNivel();
	}
	
	public void quitScreenShare() {
		setScreenShareWith(null);
		setPuxou(false);
	}
	
	public void quit() {
		setInClanChat(false);
		setInStaffChat(false);
		setOnline(false);
		setClan("Nenhum");
		setScreenShareWith(null);
		setPuxou(false);
		
		BungeeMain.runAsync(() -> {
			MySQLManager.updateValue(DataCategory.PRISMA_PLAYER, DataType.LAST_LOGGED_OUT, 
					String.valueOf(System.currentTimeMillis()), getNick());
		});
	}
	
	public boolean podeReportar() {
		if (getLastReport() + TimeUnit.SECONDS.toMillis(30) > System.currentTimeMillis()) {
			return false;
		}
		return true;
	}
	
	public boolean isValidSession() {
		if (getTimestamp() + TimeUnit.HOURS.toMillis(12) > System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public void resetPunishment() {
		setMute(new BasePunishment(getNick(), "", PunishmentType.MUTE));
	}

	public void handleLogin(ProxiedPlayer proxiedPlayer) {
		setOnline(true);
		
		try {
			getMute().load();
		} catch (Exception e) {}
		
		BungeeMain.runAsync(() -> {
			final Groups tag = Groups.getFromString(MySQLManager.getString("accounts", "nick", getNick(), "grupo"));
			setGrupo(tag);
			
			if (tag.getNivel() >= Groups.TRIAL.getNivel()) {
				handlePreferences();
			}
			
			BungeeMain.runLater(() -> {
				PermissionsManager.injectPermissions(proxiedPlayer, tag.getNome());
				handleLogs(proxiedPlayer);
			}, 1, TimeUnit.SECONDS);
		});
	}
	
	private void handleLogs(ProxiedPlayer proxiedPlayer) {
		try {
			PreparedStatement preparedStatement = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM accounts where nick='"+getNick()+"'");
			ResultSet result = preparedStatement.executeQuery();
			if (!result.next()) {
				result.close();
				preparedStatement.close();
				return;
			}
			
			Long first_logged_in = Long.valueOf(result.getString(DataType.FIRST_LOGGED_IN.getField()));
			if (first_logged_in == 0L) {
				first_logged_in = System.currentTimeMillis();
				
				MySQLManager.updateValue(DataCategory.PRISMA_PLAYER, DataType.FIRST_LOGGED_IN, 
						String.valueOf(first_logged_in), getNick());
				
				if (BungeeMain.isValid(proxiedPlayer)) {
					BungeeClient.sendPacketToServer(proxiedPlayer, 
							new PacketUpdateField(getNick(), "CustomPlayer", "FirstLogin", String.valueOf(first_logged_in)));
				}
			}
			
			MySQLManager.updateValue(DataCategory.PRISMA_PLAYER, DataType.LAST_LOGGED_IN, 
					String.valueOf(System.currentTimeMillis()), getNick());
			
			if (BungeeMain.isValid(proxiedPlayer)) {
				BungeeClient.sendPacketToServer(proxiedPlayer, 
						new PacketUpdateField(getNick(), "CustomPlayer", "LastLoggedIn", String.valueOf(System.currentTimeMillis())));
			}
			
			result.close();
			preparedStatement.close();
		} catch (SQLException ex) {
			BungeeMain.console("Ocorreu um erro ao tentar carregar os logs de um jogador. -> " + ex.getLocalizedMessage());
		}
	}

	private void handlePreferences() {
		try {
			PreparedStatement preparedStatement = Core.getMySQL().getConexão().prepareStatement("SELECT * FROM preferences where nick='"+getNick()+"'");
			ResultSet result = preparedStatement.executeQuery();
			if (!result.next()) {
				result.close();
				preparedStatement.close();
				return;
			}
			setInStaffChat(result.getBoolean("staffchat"));
			setViewReports(result.getBoolean("reports"));
			result.close();
			preparedStatement.close();
		} catch (SQLException ex) {
			BungeeMain.console("Ocorreu um erro ao tentar carregar as preferencias de um jogador. -> " + ex.getLocalizedMessage());
		}
	}
}