package com.br.gabrielsilva.prismamc.commons.core.server.types;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

@Getter @Setter
public class HungerGamesServer {
	
    private int serverID, vivos, maxPlayers, tempo, sequence;
    private boolean online;
    private Stages estagio;
    private Long lastTime;
    
    public HungerGamesServer(final int serverID) {
        this.serverID = serverID;
        this.vivos = 0;
        this.tempo = 0;
        this.maxPlayers = 0;
        this.sequence = 0;
        this.online = false;
        this.lastTime = 0L;
        this.estagio = Stages.OFFLINE;
    }
    
    public void update(Jedis jedis) {
    	if (!jedis.exists("serverInfo:hg" + getServerID())) {
    		return;
    	}
    	
    	Map<String, String> hash = jedis.hgetAll("serverInfo:hg" + getServerID());
        if (hash.containsKey("vivos")) {
            this.vivos = Integer.valueOf(hash.get("vivos"));
        }
        if (hash.containsKey("tempo")) {
            this.tempo = Integer.valueOf(hash.get("tempo"));
        }
        if (hash.containsKey("estagio")) {
            this.estagio = Stages.getStageByName(hash.get("estagio"));
        }
        if (hash.containsKey("maxPlayers")) {
            this.maxPlayers = Integer.valueOf(hash.get("maxPlayers"));
        }
        
        if (hash.containsKey("estagio")) {
            this.estagio = Stages.getStageByName(hash.get("estagio"));
            if (this.estagio == Stages.OFFLINE) {
            	this.online = false;
            }
        }
        
        if (hash.containsKey("lastTime")) {
        	Long actualTime = Long.valueOf(hash.get("lastTime"));
        	
        	if (actualTime.equals(lastTime)) {
        		this.sequence++;
        		if (this.sequence >= 60) {
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
 			this.estagio = Stages.OFFLINE;
 		}
        
        if (!this.online || this.estagio == Stages.OFFLINE || this.estagio == Stages.CARREGANDO) {
        	this.vivos = 0;
        }
        
    	hash.clear();
    	hash = null;
    }
}