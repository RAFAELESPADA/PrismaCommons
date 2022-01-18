package com.br.gabrielsilva.prismamc.commons.core.connections.redis;

import com.br.gabrielsilva.prismamc.commons.core.Core;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class PubSubListener implements Runnable {
	
	private JedisPubSub jpsh;
	private final String[] channels;

	public PubSubListener(JedisPubSub s, String... channels) {
		this.jpsh = s;
		this.channels = channels;
	}

	@Override
	public void run() {
		boolean broken = false;
		
		try (Jedis rsc = Core.getRedis().getPool().getResource()) {
			try {
				rsc.subscribe(jpsh, channels);
			} catch (Exception e) {
				Core.console("PubSub error, attempting to recover. -> " + e.getLocalizedMessage());
				try {
					jpsh.unsubscribe();
				} catch (Exception e1) {}
				broken = true;
			}
		}

		if (broken) {
			run();
		}
	}

	public void addChannel(String... channel) {
		jpsh.subscribe(channel);
	}

	public void removeChannel(String... channel) {
		jpsh.unsubscribe(channel);
	}

	public void poison() {
		jpsh.unsubscribe();
	}
}