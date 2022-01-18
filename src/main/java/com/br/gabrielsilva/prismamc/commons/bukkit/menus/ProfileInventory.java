package com.br.gabrielsilva.prismamc.commons.bukkit.menus;

import java.text.SimpleDateFormat;

import org.bukkit.Material;

import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuInventory;
import com.br.gabrielsilva.prismamc.commons.core.base.BasePunishment;
import com.br.gabrielsilva.prismamc.commons.core.connections.mysql.MySQLManager;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

public class ProfileInventory extends MenuInventory {

	public ProfileInventory(String title) {
		super(title, 6);
	}

	public void createInventory(String opener, BukkitPlayer bukkitPlayer, BasePunishment baseBan, BasePunishment baseMute) {
		final Groups tag = bukkitPlayer.getData(DataType.GRUPO).getGrupo();
		final PlayerRank playerRank = PlayerRank.getRanking(bukkitPlayer.getData(DataType.XP).getInt());
			
		setItem(13, new ItemBuilder().material(Material.SKULL_ITEM).durability(3).headName(bukkitPlayer.getNick())
		.name("§a" + bukkitPlayer.getNick()).lore(new String[] {
		"",
		"§fCargo: " + tag.getCor() + "§l" + tag.getNome(),
		"§fExpira em: §c" + (bukkitPlayer.getData(DataType.GRUPO_TIME).getLong() == 0 ? "Nunca" : 
		DateUtils.formatDifference(bukkitPlayer.getData(DataType.GRUPO_TIME).getLong())),
		"",
		"§fLiga: §7[" + playerRank.getCor() + playerRank.getSimbolo() + "§7] " + playerRank.getCor() + playerRank.getNome(),
		"§fXP atual: §a" + StringUtils.reformularValor(bukkitPlayer.getData(DataType.XP).getInt()),
		"",
		"§fRanking Global: §a#" + MySQLManager.getPlayerPositionRanking(bukkitPlayer.getNick()),
		"",
		"§fClan: §a" + (bukkitPlayer.getData(DataType.CLAN).getString().equalsIgnoreCase("Nenhum") ? 
				"§cN/A" : bukkitPlayer.getData(DataType.CLAN).getString())
		}).build());
				
		setItem(29, new ItemBuilder().material(Material.MUSHROOM_SOUP).name("§aHungerGames").lore(new String[] {
		"§fVitórias: §7" + bukkitPlayer.getData(DataType.HG_WINS).getInt(),
		"§fKills: §7" + bukkitPlayer.getData(DataType.HG_KILLS).getInt(),
		"§fDeaths: §7" + bukkitPlayer.getData(DataType.HG_DEATHS).getInt(),
		}).build());
			
		setItem(31, new ItemBuilder().material(Material.IRON_FENCE).name("§aGladiator").lore(new String[] {
		"§fVitórias: §7" + bukkitPlayer.getData(DataType.GLADIATOR_WINS).getInt(),
		"§fDerrotas: §7" + bukkitPlayer.getData(DataType.GLADIATOR_LOSES).getInt(),
		"§fWinStreak: §7" + bukkitPlayer.getData(DataType.GLADIATOR_WINSTREAK).getInt(),
		"§fWinStreak maximo: §7" + bukkitPlayer.getData(DataType.GLADIATOR_MAXWINSTREAK).getInt(),
		}).build());
		
		setItem(33, new ItemBuilder().material(Material.IRON_CHESTPLATE).name("§aKitPvP").lore(new String[] {
		"§fMatou: §7" + bukkitPlayer.getData(DataType.PVP_KILLS).getInt(),
		"§fMorreu: §7" + bukkitPlayer.getData(DataType.PVP_DEATHS).getInt(),
		"§fKillStreak: §7" + bukkitPlayer.getData(DataType.PVP_KILLSTREAK).getInt(),
		"§fKillStreak maximo: §7" + bukkitPlayer.getData(DataType.PVP_MAXKILLSTREAK).getInt()
		}).build());
		
		String[] naoBanido = new String[] {
		"§fBanido §cNão"};
		
		String[] banido = new String[] {
			"§fBanido §aSim",
			"§fMotivo §c" + baseBan.getMotivo(),
			"§fExpira " + (baseBan.isPermanent() ? "§cNUNCA" : "em §c" + DateUtils.formatDifference(baseBan.getPunishmentTime())),
			"§fPunido por §c" + baseBan.getPunidoPor()};
		
		setItem(47, new ItemBuilder().material(Material.getMaterial(356)).name("§cBan").lore(baseBan.isAplicado() ? banido : naoBanido).build());
		
		String[] naoMutado = new String[] {
		"§fMutado §cNão"};
		
		String[] mutado = new String[] {
			"§fMutado §aSim",
			"§fMotivo §c" + baseMute.getMotivo(),
			"§fExpira " + (baseMute.isPermanent() ? "§cNUNCA" : "em §c" + DateUtils.formatDifference(baseMute.getPunishmentTime())),
			"§fPunido por §c" + baseMute.getPunidoPor()};
		
		setItem(51, new ItemBuilder().material(Material.REDSTONE_COMPARATOR).name("§cMute").lore(baseMute.isAplicado() ? mutado : naoMutado).build());
			
		String firstLogin = "Nunca",
				lastLogin = "Nunca",
				lastLoggout = "Nunca";
		
		if (bukkitPlayer.getData(DataType.FIRST_LOGGED_IN).getLong() != 0) {
			firstLogin = "" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(bukkitPlayer.getData(DataType.FIRST_LOGGED_IN).getLong());
		}
		
		if (bukkitPlayer.getData(DataType.LAST_LOGGED_IN).getLong() != 0) {
			lastLogin = "" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(bukkitPlayer.getData(DataType.LAST_LOGGED_IN).getLong());
		}
		
		if (bukkitPlayer.getData(DataType.LAST_LOGGED_OUT).getLong() != 0) {
			lastLoggout = "" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(bukkitPlayer.getData(DataType.LAST_LOGGED_OUT).getLong());
		}
		
		setItem(49, new ItemBuilder().material(Material.PAPER).name("§aInformações de Login").lore(new String[] {
		"§fPrimeiro Login: §7" + firstLogin,
		"§fUltimo Login: §7" + lastLogin,
		"§fUltimo loggout: §7" + lastLoggout
		}).build());
	}
}