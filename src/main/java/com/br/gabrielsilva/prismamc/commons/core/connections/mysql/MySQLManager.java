package com.br.gabrielsilva.prismamc.commons.core.connections.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.PluginInstance;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;

public class MySQLManager {
	
	public static int getPlayerPositionRanking(String name) {
		return getPlayerPosition("accounts", "xp", "nick", name);
	}
	
	public static int getPlayerPosition(String table, String field, String where, String name) {
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
					"SELECT COUNT(*) FROM "+table+" WHERE "+field+" > (SELECT "+field+" from "+table+" WHERE "+where+"='"+name+"')");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				result.close();
				preparedStatament.close();
				return 0;
			}
			int pos = Integer.valueOf(result.getString("COUNT(*)")) + 1;
			result.close();
			preparedStatament.close();
			return pos;
		} catch (SQLException ex) {
			Core.console("Ocorreu um erro ao tentar obter a posição de um Clan. -> " + ex.getLocalizedMessage());
			return 0;
		}
	}
	
	public static void deleteFromTable(String table, String where, String nome) {
		if (contains(table, where, nome)) {
			executeUpdateAsync("DELETE FROM "+table+" WHERE "+where+"='" + nome + "';");
		}
	}
	
	public static void deleteFromTable(String table, String nome) {
		if (contains(table, "nome", nome)) {
			executeUpdateAsync("DELETE FROM "+table+" WHERE nome='" + nome + "';");
		}
	}
	
	public static synchronized ResultSet executeQuery(String query) {
		try {
			return Core.getMySQL().getConexão().createStatement().executeQuery(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static synchronized void executeUpdateAsync(String update) {
		try {
			Statement s = Core.getMySQL().getConexão().createStatement();
			s.executeUpdate(update);
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void executeUpdate(String update) {
		try {
			Statement s = Core.getMySQL().getConexão().createStatement();
			s.executeUpdate(update);
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateValue(DataCategory dataCategory, DataType dataType, String value, String nick) {
		if (Core.getPluginInstance() == PluginInstance.SPIGOT) {
			BukkitMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + dataCategory.getTableName() + " SET " + dataType.getField() + "='" + value +"' where nick='"+nick+"';");
			});
		} else {
			BungeeMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + dataCategory.getTableName() + " SET " + dataType.getField() + "='" + value +"' where nick='"+nick+"';");
			});
		}
	}
	
	public static void atualizarStatus(String tabela, String status, String onde, String onde2, String valor) {
		if (Core.getPluginInstance() == PluginInstance.SPIGOT) {
			BukkitMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + tabela + " SET " + status + "='" + valor +"' where '"+onde+"'='"+onde2+"';");
			});
		} else {
			BungeeMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + tabela + " SET " + status + "='" + valor +"' where '"+onde+"'='"+onde2+"';");
			});
		}
	}
	
	public static void atualizarStatus(String tabela, String status, String nick, String valor) {
		if (Core.getPluginInstance() == PluginInstance.SPIGOT) {
			BukkitMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + tabela + " SET " + status + "='" + valor +"' where nick='"+nick+"';");
			});
		} else {
			BungeeMain.runAsync(() -> {
				executeUpdateAsync("UPDATE " + tabela + " SET " + status + "='" + valor +"' where nick='"+nick+"';");
			});
		}
	}
	
	public static synchronized boolean contains(String tabela, String onde, String ondeTwo) {
		try {
			ResultSet set = executeQuery("SELECT * FROM `" + tabela + "` WHERE `" + onde + "`='" + ondeTwo + "';");
			if (!set.next()) {
				set.close();
				return false;
			}
			set.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public static synchronized String getString(String tabela, String onde, String ondeTwo, String obter) {
		try {
			ResultSet set = executeQuery("SELECT * FROM `" + tabela + "` WHERE `" + onde + "`='" + ondeTwo + "';");
			if (!set.next()) {
				set.close();
				return "N/A";
			}
			String obtido = set.getString(obter);
			set.close();
			return obtido;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "N/A";
	}
	
	public static synchronized boolean getBoolean(String tabela, String onde, String ondeTwo, String obter) {
		try {
			ResultSet set = executeQuery("SELECT * FROM `" + tabela + "` WHERE `" + onde + "`='" + ondeTwo + "';");
			if (!set.next()) {
				set.close();
				return false;
			}
			boolean obtido = set.getBoolean(obter);
			set.close();
			return obtido;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}