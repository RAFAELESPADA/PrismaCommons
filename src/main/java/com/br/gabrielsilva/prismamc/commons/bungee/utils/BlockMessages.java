package com.br.gabrielsilva.prismamc.commons.bungee.utils;

import java.util.ArrayList;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;

public class BlockMessages {

	private static List<String> messagesBlockeds = new ArrayList<>();
	
	private static String[] badMessages = {".com", ".com.br", "jogar.", ".net", ".zapto.org", ".zapto", ".ddns", "-net", "http", "desire", 
	"desirehost", ".no-ip", ".virtus", ".servegame", ".sytes", ".mojang", ".org", ".cookie", ".tk", "mush", "wave", "likekits", 
	"zenix", ".cc", "wavemc", "teckmc", "slovermc", "slimemc",
	"redesky", "skyminijogos", "skyminigames", "atlanticmc", "extrememc", ".desire.ho",
	"weaven", "windwars", "wave", "zeet", "zeetzmc", "kindo.me", "kindome", "kindo", "hevermc",
	"hevenmc", "heavenmc", "howlkmc", "zenix", "fademc", "battlebits", "battle", "atlantic", "ocean", "logicmc", "logic",
	"reis.host", "reishost", "blasthost", "blasthosting", "hever", "hevermc", "firemc", "firenetwork", "mc-weaven"};
	
	
	public static boolean hasDivulgation(String nick, String msg) {
		if (msg.toLowerCase().contains("prisma")) {
			return false;
		}
		if (msg.toLowerCase().contains("prismamc")) {
			return false;
		}
		
		msg = reformular(msg).toLowerCase();
		if (messagesBlockeds.contains(msg)) {
			return true;
		}
		boolean isBad = false;
		
		for (String blockeds : badMessages) {
			 if (msg.contains(blockeds)) {
				 isBad = true;
				 messagesBlockeds.add(msg);
				 BungeeMain.console("Mensagem de -> " + nick + " foi bloqueada por conter a palavra -> " + blockeds);
				 break;
			 }
		}
		return isBad;
	}
	
	private static String reformular(String msg) {
		String newMessage = msg.toLowerCase();
		
		if (newMessage.contains("_")) {
			newMessage = msg.replaceAll("_", "");
		}

		if (newMessage.contains(" ")) {
			newMessage = newMessage.replaceAll(" ", "");
		}
		if (newMessage.contains(",")) {
			newMessage = msg.replaceAll(",", "");
		}
		if (newMessage.contains("-")) {
			newMessage = msg.replaceAll("-", "");
		}
		if (newMessage.contains("=")) {
			newMessage = msg.replaceAll("=", "");
		}
		if (newMessage.contains(":")) {
			newMessage = msg.replaceAll(":", "");
		}
		if (newMessage.contains("_")) {
			newMessage = msg.replaceAll("_", "");
		}
		if (newMessage.contains("3")) {
			newMessage = msg.replaceAll("3", "e");
		}
		if (newMessage.contains("1")) {
			newMessage = msg.replaceAll("1", "i");
		}
		if (newMessage.contains("4")) {
			newMessage = msg.replaceAll("4", "a");
		}
		if (newMessage.contains("5")) {
			newMessage = msg.replaceAll("5", "s");
		}
		if (newMessage.contains("0")) {
			newMessage = msg.replaceAll("0", "o");
		}
		return newMessage;
	}
}