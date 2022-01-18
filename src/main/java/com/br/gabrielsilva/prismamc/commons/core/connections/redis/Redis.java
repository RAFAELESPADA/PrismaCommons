package com.br.gabrielsilva.prismamc.commons.core.connections.redis;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.connections.BaseConnection;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Getter @Setter
public class Redis extends BaseConnection {

	private JedisPool pool;
	private String hostname, senha;
	private int porta;

	public Redis(JedisPool pool) {
		this.pool = pool;
	}

	public Jedis createJedisClient() {
		return pool.getResource();
	}
	
	public void openConnection3() {
		Core.console("Tentando estabelecer conexão com o Redis...");

		long started = System.currentTimeMillis();

		try {
			JedisPoolConfig config = new JedisPoolConfig();
			setPool(new JedisPool(config, "127.0.0.1", 6379, 0, "hidandomalritualdobem84111656.t"));
		} catch (Exception ex) {
		} finally {
			try {
				getPool().getResource().ping();
				Core.console("Pool do Redis estabelecida em: §a" + DateUtils.getElapsed(started));
			} catch (JedisConnectionException ex) {
				Core.console("§cOcorreu um erro ao tentar estabelecer conexão com o Redis -> " + ex.getLocalizedMessage());
				getPool().close();
				pool = null;
			}
		}
	}

	@Override
	public void openConnection() {
		// esse método vai ficar inutil, tem que ver isso dps
	}

	public void closeConnection() {
		if (getPool() != null) {
			getPool().destroy();
			Core.console("Conexão com o REDIS encerrada.");
		}
	}
	
	public boolean isConnected() {
		if (getPool() == null) {
			return false;
		}
		return !getPool().isClosed();
	}
}