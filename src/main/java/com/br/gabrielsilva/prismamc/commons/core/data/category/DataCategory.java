package com.br.gabrielsilva.prismamc.commons.core.data.category;

import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import lombok.Getter;

import static com.br.gabrielsilva.prismamc.commons.core.data.type.DataType.*;

@Getter
public enum DataCategory {

	PRISMA_PLAYER("accounts", FAKE, CLAN, DOUBLEXP, DOUBLEXP_TIME, DOUBLECOINS, DOUBLECOINS_TIME, 
			GRUPO, GRUPO_TIME, PERMS, XP, COINS, CASH, LAST_IP, FIRST_LOGGED_IN, LAST_LOGGED_IN, LAST_LOGGED_OUT),
	
	PREFERENCIAS("preferences", TELL, REPORTS, JOIN_ADMIN, STAFFCHAT),
	
	KITPVP("kitpvp", PVP_KILLS, PVP_DEATHS, PVP_KILLSTREAK, PVP_MAXKILLSTREAK, ONEVSONE_WINS, ONEVSONE_LOSES),
	
	HUNGER_GAMES("hungergames", HG_KILLS, HG_DEATHS, HG_WINS),
	
	REGISTER("registros", REGISTRO_SENHA, REGISTRO_DATA),
	
	GLADIATOR("gladiator", GLADIATOR_WINS, GLADIATOR_LOSES, GLADIATOR_WINSTREAK, GLADIATOR_MAXWINSTREAK);
	
	private String tableName;
	private DataType[] dataTypes;
	
	private DataCategory(String tableName, DataType... dataTypes) {
		this.tableName = tableName;
		this.dataTypes = dataTypes;
	}

	public boolean create() {
		if (this == REGISTER) {
			return false;
		}
		return true;
	}
	
	public static DataCategory getCategoryByName(String name) {
		DataCategory finded = DataCategory.PRISMA_PLAYER;
		
		for (DataCategory datas : DataCategory.values()) {
			 if (datas.getTableName().equalsIgnoreCase(name)) {
				 finded = datas;
				 break;
			 }
		}
		
		return finded;
	}
	
	public String buildTableQuery() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS `" + getTableName() + "` (");
		
		sb.append("nick varchar(20)");
		
		int j = 0;
		int max = dataTypes.length;
		
		while (j < max) {
			DataType next = dataTypes[j];
			sb.append(", `" + next.getField() + "` " + next.getTableType());
			++j;
		}
		return sb.append(");").toString();
	}
}