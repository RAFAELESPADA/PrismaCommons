package com.br.gabrielsilva.prismamc.commons.bungee.commands.register;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.br.gabrielsilva.prismamc.commons.core.connections.redis.RedisAPI;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;
import com.br.gabrielsilva.prismamc.commons.bungee.account.BungeePlayer;
import com.br.gabrielsilva.prismamc.commons.bungee.commands.BungeeCommandSender;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.clan.Clan;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BungeeClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketSetClanTag;
import com.br.gabrielsilva.prismamc.commons.custompackets.bukkit.packets.PacketUpdateField;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClanCommand implements CommandClass {
	
	public static HashMap<String, String> Convite = new HashMap<>();
	
	@Command(name = "clan", runAsync = true)
	public void execute(BungeeCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
		
		if (args.length == 0) {
			sendHelp(commandSender);
			return;
		}
		ProxiedPlayer proxiedPlayer = commandSender.getPlayer();
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("sair")) {
				sair(proxiedPlayer);
			} else if (args[0].equalsIgnoreCase("deletar")) {
				deletar(proxiedPlayer);
			} else if (args[0].equalsIgnoreCase("chat")) {
				BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
				if (proxyPlayer.getClan().equals("Nenhum")) {
					proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
					return;
				}
				if (proxyPlayer.isInClanChat()) {
					proxyPlayer.setInClanChat(false);
					proxiedPlayer.sendMessage(PluginMessages.VOCÊ_SAIU_DO_CHAT_DO_CLAN);
				} else {
					proxyPlayer.setInClanChat(true);
					proxiedPlayer.sendMessage(PluginMessages.VOCÊ_ENTROU_NO_CHAT_DO_CLAN);
				}
			} else if (args[0].equalsIgnoreCase("aceitar")) { 
				aceitar(proxiedPlayer);
			} else if (args[0].equalsIgnoreCase("stats")) { 
				BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
				if (proxyPlayer.getClan().equals("Nenhum")) {
					proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
					return;
				}
				sendClanInfo(proxiedPlayer, proxyPlayer.getClan());
			} else if (args[0].equalsIgnoreCase("top")) {
				sendTopClans(proxiedPlayer);
			} else {
				sendHelp(commandSender);
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("convidar")) {
				ProxiedPlayer p1 = ProxyServer.getInstance().getPlayer(args[1]);
				convidar(proxiedPlayer, p1);
			} else if (args[0].equalsIgnoreCase("expulsar")) {
				expulsar(proxiedPlayer, args[1]);
			} else if (args[0].equalsIgnoreCase("promover")) {
				addMod(proxiedPlayer, args[1]);
			} else if (args[0].equalsIgnoreCase("rebaixar")) {
				removeAdmin(proxiedPlayer, args[1]);
			} else if (args[0].equalsIgnoreCase("stats")) {
				sendClanInfo(proxiedPlayer, args[1]);
			} else {
				sendHelp(proxiedPlayer);
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("criar")) {
				criar(proxiedPlayer, args[1], args[2]);
			} else {
				sendHelp(proxiedPlayer);
			}
		} else {
			sendHelp(proxiedPlayer);
		}
		return;
	}

	private void sendTopClans(ProxiedPlayer proxiedPlayer) {
		BungeeMain.runAsync(() -> {
			try {
				PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
				"SELECT * FROM clans ORDER BY elo DESC LIMIT 10");
				
				ResultSet result = preparedStatament.executeQuery();
				proxiedPlayer.sendMessage("");
				proxiedPlayer.sendMessage("§6§lTOP 10 CLANS!");
				proxiedPlayer.sendMessage("");
				
				int id = 0;
				while (result.next()) {
					id++;
					
					proxiedPlayer.sendMessage("§a" + id + " §7- " + result.getString("nome") + " §f: §e" + StringUtils.reformularValor(result.getInt("elo")));
				}
				
				while (id != 10) {
					id++;
					proxiedPlayer.sendMessage("§a" + id + " §7- Nenhum §f: §e0");
				}
				result.close();
				preparedStatament.close();
				proxiedPlayer.sendMessage("");
				proxiedPlayer.sendMessage("§aUtilize /clan stats <Nome> para ver o status de um clan.");
			} catch (SQLException ex) {
				proxiedPlayer.sendMessage("§cOcorreu um erro ao tentar obter a lista atualizada.");
				
			}
		});
	}

	private	void criar(ProxiedPlayer player, String nome, String tag) {
		if (nome.equalsIgnoreCase("Nenhum")) {
			player.sendMessage(PluginMessages.CLAN_NAME_INVALIDO);
			return;
		}
		if (nome.length() < 5 || nome.length() > 20) {
			player.sendMessage(PluginMessages.CLAN_NAME_INVALIDO2);
			return;
		}
		if (tag.length() <= 3 || tag.length() > 5) {
			player.sendMessage(PluginMessages.CLAN_TAG_INVALIDA);
			return;
		}
		if (!isLegal(nome)) {
			player.sendMessage("§cNome inválido.");
			return;
		}
		if (!isLegal(tag)) {
			player.sendMessage("§cTAG inválida.");
			return;
		}
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(player.getName().toLowerCase());
		if (!proxyPlayer.getClan().equalsIgnoreCase("Nenhum")) {
			player.sendMessage(PluginMessages.VOCÊ_JA_ESTA_EM_UM_CLAN);
			return;
		}
		
		if (MySQLManager.contains("clans", "nome", nome)) {
			player.sendMessage(PluginMessages.CLAN_EXIST);
			return;
		}
		
		try {
			PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
			"INSERT INTO clans(nome, tag, dono, membros, admins, elo, participantes) VALUES (?, ?, ?, ?, ?, ?, ?)");
			
			preparedStatament.setString(1, nome);
			preparedStatament.setString(2, tag);
			preparedStatament.setString(3, player.getName());
			preparedStatament.setString(4, "");
			preparedStatament.setString(5, "");
			preparedStatament.setInt(6, 0);
			preparedStatament.setInt(7, 1);
			
			preparedStatament.executeUpdate();
			
			preparedStatament.close();
		} catch (SQLException ex) {
			Core.console("Erro ao criar um clan -> " + ex.getLocalizedMessage());
			player.sendMessage("§cOcorreu um erro ao tentar criar o clan, tente novamente.");
			return;
		}
		
		proxyPlayer.setClan(nome);
		player.sendMessage(PluginMessages.CLAN_CRIADO);
		
		Clan clan = new Clan(nome, player.getName(), tag, 0, 1, new ArrayList<>(), new ArrayList<>());
		clan.addOnline(player);
		Core.getClanManager().putClan(nome, clan);
			
		BungeeClient.sendPacketToServer(player, new PacketSetClanTag(nome, tag));
		BungeeClient.sendPacketToServer(player, new PacketUpdateField(player.getName(), "CustomPlayer", "Clan", nome));
	}
	
	private void deletar(ProxiedPlayer proxiedPlayer) {
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equalsIgnoreCase("Nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (!clanStatus.getDono().equals(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.VOCE_NÃO_PODE_DELETAR);
			return;
		}
		
		for (String nick : clanStatus.getAllParticipantes()) {
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
		
		proxyPlayer.setClan("Nenhum");
		MySQLManager.deleteFromTable("clans", clanStatus.getNome());
		Core.getClanManager().removeClanData(clanStatus.getNome());
	}
	
	private void sair(ProxiedPlayer proxiedPlayer) {
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equals("Nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (clanStatus.getDono().equals(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_PODE_SAIR);
			return;
		}
		clanStatus.removeOnline(proxiedPlayer);
		clanStatus.removeAdmin(proxiedPlayer.getName());
		clanStatus.removeMembro(proxiedPlayer.getName());
		
		proxyPlayer.setClan("Nenhum");
		proxyPlayer.setInClanChat(false);
		
		for (Object object : clanStatus.getOnlines()) {
			 ProxiedPlayer ons = (ProxiedPlayer) object;
			 ons.sendMessage(PluginMessages.PLAYER_SAIU_DO_CLAN.replace("%nick%", proxiedPlayer.getName()));
		}
		proxiedPlayer.sendMessage(PluginMessages.VOCÊ_SAIU_DO_CLAN);
		
		BungeeClient.sendPacketToServer(proxiedPlayer, new PacketUpdateField(proxiedPlayer.getName(), "CustomPlayer", "Clan", "Nenhum"));
		
		Core.getClanManager().saveClan(clanStatus.getNome());
	}
	
	private void expulsar(ProxiedPlayer proxiedPlayer, String nick) {
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equals("Nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (!clanStatus.hasPerm(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NAO_PODE_EXPULSAR);
			return;
		}
		
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (target == null) {
			
			try {
				PreparedStatement preparedStatament = Core.getMySQL().getConexão().prepareStatement(
				"SELECT * FROM accounts WHERE nick='"+nick+"'");
				ResultSet result = preparedStatament.executeQuery();
				if (!result.next()) {
					proxiedPlayer.sendMessage(PluginMessages.NAO_TEM_CONTA);
					result.close();
					preparedStatament.close();
					return;
				}
				String realNick = result.getString("nick"), 
						clan = result.getString("clan");
				
				result.close();
				preparedStatament.close();
				if (!clan.equalsIgnoreCase(proxyPlayer.getClan())) {
					proxiedPlayer.sendMessage(PluginMessages.JOGADOR_NAO_ESTA_NO_SEU_CLAN);
					return;
				}
				
				clanStatus.removeAdmin(realNick);
				clanStatus.removeMembro(realNick);
				
				proxiedPlayer.sendMessage(PluginMessages.JOGADOR_EXPULSO_CLAN.replace("%nick%", nick));
				MySQLManager.atualizarStatus("accounts", "clan", nick, "Nenhum");
				
				 try {
					if (RedisAPI.categoryHasCache(nick, DataCategory.PRISMA_PLAYER)) {
						RedisAPI.modifyValue(nick, DataCategory.PRISMA_PLAYER, DataType.CLAN, "Nenhum");
					 }
				} catch (Exception ex) {}
				 
				 Core.getClanManager().saveClan(realNick);
			} catch (SQLException ex) {
				Core.console("Ocorreu um erro ao tentar expulsar um jogador offline -> " + ex.getLocalizedMessage());
				proxiedPlayer.sendMessage("§cOcorreu um erro, tente novamente.");
				return;
			}
		} else {
			if (target == proxiedPlayer) {
				proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_PODE_SE_EXPULSAR);
				return;
			}
			BungeePlayer proxyTarget = BungeeMain.getManager().getSessionManager().getSession(target.getName().toLowerCase());
			if (proxyTarget.getClan().equals(proxyPlayer.getClan())) {
				proxiedPlayer.sendMessage(PluginMessages.JOGADOR_NAO_ESTA_NO_SEU_CLAN);
				return;
			}
			if (clanStatus.getDono().equals(target.getName())) {
				proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_PODE_EXPULSAR_O_DONO);
				return;
			}
			
			proxyTarget.setClan("Nenhum");
			proxyTarget.setInClanChat(false);
			clanStatus.removeOnline(target);
			clanStatus.removeAdmin(target.getName());
			clanStatus.removeMembro(target.getName());
			
			target.sendMessage(PluginMessages.VOCÊ_FOI_EXPULSO_DO_CLAN);
			
			for (Object object : clanStatus.getOnlines()) {
				 ProxiedPlayer ons = (ProxiedPlayer) object;
				 ons.sendMessage(PluginMessages.PLAYER_EXPULSO_DO_CLAN.replace("%nick%", target.getName()));
			}
			
			BungeeClient.sendPacketToServer(target, 
					new PacketUpdateField(target.getName(), "CustomPlayer", "Clan", "Nenhum"));
			
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_EXPULSO_CLAN.replace("%nick%", target.getName()));
			
			Core.getClanManager().saveClan(clanStatus.getNome());
		}
	}
	
	private void aceitar(ProxiedPlayer proxiedPlayer) {
		if (!Convite.containsKey(proxiedPlayer.getName().toLowerCase())) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_TEM_CONVITE_PENDENTE);
			return;
		}
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (!proxyPlayer.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_JA_ESTA_EM_UM_CLAN);
			Convite.remove(proxiedPlayer.getName().toLowerCase());
			return;
		}
		if (!Core.getClanManager().hasClanData(Convite.get(proxiedPlayer.getName().toLowerCase()))) {
			proxiedPlayer.sendMessage(PluginMessages.CLAN_INEXISTENTE);
			Convite.remove(proxiedPlayer.getName().toLowerCase());
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(Convite.get(proxiedPlayer.getName().toLowerCase()));
		if (clanStatus.getParticipantes() >= 15) {
			proxiedPlayer.sendMessage(PluginMessages.CLAN_FULL);
			Convite.remove(proxiedPlayer.getName().toLowerCase());
			return;
		}
		for (Object object : clanStatus.getOnlines()) {
			 ProxiedPlayer ons = (ProxiedPlayer) object;
			 ons.sendMessage(PluginMessages.PLAYER_ENTROU_NO_CLAN.replace("%nick%", proxiedPlayer.getName()));
		}
		proxyPlayer.setClan(clanStatus.getNome());
		clanStatus.addOnline(proxiedPlayer);
		clanStatus.addMembro(proxiedPlayer.getName());
		Convite.remove(proxiedPlayer.getName().toLowerCase());
		
		proxiedPlayer.sendMessage(PluginMessages.VOCÊ_ENTROU_NO_CLAN);
		
		BungeeClient.sendPacketToServer(proxiedPlayer, 
				new PacketUpdateField(proxiedPlayer.getName(), "CustomPlayer", "Clan", clanStatus.getNome()));
		
		BungeeClient.sendPacketToServer(proxiedPlayer, new PacketSetClanTag(clanStatus.getNome(), clanStatus.getTag()));
		
		Core.getClanManager().saveClan(clanStatus.getNome());
	}
	
	private void convidar(ProxiedPlayer proxiedPlayer, ProxiedPlayer proxiedPlayer1) {
		if (!BungeeMain.isValid(proxiedPlayer1)) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		if (proxiedPlayer1 == proxiedPlayer) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NAO_PODE_SE_CONVIDAR);
			return;
		}
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (!clanStatus.hasPerm(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NAO_PODE_CONVIDAR);
			return;
		}
		if (Convite.containsKey(proxiedPlayer1.getName().toLowerCase())) {
			proxiedPlayer.sendMessage(PluginMessages.JA_TEM_CONVITE);
			return;
		}
		BungeePlayer proxyPlayer1 = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer1.getName().toLowerCase());
		if (!proxyPlayer1.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_JA_ESTA_EM_UM_CLAN);
			return;
		}
		if (clanStatus.getParticipantes() >= 15) {
			proxiedPlayer.sendMessage(PluginMessages.CLAN_FULL);
			return;
		}
		proxiedPlayer.sendMessage(PluginMessages.VOCÊ_CONVIDOU.replace("%nick%", proxiedPlayer1.getName()));	
		proxiedPlayer1.sendMessage(PluginMessages.VOCÊ_RECEBEU_CONVITE.replace("%clan%", clanStatus.getNome()));
		Convite.put(proxiedPlayer1.getName().toLowerCase(), clanStatus.getNome());
		ProxyServer.getInstance().getScheduler().schedule(BungeeMain.getInstance(), () -> {
			if (Convite.containsKey(proxiedPlayer1.getName().toLowerCase())) {
				Convite.remove(proxiedPlayer1.getName().toLowerCase());
			}
		}, 15, TimeUnit.SECONDS);
	}
	
	private void sendClanInfo(ProxiedPlayer proxiedPlayer, String nome) {
		if (!MySQLManager.contains("clans", "nome", nome)) {
			proxiedPlayer.sendMessage(PluginMessages.CLAN_INEXISTENTE);
			return;
		}
		
		if (!Core.getClanManager().hasClanData(nome)) {
			Core.getClanManager().loadClan(nome);
		}
		
		
		final Clan clanData = Core.getClanManager().getClan(nome);
		//final ClanRank clanRank = ClanRank.getRanking(clanData.getElo());
		
		proxiedPlayer.sendMessage("");
		proxiedPlayer.sendMessage("Nome: §a" + clanData.getNome() + " §8[§7" + clanData.getTag() + "§8]");
		//proxiedPlayer.sendMessage("Rank: " + clanRank.getCor() + clanRank.getSimbolo() + " " + clanRank.getNome());
		proxiedPlayer.sendMessage("ELO: §b" + StringUtils.reformularValor(clanData.getElo()));
		//proxiedPlayer.sendMessage("Posição: §a#" + getClanPosition(clanData.getNome()));
		
		String membros = "", admins = "";
		
	    if (clanData.getMembros().size() != 1) {
			for (String nick : clanData.getMembros()) {
				 if (nick.equals(clanData.getDono())) {
					 continue;
				 }
				 if (clanData.getAdmins().contains(nick)) {
					 continue;
				 }
				 if (membros.equals("")) {
					 membros = "§f,§7" + nick;
				 } else {
					 membros = membros + "§f,§7" + nick;
				 }
			}
	    }
		
	    if (clanData.getAdmins().size() != 1) {
			for (String nick : clanData.getAdmins()) {
				 if (nick.equals(clanData.getDono())) {
					 continue;
				 }
				 if (admins.equals("")) {
					 admins = "§f, §c" + nick;
				 } else {
					 admins = admins + "§f, §c" + nick;
				 }
			}
	    }
	    
	    proxiedPlayer.sendMessage("§fMembros: §4" + clanData.getDono() + membros + admins);
	    proxiedPlayer.sendMessage("");
	}

	private void addMod(ProxiedPlayer proxiedPlayer, String nick) {
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (!clanStatus.getDono().equals(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.APENAS_O_DONO_PODE_PROMOVER);
			return;
		}
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (!BungeeMain.isValid(target)) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		if (target == proxiedPlayer) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_PODE_SE_PROMOVER);
			return;
		}
		BungeePlayer proxyTarget = BungeeMain.getManager().getSessionManager().getSession(target.getName().toLowerCase());
		if (proxyTarget.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_JA_ESTA_EM_UM_CLAN);
			return;
		}
		if (!proxyTarget.getClan().equals(proxyPlayer.getClan())) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_NAO_ESTA_NO_SEU_CLAN);
			return;
		}
		if (clanStatus.getAdmins().contains(target.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.JA_ESTA_PROMOVIDO);
			return;
		}
		clanStatus.removeMembro(target.getName());
		clanStatus.addAdmin(target.getName());
		target.sendMessage(PluginMessages.VOCÊ_FOI_PROMOVIDO);
		proxiedPlayer.sendMessage("§aJogador promovido com sucesso.");
		
		Core.getClanManager().saveClan(clanStatus.getNome());
	}
	
	private void removeAdmin(ProxiedPlayer proxiedPlayer, String nick) {
		BungeePlayer proxyPlayer = BungeeMain.getManager().getSessionManager().getSession(proxiedPlayer.getName().toLowerCase());
		if (proxyPlayer.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.VOCÊ_NÃO_ESTA_EM_UM_CLAN);
			return;
		}
		Clan clanStatus = Core.getClanManager().getClan(proxyPlayer.getClan());
		if (!clanStatus.getDono().equals(proxiedPlayer.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.APENAS_O_DONO_PODE_REBAIXAR);
			return;
		}
		ProxiedPlayer target = ProxyServer.getInstance().getPlayer(nick);
		if (!BungeeMain.isValid(target)) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		if (target == proxiedPlayer) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_OFFLINE);
			return;
		}
		BungeePlayer proxyTarget = BungeeMain.getManager().getSessionManager().getSession(target.getName().toLowerCase());
		if (proxyTarget.getClan().equalsIgnoreCase("nenhum")) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_NAO_ESTA_NO_SEU_CLAN);
			return;
		}
		if (!proxyTarget.getClan().equals(proxyPlayer.getClan())) {
			proxiedPlayer.sendMessage(PluginMessages.JOGADOR_NAO_ESTA_NO_SEU_CLAN);
			return;
		}
		if (!clanStatus.getAdmins().contains(target.getName())) {
			proxiedPlayer.sendMessage(PluginMessages.NAO_ESTA_PROMOVIDO);
			return;
		}
		clanStatus.removeAdmin(target.getName());
		clanStatus.addMembro(target.getName());
		target.sendMessage(PluginMessages.VOCÊ_FOI_REBAIXADO);
		proxiedPlayer.sendMessage("§aJogador rebaixado com sucesso.");
		
		Core.getClanManager().saveClan(clanStatus.getNome());
	}
	
	private void sendHelp(CommandSender commandSender) {
		commandSender.sendMessage("§6§lCLANS:");
		commandSender.sendMessage("");
		commandSender.sendMessage("§bUse /clan criar [nome] [tag]  -  Para criar um clan.");
		commandSender.sendMessage("§bUse /clan convidar [nick]  - Para convidar um jogador para seu clan.");
		commandSender.sendMessage("§bUse /clan aceitar -  Para aceitar um convite recente.");
		commandSender.sendMessage("§bUse /clan promover [nick]  - Para adicionar um administrador ao clan.");
		commandSender.sendMessage("§bUse /clan rebaixar [nick] - Para remover um administrador do clan.");
		commandSender.sendMessage("§bUse /clan chat - Para entrar/sair no chat clan.");
		commandSender.sendMessage("§bUse /clan sair  §f-  §bPara sair do clan.");
		commandSender.sendMessage("§bUse /clan deletar - Para deletar o clan.");
		commandSender.sendMessage("§bUse /clan expulsar [nick] - Para expulsar um jogador do clan.");
		commandSender.sendMessage("§bUse /clan stats - Para ver os status do seu clan.");
		commandSender.sendMessage("§bUse /clan stats [nome] - Para ver os status de um clan.");
		commandSender.sendMessage("§bUse /clan top - Para ver o ranking atualizado.");
		commandSender.sendMessage("");
	}
	
	public boolean isLegal(String clanName) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]{3,16}");
		Matcher matcher = pattern.matcher(clanName);
		return matcher.matches();
	}
}