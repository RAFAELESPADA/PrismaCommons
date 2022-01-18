package com.br.gabrielsilva.prismamc.commons.core.connections.redis;

import java.util.Map;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;

import redis.clients.jedis.Jedis;

public class RedisAPI {
	
	public static void removeCacheIfExist(String nick, DataCategory dataCategory) throws Exception {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			if (jedis.exists(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase())) {
				jedis.del(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase());
			}
		}
	}
	
	public static boolean categoryHasCache(String nick, DataCategory dataCategory) throws Exception {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			if (jedis.exists(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase())) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public static void modifyValue(String nick, DataCategory dataCategory, DataType dataType, String value) throws Exception {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			if (jedis.exists(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase())) {
				Map<String, String> hash = jedis.hgetAll(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase());
				
				if (hash.containsKey(dataType.getField().toLowerCase())) {
					hash.put(dataType.getField().toLowerCase(), value);
				}
				
				jedis.hmset(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase(), hash);
				jedis.expire(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase(), (60 * 8));
				
				hash.clear();
				hash = null;
			}
		}
	}
	
	public static boolean categoryHasCache(Jedis jedis, String nick, DataCategory dataCategory) throws Exception {
		if (jedis.exists(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase())) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void modifyValue(Jedis jedis, String nick, DataCategory dataCategory, DataType dataType, String value) throws Exception {
		Map<String, String> hash = jedis.hgetAll(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase());
				
		if (hash.containsKey(dataType.getField().toLowerCase())) {
			hash.put(dataType.getField().toLowerCase(), value);
		}
				
		jedis.hmset(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase(), hash);
		jedis.expire(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase(), (60 * 8));
		
		hash.clear();
		hash = null;
	}
	
	public static void cleanCachePlayer(String nick) throws Exception {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			 for (DataCategory dataCategory : DataCategory.values()) {
				  if (jedis.exists(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase())) {
					  jedis.del(dataCategory.name().toLowerCase() + ":" + nick.toLowerCase());
				  }
			 }
		}
	}
}