package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerRequestEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private Player player;
	private boolean cancelled = false;
	private RequestType requestType;
	
	public PlayerRequestEvent(Player player, RequestType requestType) {
		this.player = player;
		this.requestType = requestType;
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

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType requestType) {
		this.requestType = requestType;
	}

	public enum RequestType {
		FAKE, SKIN;
	}
}