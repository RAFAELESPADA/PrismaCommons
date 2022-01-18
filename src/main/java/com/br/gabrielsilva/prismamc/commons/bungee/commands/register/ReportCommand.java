package com.br.gabrielsilva.prismamc.commons.bungee.commands.register;

import java.util.HashMap;
import java.util.Map;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class ReportCommand implements CommandClass {
	
	@Command(name = "report", aliases= {"reportar"}, runAsync = true)
	public void execute(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (args.length < 2) {
			proxiedPlayer.sendMessage("§cUtilize: /report [nome] [motivo]");
			return;
		}
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer);
		if (!proxyPlayer.podeReportar()) {
			proxiedPlayer.sendMessage("§cAguarde para reportar um jogador novamente.");
			return;
		}
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(args[0]);
		if ((player == null) || (player.getServer() == null)) {
			proxiedPlayer.sendMessage("§cO jogador não está em nenhum servidor.");
			return;
		}
		if (player == proxiedPlayer) {
			proxiedPlayer.sendMessage("§cVocê não pode se reportar.");
			return;
		}
		String motivo = StringUtils.createArgs(1, args);
		
		addReportToRedis(player.getName(), proxiedPlayer.getName(), motivo);
		
		proxiedPlayer.sendMessage("§aJogador reportado com sucesso.");
		sendMessage(player, proxiedPlayer, motivo);
		proxyPlayer.setLastReport(System.currentTimeMillis());
	}
	
	private void addReportToRedis(String name, String reportou, String motivo) {
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			 int id = getReportIDByNick(name);
			 
			 if (id != 0) {
				 Map<String, String> hash = jedis.hgetAll("report:" + id);
				 int denuncias = Integer.valueOf(hash.get("denuncias")) + 1;
				 hash.put("denuncias", "" + denuncias);
				 hash.put("glassID", "5");
				 hash.put("lastReport", "" + System.currentTimeMillis());
				 
				 int motiveID = 0;
				 
				 for (int i = 1; i <= 10;i++) {
					  if (!hash.containsKey("motivo-" + i)) {
						  motiveID = i;
						  break;
					  }
				 }
				 
				 if (motiveID != 0) {
					 hash.put("motivo-" + motiveID, motivo);
					 hash.put("reportou-" + motiveID, reportou);
				 }
				 
				 jedis.hmset("report:" + id, hash);
				 jedis.expire("report:" + id, 3600);
				 
				 hash.clear();
				 hash = null;
			 } else {
				 int toAdd = 0;
				 
				 for (int i = 1; i <= 100; i++) {
					  if (!jedis.exists("report:" + i)) {
						  toAdd = i;
						  break;
					  }
				 }
				 
				 if (toAdd == 0) {
					 return;
				 }
				 HashMap<String, String> hash = new HashMap<>();
				 
				 hash.put("nick", name);
				 hash.put("motivo-1", motivo);
				 hash.put("reportou-1", reportou);
				 hash.put("lastReport", "" + System.currentTimeMillis());
				 hash.put("glassID", "5");
				 hash.put("denuncias", "1");
				 
				 jedis.hmset("report:" + toAdd, hash);
				 jedis.expire("report:" + toAdd, 3600);
				 
				 hash.clear();
				 hash = null;
			 }
		}
	}
	
	private int getReportIDByNick(String nick) {
		int id = 0;
		
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			 for (int i = 1; i <= 100; i++) {
				  if (jedis.exists("report:" + i)) {
					  Map<String, String> hash = jedis.hgetAll("report:" + i);
					  if (hash.get("nick").equalsIgnoreCase(nick)) {
						  id = i;
						  hash.clear();
						  hash = null;
						  break;
					  }
					  hash.clear();
					  hash = null;
				  }
			 }
		}
		return id;
	}

	public static void sendMessage(ProxiedPlayer p, ProxiedPlayer t, String motivo) {
		for (ProxiedPlayer proxiedPlayer : ProxyServer.getInstance().getPlayers()) {
			 if (proxiedPlayer.hasPermission("prismamc.staff")) {
				 if (BungeeMain.isValid(proxiedPlayer)) {
					 if (BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName()).isViewReports()) {
						 proxiedPlayer.sendMessage(PluginMessages.NEW_REPORT);
					 }
				 }
			 }
		}
	}
}