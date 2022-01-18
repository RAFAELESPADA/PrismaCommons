package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerStopEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}