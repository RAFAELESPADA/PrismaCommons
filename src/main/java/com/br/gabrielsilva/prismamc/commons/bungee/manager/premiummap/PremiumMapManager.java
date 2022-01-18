package com.br.gabrielsilva.prismamc.commons.bungee.manager.premiummap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.google.common.base.Charsets;
import lombok.Getter;

public class PremiumMapManager {

	@Getter
	private HashMap<String, PremiumMap> premiumMaps;

	@Getter
	private List<String> changedNicks;
	
	public void init() {
		this.premiumMaps = new HashMap<>();
		this.changedNicks = new ArrayList<>();
	}

	public void loadAll() {
		BungeeMain.runAsync(() -> {
			try {
				PreparedStatement preparedStatament = Core.getMySQL().getConexão()
						.prepareStatement("SELECT * FROM premium_map");
				ResultSet result = preparedStatament.executeQuery();

				while (result.next()) {
					final boolean premium = result.getBoolean("premium");
					final UUID uuidProfile = UUID.fromString(result.getString("uuid"));
					final String nick = result.getString("nick");
					getPremiumMaps().put(nick.toLowerCase(), new PremiumMap(uuidProfile, nick, premium));
				}

				preparedStatament.close();
				result.close();

				BungeeMain.setLoaded(true);
			} catch (SQLException ex) {
				Core.desligar();
			}
		});
	}

	public boolean load(String nick) {
		long started = System.currentTimeMillis();
		
		boolean premium = false;

		UUID uuidProfile = null;
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM `premium_map` WHERE nick='" + nick + "'");
			
			ResultSet result = preparedStatament.executeQuery();
			if (result.next()) {
				premium = result.getBoolean("premium");
				uuidProfile = UUID.fromString(result.getString("uuid"));
			} else {
				UUID uuid = Core.getUuidFetcher().getUUID(nick);
				premium = (uuid == null ? false : true);
				
				uuidProfile = (uuid != null ? uuid : UUID.nameUUIDFromBytes(("OfflinePlayer:" + nick).getBytes(Charsets.UTF_8)));
				
				if (premium && hasChangedNick(uuidProfile, nick)) {
					result.close();
					preparedStatament.close();
					changedNicks.add(nick);
					return true;
				}
				
				if (!changedNicks.contains(nick)) {
					PreparedStatement insert = Core.getMySQL().getConexão()
							.prepareStatement("INSERT INTO premium_map(nick, uuid, premium) VALUES (?, ?, ?)");

					insert.setString(1, nick);
					insert.setString(2, String.valueOf(uuidProfile));
					insert.setBoolean(3, premium);

					insert.executeUpdate();
					insert.close();
				}
			}
			result.close();
			preparedStatament.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
		getPremiumMaps().put(nick.toLowerCase(), new PremiumMap(uuidProfile, nick, premium));
		return true;
	}
	
	public boolean hasChangedNick(UUID uuid, String newNick) {
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM `premium_map` WHERE uuid='" + uuid.toString() + "'");
			
			ResultSet result = preparedStatament.executeQuery();
			if (result.next()) {
				updateNick(uuid, result.getString("nick"), newNick);
				result.close();
				preparedStatament.close();
				return true;
			}
			result.close();
			preparedStatament.close();
			return false;
		} catch (SQLException ex) {
			return false;
		}
	}
	
	private String[] tabelasToUpdate = {"accounts", "hungergames", "kitpvp", "gladiator", "premium_map", "bans", "mutes"};
	
	private void updateNick(UUID uuid, String oldNick, String newNick) {
		getPremiumMaps().put(newNick.toLowerCase(), new PremiumMap(uuid, newNick, true));
		
		if (!oldNick.equalsIgnoreCase(newNick)) {
			BungeeMain.console("[PremiumMap] detectou a mudança de Nick de " + oldNick + " para " + newNick + "!");
			
			if (containsMap(oldNick)) {
				getPremiumMaps().remove(oldNick);
			}
			
			final String clanName = MySQLManager.getString("accounts", "nick", oldNick, "clan");
			if (!clanName.equalsIgnoreCase("Nenhum")) {
				if (!Core.getClanManager().hasClanData(clanName)) {
					Core.getClanManager().loadClan(clanName);
				}
				
				Clan clan = Core.getClanManager().getClan(clanName);
				if (clan.getDono().equalsIgnoreCase(oldNick)) {
					clan.setDono(newNick);
				}
				
				if (clan.getAdmins().contains(oldNick)) {
				    clan.removeAdmin(oldNick);
				    clan.addAdmin(newNick);
				} else {
					clan.removeMembro(oldNick);
					clan.addMembro(newNick);
				}
				
				Core.getClanManager().saveClan(clanName);
			}
			
			for (String table : tabelasToUpdate) {
				 BungeeMain.runAsync(() -> {
					 if (MySQLManager.contains(table, "nick", oldNick)) {
						 MySQLManager.atualizarStatus(table, "nick", oldNick, newNick);
					 }
				 });
			}
			if (BungeeMain.getManager().getSessionManager().hasSession(oldNick)) {
				BungeeMain.getManager().getSessionManager().removeSession(oldNick);
			}
		}
	}
	
	public void removePremiumMap(String nickTarget) {
		getPremiumMaps().remove(nickTarget.toLowerCase());
	}

	public int getCrackedAmount() {
		int crackedAmount = 0;
		
		for (PremiumMap premiumMap : getPremiumMaps().values()) {
			 if (!premiumMap.isPremium()) {
				 crackedAmount++;
			 }
		}
		
		return crackedAmount;
	}
	
	public int getPremiumAmount() {
		int premiumAmont = 0;
		
		for (PremiumMap premiumMap : getPremiumMaps().values()) {
			 if (premiumMap.isPremium()) {
				 premiumAmont++;
			 }
		}
		
		return premiumAmont;
	}
	
	public PremiumMap getPremiumMap(String nick) {
		return getPremiumMaps().get(nick.toLowerCase());
	}

	public boolean containsMap(String nick) {
		return getPremiumMaps().containsKey(nick.toLowerCase());
	}

	public void clear() {
		this.premiumMaps.clear();
	}
}