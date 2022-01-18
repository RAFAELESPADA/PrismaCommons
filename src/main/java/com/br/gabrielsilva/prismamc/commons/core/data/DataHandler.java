package com.br.gabrielsilva.prismamc.commons.core.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import com.br.gabrielsilva.prismamc.commons.core.connections.redis.RedisAPI;
import com.br.gabrielsilva.prismamc.commons.core.data.Data;
import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.data.type.LoaderType;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

@Getter @Setter
public class DataHandler {

	private Map<DataType, Data> datas;
	private Map<DataCategory, Boolean> loadedCategories;
	private String name;
	
	public DataHandler(String name) {
		setName(name);
		setDatas(new ConcurrentHashMap<>());
		setLoadedCategories(new ConcurrentHashMap<>());
		
		for (DataCategory dataCategory : DataCategory.values()) {
			 getLoadedCategories().put(dataCategory, false);
		}
	}
	
	public boolean isCategoryLoaded(DataCategory category) {
		return this.loadedCategories.get(category);
	}

	public Data getData(DataType dataType) {
		return this.datas.get(dataType);
	}
	
	public int getInt(DataType dataType) {
		return this.datas.get(dataType).getInt();
	}
	
	public String getString(DataType dataType) {
		return this.datas.get(dataType).getString();
	}
	
	public Boolean getBoolean(DataType dataType) {
		return this.datas.get(dataType).getBoolean();
	}
	
	public Long getLong(DataType dataType) {
		return this.datas.get(dataType).getLong();
	}
	
	public boolean categoryHasCache(DataCategory dataCategory) {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			return jedis.exists(dataCategory.name().toLowerCase() + ":" + getName().toLowerCase());
		} catch (Exception ex) {
			return false;
		}
	}
	
	public boolean load(DataCategory category) {
		
		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() {
				LoaderType loaderType = LoaderType.MYSQL;
				
				try {
					if (RedisAPI.categoryHasCache(getName(), category)) {
						loaderType = LoaderType.REDIS;
					}
				} catch (Exception e) {}
				
				if (ServerOptions.isDebug()) {
					BukkitMain.console(category.name() + " de " + getName() + " sendo carregado com -> " + loaderType.getName());
				}
				
				for (DataType dataType : category.getDataTypes()) {
					 datas.put(dataType, new Data(dataType.getDefaultValue()));
				}
				
				try {
					if (loaderType == LoaderType.MYSQL) {
					    loadMySQL(category, category.getDataTypes());
					} else {
					    loadRedis(category, category.getDataTypes());
					}
					
					loadedCategories.put(category, true);
					return true;
				} catch (Exception ex) {
					BukkitMain.console("Ocorreu um erro ao tentar carregar a categoria '"+ category +"' ("+loaderType.getName() + ") -> " + ex.getLocalizedMessage());
					loadedCategories.put(category, false);
					return false;
				}	
			}
		};
		
		try {
			return callable.call();
		} catch (Exception ex) {
			loadedCategories.put(category, false);
			return false;
		}
	}
	
	public void updateValues(DataCategory category, DataType... dataTypes) {
		updateValues(category, false, dataTypes);
	}
	
	public void updateValues(DataCategory category, boolean updateCache, DataType... dataTypes) {
		BukkitMain.runAsync(() -> {
			try {
				PreparedStatement s = prepareStatament(createUpdateStringQuery(category, dataTypes));
				s.execute();
				s.close();
			} catch (SQLException ex) {
				BukkitMain.console("Ocorreu um erro ao tentar atualizar um valor(MySQL)"
						+ " da categoria " + category.name() + " de " + name + " -> " + ex.getLocalizedMessage());
			}
			
			if (!updateCache) {
				return;
			}
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				 if (RedisAPI.categoryHasCache(jedis, getName(), category)) {
					 for (DataType current : dataTypes) {
						  if (current == DataType.PERMS) {
							  RedisAPI.modifyValue(getName(), category, current, StringUtils.formatArrayToString(getData(current).getList(), true));
						  } else {
							 Object data = getData(current).getObject();
							 RedisAPI.modifyValue(jedis, getName(), category, current, String.valueOf(data));
						  }
					 }
				 }
			} catch (Exception ex) {
				BukkitMain.console("Ocorreu um erro ao tentar atualizar um valor(Redis)"
						+ " da categoria " + category.name() + " de " + name + " -> " + ex.getLocalizedMessage());
			}
		});
	}
	
	public void saveCategory(DataCategory... categories) {
		BukkitMain.runAsync(() -> {
			
			int inx = 0,
					max = categories.length;
			
			long started = 0L;
			
			while (inx < max) {
				DataCategory current = categories[inx];
				
				if (ServerOptions.isDebug()) {
					started = System.currentTimeMillis();
					BukkitMain.console("Salvando " + current.name() + " de " + name + "...");
				}
				
				try {
					PreparedStatement s = prepareStatament(createUpdateStringQuery(current, current.getDataTypes()));
					s.execute();
					s.close();
					
					if (ServerOptions.isDebug()) {
						BukkitMain.console(current.name() + " de " + name + " salvo em -> " + DateUtils.getElapsed(started));
					}
				} catch (SQLException ex) {
					BukkitMain.console("Ocorreu um erro ao tentar salvar " + current.name() + " de " + name + " -> " + ex.getLocalizedMessage());
				}
				
				sendCache(current);
				inx++;
			}		
		});
	}
	
	public void sendCache(DataCategory... categories) {
		int inx = 0,
				max = categories.length;
		
		while (inx < max) {
			DataCategory current = categories[inx];
			
			long started = System.currentTimeMillis();
			
			if (ServerOptions.isDebug()) {
				BukkitMain.console("Salvando o cache " + current.name() + " de " + name + "...");
			}
			
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				Map<String, String> hash = createHashRedis(current, current.getDataTypes());
			
				jedis.hmset(current.name().toLowerCase() + ":" + getName().toLowerCase(), hash);
				jedis.expire(current.name().toLowerCase() + ":" + getName().toLowerCase(), (60 * 10));
				
				if (ServerOptions.isDebug()) {
					BukkitMain.console("Cache " + current.name() + " de " + name + " salvo em -> " + DateUtils.getElapsed(started));
				}
				
				hash.clear();
				hash = null;
			} catch (Exception ex) {
				BukkitMain.console("Ocorreu um erro ao tentar salvar o cache " + current.name() + " de " + name + " -> " + ex.getLocalizedMessage());
			}
			inx++;
		}
	}
	
	private Map<String, String> createHashRedis(DataCategory category, DataType[] dataTypes) {
		Map<String, String> hash = new HashMap<>();
		
		int inx = 0,
				max = dataTypes.length;
		
		while (inx < max) {
			DataType current = dataTypes[inx];
			
			boolean fromListToString = false;

			if (current == DataType.PERMS) {
				fromListToString = true;
			}
			
			if (fromListToString) {
				hash.put(current.getField().toLowerCase(), StringUtils.formatArrayToStringWithoutSpace(getData(current).getList(), true));
			} else {
				Object data = getData(current).getObject();
				hash.put(current.getField().toLowerCase(), String.valueOf(data));
			}
			inx++;
		}
		
		return hash;
	}
	
	private void loadRedis(DataCategory category, DataType[] dataTypes) throws Exception {
		long started = System.currentTimeMillis();
		
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
		 	 Map<String, String> hash = jedis.hgetAll(category.name().toLowerCase() + ":" + getName().toLowerCase());
				
			 for (DataType dataType : dataTypes) {
				  datas.put(dataType, new Data(dataType.getDefaultValue()));
					 
				  if (dataType == DataType.PERMS) {
					  getData(dataType).setValue(StringUtils.reformuleFormattedWithoutSpace(hash.get(dataType.getField().toLowerCase())));
				  } else {
					  if (dataType.getClassExpected().equalsIgnoreCase("int")) {
						  getData(dataType).setValue(Integer.valueOf(hash.get(dataType.getField().toLowerCase())));
					  } else if (dataType.getClassExpected().equalsIgnoreCase("boolean")) {
					 	  getData(dataType).setValue(Boolean.valueOf(hash.get(dataType.getField().toLowerCase())));
					  } else if (dataType.getClassExpected().equalsIgnoreCase("long")) {
						 getData(dataType).setValue(Long.valueOf(hash.get(dataType.getField().toLowerCase()))); 
					  } else {
						  getData(dataType).setValue(hash.get(dataType.getField().toLowerCase()));
				 	  }
				  }
			}
			
			hash.clear();
			hash = null;
			
			if (ServerOptions.isDebug()) {
				BukkitMain.console(category.name() + " de " + getName() + " carregado em -> " + DateUtils.getElapsed(started) + " (Redis)");
			}
		}
	}
	
	public PreparedStatement prepareStatament(String sql) throws SQLException {
		return Core.getMySQL().getConexão().prepareStatement(sql);
	}

	private String createInsertIntoStringQuery(DataCategory category, DataType... dataTypes) {
		StringBuilder b = new StringBuilder();
		b.append("INSERT INTO `" + category.getTableName() + "` (");

		int inx = 0;
		int max = dataTypes.length;

		b.append("`nick`");
		
		while (inx < max) {
			DataType current = dataTypes[inx];

			b.append(", `" + current.getField() + "`");

			inx++;
		}

		b.append(") VALUES (");

		inx = 0;

		b.append("'" + name + "'");
		
		while (inx < max) {
			DataType current = dataTypes[inx];

			Object value = current.getDefaultValue();

			if (value == null) {
				value = "";
			}
			
			if (current == DataType.PERMS) {
				value = "";
			}
			
			if (current.getClassExpected().equalsIgnoreCase("Boolean")) {
				if (value.toString().equalsIgnoreCase("true")) {
					value = 1;
				} else {
					value = 0;
				}
			}
			
			b.append(", '" + value + "'");

			inx++;
		}

		b.append(");");
		return b.toString();
	}
	
	private String createUpdateStringQuery(DataCategory category, DataType... dataTypes) {
		StringBuilder b = new StringBuilder();
		
		b.append("UPDATE `" + category.getTableName() + "` SET ");
		
		int inx = 0,
				max = dataTypes.length;
		
		while (inx < max) {
			DataType current = dataTypes[inx];
			
			Object data = getData(current).getObject();
			
			if (current == DataType.PERMS) {
				if (inx == 0) {
					b.append("`" + current.getField() + "`='" + StringUtils.formatArrayToString(getData(current).getList(), true) + "'");
				} else {
				    b.append(", `" + current.getField() + "`='" + StringUtils.formatArrayToString(getData(current).getList(), true) + "'");
				}
				
			} else {
				data = convert(current.getClassExpected(), data);
				
				if (inx == 0) {
					b.append("`" + current.getField() + "`='" + data + "'");
				} else {
				    b.append(", `" + current.getField() + "`='" + data + "'");
				}
			}
			inx++;
		}
		
		b.append(" WHERE `nick`='" + name + "';");
		return b.toString();
	}
	
	private static Object convert(String classExpected, Object value) {
		Object data = value;
		if (classExpected.equalsIgnoreCase("Boolean")) {
			if (data.toString().equalsIgnoreCase("true")) {
				data = 1;
			} else {
				data = 0;
			}
		} else if (classExpected.equalsIgnoreCase("String")) {
			data = value.toString();
			if (data == null) {
				data = "";
			}
			if (data.toString().isEmpty()) {
				data = "";
			}
		}
		return data;
	}

	private void loadMySQL(DataCategory category, DataType... dataTypes) throws SQLException {
		long started = System.currentTimeMillis();
		
		PreparedStatement s = prepareStatament(
				"SELECT * FROM `" + category.getTableName() + "` WHERE `nick`='" + name + "';");
		ResultSet r = s.executeQuery();

		if (r.next()) {
			int inx = 0;
			int max = dataTypes.length;

			while (inx < max) {
				DataType current = dataTypes[inx];
				boolean fromStringToList = false;

				if (current == DataType.PERMS) {
					fromStringToList = true;
				}

				getData(current).setValue(
						getDataFromResultSet(r, current.getField(), current.getClassExpected(), fromStringToList));
				inx++;
			}
			if (ServerOptions.isDebug()) {
				BukkitMain.console(category.name() + " de " + getName() + " carregado em -> " + DateUtils.getElapsed(started) + " (MySQL)");
			}
		} else {
			if (category.create()) {
				started = System.currentTimeMillis();
				
				if (ServerOptions.isDebug()) {
					BukkitMain.console("Criando " + category.name() + " de " + name + "...");
				}
			    PreparedStatement p = prepareStatament(createInsertIntoStringQuery(category, dataTypes));
			    p.execute();
			    p.close();
				if (ServerOptions.isDebug()) {
					BukkitMain.console(category.name() + " de " + name + " criada em -> " + DateUtils.getElapsed(started));
				}
			}
		}

		r.close();
		s.close();
	}
	
	private Object getDataFromResultSet(ResultSet resultSet, String fieldName, String classExpected,
			boolean fromStringToList) throws SQLException {
		if (classExpected.equalsIgnoreCase("String"))
			return (fromStringToList ? StringUtils.reformuleFormattedWithoutSpace(resultSet.getString(fieldName))
					: resultSet.getString(fieldName));
		else if (classExpected.equalsIgnoreCase("Int"))
			return resultSet.getInt(fieldName);
		else if (classExpected.equalsIgnoreCase("Long"))
			return resultSet.getLong(fieldName);
		else if (classExpected.equalsIgnoreCase("Boolean"))
			return resultSet.getBoolean(fieldName);
		return "0";
	}
	
	public void saveLoadeds() {
		for (DataCategory dataCategory : DataCategory.values()) {
			 if (isCategoryLoaded(dataCategory)) {
				 saveCategory(dataCategory);
			 }
		}
	}
}