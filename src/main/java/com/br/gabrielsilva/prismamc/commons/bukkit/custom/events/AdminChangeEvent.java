package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AdminChangeEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Player player;
	private ChangeType changeType;
	private boolean cancelled = false;
	
	public AdminChangeEvent(Player player, ChangeType changeType) {
		this.player = player;
		this.changeType = changeType;
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

	public void setCancelled(boolean cancel) { 
		cancelled = cancel; 
	}

	public boolean isCancelled() { 
		return cancelled;
	}
	
	public ChangeType getChangeType() {
		return changeType;
	}

	public enum ChangeType {
		ENTROU, SAIU;
	}
}