package com.br.gabrielsilva.prismamc.commons.core.utils.loaders;

import com.br.gabrielsilva.prismamc.commons.core.utils.loaders.listeners.BukkitListeners;

public class ListenerLoader {
	
	public static void loadListenersBukkit(Object instance, String packageName) {
		BukkitListeners.loadListeners(instance, packageName);
	}
}