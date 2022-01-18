package com.br.gabrielsilva.prismamc.commons.bungee.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;

import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class MotdListener implements Listener {

	public static String linha1 = "§6§lKOMBO§f§lPVP §bKITPVP , HG, GLADIATOR",
			manutenção = "§cEstamos em manutenção";
	
	public static List<String> linha2 = new ArrayList<>();
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void oPing(ProxyPingEvent event) {
		ServerPing ping = event.getResponse();
		
		if (BungeeMain.isGlobalWhitelist()) {
			ping.setVersion(new ServerPing.Protocol("Manutenção", 2));
			ping.setDescription(linha1 + "\n" + manutenção);
			return;
		}
		
		ping.setDescription(linha1 + 
				"\n" + getRandomLine2());
	}
	
	public String getRandomLine2() {
		int escolhido = new Random().nextInt(linha2.size());
		return linha2.get(escolhido);
	}
}