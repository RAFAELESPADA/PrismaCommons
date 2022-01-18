package com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.skinsrestorer.MojangAPI.SkinRequestException;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Jedis;

public class SkinStorage {

    private static Class<?> property;

    static {
        try {
            property = Class.forName("com.mojang.authlib.properties.Property");
        } catch (Exception e) {
            try {
                property = Class.forName("net.md_5.bungee.connection.LoginResult$Property");
            } catch (Exception ex) {
                try {
                    property = Class.forName("net.minecraft.util.com.mojang.authlib.properties.Property");
                } catch (Exception exc) {
                    System.out.println(
                            "[SkinsRestorer] Could not find a valid Property class! Plugin will not work properly");
                }
            }
        }
    }

    public static Object createProperty(String name, String value, String signature) {
        try {
            return ReflectionUtil.invokeConstructor(property,
            new Class<?>[]{String.class, String.class, String.class}, name, value, signature);
        } catch (Exception e) {}
        return null;
    }
    
	public static Object getOrCreateSkinForPlayer(String name) throws MojangAPI.SkinRequestException {
        String skin = getPlayerSkin(name);
        
        if (skin == null) {
            skin = name.toLowerCase();
        }
        
        Object textures = null;
        
        textures = getSkinData(skin);
        
        if (textures != null) {
            return textures;
        }
        
        if (BungeeMain.getManager().getPremiumMapManager().containsMap(name)) {
        	if (!BungeeMain.getManager().getPremiumMapManager().getPremiumMap(name).isPremium()) {
        		textures = getSkinData("0171");
        	}
        }
        
        if (textures != null) {
            return textures;
        }
        
        String uuid = "N/A";
        if (BungeeMain.getManager().getPremiumMapManager().containsMap(name)) {
        	uuid = String.valueOf(BungeeMain.getManager().getPremiumMapManager().getPremiumMap(name).getUUID()).replaceAll("-", "");
        }
        
        final String sname = skin;
        try {
            Object props = null;

            if (uuid.equals("N/A")) {
            	uuid = MojangAPI.getUUID(sname);
            }
            
            textures = MojangAPI.getSkinProperty(uuid);

            String value = Base64Coder.decodeString((String) ReflectionUtil.invokeMethod(textures, "getValue"));

            JsonElement element = new JsonParser().parse(value);
            JsonObject obj = element.getAsJsonObject();
            JsonObject textureObj = obj.get("textures").getAsJsonObject();

            String newurl;
            if (textureObj.has("SKIN")) {
                newurl = textureObj.get("SKIN").getAsJsonObject().get("url").getAsString();
            }
            
            setSkinData(sname, textures);
        } catch (MojangAPI.SkinRequestException e) {
            throw new MojangAPI.SkinRequestException(e.getReason());
        } catch (Exception e2) {
            throw new MojangAPI.SkinRequestException("aguarde");
        }
        return textures;
    }
	
	public static void createSkin(String name) throws SkinRequestException {
        Object textures = null;
        
        String uuid = "N/A";
        if (BungeeMain.getManager().getPremiumMapManager().containsMap(name)) {
        	uuid = String.valueOf(BungeeMain.getManager().getPremiumMapManager().getPremiumMap(name).getUUID()).replaceAll("-", "");
        }
        
		try {
            Object props = null;

            if (uuid.equals("N/A")) {
            	uuid = MojangAPI.getUUID(name);
            }
            
            textures = MojangAPI.getSkinProperty(uuid);

            String value = Base64Coder.decodeString((String) ReflectionUtil.invokeMethod(textures, "getValue"));

            JsonElement element = new JsonParser().parse(value);
            JsonObject obj = element.getAsJsonObject();
            JsonObject textureObj = obj.get("textures").getAsJsonObject();

            String newurl;
            if (textureObj.has("SKIN")) {
                newurl = textureObj.get("SKIN").getAsJsonObject().get("url").getAsString();
            }
            
            setSkinData(name, textures);
        } catch (MojangAPI.SkinRequestException e) {
            throw new MojangAPI.SkinRequestException(e.getReason());
        } catch (Exception e2) {
            throw new MojangAPI.SkinRequestException("aguarde");
        }
	}

    public static String getPlayerSkin(String name) {
    	if (!Core.getRedis().isConnected()) {
    		return name.toLowerCase();
    	}
        name = name.toLowerCase();
        
        try (Jedis jedis = Core.getRedis().getPool().getResource()) {
             if (jedis.exists("PlayerSkin:" + name)) {
        		 String skinUsing = jedis.get("PlayerSkin:" + name);
        		 if (skinUsing.isEmpty() || skinUsing.equalsIgnoreCase(name)) {
        			 removePlayerSkin(name);
        			 skinUsing = name;
        		 }
        		 jedis.expire("PlayerSkin:" + name, (3600*12));
        		 return skinUsing;
        	 }
        }
        try {
        	PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
        	"SELECT * FROM playerSkin where nick='" + name+ "';");
        	ResultSet result = preparedStatament.executeQuery();
        	if (!result.next()) {
        		result.close();
        		preparedStatament.close();
        		return name;
        	}
        	String skinUsing = result.getString("skin");
		    result.close();
		    preparedStatament.close();
		    try (Jedis jedis = Core.getRedis().getPool().getResource()) {
	    	    jedis.set("PlayerSkin:" + name, skinUsing);
	    	    jedis.expire("PlayerSkin:" + name, (3600*12));
		    }
			return skinUsing;
        } catch (SQLException ex) {
        	BungeeMain.console("Ocorreu um erro ao tentar obter a skin do jogador -> " + ex.getLocalizedMessage());
        }
		return name;
    }

    public static Object getSkinData(String name) {
        name = name.toLowerCase();
        if (!Core.getRedis().isConnected()) {
        	return name;
        }
        
        try (Jedis jedis = Core.getRedis().getPool().getResource()) {
        	 if (jedis.exists("SkinCache:" + name)) {
        		 Map<String, String> hashRedis = jedis.hgetAll("SkinCache:" + name);
        		
            	 String value = hashRedis.get("value"), 
            			signature = hashRedis.get("signature"),
            			    timestamp = hashRedis.get("timestamp");
        		 hashRedis.clear();
        		
                 if (isOld(Long.valueOf(timestamp))) {
                     Object skin = MojangAPI.getSkinProperty(MojangAPI.getUUID(name));
                     if (skin != null) {
                         SkinStorage.setSkinData(name, skin);
                     }
                 }
                 jedis.expire("SkinCache:" + name, (3600*12));
                 return createProperty("textures", value, signature); 
        	 }
        } catch (SkinRequestException e) {}
        
        try {
        	PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
        			"SELECT * FROM skins where nick='" + name+ "';");
        	ResultSet result = preparedStatament.executeQuery();
        	if (!result.next()) {
        		result.close();
        		preparedStatament.close();
        		return null;
        	}
        	String value = result.getString("value"),
        			signature = result.getString("signature"),
        			timestamp = result.getString("timestamp");
        	
        	result.close();
        	preparedStatament.close();
        	
            if (isOld(Long.valueOf(timestamp))) {
                Object skin = MojangAPI.getSkinProperty(MojangAPI.getUUID(name));
                if (skin != null) {
                    SkinStorage.setSkinData(name, skin);
                }
            } else {
            	HashMap<String, String> hash = new HashMap<>();
            	hash.put("value", value);
            	hash.put("signature", signature);
            	hash.put("timestamp", timestamp);
            	try (Jedis jedis = Core.getRedis().getPool().getResource()) {
            	    jedis.hmset("SkinCache:" + name, hash);
                	jedis.expire("SkinCache:" + name, (3600*12));
            	}
            	hash.clear();
            }
            return createProperty("textures", value, signature);
        } catch (SQLException ex) {
        	BungeeMain.console("Ocorreu um erro ao tentar obter a data de uma skin (SQL) -> " + ex.getLocalizedMessage());
        } catch (SkinRequestException ex) {
        	BungeeMain.console("Ocorreu um erro ao tentar obter a data de uma skin '"+name+"' -> " + ex.getLocalizedMessage());
		}
        return getSkinData("0171");
    }

    public static boolean isOld(long timestamp) {
        if (timestamp + TimeUnit.MINUTES.toMillis(1584) <= System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public static void removePlayerSkin(String name) {
        name = name.toLowerCase();
        if (Core.getRedis().isConnected()) {
        	try (Jedis jedis = Core.getRedis().getPool().getResource()) {
        		if (jedis.exists("PlayerSkin:" + name)) {
        			String skinUsing = jedis.get("PlayerSkin:" + name);
        			if (!skinUsing.equals(name)) {
        				jedis.set("PlayerSkin:" + name, name);
        			}
        		}
        	}
        }
        
        try {
        	PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
        	"SELECT * FROM playerSkin where nick='" + name+ "';");
        	ResultSet result = preparedStatament.executeQuery();
        	if (!result.next()) {
        		result.close();
        		preparedStatament.close();
        		return;
        	}
    		result.close();
    		preparedStatament.close();
			Statement s = Core.getMySQL().getConexão().createStatement();
			s.executeUpdate("DELETE FROM playerSkin WHERE nick='"+name+"'");
			s.close();
        } catch (SQLException ex) {
        	Core.console("Erro ao remover a skin de um Jogador -> " + ex.getLocalizedMessage());
        }
    }

    public static void removeSkinData(String name) {
        name = name.toLowerCase();
        
        if (Core.getRedis().isConnected()) {
        	try (Jedis jedis = Core.getRedis().getPool().getResource()) {
            	if (jedis.exists("SkinCache:" + name)) {
            		jedis.del("SkinCache:" + name);
            	}
        	}
        }
        try {
        	PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
        			"SELECT * FROM skins where nick='" + name+ "';");
        	ResultSet result = preparedStatament.executeQuery();
        	if (!result.next()) {
        		result.close();
        		preparedStatament.close();
        		return;
        	}
    		result.close();
    		preparedStatament.close();
			Statement s = Core.getMySQL().getConexão().createStatement();
			s.executeUpdate("DELETE FROM skins WHERE nick='"+name+"'");
			s.close();
        } catch (SQLException ex) {
        	Core.console("Erro ao remover a skin do Banco de Dados -> " + ex.getLocalizedMessage());
        }
    }

    public static void setPlayerSkin(String name, String skin) {
        name = name.toLowerCase();
        
        if (Core.getRedis().isConnected()) {
        	try (Jedis jedis = Core.getRedis().getPool().getResource()) {
    			jedis.set("PlayerSkin:" + name, skin);
    			jedis.expire("PlayerSkin:" + name, (3600*12));
        	}
        }
        
        try {
    		PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
    				"SELECT * FROM playerSkin where nick='" + name+ "';");
    		ResultSet result = preparedStatament.executeQuery();
    		if (!result.next()) {
           		result.close();
        		preparedStatament.close();
        		
        		PreparedStatement set = Core.getMySQL().getConexão().prepareStatement(
        				"INSERT INTO playerSkin (nick, skin) VALUES (?, ?)");
        		
        		set.setString(1, name);
        		set.setString(2, skin);
        		
        		set.executeUpdate();
        		set.close();
        		return;
    		}
       		result.close();
    		preparedStatament.close();
    		
    		PreparedStatement set = Core.getMySQL().getConexão().prepareStatement(
    				"UPDATE playerSkin SET skin=? where nick='"+name+"'");
    		
    		set.setString(1, skin);
    		
    		set.executeUpdate();
    		set.close();
    	} catch (SQLException ex) {
    		Core.console("Erro ao setar skin de um jogador no MySQL -> " + ex.getLocalizedMessage());
    	}
    }

    public static void setSkinData(String name, Object textures) {
        name = name.toLowerCase();
       
        String value = "", 
        		signature = "", 
        		    timestamp = "";
        try {
            value = (String) ReflectionUtil.invokeMethod(textures, "getValue");
            signature = (String) ReflectionUtil.invokeMethod(textures, "getSignature");
            timestamp = String.valueOf(System.currentTimeMillis());
        } catch (Exception e) {}
        
        if (Core.getRedis().isConnected()) {
        	HashMap<String, String> hash = new HashMap<>();
        	hash.put("value", value);
    	    hash.put("signature", signature);
    	    hash.put("timestamp", timestamp);
    	    try (Jedis jedis = Core.getRedis().getPool().getResource()) {
    	        jedis.hmset("SkinCache:" + name, hash);
    	        jedis.expire("SkinCache:" + name, (3600*6));
    	    }
    	    hash.clear();
        }
    	try {
    		PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
    				"SELECT * FROM skins where nick='" + name+ "';");
    		ResultSet result = preparedStatament.executeQuery();
    		if (!result.next()) {
           		result.close();
        		preparedStatament.close();
        		
        		PreparedStatement set = Core.getMySQL().getConexão().prepareStatement(
        				"INSERT INTO skins (nick, value, signature, timestamp) VALUES (?, ?, ?, ?)");
        		
        		set.setString(1, name);
        		set.setString(2, value);
        		set.setString(3, signature);
        		set.setString(4, timestamp);
        		
        		set.executeUpdate();
        		set.close();
        		return;
    		}
       		result.close();
    		preparedStatament.close();
    		
    		PreparedStatement set = Core.getMySQL().getConexão().prepareStatement(
    				"UPDATE skins SET value=?, signature=?, timestamp=? where nick='"+name+"'");
    		
    		set.setString(1, value);
    		set.setString(2, signature);
    		set.setString(3, timestamp);
    		
    		set.executeUpdate();
    		set.close();
    	} catch (SQLException ex) {
    		Core.console("Erro ao setar skin no MySQL -> " + ex.getLocalizedMessage());
    	}  
    }
}