package com.br.gabrielsilva.prismamc.commons.core.connections;

public abstract class BaseConnection {
	
	public abstract void openConnection();
	public abstract void closeConnection();
	public abstract boolean isConnected();
}