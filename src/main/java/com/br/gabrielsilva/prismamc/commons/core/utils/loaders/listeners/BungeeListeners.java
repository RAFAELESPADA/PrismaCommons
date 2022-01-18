package com.br.gabrielsilva.prismamc.commons.core.utils.loaders.listeners;

import com.br.gabrielsilva.prismamc.commons.core.utils.ClassGetter;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeListeners {
	
	public static void loadListeners(Plugin instance, String packageName) {
		for (Class<?> classes : ClassGetter.getClassesForPackage(instance, packageName)) {
			 try {
				if (Listener.class.isAssignableFrom(classes)) {
					Listener listener = (Listener) classes.newInstance();
					if (listener == null)
						continue;
					ProxyServer.getInstance().getPluginManager().registerListener(instance, listener);
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}

