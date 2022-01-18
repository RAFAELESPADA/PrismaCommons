package com.br.gabrielsilva.prismamc.commons.bukkit.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.ClickType;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuClickHandler;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuInventory;
import com.br.gabrielsilva.prismamc.commons.core.data.DataHandler;
import com.br.gabrielsilva.prismamc.commons.core.data.category.DataCategory;
import com.br.gabrielsilva.prismamc.commons.core.data.type.DataType;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketBungeeUpdateField;

public class PreferencesInventory extends MenuInventory {

	public PreferencesInventory(Player player) {
		super("Preferências", 4);
		
		setItem(10, new ItemBuilder().material(Material.PAPER).name("§eReceber mensagens privadas").
			lore(new String[] {"§aClique para alterar!"}).build(), new MenuClickHandler() {
			
			public void onClick(Player player, Inventory inv, ClickType type, ItemStack stack, int slot) {
				 DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
	    		 if (dataHandler.getBoolean(DataType.TELL)) {
		    		 dataHandler.getData(DataType.TELL).setValue(false);
		    		 player.sendMessage("§cMensagens privadas desativada!");
	    		 } else {
		    		 dataHandler.getData(DataType.TELL).setValue(true);
		    		 player.sendMessage("§aMensagens privadas ativada!");
	    		 }
	    		 
	   		     updateInventory(player);
		 		 dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.TELL);
			}
		});
		
		setItem(12, new ItemBuilder().material(Material.EYE_OF_ENDER).name("§eReceber reports").
				lore(new String[] {"§aClique para alterar!"}).build(), new MenuClickHandler() {
					
			public void onClick(Player player, Inventory inv, ClickType type, ItemStack stack, int slot) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
	    		  if (dataHandler.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
		    		  player.closeInventory();
		    		  player.sendMessage("§cApenas STAFFERS podem alterar esta preferência.");
		    		  return;
	    		  }
		    	  if (dataHandler.getBoolean(DataType.REPORTS)) {
		    		  dataHandler.getData(DataType.REPORTS).setValue(false);
		    		  player.sendMessage("§cAgora você não irá receber notificações de reports!");
		    	  } else {
		    		  dataHandler.getData(DataType.REPORTS).setValue(true);
		    		  player.sendMessage("§aAgora você recebera notificações de reports!");
		    	  }
		 		  dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.REPORTS);
		 		  
		 		  updateInventory(player);
	    		  BukkitClient.sendPacket(player, new PacketBungeeUpdateField(dataHandler.getName(), 
	    				  "ProxyPlayer", "UpdateBungeePlayer", "reports", dataHandler.getBoolean(DataType.REPORTS).toString()));
			}
		});
		
		setItem(14, new ItemBuilder().material(Material.SLIME_BALL).name("§eEntrar ja no modo admin").
				lore(new String[] {"§aClique para alterar!"}).build(), new MenuClickHandler() {
					
			public void onClick(Player player, Inventory inv, ClickType type, ItemStack stack, int slot) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
	    		  if (dataHandler.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
		    		  player.closeInventory();
		    		  player.sendMessage("§cApenas STAFFERS podem alterar esta preferência.");
		    		  return;
	    		  }
	    		  
		    	  if (dataHandler.getBoolean(DataType.JOIN_ADMIN)) {
		    		  dataHandler.getData(DataType.JOIN_ADMIN).setValue(false);
		    		  player.sendMessage("§cAgora você não entrará nos servidores no modo ADMIN!");
		    	  } else {
		    		  dataHandler.getData(DataType.JOIN_ADMIN).setValue(true);
		    		  player.sendMessage("§aAgora você entrará ja com o modo ADMIN ativado!");
		    	  }
		 		  dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.JOIN_ADMIN);
				  updateInventory(player);
			}
		});
		
		setItem(16, new ItemBuilder().material(Material.BOOK).name("§eEntrar com StaffChat").
				lore(new String[] {"§aClique para alterar!"}).build(), new MenuClickHandler() {
					
			public void onClick(Player player, Inventory inv, ClickType type, ItemStack stack, int slot) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
				
				if (dataHandler.getData(DataType.GRUPO).getGrupo().getNivel() < Groups.TRIAL.getNivel()) {
		    		player.closeInventory();
		    		player.sendMessage("§cApenas STAFFERS podem alterar esta preferência.");
		    		return;
		    	}
	    		  	
	    		if (dataHandler.getBoolean(DataType.STAFFCHAT)) {
		    		dataHandler.getData(DataType.STAFFCHAT).setValue(false);
		    		player.sendMessage("§cVocê saiu do StaffChat!");
	    		} else {
		    	    dataHandler.getData(DataType.STAFFCHAT).setValue(true);
		    		player.sendMessage("§aVocê entrou no StaffChat");
	    		}
		 		dataHandler.updateValues(DataCategory.PREFERENCIAS, true, DataType.STAFFCHAT);
		 		updateInventory(player);
	    		  BukkitClient.sendPacket(player, new PacketBungeeUpdateField(dataHandler.getName(), 
	    				  "ProxyPlayer", "UpdateBungeePlayer", "staffchat", dataHandler.getBoolean(DataType.STAFFCHAT).toString()));
			}
		});
		
		updateInventory(player);
	}
	
	private void updateInventory(Player player) {
		ItemStack ativado = new ItemBuilder().material(Material.STAINED_GLASS_PANE).name("§aAtivado").durability(5).build(),
				desativado = new ItemBuilder().material(Material.STAINED_GLASS_PANE).name("§cDesativado").durability(14).build();
		
		final DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
		
		if (dataHandler.getBoolean(DataType.TELL)) {
			setItem(19, ativado);
		} else {
			setItem(19, desativado);
		}
		
		if (dataHandler.getBoolean(DataType.REPORTS)) {
			setItem(21, ativado);
		} else {
			setItem(21, desativado);
		}
		
		if (dataHandler.getBoolean(DataType.JOIN_ADMIN)) {
			setItem(23, ativado);
		} else {
			setItem(23, desativado);
		}
		
		if (dataHandler.getBoolean(DataType.STAFFCHAT)) {
			setItem(25, ativado);
		} else {
			setItem(25, desativado);
		}
	}
}