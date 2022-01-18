package com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.actionbar.ActionBar;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.event.CooldownFinshEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.event.CooldownStartEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.types.Cooldown;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.types.ItemCooldown;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.UpdateEvent.UpdateType;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

public class CooldownAPI implements Listener {

    private static final Map<UUID, List<Cooldown>> map = new ConcurrentHashMap<>();

    public static void removeAllCooldowns(Player player) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            Iterator<Cooldown> it = list.iterator();
            while (it.hasNext()) {
                Cooldown cooldown = it.next();
                it.remove();
            }
        }
    }
    
    public static void sendMessage(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) {
                	player.sendMessage("§cAguarde " + StringUtils.toMillis(cooldown.getRemaining()) + " segundos para usar a habilidade novamente.");
                }
        }       
    }
    
    public static void addCooldown(Player player, Cooldown cooldown) {
        CooldownStartEvent event = new CooldownStartEvent(player, cooldown);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            List<Cooldown> list = map.computeIfAbsent(player.getUniqueId(), v -> new ArrayList<>());
            list.add(cooldown);
        }
    }

    public static boolean removeCooldown(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            Iterator<Cooldown> it = list.iterator();
            while (it.hasNext()) {
                Cooldown cooldown = it.next();
                if (cooldown.getName().equals(name)) {
                    it.remove();
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasCooldown(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) {
                	return true;
                }
        }
        return false;
    }
    
    public static boolean inCooldown(Player player, String name) {
        if (map.containsKey(player.getUniqueId())) {
            List<Cooldown> list = map.get(player.getUniqueId());
            for (Cooldown cooldown : list)
                if (cooldown.getName().equals(name)) {
                	if (cooldown.expired()) {
                		return false;
                	} else {
                		return true;
                	}
                }
        }
        return false;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEGUNDO)
            return;
        
        for (UUID uuid : map.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                List<Cooldown> list = map.get(uuid);
                Iterator<Cooldown> it = list.iterator();

                /* Found Cooldown */
                Cooldown found = null;
                while (it.hasNext()) {
                    Cooldown cooldown = it.next();
                    if (!cooldown.expired()) {
                        if (cooldown instanceof ItemCooldown) {
                            ItemStack hand = player.getInventory().getItemInHand();
                            if (hand != null && hand.getType() != Material.AIR) {
                                ItemCooldown item = (ItemCooldown) cooldown;
                                if (hand.equals(item.getItem())) {
                                    item.setSelected(true);
                                    found = item;
                                    break;
                                }
                            }
                            continue;
                        }
                        found = cooldown;
                        continue;
                    }
                    it.remove();
                    ActionBar.sendActionBar(player, "");
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
                    CooldownFinshEvent e = new CooldownFinshEvent(player, cooldown);
                    Bukkit.getServer().getPluginManager().callEvent(e);
                }

                /* Display Cooldown */
                if (found != null) {
                	display(player, found);
                } else if (list.isEmpty()) {
                	map.remove(uuid);
                } else {
                    Cooldown cooldown = list.get(0);
                    if (cooldown instanceof ItemCooldown) {
                        ItemCooldown item = (ItemCooldown) cooldown;
                        if (item.isSelected()) {
                            item.setSelected(false);
                        }
                    }
                }
            }
        }
    }
    
    private void display(Player player, Cooldown cooldown) {
        StringBuilder bar = new StringBuilder();
        double percentage = cooldown.getPercentage();
        double remaining = cooldown.getRemaining();
        double count = 20 - Math.max(percentage > 0D ? 1 : 0, percentage / 5);
        
        for (int a = 0; a < count; a++)
             bar.append("§a§l:");
        
        for (int a = 0; a < 20 - count; a++)
             bar.append("§c§l:");
        
        String name = cooldown.getName();
        ActionBar.sendActionBar(player, name + " " + bar.toString() + "§f " + StringUtils.toMillis(cooldown.getRemaining()));
    }
}