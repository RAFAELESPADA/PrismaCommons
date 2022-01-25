package com.br.gabrielsilva.prismamc.commons.bukkit.api.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.tag.TagAPI;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.AdminChangeEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.AdminChangeEvent.ChangeType;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.server.ServerType;
import com.br.gabrielsilva.prismamc.commons.core.tags.Tag;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.PluginMessages;

import lombok.Getter;

@Getter
public class VanishManager implements Listener {

	private static List<Player> invisiveis = new ArrayList<>(),
			admin = new ArrayList<>();
	
	private static HashMap<UUID, ItemStack[]> itens = new HashMap<>(), 
			armadura = new HashMap<>();
	
	public static final ItemStack JOGADORES_ONLINE = new ItemBuilder().material(Material.COMPASS).name("§aJogadores Online").build(),
			JOGADORES_VIVOS = new ItemBuilder().material(Material.COMPASS).name("§aJogadores Vivos").build(),
			FAST_ADMIN = new ItemBuilder().material(Material.SLIME_BALL).name("§aTroca Rápida").build();
	
	public static void esconder(Player player) {
		if (invisiveis.contains(player)) {
			return;
		}
		invisiveis.add(player);
		
		Tag tag = 
				BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo().getTag();
		
		for (Player onlines : Bukkit.getOnlinePlayers()) {
			 if (!TagAPI.hasPermission(onlines, tag)) {
				 onlines.hidePlayer(player);
			 }
		}
		
		player.sendMessage(PluginMessages.PLAYER_FICOU_INVISIVEL.replace("%grupo%", tag.getColor() + "§l" + tag.getTag()));
		tag = null;
		player = null;
	}
	
	public static void mostrar(Player player) {
		if (!invisiveis.contains(player)) {
			return;
		}
		invisiveis.remove(player);
		
		for (Player onlines : Bukkit.getOnlinePlayers()) {
			 onlines.showPlayer(player);
		}
		
		player.sendMessage(PluginMessages.PLAYER_FICOU_VISIVEL);
	}
	
	public static void updateInvisibles(Player player) {
		for (Player invisible : invisiveis) {
			 if (invisible != null && invisible.isOnline()) {
				 Groups tag = 
						 BukkitMain.getManager().getDataManager().getBukkitPlayer(invisible.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo();
				 
				 if (TagAPI.hasPermission(player, tag)) {
					 player.showPlayer(invisible);
				 } else {
					 player.hidePlayer(invisible);
				 }
				 tag = null;
			 }
		}
	}
	
	public static void setInAdmin(Player player, boolean set) {
		if (set) {
			if (!admin.contains(player)) {
				admin.add(player);
			}
		} else {
			if (admin.contains(player)) {
				admin.remove(player);
			}
		}
	}
	
	public static void changeAdmin(Player player) {
		changeAdmin(player, true);
	}
	
	public static void changeAdmin(Player player, boolean callEvent) {
		if (admin.contains(player)) {
			player.sendMessage(PluginMessages.SAIU_DO_ADMIN);
			player.setGameMode(GameMode.SURVIVAL);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			if (itens.containsKey(player.getUniqueId())) {
				player.getInventory().setContents(itens.get(player.getUniqueId()));
				itens.remove(player.getUniqueId());
			}
			if (armadura.containsKey(player.getUniqueId())) {
				player.getInventory().setArmorContents(armadura.get(player.getUniqueId()));
				armadura.remove(player.getUniqueId());
			}
			
			final Groups tag =
					BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo();
			
			for (Player onlines : Bukkit.getOnlinePlayers()) {
				 if (player == onlines) {
					 continue;
				 }
				 onlines.showPlayer(player);
				 if (TagAPI.hasPermission(onlines, tag)) {
					 onlines.sendMessage(PluginMessages.PLAYER_SAIU_DO_ADMIN.replace("%nick%", player.getName()));
				 }
			}
			
			admin.remove(player);
			
			if (invisiveis.contains(player)) {
				invisiveis.remove(player);
			}
			
			if (callEvent) {
				Bukkit.getPluginManager().callEvent(new AdminChangeEvent(player, ChangeType.SAIU));
			}
			
		} else {
			player.sendMessage(PluginMessages.ENTROU_NO_ADMIN);
			
			if (!admin.contains(player)) {
			    admin.add(player);
			}
			
			if (!invisiveis.contains(player)) {
				invisiveis.add(player);
			}
			
			itens.put(player.getUniqueId(), player.getInventory().getContents());
			armadura.put(player.getUniqueId(), player.getInventory().getArmorContents());
			
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setGameMode(GameMode.CREATIVE);
			
			if (BukkitMain.getServerType() == ServerType.HG || BukkitMain.getServerType() == ServerType.EVENTO) {
				player.getInventory().setItem(3, JOGADORES_VIVOS);
			} else {
				player.getInventory().setItem(3, JOGADORES_ONLINE);
			}
			player.getInventory().setItem(5, FAST_ADMIN);
			
			final Groups tag =
					BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler().getData(DataType.GRUPO).getGrupo();
			
			for (Player onlines : Bukkit.getOnlinePlayers()) {
				 if (player == onlines) {
					 continue;
				 }
				 if (TagAPI.hasPermission(onlines, tag)) {
					 onlines.sendMessage(PluginMessages.PLAYER_ENTROU_NO_ADMIN.replace("%nick%", player.getName()));
				 } else {
					 onlines.hidePlayer(player);
				 }
			}
			
			if (callEvent) {
				Bukkit.getPluginManager().callEvent(new AdminChangeEvent(player, ChangeType.ENTROU));
			}
		}
	}
	
	public static void remove(Player player) {
		if (admin.contains(player)) {
			admin.remove(player);
		}
		if (invisiveis.contains(player)) {
			invisiveis.remove(player);
		}
		UUID uniqueId = player.getUniqueId();
		if (itens.containsKey(uniqueId)) {
			itens.remove(uniqueId);
		}
		if (armadura.containsKey(uniqueId)) {
			armadura.remove(uniqueId);
		}
	}
	@EventHandler
	/*     */   public void onInteractPlayerSlimeFun(PlayerInteractEvent e)
		/*     */   {
		/* 154 */     final Player p = e.getPlayer();
		/* 155 */     if ((admin.contains(p)) && (
				/* 156 */       (e.getAction() == Action.RIGHT_CLICK_BLOCK) || (e.getAction() == Action.RIGHT_CLICK_AIR)))
			/*     */     {
			/* 158 */       if (p.getItemInHand().getType() == Material.SLIME_BALL)
				/*     */       {
				/* 160 */         p.chat("/admin");
				/* 161 */         Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitMain.getInstance(), new Runnable()
						/*     */         {
					/*     */           public void run()
					/*     */           {
						/* 165 */             p.chat("/admin");
						/*     */           }
					/* 167 */         }, 10L);
				/*     */       }
			/*     */     }
		/*     */   }
	
	public static boolean inAdmin(Player player) {
		return admin.contains(player);
	}
	
	public static boolean isInvisivel(Player player) {
		return invisiveis.contains(player);
	}
}
