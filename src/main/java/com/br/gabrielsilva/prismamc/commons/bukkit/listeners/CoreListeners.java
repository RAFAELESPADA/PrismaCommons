package com.br.gabrielsilva.prismamc.commons.bukkit.listeners;


import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.player.VanishManager;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.server.ServerOptions;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;

public class CoreListeners implements Listener {

	private static ItemStack lapis;
	
	public CoreListeners() {
		ItemStack sopa = new ItemBuilder().material(Material.MUSHROOM_SOUP).build();
		ShapelessRecipe cocoa = new ShapelessRecipe(sopa);
		ShapelessRecipe cactus = new ShapelessRecipe(sopa);

		cactus.addIngredient(Material.BOWL);
		cactus.addIngredient(1, Material.CACTUS);
		
		cocoa.addIngredient(Material.BOWL);
		cocoa.addIngredient(Material.INK_SACK, 3);

		Bukkit.addRecipe(cocoa);
		Bukkit.addRecipe(cactus);
		
	    Dye d = new Dye();
	    d.setColor(DyeColor.BLUE);
	    lapis = d.toItemStack();
	    lapis.setAmount(3);
	}
	
	@EventHandler
	public void openInventoryEvent(InventoryOpenEvent e) {
	    if (e.getInventory() instanceof EnchantingInventory) {
	        e.getInventory().setItem(1, lapis);
	    }
	}
	
	@EventHandler
	public void closeInventoryEvent(InventoryCloseEvent e) {
	    if ((e.getInventory() instanceof EnchantingInventory)) {
	    	 e.getInventory().setItem(1, null);
	    }
	}
	
	@EventHandler
	public void inventoryClickEvent(InventoryClickEvent e) {
	    if ((e.getClickedInventory() instanceof EnchantingInventory)) {
	         if (e.getSlot() == 1) {
	             e.setCancelled(true);
	         }
	    }
	}
	
	@EventHandler
	public void enchantItemEvent(EnchantItemEvent event) {
		event.getInventory().setItem(1, lapis);
	}
	


	@EventHandler
	public void onAchievementListener(PlayerAchievementAwardedEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onClick(PlayerInteractEntityEvent event) {
		if ((event.getPlayer() instanceof Player) && (event.getRightClicked() instanceof Player)) {
			 Player player = event.getPlayer();
			 Player clicked = (Player)event.getRightClicked();
			 if ((player.getItemInHand().getType().equals(Material.AIR)) && (VanishManager.inAdmin(player))) {
				  player.performCommand("invsee " + clicked.getName());
			 }
		}
	}
	
	@EventHandler
	public void fixDamages(EntityDamageEvent e) {
		if (e.isCancelled())
			return;
				
		if (e.getEntity() instanceof Player) {
			if (e.getCause() == DamageCause.SUFFOCATION) {
				e.setDamage(e.getDamage() + 1.0D);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event) {
		if (!ServerOptions.isDano()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onDamageByEntityLow(EntityDamageByEntityEvent event) {
		if (!ServerOptions.isPvP()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void worldEditListener(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) {
			return;
		}
		if (event.getClickedBlock() != null) {
			Player player = event.getPlayer();
			if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.WOOD_AXE) {
				if (player.getItemInHand().hasItemMeta()) {
					if (player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("§e§lWORLDEDIT")) {
						if (event.getAction().name().contains("LEFT")) {
							event.setCancelled(true);
							BukkitMain.getManager().getWorldEditManager().setPos1(player, event.getClickedBlock().getLocation());
						} else {
							BukkitMain.getManager().getWorldEditManager().setPos2(player, event.getClickedBlock().getLocation());
						}
					}
				}
			}
		}
	}
	
	protected class SyncPlayerKick implements Runnable {

		private Player p;
		private String message;
		
		public SyncPlayerKick(Player p, String message) {
			this.p = p;
			this.message = message;
		}

		@Override
		public void run() {
			p.kickPlayer(message);
		}
	}
}