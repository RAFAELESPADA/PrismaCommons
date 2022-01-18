package com.br.gabrielsilva.prismamc.commons.bukkit.api.menu;

import org.bukkit.entity.Player;

public interface MenuUpdateHandler {
	
    void onUpdate(Player player, MenuInventory menu);
}