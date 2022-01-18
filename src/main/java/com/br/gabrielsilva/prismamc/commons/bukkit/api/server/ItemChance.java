package com.br.gabrielsilva.prismamc.commons.bukkit.api.server;

import java.util.Random;
import org.bukkit.inventory.ItemStack;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemChance {
	
	private int chance;
	private ItemStack item;
	private int randomStack;
	
	public ItemChance(ItemStack item, int chance, int randomStack) {
		this.item = item;
		this.chance = chance;
		this.randomStack = randomStack;
	}
	
	public ItemStack getItem() {
		if (randomStack != 0)
			return new ItemBuilder().material(item.getType()).durability(item.getDurability()).
					amount(new Random().nextInt(randomStack) + 1).build();
		return item;
	}
}