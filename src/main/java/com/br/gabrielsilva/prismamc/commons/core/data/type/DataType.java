package com.br.gabrielsilva.prismamc.commons.core.data.type;

import lombok.Getter;
import java.util.ArrayList;

@Getter
public enum DataType {

	//PRISMA PLAYER
	FAKE("", "String", "fake", "VARCHAR(20)"),
	CLAN("Nenhum", "String", "clan", "VARCHAR(20)"),
	DOUBLEXP(0, "Int", "doubleXP", "VARCHAR(100)"),
	DOUBLEXP_TIME(0L, "Long", "doubleXP_time", "VARCHAR(100)"),
	DOUBLECOINS(0, "Int", "doubleCoins", "int"),
	DOUBLECOINS_TIME(0L, "Long", "doubleCoins_time", "VARCHAR(100)"),
	GRUPO("Membro", "String", "grupo", "VARCHAR(20)"),
	GRUPO_TIME(0L, "Long", "grupo_time", "VARCHAR(100)"),
	PERMS(new ArrayList<>(), "String", "perms", "VARCHAR(1000)"),
	XP(0, "Int", "xp", "int"),
	COINS(0, "Int", "coins", "int"),
	CASH(0, "Int", "cash", "int"),
	LAST_IP("", "String", "last_ip", "VARCHAR(20)"),
	FIRST_LOGGED_IN(0L, "Long", "first_logged_in", "VARCHAR(100)"),
	LAST_LOGGED_IN(0L, "Long", "last_logged_in", "VARCHAR(100)"),
	LAST_LOGGED_OUT(0L, "Long", "last_logged_out", "VARCHAR(100)"),
	
	//REGISTRO
	REGISTRO_SENHA("", "String", "senha", "VARCHAR(150)"),
	REGISTRO_DATA("", "String", "registrado", "VARCHAR(25)"),
	
	//PREFERENCIAS
	TELL(true, "Boolean", "tell", "boolean"),
	REPORTS(true, "Boolean", "reports", "boolean"),
	JOIN_ADMIN(false, "Boolean", "join_admin", "boolean"),
	STAFFCHAT(false, "Boolean", "staffchat", "boolean"),
	
	//GLADIATOR
	GLADIATOR_WINS(0, "Int", "wins", "int"),
	GLADIATOR_LOSES(0, "Int", "loses", "int"),
	GLADIATOR_WINSTREAK(0, "Int", "winstreak", "int"),
	GLADIATOR_MAXWINSTREAK(0, "Int", "maxWinstreak", "int"),
	
	//HG
	HG_KILLS(0, "Int", "kills", "int"),
	HG_DEATHS(0, "Int", "deaths", "int"),
	HG_WINS(0, "Int", "wins", "int"),
	
	//KITPVP
	PVP_KILLS(0, "Int", "kills", "int"),
	PVP_DEATHS(0, "Int", "deaths", "int"),
	PVP_KILLSTREAK(0, "Int", "killstreak", "int"),
	PVP_MAXKILLSTREAK(0, "Int", "maxkillstreak", "int"),
	ONEVSONE_WINS(0, "Int", "1v1_wins", "int"),
	ONEVSONE_LOSES(0, "Int", "1v1_loses", "int");
	
	private Object defaultValue;
	private String classExpected, field, tableType;
	
	private DataType(Object defaultValue, String classExpected, String field, String tableType) {
		this.defaultValue = defaultValue;
		this.field = field;
		this.classExpected = classExpected;
		this.tableType = tableType;
	}
	
	public static DataType getDataTypeByField(String field) {
		DataType finded = null;
		
		for (DataType datas : DataType.values()) {
			 if (datas.getField().equalsIgnoreCase(field)) {
				 finded = datas;
				 break;
			 }
		}
		
		return finded;
	}
}