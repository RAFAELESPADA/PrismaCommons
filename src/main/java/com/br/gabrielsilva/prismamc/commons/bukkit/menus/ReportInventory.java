package com.br.gabrielsilva.prismamc.commons.bukkit.menus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.ClickType;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuClickHandler;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuInventory;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuItem;
import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.utils.system.DateUtils;
import com.br.gabrielsilva.prismamc.commons.custompackets.BukkitClient;
import com.br.gabrielsilva.prismamc.commons.custompackets.bungee.packets.PacketFindPlayerByReport;

import redis.clients.jedis.Jedis;

public class ReportInventory extends MenuInventory {

    private static final int ITEMS_PER_PAGE = 28;
    private static final int PREVIOUS_PAGE_SLOT = 27;
    private static final int NEXT_PAGE_SLOT = 35;
    private static final int CENTER = 31;

    private static final int REPORTS_PER_ROW = 7;
	
    public ReportInventory(Player player) {
    	this(player, 1);
    }
    
	public ReportInventory(Player player1, int page) {
		super("Lista de Reports", 6);
		
		List<MenuItem> items = getActualReports();

        int pageStart = 0;
        int pageEnd = ITEMS_PER_PAGE;
        if (page > 1) {
            pageStart = ((page - 1) * ITEMS_PER_PAGE);
            pageEnd = (page * ITEMS_PER_PAGE);
        }
        if (pageEnd > items.size()) {
            pageEnd = items.size();
        }
		
        if (page == 1) {
        	setItem(PREVIOUS_PAGE_SLOT, new ItemBuilder().material(Material.INK_SACK).durability(8).name("§cPágina Anterior").build());
        } else {
        	setItem(new MenuItem(new ItemBuilder().material(Material.INK_SACK).durability(10).name("§aPágina Anterior").build(),
        			(player, arg1, arg2, arg3, arg4) -> new ReportInventory(player, page - 1).open(player)), PREVIOUS_PAGE_SLOT);
        }

        if ((items.size() / ITEMS_PER_PAGE) + 1 <= page) {
           	setItem(NEXT_PAGE_SLOT, new ItemBuilder().material(Material.INK_SACK).durability(8).name("§cPróxima Página").build());
        } else {
        	setItem(new MenuItem(new ItemBuilder().material(Material.INK_SACK).durability(10).name("§aPróxima Página").build(),
        			(player, arg1, arg2, arg3, arg4) -> new ReportInventory(player, page + 1).open(player)), NEXT_PAGE_SLOT);
        }

        int kitSlot = 10;

        for (int i = pageStart; i < pageEnd; i++) {
             MenuItem item = items.get(i);
             setItem(item, kitSlot);
             if (kitSlot % 9 == REPORTS_PER_ROW) {
                 kitSlot += 3;
                 continue;
             }
             kitSlot += 1;
        }
        
        if (items.size() == 0) {
        	setItem(new ItemBuilder().material(Material.INK_SACK).name("§cNenhum report para mostrar!").durability(1).build(), CENTER);
        }
	}
	
	private List<MenuItem> getActualReports() {
		List<MenuItem> lista = new ArrayList<>();
		
		if (!Core.getRedis().isConnected()) {
			return lista;
		}
		
		try {
			Jedis jedis = Core.getRedis().getPool().getResource();
				
			for (int i = 1; i <= 100; i++) {
				 String toCheck = "report:" + i;
				 if (!jedis.exists(toCheck)) {
					 continue;
				 }

				 Map<String, String> hash = jedis.hgetAll(toCheck);
				 
				 String color = hash.get("glassID").equalsIgnoreCase("5") ? "§a" : hash.get("glassID").equals("4") ? "§e" : "§c";
				 
				 List<String> motivos = new ArrayList<>(),
						 reportou = new ArrayList<>();
				 
				 for (int motiveID = 1; motiveID <= 10; motiveID++) {
					  if (hash.containsKey("motivo-" + motiveID)) {
						  motivos.add(hash.get("motivo-" + motiveID));
					  }
					  if (hash.containsKey("reportou-" + motiveID)) {
						  reportou.add(hash.get("reportou-" + motiveID));
					  }
				 }
				 
				 String[] lore = new String[6 + motivos.size()];
				 lore[0] = "";
				 lore[1] = "§fDenúncias: " + color + hash.get("denuncias");
				 lore[2] = "§fStatus: " + color + (hash.get("glassID").equalsIgnoreCase("5") ? "§aNinguém viu" : hash.get("glassID").equals("4") ? "§eJa verificaram" : "§cBanido");
				 lore[3] = "§fUltima denúncia: " + color + DateUtils.getElapsed(Long.valueOf(hash.get("lastReport")));
				 lore[4] = "";
				 lore[5] = color + (motivos.size() > 1 ? "Motivos - Quem reportou" : "Motivo - Quem Reportou");
				 
				 int a = 1,
						 reportouID = 0;
				 for (String motivo : motivos) {
					  lore[5 + a] = "§f" + motivo + " - " + reportou.get(reportouID);
					  a++;
					  reportouID++;
				 }
				 
				 ItemStack itemStack = new ItemBuilder().material(Material.STAINED_GLASS_PANE).name(color + hash.get("nick")).
						 durability(Integer.valueOf(hash.get("glassID"))).lore(lore).build();
				 		 
				 lista.add(new MenuItem(itemStack, defaultClickHandler));

				 hash.clear();
				 hash = null;
				 
				 motivos.clear();
				 motivos = null;
				 
				 itemStack = null;
				 color = null;
			}
			if (jedis != null) {
				jedis.close();
				jedis = null;
			}
			return lista;
		} catch (Exception ex) {
			return null;
		}
	}
	
	private MenuClickHandler defaultClickHandler = new MenuClickHandler() {

		public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
			String nick = stack.getItemMeta().getDisplayName();
			if (nick.contains("§a")) {
				nick = nick.replace("§a", "");
			}
			if (nick.contains("§e")) {
				nick = nick.replace("§e", "");
			}
			if (nick.contains("§c")) {
				nick = nick.replace("§c", "");
			}
			
			p.closeInventory();
			
			BukkitClient.sendPacket(p, new PacketFindPlayerByReport(
					BukkitMain.getManager().getDataManager().getBukkitPlayer(p.getUniqueId()).getNick(), nick));
			
		}
	};
}