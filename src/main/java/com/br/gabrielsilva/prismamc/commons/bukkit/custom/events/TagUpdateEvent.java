package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TagUpdateEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Player player;
	
	public TagUpdateEvent(Player player) {
		this.player = player;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}
}