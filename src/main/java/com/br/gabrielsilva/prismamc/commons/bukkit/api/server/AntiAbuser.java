package com.br.gabrielsilva.prismamc.commons.bukkit.api.server;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.account.BukkitPlayer;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.PlayerGriefEvent;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

public class AntiAbuser implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventory(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		
		if (event.getSlot() < 0)
			return;
		
		Player player = (Player) event.getWhoClicked();
		if (player.hasMetadata("inventory-view")) {
			UUID uuid = UUID.fromString(player.getMetadata("inventory-view").get(0).asString());
			Player target = Bukkit.getPlayer(uuid);
			if (target != null && target.isOnline()) {
				final ItemStack itemClicked = event.getCurrentItem();
				
				String itemName = itemClicked.getType().name();
				if (itemClicked.getType() == Material.POTION) {
					itemName = "Poção de " + getPotionName(itemClicked.getDurability());
				}
				
			    if (itemClicked != null && itemClicked.getType() != Material.AIR) {
				    ServerAPI.sendLogAsync(player.getName() + " clicou no item '"+itemName+"' com o inventario do jogador: " + target.getName() + " aberto.");
					ServerAPI.warnStaff("§7[" + player.getName() + " clicou no item '"+itemName+" com o inventario do jogador " + target.getName() + " aberto.]", Groups.DONO);
				} else {
					BukkitMain.runLater(() -> {
						if (!target.isOnline()) {
							return;
						}
						if (target.getInventory().getItem(event.getSlot()) != null) {
							final ItemStack itemClicked1 = target.getInventory().getItem(event.getSlot());
							String itemName1 = itemClicked1.getType().name();
							if (itemClicked1.getType() == Material.POTION) {
								itemName1 = "Poção de " + getPotionName(itemClicked1.getDurability());
							}
							
							ServerAPI.sendLogAsync(player.getName() + " clicou no item '"+itemName1+"' com o inventario do jogador: " + target.getName() + " aberto.");
							ServerAPI.warnStaff("§7[" + player.getName() + " clicou no item '"+itemName1+" com o inventario do jogador " + target.getName() + " aberto.]", Groups.DONO);
						}
					}, 10L);
				}
			}
		}
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		if (player.hasMetadata("inventory-view")) {
			player.removeMetadata("inventory-view", BukkitMain.getInstance());
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			if (!VanishManager.inAdmin((Player) event.getDamager())) {
				return;
			}
			final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(event.getDamager().getUniqueId());
			if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
				return;
			}
			PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent((Player) event.getDamager()); 
			Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
			
			if (!playerGriefEvent.isCancelled()) {
			    ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " hitou " + ((Player)event.getEntity()).getName() + " utilizando " + 
			    ((Player) event.getDamager()).getItemInHand().getType().name());
			    
				ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " hitou um player no ADMIN]", Groups.DONO);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.PHYSICAL)) {
			return;
		}
		
		final Player player = event.getPlayer();
		if (player.getItemInHand().getType() != null && player.getItemInHand().getType() != Material.AIR) {
			if (player.getItemInHand().getType() == Material.POTION) {
				final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
					return;
				}
				
				PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent(player); 
				Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
				
				if (!playerGriefEvent.isCancelled()) {
					event.setCancelled(true);
					
					ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " arremessou uma poção do tipo " + getPotionName(player.getItemInHand().getDurability()));
					ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " arremessou uma poção do tipo " +
							getPotionName(player.getItemInHand().getDurability()) + "]", Groups.DONO);
				}
			} else if (player.getItemInHand().getType() == Material.LAVA_BUCKET) {
				if (event.getClickedBlock() == null) {
					return;
				}
				
				final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
					return;
				}
				
				PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent(player); 
				Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
				
				if (!playerGriefEvent.isCancelled()) {
					event.setCancelled(true);
				    ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " tentou utilizar um balde de lava.");
					ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " tentou utilizar um balde de lava]", Groups.DONO);
				}
			} else if (player.getItemInHand().getType() == Material.WATER_BUCKET) {
				if (event.getClickedBlock() == null) {
					return;
				}
				
				final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
					return;
				}
				
				PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent(player); 
				Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
				
				if (!playerGriefEvent.isCancelled()) {
					event.setCancelled(true);
					ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " tentou utilizar um balde de agua.");
					ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " tentou utilizar um balde de agua]", Groups.DONO);
				}
			} else if (player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
				if (event.getClickedBlock() == null) {
					return;
				}
				
				final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
					return;
				}
				
				PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent(player); 
				Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
				
				if (!playerGriefEvent.isCancelled()) {
					event.setCancelled(true);
					ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " utilizou o isqueiro.");
					ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " tentou utilizar isqueiro]", Groups.DONO);
				}
			} else if (player.getItemInHand().getType() == Material.MONSTER_EGG) {
				if (event.getClickedBlock() == null) {
					return;
				}
				
				final BukkitPlayer bukkitPlayer = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId());
				if (bukkitPlayer.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
					return;
				}
				
				PlayerGriefEvent playerGriefEvent = new PlayerGriefEvent(player); 
				Bukkit.getServer().getPluginManager().callEvent(playerGriefEvent);
				
				if (!playerGriefEvent.isCancelled()) {
					event.setCancelled(true);
					ServerAPI.sendLogAsync(bukkitPlayer.getNick() + " tentou spawnar um MOB");
					ServerAPI.warnStaff("§7[" + bukkitPlayer.getNick() + " tentou spawnar um MOB]", Groups.DONO);
				}
			}
		}
	}
	
	private final Integer[] potsRegen = {8193, 8225, 8257, 16385, 16417, 16449},
			potsSpeed = {8194, 8226, 8258, 16386, 16418, 16450},
			potsFireResistence = {8227, 8259, 16419, 16451},
			potsPosion = {8196, 8228, 8260, 16388, 16420, 16452},
			potsStrenght = {8201, 8233, 8265, 16393, 16425, 16457};
	
	private String getPotionName(int durability) {
		String name = "Unkown";
		
		for (Integer pot : potsRegen) {
			 if (durability == pot) {
				 name = "Regeneration";
				 break;
			 }
		}
		
		for (Integer pot : potsSpeed) {
			 if (durability == pot) {
				 name = "Speed";
				 break;
			 }
		}
		
		for (Integer pot : potsFireResistence) {
			 if (durability == pot) {
				 name = "FireResistence";
				 break;
			 }
		}
		
		for (Integer pot : potsPosion) {
			 if (durability == pot) {
				 name = "Poison";
				 break;
			 }
		}
		
		for (Integer pot : potsStrenght) {
			 if (durability == pot) {
				 name = "Strength";
				 break;
			 }
		}
		
		return name;
	}
}