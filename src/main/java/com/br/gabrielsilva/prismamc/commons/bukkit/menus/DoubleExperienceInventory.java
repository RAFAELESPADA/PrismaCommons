package com.br.gabrielsilva.prismamc.commons.bukkit.menus;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
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
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;

public class DoubleExperienceInventory extends MenuInventory {

	public DoubleExperienceInventory(Player player) {
		super("Double XP", 3);
		
		final DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
		final int amount = dataHandler.getInt(DataType.DOUBLEXP);
		
		final Long time = dataHandler.getData(DataType.DOUBLEXP_TIME).getLong();
		String ativado = "§cNão",
				tempoRestante = "§7N/A";
		
		if (time > System.currentTimeMillis()) {
			ativado = "§aSim";
			tempoRestante = DateUtils.formatDifference(time);
		}
		
		setItem(13, new ItemBuilder().material(Material.EXP_BOTTLE).amount(amount == 0 ? 1 : amount).name("§b§lDOUBLE XP").lore(new String[] {
		"",
		"§fVocê possuí: §a" + amount,
		"§fAtivado: " + ativado,
		"§fTempo restante: " + tempoRestante,
		"",
		"§aBotão esquerdo -> Ativar",
		"§aBotão direito -> Comprar",
		"§7Preço: 1.500 coins!"
		}).build(), new MenuClickHandler() {
			
			public void onClick(Player player, Inventory inv, ClickType type, ItemStack stack, int slot) {
				DataHandler dataHandler = BukkitMain.getManager().getDataManager().getBukkitPlayer(player.getUniqueId()).getDataHandler();
				if (type == ClickType.LEFT) {
					if (dataHandler.getInt(DataType.DOUBLEXP) == 0) {
						player.sendMessage("§cVocê não possuí nenhum Double XP para ativar!");
						player.closeInventory();
						return;
					}
		 			dataHandler.getData(DataType.DOUBLEXP).remove();
		 			dataHandler.getData(DataType.DOUBLEXP_TIME).setValue(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
		 			
		 			dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.DOUBLEXP, DataType.DOUBLEXP_TIME);
		 			
		 			player.sendMessage("§aVocê ativou o DoubleXP por 1 hora!");
		 			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
		 			
		 			player.closeInventory();
				} else {
					if (dataHandler.getInt(DataType.COINS) < 1500) {
						player.sendMessage("§cVocê não possuí Coins o suficiente para comprar!");
						player.closeInventory();
						return;
					}
		 			dataHandler.getData(DataType.DOUBLEXP).add();
		 			dataHandler.getData(DataType.COINS).remove(1500);
		 			
		 			dataHandler.updateValues(DataCategory.PRISMA_PLAYER, true, DataType.DOUBLEXP, DataType.COINS);
		 			
		 			player.sendMessage("§aVocê comprou um DoubleXP!");
		 			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
				}
			}
		});
	}
}