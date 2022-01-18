package com.br.gabrielsilva.prismamc.commons.bukkit.custom;

import org.bukkit.Bukkit;

import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;

public class UpdateScheduler implements Runnable {

	private int currentSeconds;
	
	@Override
	public void run() {
		currentSeconds++;
		
		Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.SEGUNDO));
		
		if (currentSeconds % 60 == 0) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.MINUTO));
		}
		
		if (currentSeconds % 3600 == 0) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.HORA));
		}
	}
}