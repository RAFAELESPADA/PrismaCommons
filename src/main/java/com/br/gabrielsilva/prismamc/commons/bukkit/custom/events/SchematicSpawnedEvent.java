package com.br.gabrielsilva.prismamc.commons.bukkit.custom.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.object.Schematic;

public class SchematicSpawnedEvent extends Event {

	public static final HandlerList handlers = new HandlerList();
	private String nome;
	private boolean forced;
	private Schematic schematic;
	
	public SchematicSpawnedEvent(String nome, boolean forced, Schematic schematic) {
		this.nome = nome;
		this.forced = forced;
		this.schematic = schematic;
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public boolean isForced() {
		return forced;
	}

	public void setForced(boolean forced) {
		this.forced = forced;
	}

	public Schematic getSchematic() {
		return schematic;
	}

	public void setSchematic(Schematic schematic) {
		this.schematic = schematic;
	}
}