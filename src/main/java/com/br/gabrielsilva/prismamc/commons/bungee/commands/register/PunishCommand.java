package com.br.gabrielsilva.prismamc.commons.bungee.commands.register;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.base.BasePunishment;
import com.br.gabrielsilva.prismamc.commons.core.base.PunishmentType;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.connections.redis.RedisAPI;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class PunishCommand implements CommandClass {

	@Command(name = "banstats", aliases = {"bs"}, runAsync = true)
	public void banstats(BungeeCommandSender commandSender, String label, String[] args) {
		try {
			if (!commandSender.hasPermission("prismamc.dono")) {
				commandSender.sendMessage("Sem permissao");
				return;
			}
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
					"SELECT * from bans");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				commandSender.sendMessage("§cNenhum banimento no momento.");
				result.close();
				preparedStatament.close();
				return;
			}

			Map<String, Integer> unsortedMap = new HashMap<>();

			int totalDeBans = 0;

			while (result.next()) {
				String nick = result.getString("baniu");
				if (!unsortedMap.containsKey(nick)) {
					unsortedMap.put(nick, 1);
				} else {
					unsortedMap.put(nick, unsortedMap.get(nick) + 1);
				}
				totalDeBans++;
			}

			result.close();
			preparedStatament.close();

			final Map<String, Integer> bans = unsortedMap.entrySet()
					.stream()
					.sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			int rank = 1;

			commandSender.sendMessage("");
			commandSender.sendMessage("§aRanking de Banimentos");
			commandSender.sendMessage("");

			for (String nicks : bans.keySet()) {
				commandSender.sendMessage("§a" + rank + "º §f" + nicks + " §a" + bans.get(nicks) + " §fbanimentos.");
				rank++;
			}

			commandSender.sendMessage("");
			commandSender.sendMessage("§fTotal de §a" + StringUtils.reformularValor(totalDeBans) + " §fbanimentos no servidor.");
			commandSender.sendMessage("");

			unsortedMap.clear();
			unsortedMap = null;
			bans.clear();
		} catch (SQLException ex) {
			commandSender.sendMessage("§cOcorreu um erro, tente novamente.");
			BungeeMain.console("§cOcorreu um erro ao tentar verificar os status de banimentos. -> " + ex.getLocalizedMessage());
		}
	}

	@Command(name = "unmute", aliases = {"desmutar"}, groupsToUse = {Groups.TRIAL}, runAsync = true)
	public void unmute(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage(PluginMessages.UNMUTE);
			return;
		}


		
		String nick = MySQLManager.getString("accounts", "nick", args[0], "nick");
		if (nick.equalsIgnoreCase("N/A")) {
			commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
			return;
		}
		
		BasePunishment basePunish = new BasePunishment(nick, "", PunishmentType.MUTE);
		try {
			basePunish.load();
		} catch (Exception e) {
			commandSender.sendMessage("§cOcorreu um erro ao tentar carregar um punimento...");
			e.printStackTrace();
			return;
		}
		
		if (!basePunish.isAplicado()) {
			commandSender.sendMessage(PluginMessages.CONTA_NÃO_ESTA_MUTADA);
			return;
		}
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (target != null) {
			BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSessions().get(target.getName().toLowerCase());
			if (proxyPlayer.getMute().isAplicado()) {
				proxyPlayer.resetPunishment();
			}
		}
		basePunish.unmute();
		commandSender.sendMessage(PluginMessages.CONTA_DESMUTADA);
	}

	
	@Command(name = "unban", aliases = { "desbanir" }, groupsToUse = {Groups.TRIAL}, runAsync = true)
	public void unban(BungeeCommandSender commandSender, String label, String[] args) {
		if (args.length != 1) {
			commandSender.sendMessage(PluginMessages.UNBAN);
			return;
		}

		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM accounts WHERE nick='"+args[0]+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				result.close();
				preparedStatament.close();
				return;
			}
			
			BasePunishment basePunishment = new BasePunishment(result.getString("nick"), result.getString(DataType.LAST_IP.getField()),
					PunishmentType.BAN);
			
			result.close();
			preparedStatament.close();
			
			try {
				basePunishment.load();
			} catch (Exception e) {
				commandSender.sendMessage("§cOcorreu um erro ao tentar carregar um punimento...");
				e.printStackTrace();
				return;
			}
			
			if (!basePunishment.isAplicado()) {
				commandSender.sendMessage(PluginMessages.CONTA_NÃO_ESTA_BANIDA);
				return;
			}
			
			basePunishment.unban();
			commandSender.sendMessage(PluginMessages.CONTA_DESBANIDA);
			
			BungeeMain.getManager().warnStaff(PluginMessages.PLAYER_FOI_DESBANIDO_PARA_STAFFER.replace("%nick%", 
					basePunishment.getConta()).replace("%staffer%", commandSender.getNick()));
			
		} catch (SQLException ex) {
			BungeeMain.console("Ocorreu um erro ao tentar desbanir uma conta. > " + ex.getLocalizedMessage());
			commandSender.sendMessage("§cOcorreu um erro, tente novamente...");
		}
	}
	
	@Command(name = "mute", aliases = { "mutar" }, runAsync = true)
	public void mute(BungeeCommandSender commandSender, String label, String[] args) {
		String mutou = commandSender.getNick();
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length < 2) {
			commandSender.sendMessage(PluginMessages.MUTE);
			return;
		}
		
		if (!MySQLManager.contains("accounts", "nick", args[0])) {
			commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
			return;
		}
		
		if (MySQLManager.contains("mutes", "nick", args[0])) {
			commandSender.sendMessage("§cEstá conta ja está mutada.");
			return;
		}
		
		String 	motivo = StringUtils.createArgs(1, args);
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
		
		boolean continuar = true;
		if (!mutou.equalsIgnoreCase("CONSOLE")) {
			if (BungeeMain.isValid(target)) {
				BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(target),
						proxyExpulsou = BungeeMain.getManager().getSessionManager().getSession(commandSender.getPlayer());
				
				if (proxyPlayer.getGrupo().getNivel() > proxyExpulsou.getGrupo().getNivel()) {
					commandSender.sendMessage("§cVocê não pode mutar alguém com o grupo superior ao seu.");
					continuar = false;
				}
				
			}
		}
		
		if (!continuar) {
			return;
		}
		
		handleMute(args[0], motivo, mutou, 0L);
		commandSender.sendMessage(PluginMessages.JOGADOR_MUTADO_PERMANENTEMENTE);
	}
	
	@Command(name = "tempmute", runAsync = true)
	public void tempmute(BungeeCommandSender commandSender, String label, String[] args) {
		String mutou = commandSender.getNick();
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length < 3) {
			commandSender.sendMessage(PluginMessages.TEMPMUTE);
			return;
		}
		
		if (!MySQLManager.contains("accounts", "nick", args[0])) {
			commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
			return;
		}
		
		if (MySQLManager.contains("mutes", "nick", args[0])) {
			commandSender.sendMessage("§cEstá conta ja está mutada.");
			return;
		}
		
		String 	motivo = "";
		
		motivo = StringUtils.createArgs(2, args);
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
		
		boolean continuar = true;
		if (!mutou.equalsIgnoreCase("CONSOLE")) {
			if (BungeeMain.isValid(target)) {
				BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(target),
						proxyExpulsou = BungeeMain.getManager().getSessionManager().getSession(commandSender.getPlayer());
				
				if (proxyPlayer.getGrupo().getNivel() > proxyExpulsou.getGrupo().getNivel()) {
					commandSender.sendMessage("§cVocê não pode mutar alguém com o grupo superior ao seu.");
					continuar = false;
				}
				
			}
		}
		
		if (!continuar) {
			return;
		}
		
		long tempo = 0;
		try {
			tempo = DateUtils.parseDateDiff(args[1], true);
		} catch (Exception ex) {
			commandSender.sendMessage(PluginMessages.TEMPO_INVALIDO);
			return;
		}
		
		handleMute(args[0], motivo, mutou, tempo);
		commandSender.sendMessage(PluginMessages.JOGADOR_MUTADO_TEMPORARIAMENTE.replace("%duração%", DateUtils.formatDifference(tempo + 300)));
	}
	
	@Command(name = "tempban", runAsync = true)
	public void tempban(BungeeCommandSender commandSender, String label, String[] args) {
		String baniu = commandSender.getNick();
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length < 3) {
			commandSender.sendMessage(PluginMessages.TEMPBAN);
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM accounts WHERE nick='"+args[0]+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				result.close();
				preparedStatament.close();
				return;
			}
			
			String nick = result.getString("nick"), 
					ultimoIP = result.getString("last_ip"), 
					clan = result.getString("clan"), 
					motivo = "";
			
			result.close();
			preparedStatament.close();
			
			BasePunishment basePunishment = new BasePunishment(nick, ultimoIP, PunishmentType.BAN);
			
			try {
				basePunishment.load();
			} catch (Exception e) {
				commandSender.sendMessage("§cOcorreu um erro ao tentar carregar um punimento...");
				e.printStackTrace();
				return;
			}
			
			if (basePunishment.isApplied()) {
				commandSender.sendMessage("§cEstá conta já está banida.");
				return;
			}
			
			motivo = StringUtils.createArgs(2, args);
			
			ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
			boolean continuar = true;
			if (!baniu.equalsIgnoreCase("CONSOLE")) {
				if (BungeeMain.isValid(target)) {
					BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(target),
							proxyExpulsou = BungeeMain.getManager().getSessionManager().getSession(commandSender.getPlayer());
					
					if (proxyPlayer.getGrupo().getNivel() >= proxyExpulsou.getGrupo().getNivel()) {
						commandSender.sendMessage("§cVocê não pode banir alguém com o grupo superior ao seu.");
						continuar = false;
					}
					
				}
			}
			
			if (!continuar) {
				return;
			}
			
			String ipBan = "prismamc.com.br";
			if (!baniu.equalsIgnoreCase("CONSOLE")) {
				ipBan = commandSender.getPlayer().getServer().getInfo().getName().toUpperCase();
			}
			
			long tempo = 0;
			try {
				tempo = DateUtils.parseDateDiff(args[1], true);
			} catch (Exception ex) {
				commandSender.sendMessage(PluginMessages.TEMPO_INVALIDO);
				return;
			}
			
			commandSender.sendMessage(PluginMessages.JOGADOR_BANIDO_TEMPORARIAMENTE.replace(
					"%duração%", DateUtils.formatDifference(tempo + 300)));
			
			handleBan(target, nick, ultimoIP, motivo, baniu, String.valueOf(tempo), clan, ipBan);
			
			ProxyServer.getInstance().broadcast(PluginMessages.JOGADOR_FOI_BANIDO.replace("%nick%", nick).
					replace("%tempo%", "temporariamente"));
			
			if (target != null) {
				handleKick(target, motivo, baniu, tempo);
			}
			
			removeCache(nick);
		} catch (SQLException ex) {
			BungeeMain.console("Ocorreu um erro ao tentar desbanir uma conta. > " + ex.getLocalizedMessage());
			commandSender.sendMessage("§cOcorreu um erro, tente novamente...");
		}
	}
	
	@Command(name = "ban", runAsync = true)
	public void ban(BungeeCommandSender commandSender, String label, String[] args) {
		String baniu = commandSender.getNick();
		ProxiedPlayer p = commandSender.getPlayer();
		if (!p.hasPermission("prismamc.staff")) {
			p.sendMessage("Sem permissao");
			return;
		}
		if (args.length < 2) {
			commandSender.sendMessage(PluginMessages.BAN);
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"SELECT * FROM accounts WHERE nick='"+args[0]+"';");
			ResultSet result = preparedStatament.executeQuery();
			if (!result.next()) {
				commandSender.sendMessage(PluginMessages.NAO_TEM_CONTA);
				result.close();
				preparedStatament.close();
				return;
			}
			
			String nick = result.getString("nick"), 
					ultimoIP = result.getString("last_ip"), 
					clan = result.getString("clan"), 
					motivo = "";
			
			result.close();
			preparedStatament.close();
			
			BasePunishment basePunishment = new BasePunishment(nick, ultimoIP, PunishmentType.BAN);
			
			try {
				basePunishment.load();
			} catch (Exception e) {
				commandSender.sendMessage("§cOcorreu um erro ao tentar carregar um punimento...");
				e.printStackTrace();
				return;
			}
			
			if (basePunishment.isApplied()) {
				commandSender.sendMessage("§cEstá conta já está banida.");
				return;
			}
			
			motivo = StringUtils.createArgs(1, args);
			
			ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
			boolean continuar = true;
			if (!baniu.equalsIgnoreCase("CONSOLE")) {
				if (BungeeMain.isValid(target)) {
					BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(target),
							proxyExpulsou = BungeeMain.getManager().getSessionManager().getSession(commandSender.getPlayer());
					
					if (proxyPlayer.getGrupo().getNivel() > proxyExpulsou.getGrupo().getNivel()) {
						commandSender.sendMessage("§cVocê não pode banir alguém com o grupo superior ao seu.");
						continuar = false;
					}
					
				}
			}
			
			if (!continuar) {
				return;
			}
			
			String ipBan = "prismamc.com.br";
			if (!baniu.equalsIgnoreCase("CONSOLE")) {
				ipBan = commandSender.getPlayer().getServer().getInfo().getName().toUpperCase();
			}
			
			commandSender.sendMessage(PluginMessages.JOGADOR_BANIDO_PERMANENTEMENTE);
			
			handleBan(target, nick, ultimoIP, motivo, baniu, "0", clan, ipBan);
			
			ProxyServer.getInstance().broadcast(PluginMessages.JOGADOR_FOI_BANIDO.replace("%nick%", nick).
					replace("%tempo%", "permanentemente"));
			
			if (target != null) {
				handleKick(target, motivo, baniu, 0L);
			}
			
			removeCache(nick);
		} catch (SQLException ex) {
			BungeeMain.console("Ocorreu um erro ao tentar desbanir uma conta. > " + ex.getLocalizedMessage());
			commandSender.sendMessage("§cOcorreu um erro, tente novamente...");
		}
	}
	
	public static void removeCache(String nick) {
		if (!Core.getRedis().isConnected()) {
			return;
		}
			
		try (Jedis jedis = Core.getRedis().getPool().getResource()) {
			for (int i = 1; i <= 100; i++) {
			     if (jedis.exists("report:" + i)) {
				     Map<String, String> hash = jedis.hgetAll("report:" + i);
					 if (hash.containsKey("nick")) {
					     if (hash.get("nick").equalsIgnoreCase(nick)) {
						     hash.put("glassID", "14");
								 
							 jedis.hmset("report:" + i, hash);
							 jedis.expire("report:" + i, 3600); 
							 break;
					     }
					 }
					 hash.clear();
					 hash = null;
			     }
			}
		}
	}

	public static void handleBan(ProxiedPlayer quit, String nick, String ultimoIP, String motivo, 
			String baniu, String tempo, String clan, String ipBan) {
		
	
		if (quit != null) {
			BungeePlayer bungeePlayer = BungeeMain.getManager().getSessionManager().getSession(quit);
			if (bungeePlayer.inScreenShare()) {
				ProxiedPlayer other = ProxyServer.getInstance().getPlayer(bungeePlayer.getScreenShareWith());
				BungeeMain.getManager().getSessionManager().getSession(other).quitScreenShare();
				other.connect(ProxyServer.getInstance().getServerInfo("Lobby"));
				bungeePlayer.quitScreenShare();
			}
		}
		
		String message = PluginMessages.JOGADOR_FOI_BANIDO_STAFFER.replace("%baniu%", baniu).replace("%servidor%", ipBan).replace("%nick%", nick).
				replace("%tempo%", (tempo.equalsIgnoreCase("0") ? "PERMANENTEMENTE" : "TEMPORARIAMENTE")).replace("%motivo%", motivo);
		
		BungeeMain.getManager().warnStaff(message);
		
		BasePunishment basePunishment = new BasePunishment(nick, ultimoIP, PunishmentType.BAN);
		basePunishment.setMotivo(motivo);
		basePunishment.setPunidoPor(baniu);
		basePunishment.setPunishmentTime(Long.valueOf(tempo));
		basePunishment.banir();
		
		BungeeMain.runAsync(() -> {
			if (tempo.equals("0")) {
				MySQLManager.deleteFromTable("hungergames", "nick", nick);
				MySQLManager.deleteFromTable("gladiator", "nick", nick);
				MySQLManager.deleteFromTable("kitpvp", "nick", nick);
			}
			
			removePlayerFromClan(quit, nick, clan);
		});
	}
	
	public static void handleKick(ProxiedPlayer target, String motivo, String baniu, Long tempo) {
		BungeeMain.runLater(() -> {
			target.disconnect(PluginMessages.VOCÊ_FOI_BANIDO.replace("%action%", (tempo == 0L ? "permanentemente" : "temporariamente")).
			replace("%baniu%", baniu).replace("%motivo%", motivo).replace("%tempo%", (tempo == 0L ? "Nunca" : DateUtils.formatDifference(tempo + 1200))));
		}, 10);
		
		if (BungeeMain.getManager().getSessionManager().hasSession(target)) {
			BungeeMain.getManager().getSessionManager().removeSession(target);
		}
	}
	
	public static void handleMute(String nick, String motivo, String mutou, Long tempo) {
		BasePunishment basePunishment = new BasePunishment(nick, "", PunishmentType.MUTE);
		basePunishment.setAplicado(true);
		basePunishment.setMotivo(motivo);
		basePunishment.setPunidoPor(mutou);
		basePunishment.setPunishmentTime(tempo);
		basePunishment.mute();
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick); 
		if (target != null) {
			if (basePunishment.isPermanent()) {
				target.sendMessage(PluginMessages.VOCÊ_FOI_MUTADO_PERMANENTEMENTE.replace("%motivo%", motivo));
			} else {
				target.sendMessage(PluginMessages.VOCÊ_FOI_MUTADO_TEMPORARIAMENTE.replace("%motivo%", motivo).replace("%duração%", 
						DateUtils.formatDifference(tempo + 300L)));
			}
			BungeeMain.getManager().getSessionManager().getSessions().get(target.getName().toLowerCase()).setMute(basePunishment);
		}
	}
	
	private static void removePlayerFromClan(ProxiedPlayer quit, String playerNick, String clanName) {
		if (!Core.getClanManager().hasClanData(clanName)) {
			Core.getClanManager().loadClan(clanName);
		}
		
		
		Clan clan = Core.getClanManager().getClan(clanName);
		if (clan.getDono().equalsIgnoreCase(playerNick)) {
			
			for (String nick : clan.getMembros()) {
				 ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
				 if (target != null) {
					 target.sendMessage(PluginMessages.CLAN_DESFEITO);
					 BungeePlayer proxyTarget = BungeeMain.getManager().getSessionManager().getSession(nick.toLowerCase());
					 proxyTarget.setClan("Nenhum");
					 proxyTarget.setInClanChat(false);
					 
					 BungeeClient.sendPacketToServer(target, new PacketUpdateField(target.getName(), "CustomPlayer", "Clan", "Nenhum"));
				 } else {
					 MySQLManager.atualizarStatus("accounts", "clan", nick, "Nenhum");
					 
					 try {
						if (RedisAPI.categoryHasCache(nick, DataCategory.PRISMA_PLAYER)) {
							RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.CLAN, "Nenhum");
						 }
					} catch (Exception ex) {}
				 }
			}
			MySQLManager.deleteFromTable("clans", clan.getNome());
			Core.getClanManager().removeClanData(clan.getNome());
		} else {
			clan.removeAdmin(playerNick);
			clan.removeMembro(playerNick);
					
			for (Object object : clan.getOnlines()) {
				 ProxiedPlayer onlines = (ProxiedPlayer) object;
				 onlines.sendMessage("§6§l[CLAN] §7" + playerNick + " §fsaiu do clan.");		
			}
					
			clan.removeOnline(quit);
			
			Core.getClanManager().saveClan(clan.getNome());
		}
	}
}