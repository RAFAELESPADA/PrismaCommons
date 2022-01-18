package com.br.gabrielsilva.prismamc.commons.bukkit.api.tag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.TagUpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.listeners.ChatListener;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;

public class TagAPI {

	public static Groups getBestTag(Player player, Groups grupo) {
		if (grupo != Groups.MEMBRO) {
			return grupo;
		}
		if (BukkitMain.getServerType() == ServerType.EVENTO) {
			return Groups.EVENTO;
		}
		return Groups.MEMBRO;
	}
	
	public static void update(Player player, Groups group) {
		update(player, group.getTag());
	}
	
	public static void update(Player player, Tag tag) {
		String nick = player.getName();
		
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		bukkitPlayer.setTag(tag);
		
		boolean hasClan = !bukkitPlayer.getString(DataType.CLAN).equalsIgnoreCase("Nenhum");
		
		String teamID = tag.getTeamCharacter() + player.getUniqueId().toString().substring(0, 14),
				prefixo = tag.getNome() == Groups.MEMBRO.getTag().getNome() ? "§7" : tag.getColor() + "§l" + tag.getTag() + tag.getColor() + " ",
				sufixo = "",
				sufixoDisplayName = sufixo = getSuffix(bukkitPlayer.containsFake() ? PlayerRank.UNRANKED : bukkitPlayer.getRank());
		
		if (hasClan) {
			if (bukkitPlayer.containsFake()) {
				sufixo = getSuffix(PlayerRank.UNRANKED);
			} else {
				sufixo = getSuffixClan(bukkitPlayer.getString(DataType.CLAN));
			}
		} else {
			sufixo = getSuffix(bukkitPlayer.containsFake() ? PlayerRank.UNRANKED : bukkitPlayer.getRank());
		}
				
		Team now = createTeamIfNotExists(player, nick, teamID, prefixo, sufixo);
		
		for (Team old : player.getScoreboard().getTeams()) {
			 if (old.hasEntry(player.getName()) && !old.getName().equals(now.getName())) {
				 old.unregister();
				 old = null;
			 }
		}
		
		player.setDisplayName(tag.getNome() == Groups.MEMBRO.getTag().getNome() ? "§7" + player.getName() + sufixoDisplayName :
			tag.getColor() + "§l" + tag.getTag() + " " + tag.getColor() + player.getName() + sufixoDisplayName);
		
		for (Player o : Bukkit.getOnlinePlayers()) {
			 if (o.getUniqueId() == player.getUniqueId())
				 continue;
				
			 String nickOnline = o.getName(),
					 letherOnline = o.getUniqueId().toString().substring(0, 14);
			 
			BukkitPlayer bukkitPlayerOnline = BukkitMain.getManager().getDataManager().getBukkitPlayer(o.getUniqueId());
			
			Tag t = bukkitPlayerOnline.getTag();
			 
			String id = t.getTeamCharacter() + letherOnline;
			 
			boolean hasClan1 = !bukkitPlayerOnline.getString(DataType.CLAN).equalsIgnoreCase("Nenhum");
			
			String pre = t.getColor() + (t.getNome() == Groups.MEMBRO.getTag().getNome() ? "" : "§l" + t.getTag() + t.getColor() + " "), 
					suf = "";
			 
			if (hasClan1) {
				if (bukkitPlayerOnline.containsFake()) {
					suf = getSuffix(PlayerRank.UNRANKED);
				} else {
					suf = getSuffixClan(bukkitPlayerOnline.getString(DataType.CLAN));
				}
			} else {
				suf = getSuffix(bukkitPlayerOnline.containsFake() ? PlayerRank.UNRANKED : bukkitPlayerOnline.getRank());
			}
			
			Team to = createTeamIfNotExists(player, o.getName(), id, pre, suf);
			
			for (Team old : player.getScoreboard().getTeams()) {
				if (old.hasEntry(o.getName()) && !old.getName().equals(to.getName())) {
					old.unregister();
					old = null;
				}
			}
			
			createTeamIfNotExists(o, nick, now.getName(), now.getPrefix(), now.getSuffix());
			t = null;
			bukkitPlayerOnline = null;
			suf = null;
			pre = null;
			id = null;
		}
		teamID = null;
		player = null;
		prefixo = null;
		sufixo = null;
		tag = null;
		nick = null;
		bukkitPlayer = null;
		sufixoDisplayName = null;
		
		Bukkit.getServer().getPluginManager().callEvent(new TagUpdateEvent(player));
	}
	
	private static String getSuffix(PlayerRank playerRank) {
		return " §7[" + playerRank.getCor() + playerRank.getSimbolo() + "§7]";
	}
	
	private static String getSuffixClan(String clanName) {
		return " §8[" + ChatListener.clanTag.getOrDefault(clanName, "ERROR") + "]";
	}

	private static Team createTeamIfNotExists(Player p, String entrie, String teamID, String prefix, String suffix) {
		if (p.getScoreboard() == null) {
			p.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
		}
		
		Team team = p.getScoreboard().getTeam(teamID);
		if (team == null) {
			team = p.getScoreboard().registerNewTeam(teamID);
		}
		
		if (!team.hasEntry(entrie)) {
			team.addEntry(entrie);
		}
		
		team.setPrefix(prefix);
		team.setSuffix(suffix);
		return team;
	}
	
	public static boolean hasPermission(final Player player, final Groups group) {
		return hasPermission(player, group.getTag());
	}
	
	public static boolean hasPermission(final Player player, final Tag tag) {
		if (tag.getNivel() == Groups.RANDOM.getNivel()) {
			return false;
		} else if (tag.getNivel() == Groups.MEMBRO.getNivel()) {
			return true;
		} else if (tag.getNivel() == Groups.DEV.getNivel()) {
			if (BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getNick().equals("BiielBr")) {
				return true;
			} else {
				return false;
			}
		} else {
			if (player.hasPermission("tag.all")) {
				return true;
			}
			if (player.hasPermission("tag." + tag.getNome().toLowerCase())) {
				return true;
			}
			if (BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getNivel() >= tag.getNivel()) {
				return true;
			}
		}
		return false;
	}
}