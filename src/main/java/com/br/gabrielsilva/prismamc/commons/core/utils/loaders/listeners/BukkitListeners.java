package com.br.gabrielsilva.prismamc.commons.core.utils.loaders.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.br.gabrielsilva.prismamc.commons.core.utils.ClassGetter;

public class BukkitListeners {
	
	public static void loadListeners(Object instance, String packageName) {
		for (Class<?> classes : ClassGetter.getClassesForPackage(instance, packageName)) {
			 try {
				if (Listener.class.isAssignableFrom(classes)) {
					Listener listener  = (Listener) classes.newInstance();
					Bukkit.getPluginManager().registerEvents(listener, (Plugin) instance);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}

