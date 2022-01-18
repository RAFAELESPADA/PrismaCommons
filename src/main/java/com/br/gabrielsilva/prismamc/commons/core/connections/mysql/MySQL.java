package com.br.gabrielsilva.prismamc.commons.core.connections.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.connections.BaseConnection;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MySQL extends BaseConnection {

	private Connection conexão;
	private String host, porta, database, usuario, senha;
	
	public void openConnection() {
    	long started = System.currentTimeMillis();
	    try {
	    	setConexão(DriverManager.getConnection(
	    			"jdbc:mysql://" + "116.203.228.35" + ":" + 3306 + "/" + "s9964_contas" + "?autoReconnect=true&verifyServerCertificate=false&useSSL=false", "u9964_vUhe0xojZD", "Sz!7Kz4S^xGWJTkyeoKJbogt"));
            createTables();
	    } catch (SQLException e) {
	    	Core.console("Ocorreu um erro ao tentar estabelecer a conexão com o MySQL. > " + e.getLocalizedMessage());
	    } finally {
	    	if (isConnected()) {
	    		Core.console("Conexão com o MySQL estabelecida em: §a" + DateUtils.getElapsed(started));
	    	}
	    }
	}

	public void closeConnection() {
		try {
			if (conexão != null) {
				conexão.close();
				Core.console("Conexão com o MySQL encerrada.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean isConnected() {
		if (conexão == null) {
			return false;
		}
	    return true;
	}
	
    public void createTables() {
    	
    	for (DataCategory categorys : DataCategory.values()) {
			try {
				PreparedStatement s = getConexão().prepareStatement(categorys.buildTableQuery());
				s.execute();
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
				BukkitMain.console("Ocorreu um erro ao tentar verificar uma tabela.");
				BukkitMain.desligar();
				return;
			}
    	}
    	
    	synchronized (this) {
    		MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS premium_map(nick varchar(20), uuid varchar(100), premium boolean);");
    		
    		MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS bans("
    		+ "nick varchar(20), IP varchar(100), motivo varchar(200), baniu varchar(40), tempo varchar(40));");
    		
    		MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS mutes("
    		+ "nick varchar(20), motivo varchar(200), mutou varchar(40), tempo varchar(40));");
    		
    	   	MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS clans("
    	   	+ "nome varchar(20), tag varchar(20), dono varchar(20), membros varchar(1000), admins varchar(1000), "
    	   	+ "elo int, participantes int);");
        
         	MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS playerSkin(nick varchar(16), skin varchar(16));");
    	   	
    	   	MySQLManager.executeUpdateAsync("CREATE TABLE IF NOT EXISTS skins(nick varchar(16), value text, signature text, timestamp text);");
		}
    }
}