package com.br.gabrielsilva.prismamc.commons.bukkit.api.player;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.title.TitleAPI;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.rank.PlayerRank;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

import redis.clients.jedis.Jedis;

public class PlayerAPI {
	
	public static void updateTab(Player player) {
		BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
		
		final PlayerRank rank = PlayerRank.getRanking(bukkitPlayer.getInt(DataType.XP));
		final Groups tag = bukkitPlayer.getData(DataType.GRUPO).getGrupo();
		
		final String clan = bukkitPlayer.getString(DataType.CLAN).equalsIgnoreCase("Nenhum") ? 
				"§cN/A" : "§a" + bukkitPlayer.getString(DataType.CLAN);
		
		String header = "\n§6§lKombo§f§lPvP\n\n"
				+ "          §7Nome: §a" + bukkitPlayer.getNick() + " §0- §7Liga: " + rank.getCor() + rank.getNome() + " §0- §7Cargo: " + tag.getCor() + "§l" + tag.getNome() + "\n"
				+ "§7Clan: " + clan + " §0- §7Moedas: §e" + StringUtils.reformularValor(bukkitPlayer.getInt(DataType.COINS)) + "\n\n";
		
		String footer = "\n\n  §7Discord: §5https://discord.gg/DZsabJSKdB\n"
		         + "  §7Loja: §6https://discord.gg/DZsabJSKdB\n"
				 + "§7Twitter: §b@KomboPvP7\n";
		
		TitleAPI.setHeaderAndFooter(player, header, footer);
	}
	
	public static final int KILL_COINS = 20, 
			DEATH_COINS = 5,
			DEATH_XP = 5;
	
	public static int getXPKill(Player killer, Player death) {
		int value = 0;
		
		final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(killer.getUniqueId());
		
		final PlayerRank rankKiller = PlayerRank.getRanking(bukkitPlayer.getInt(DataType.XP)),
				rankDeath = PlayerRank.getRanking(BukkitMain.getManager().getDataManager().getBukkitPlayer(death.getUniqueId()).getInt(DataType.XP));
		
		Random random = new Random();
		
		if (rankKiller == rankDeath) {
			value = (15 + random.nextInt(5));
		} else {
			if (rankDeath.getMax() > rankKiller.getMax()) {
				value = (20 + random.nextInt(5));
			} else {
				value = (5 + random.nextInt(10));
			}
		}
		
		if (value <= 0) {
			value = 5;
		}
		
		if (hasDoubleXP(bukkitPlayer.getDataHandler().getData(DataType.DOUBLEXP_TIME).getLong())) {
			value = (value * 2);
			killer.sendMessage("§b+ " + value + " XP (2x)");
		} else {
			killer.sendMessage("§b+ " + value + " XP");
		}
		return value;
	}
	
	private static boolean hasDoubleXP(Long long1) {
		if (ServerOptions.isDoubleXP()) {
			return true;
		}
		if (long1 > System.currentTimeMillis()) {
			return true;
		} else {
			return false;
		}
	}

	public static String getHealth(Player player) {
		Damageable vida = player;
		return NumberFormat.getCurrencyInstance().format(vida.getHealth() / 2).replace("$", "").replace("R", "")
				.replace(",", ".");
	}
	
	public static String getHealth(double health) {
		return NumberFormat.getCurrencyInstance().format(health / 2).replace("$", "").replace("R", "")
				.replace(",", ".");
	}
	
    public static void clearScoreBoard(Player player) {
    	player.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);
    	player.setScoreboard(player.getScoreboard());
    }
    
	public static String getAddress(Player p) {
		return p.getAddress().getAddress().getHostAddress();
	}
	
	public static void dropItemsPvP(Player player, Location loc) {
		ArrayList<ItemStack> itens = new ArrayList<>();
		
	    for (ItemStack item : player.getPlayer().getInventory().getContents()) {
	         if ((item != null) && (item.getType() != Material.AIR)) {
	        	  if (item.getType() == Material.BROWN_MUSHROOM || item.getType() == Material.RED_MUSHROOM || item.getType() == Material.BOWL ||
	        			  item.getType() == Material.MUSHROOM_SOUP) {
	        		  itens.add(item);
	        	  }
	         }
	    }
		
		dropItems(player, itens, loc);
	}
	
	public static void dropItems(Player p, Location l) {
	    ArrayList<ItemStack> itens = new ArrayList<>();
	    for (ItemStack item : p.getPlayer().getInventory().getContents()) {
	         if ((item != null) && (item.getType() != Material.AIR)) {
	        	  if (item.hasItemMeta()) {
	        		  if ((item.getItemMeta().hasDisplayName()) && (item.getItemMeta().getDisplayName().startsWith("§bKit"))) {
	        			  continue;
	        		  }
	        		  itens.add(item.clone());
	        	  } else {
	        		  itens.add(item);
	        	  }
	         }
	    }
	    for (ItemStack item : p.getPlayer().getInventory().getArmorContents()) {
	         if ((item != null) && (item.getType() != Material.AIR)) {
	              itens.add(item.clone());
	         }
	    }
	    if ((p.getPlayer().getItemOnCursor() != null) && (p.getPlayer().getItemOnCursor().getType() != Material.AIR)) {
	         itens.add(p.getPlayer().getItemOnCursor().clone());
	    }
	    dropItems(p, itens, l);
	}
	  
	public static void dropItems(Player p, List<ItemStack> itens, Location l) {
		World world = l.getWorld();
		
		for (ItemStack item : itens) {
		  	 if ((item != null) && (item.getType() != Material.AIR)) {
		         if (item.hasItemMeta()) {
		             world.dropItemNaturally(l, item.clone()).getItemStack().setItemMeta(item.getItemMeta());
		         } else {
		             world.dropItemNaturally(l, item);
		         }
		  	 }
		}
		
		p.getPlayer().getInventory().setArmorContents(new ItemStack[4]);
		p.getPlayer().getInventory().clear();
		p.getPlayer().setItemOnCursor(new ItemStack(0));
		
		itens.clear();
		itens = null;
	}
	
	public static void clearEffects(Player p) {
		for (PotionEffect effect : p.getActivePotionEffects()) {
		     p.removePotionEffect(effect.getType());
		}
	}
	
	public static boolean isFull(Inventory p) {
		if (p.firstEmpty() == -1) {
			return true;
		}
		return false;
	}
	
    public static String getAddresByRedis(String nick) {
    	String ip = "";
    	if (Core.getRedis().isConnected()) {
			try (Jedis jedis = Core.getRedis().getPool().getResource()) {
				if (jedis.exists("ip-" + nick.toLowerCase())) {
					ip = jedis.get("ip-" + nick.toLowerCase());
				}
			}
    	}
    	return ip;
    }
}