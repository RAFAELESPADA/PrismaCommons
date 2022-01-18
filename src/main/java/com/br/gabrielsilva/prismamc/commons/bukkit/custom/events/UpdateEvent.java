package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Getter;

@Getter
public class UpdateEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private UpdateType type;
	
	public UpdateEvent(UpdateType type) {
		this.type = type;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public enum UpdateType {
		SEGUNDO, MINUTO, HORA;
	}
}