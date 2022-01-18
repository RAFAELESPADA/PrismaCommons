package com.br.gabrielsilva.prismamc.commons.core.server.types;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

@Getter @Setter
public class NetworkServer {
	
    private String serverName;
    private int onlines, maxPlayers, sequence;
    private boolean online;
    private Long lastTime;
    
    public NetworkServer(final String serverName) {
        this.serverName = serverName;
        this.onlines = 0;
        this.maxPlayers = -1;
        this.sequence = 0;
        this.online = false;
        this.lastTime = 0L;
    }
    
    public void update(Jedis jedis) {
    	if (!jedis.exists("serverInfo:" + getServerName())) {
    		return;
    	}
    	
    	Map<String, String> hash = jedis.hgetAll("serverInfo:" + getServerName());
    	
        if (hash.containsKey("onlines")) {
            this.onlines = Integer.valueOf(hash.get("onlines"));
        }
        
        if (hash.containsKey("maxPlayers")) {
            this.maxPlayers = Integer.valueOf(hash.get("maxPlayers"));
        }
        
        if (hash.containsKey("lastTime")) {
        	Long actualTime = Long.valueOf(hash.get("lastTime"));
        	
        	if (actualTime.equals(lastTime)) {
        		this.sequence++;
        		if (this.sequence >= 16) {
        			this.online = false;
        		}
        	} else {
        		this.lastTime = actualTime;
        		this.sequence = 0;
        		this.online = true;
        	}
        
    		this.lastTime = actualTime;
        	
        	actualTime = null;
        }
        
        if (!this.online) {
        	this.onlines = 0;
        }
        
    	hash.clear();
    	hash = null;
    }
}