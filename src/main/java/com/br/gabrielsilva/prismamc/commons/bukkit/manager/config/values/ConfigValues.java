package com.br.gabrielsilva.prismamc.commons.bukkit.manager.config.values;

import java.util.Arrays;

import org.bukkit.Material;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.register.PlayerCommand;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import lombok.Getter;

public class ConfigValues {
	
	public static void checkAndApply(CONFIGS config) {
		if (config == CONFIGS.DANO) {
			boolean hasChange = false;
			
			for (ValuesDano values : ValuesDano.values()) {
				 if (!BukkitMain.getManager().getConfigManager().getDanoConfig().contains(values.getKey())) {
					 BukkitMain.getManager().getConfigManager().getDanoConfig().set(values.getKey(), convertValue(values.getValue(), values.getClassExpected()));
					 hasChange = true;
				 }
			}
			
			for (Material materiais : Material.values()) {
				 String name = materiais.name().toLowerCase();
				 if ((name.contains("sword")) || (name.contains("pickaxe")) || (name.contains("spade")) || (name.contains("axe"))) {
					 if (!BukkitMain.getManager().getConfigManager().getDanoConfig().contains("dano.materiais." + name)) {
						 hasChange = true;
						 BukkitMain.getManager().getConfigManager().getDanoConfig().set("dano.materiais." + name, 1.0D);
					 }
				 }
			}
			
			if (hasChange) {
				BukkitMain.getManager().getConfigManager().saveDanoConfig();
			}
		} else if (config == CONFIGS.GLOBAL_CONFIG) {
			boolean hasChange = false;
			
			for (ValuesGlobalConfig values : ValuesGlobalConfig.values()) {
				 if (!BukkitMain.getManager().getConfigManager().getGlobalConfig().contains(values.getKey())) {
					 BukkitMain.getManager().getConfigManager().getGlobalConfig().set(values.getKey(), convertValue(values.getValue(), values.getClassExpected()));
					 hasChange = true;
				 }
			}
			
			if (hasChange) {
				BukkitMain.getManager().getConfigManager().saveGlobalConfig();
			}
			apply(config);
		} else if (config == CONFIGS.BAUS) {
			boolean hasChange = false;
			
			if (!BukkitMain.getManager().getConfigManager().getBausConfig().contains("feast.itens")) {
				BukkitMain.getManager().getConfigManager().getBausConfig().set("feast.itens", Arrays.asList(
					"Material:DIAMOND_HELMET,Quantidade:1,Durabilidade:0,Chance:20",
					"Material:DIAMOND_CHESTPLATE,Quantidade:1,Durabilidade:0,Chance:19",
					"Material:DIAMOND_LEGGINGS,Quantidade:1,Durabilidade:0,Chance:19",
					"Material:DIAMOND_BOOTS,Quantidade:1,Durabilidade:0,Chance:20",
					"Material:DIAMOND_SWORD,Quantidade:1,Durabilidade:0,Chance:19",
					"Material:DIAMOND_AXE,Quantidade:1,Durabilidade:0,Chance:19",
					"Material:DIAMOND_PICKAXE,Quantidade:1,Durabilidade:0,Chance:19",
					"Material:FLINT_AND_STEEL,Quantidade:1,Durabilidade:0,Chance:26",
					"Material:WATER_BUCKET,Quantidade:1,Durabilidade:0,Chance:37",
					"Material:LAVA_BUCKET,Quantidade:1,Durabilidade:0,Chance:35",
					"Material:BOW,Quantidade:1,Durabilidade:0,Chance:40",
						
					"Material:POTION,Quantidade:1,Durabilidade:16418,Chance:32",
					"Material:POTION,Quantidade:1,Durabilidade:16424,Chance:32",
					"Material:POTION,Quantidade:1,Durabilidade:16420,Chance:33",
					"Material:POTION,Quantidade:1,Durabilidade:16428,Chance:31",
					"Material:POTION,Quantidade:1,Durabilidade:16426,Chance:30",
					"Material:POTION,Quantidade:1,Durabilidade:16417,Chance:32",
					"Material:POTION,Quantidade:1,Durabilidade:16419,Chance:33",
					"Material:POTION,Quantidade:1,Durabilidade:16421,Chance:34",
					
					"Material:COOKED_BEEF,Quantidade:30,Durabilidade:0,Chance:40",
					"Material:ENDER_PEARL,Quantidade:12,Durabilidade:0,Chance:31",
					"Material:ENDER_PEARL,Quantidade:5,Durabilidade:0,Chance:21",
					"Material:GOLDEN_APPLE,Quantidade:15,Durabilidade:0,Chance:30",
					"Material:EXP_BOTTLE,Quantidade:32,Durabilidade:0,Chance:42",
					"Material:WEB,Quantidade:28,Durabilidade:0,Chance:13",
					"Material:TNT,Quantidade:28,Durabilidade:0,Chance:39",
					"Material:DIAMOND,Quantidade:12,Durabilidade:0,Chance:13",
					"Material:ARROW,Quantidade:31,Durabilidade:0,Chance:31"
					));
						
					hasChange = true;
			}
			
			if (!BukkitMain.getManager().getConfigManager().getBausConfig().contains("minifeast.itens")) {
				BukkitMain.getManager().getConfigManager().getBausConfig().set("minifeast.itens", Arrays.asList(
					"Material:IRON_SWORD,Quantidade:1,Durabilidade:0,Chance:28",
					"Material:EXP_BOTTLE,Quantidade:15,Durabilidade:0,Chance:45",
					"Material:IRON_PICKAXE,Quantidade:1,Durabilidade:0,Chance:28",
					"Material:POTION,Quantidade:1,Durabilidade:16393,Chance:30",
					"Material:POTION,Quantidade:1,Durabilidade:16417,Chance:30",
					"Material:INK_SACK,Quantidade:35,Durabilidade:3,Chance:39",
					"Material:COOKED_BEEF,Quantidade:35,Durabilidade:0,Chance:39",
					"Material:ENDER_PEARL,Quantidade:10,Durabilidade:0,Chance:45",
					"Material:MONSTER_EGG,Quantidade:7,Durabilidade:95,Chance:34",
					"Material:BONE,Quantidade:12,Durabilidade:0,Chance:35",
					"Material:WEB,Quantidade:10,Durabilidade:0,Chance:25",
					"Material:TNT,Quantidade:10,Durabilidade:0,Chance:25",
					"Material:LAVA_BUCKET,Quantidade:1,Durabilidade:0,Chance:10"
					));
				
				hasChange = true;
			}
			
			if (hasChange) {
				BukkitMain.getManager().getConfigManager().saveBausConfig();
			}
		} else if (config == CONFIGS.PERMISSOES) {
			boolean hasChange = false;
			
			for (Groups tags : Groups.values()) {
				 if (tags.isExclusiva()) {
					 continue;
				 }
				 if (!tags.equals(Groups.DEV) && (!tags.equals(Groups.RANDOM))) {
					 if (!BukkitMain.getManager().getConfigManager().getPermsConfig().contains("permissions." + tags.getNome().toLowerCase())) {
						 if (tags.equals(Groups.MEMBRO)) {
							 BukkitMain.getManager().getConfigManager().getPermsConfig().set("permissions." + tags.getNome().toLowerCase(), Arrays.asList("tag.membro"));
						 } else {
							 BukkitMain.getManager().getConfigManager().getPermsConfig().set("permissions." + tags.getNome().toLowerCase(), Arrays.asList("tag.membro", "tag." + tags.getNome().toLowerCase()));
						 }
						 hasChange = true;
					 }
				 }
			}
			
			if (hasChange) {
				BukkitMain.getManager().getConfigManager().savePermsConfig();
			}
		}
	}
	
	private static Object convertValue(String value, String classExpected) {
		if (classExpected.equalsIgnoreCase("String"))
			return value;
		else if (classExpected.equalsIgnoreCase("Integer"))
			return Integer.valueOf(value);
		else if (classExpected.equalsIgnoreCase("Long"))
			return Long.valueOf(value);
		else if (classExpected.equalsIgnoreCase("Boolean"))
			return Boolean.valueOf(value);
		else if (classExpected.equalsIgnoreCase("List")) {
			return StringUtils.reformuleFormatted(value);
		} else if (classExpected.equalsIgnoreCase("Double")) {
			return Double.valueOf(value);
		}
		return value;
	}

	private static void apply(CONFIGS config) {
		if (config == CONFIGS.GLOBAL_CONFIG) {
			Core.getRedis().setHostname(getStringByGlobalConfig("Redis.Host"));
			Core.getRedis().setSenha(getStringByGlobalConfig("Redis.Senha"));
			Core.getRedis().setPorta(getIntegerByGlobalConfig("Redis.Porta"));
			
			Core.getMySQL().setHost(getStringByGlobalConfig("MySQL.Host"));
			Core.getMySQL().setDatabase(getStringByGlobalConfig("MySQL.Database"));
			Core.getMySQL().setUsuario(getStringByGlobalConfig("MySQL.Usuario"));
			Core.getMySQL().setSenha(getStringByGlobalConfig("MySQL.Senha"));
			Core.getMySQL().setPorta(getStringByGlobalConfig("MySQL.Porta"));
			
			BukkitMain.setServerAddress(getStringByGlobalConfig("ip_central"));
			
			for (String fakes : BukkitMain.getManager().getConfigManager().getGlobalConfig().getStringList("Servidor.Config.Fakes")) {
				 PlayerCommand.fakesRandom.add(fakes);
			}
		}
	}
	
	private static String getStringByGlobalConfig(String type) {
		return BukkitMain.getManager().getConfigManager().getGlobalConfig().getString(type);
	}
	
	private static Integer getIntegerByGlobalConfig(String type) {
		return BukkitMain.getManager().getConfigManager().getGlobalConfig().getInt(type);
	}
	
	public static Double getDoubleByGlobalConfig(String type) {
		return BukkitMain.getManager().getConfigManager().getGlobalConfig().getDouble(type);
	}

	public enum CONFIGS {
		GLOBAL_CONFIG, BAUS, PERMISSOES, DANO;
	}
	
	public enum ValuesDano {
		
		CRITICAL("dano.critical", "false", "Boolean"),
		CRITICAL_CHANCE("dano.critical_chance", "15", "Integer"),
		HITDELAY("dano.hitdelay", "true", "Boolean"),
		HITDELAY_TIME("dano.hitdelay_time", "500", "Long");
		
		@Getter
		private String key, value, classExpected;
		
		private ValuesDano(String key, String value, String classExpected) {
			this.key = key;
			this.value = value;
			this.classExpected = classExpected;
		}
	}
	
	public enum ValuesGlobalConfig {
		
		LICENCA("Licenca", "000000", "String"),
		
		MYSQL_HOST("MySQL.Host", "localhost", "String"),
		MYSQL_PORTA("MySQL.Porta", "3306", "String"),
		MYSQL_DATABASE("MySQL.Database", "database", "String"),
		MYSQL_USUARIO("MySQL.Usuario", "root", "String"),
		MYSQL_SENHA("MySQL.Senha", "senha", "String"),
		
		REDIS_HOST("Redis.Host", "localhost", "String"),
		REDIS_SENHA("Redis.Senha", "senha", "String"),
		REDIS_PORTA("Redis.Porta", "6379", "Integer"),
		
		IP_CENTRAL("ip_central", "127.0.0.1", "String"),
		
		SERVIDOR_CONFIG_FAKES("Servidor.Config.Fakes", "fakerandom1,fakerandom2,fakerandom3", "List"),
		
		KNOCKBACK_GROUND_HORIZONTAL_MULTIPLIER("Servidor.Config.Knockback.ground_horizontal_multiplier", "0.45", "Double"),
		KNOCKBACK_GROUND_VERTICAL_MULTIPLIER("Servidor.Config.Knockback.ground_vertical_multiplier", "0.35", "Double"),
		KNOCKBACK_AIR_HORIZONTAL_MULTIPLIER("Servidor.Config.Knockback.air_horizontal_multiplier", "0.40", "Double"),
		KNOCKBACK_AIR_VERTICAL_MULTIPLIER("Servidor.Config.Knockback.air_vertical_multiplier", "0.3", "Double"),
		KNOCKBACK_SPRINT_MULTIPLIER_HORIZONTAL("Servidor.Config.Knockback.sprint_multiplier_horizontal", "2", "Double"),
		KNOCKBACK_SPRINT_MULTIPLIER_VERTICAL("Servidor.Config.Knockback.sprint_multiplier_vertical", "1.3", "Double"),
		KNOCKBACK_SPRINT_YAW_FACTOR("Servidor.Config.Knockback.sprint_yaw_factor", "0.5", "Double"),
		;
		
		@Getter
		private String key, value, classExpected;
		
		private ValuesGlobalConfig(String key, String value, String classExpected) {
			this.key = key;
			this.value = value;
			this.classExpected = classExpected;
		}
	}
}