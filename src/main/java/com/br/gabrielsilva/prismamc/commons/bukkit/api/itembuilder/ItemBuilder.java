package com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

public class ItemBuilder {
	
	private Material material;
    private int amount;
    private short durability;
    private boolean useMeta, glow, inquebravel, cabeça, cor;
    private String displayName, cabeçaNome;
    private HashMap<Enchantment, Integer> enchantments, unsafeEnchantments;
    private String[] lore;
    private NBTTagCompound basicNBT;
    private NBTTagList enchNBT;
    private Color c;
    
    public ItemBuilder() {
    	this.material = Material.STONE;
        this.amount = 1;
        this.durability = 0;
        this.useMeta = false;
        this.glow = false;
        this.basicNBT = new NBTTagCompound();
        this.enchNBT = new NBTTagList();
        this.basicNBT.set("ench", this.enchNBT);
        this.inquebravel = false;
        this.cabeça = false;
        this.cabeçaNome = "";
        c = Color.WHITE;
        cor = false;
    }
  
    public ItemBuilder color(Color c) {
    	this.c = c;
	    this.cor = true;
	    return this;
    }
  
    public ItemBuilder material(Material material) {
    	this.material = material;
        return this;
    }
  
    public ItemBuilder headName(String cabeçaNome) {
	    this.cabeça = true;
	    this.cabeçaNome = cabeçaNome;
	    return this;
    }
  
    public ItemBuilder amount(int amount) {
        if (amount > 64) {
            amount = 64;
        }
        this.amount = amount;
        return this;
    }
  
    public ItemBuilder inquebravel() {
	    this.inquebravel = true;
	    return this;
    }
  
    public ItemBuilder durability(int durability) {
	    this.durability = ((short)durability);
        return this;
    }
  
    public ItemBuilder name(String text) {
        if (!this.useMeta) {
            this.useMeta = true;
        }
        this.displayName = text.replace("&", "§");
        return this;
    }
  
    public ItemBuilder addEnchant(Enchantment enchantment) {
        return addEnchant(enchantment, Integer.valueOf(1));
    }
  
    public ItemBuilder addUnsafeEnchant(Enchantment enchantment, Integer level) {
        if (this.unsafeEnchantments == null) {
            this.unsafeEnchantments = new HashMap<>();
        }
        this.unsafeEnchantments.put(enchantment, level);
        return this;
    }
    
    public ItemBuilder addEnchant(Enchantment enchantment, Integer level) {
        if (this.enchantments == null) {
            this.enchantments = new HashMap<>();
        }
        this.enchantments.put(enchantment, level);
        return this;
    }
  
    public ItemBuilder lore(String[] lore) {
    	if (!this.useMeta) {
            this.useMeta = true;
    	}
        this.lore = lore;
        return this;
    }
  
    public ItemBuilder glow() {
        this.glow = true;
        return this;
    }
  
    public org.bukkit.inventory.ItemStack build() {
    	org.bukkit.inventory.ItemStack stack = new org.bukkit.inventory.ItemStack(this.material);
        stack.setAmount(this.amount);
        stack.setDurability(this.durability);
        if ((this.enchantments != null) && (!this.enchantments.isEmpty())) {
             for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
                  stack.addEnchantment((Enchantment)entry.getKey(), ((Integer)entry.getValue()).intValue());
             }
        }
        if ((this.unsafeEnchantments != null) && (!this.unsafeEnchantments.isEmpty())) {
            for (Map.Entry<Enchantment, Integer> entry : this.unsafeEnchantments.entrySet()) {
                 stack.addUnsafeEnchantment((Enchantment)entry.getKey(), ((Integer)entry.getValue()).intValue());
            }
        }
        if (this.useMeta) {
    	    if (this.cabeça) {
			    SkullMeta meta = (SkullMeta) stack.getItemMeta();
		        if (this.displayName != null) {
		            meta.setDisplayName(this.displayName.replace("&", "§"));
		        }
		        if (this.lore != null) {
		    	    meta.setLore(Arrays.asList(this.lore));
		        }
			    meta.setOwner(this.cabeçaNome);
			    stack.setItemMeta(meta);
    	    } else {
    	    	if (this.cor) {
                    LeatherArmorMeta armorMeta = (LeatherArmorMeta)stack.getItemMeta();
                    armorMeta.setColor(c);
        		    ItemMeta meta = stack.getItemMeta();
                    if (this.displayName != null) {
                        meta.setDisplayName(this.displayName.replace("&", "§"));
                    }
                    if (this.lore != null) {
        		        meta.setLore(Arrays.asList(this.lore));
                    }
                    if (this.inquebravel) {
            	        meta.spigot().setUnbreakable(true);
                    }
                    stack.setItemMeta(armorMeta);
    	    	} else {
        		    ItemMeta meta = stack.getItemMeta();
                    if (this.displayName != null) {
                        meta.setDisplayName(this.displayName.replace("&", "§"));
                    }
                    if (this.lore != null) {
        		        meta.setLore(Arrays.asList(this.lore));
                    }
                    if (this.inquebravel) {
            	        meta.spigot().setUnbreakable(true);
                    }
            	    stack.setItemMeta(meta);
    	    	}
    	    }
        }
        if (this.glow) {
            net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);
            if (nmsStack.hasTag()) {
                nmsStack.getTag().set("ench", this.enchNBT);
            } else {
               nmsStack.setTag(this.basicNBT);
            }
            stack = CraftItemStack.asCraftMirror(nmsStack);
        }
        this.material = Material.STONE;
        this.amount = 1;
        this.durability = 0;
        if (this.useMeta) {
            this.useMeta = false;
        }
        if (this.glow) {
            this.glow = false;
        }
        if (this.displayName != null) {
            this.displayName = null;
        }
        if (this.enchantments != null) {
            this.enchantments.clear();
            this.enchantments = null;
        }
        if (this.unsafeEnchantments != null) {
            this.unsafeEnchantments.clear();
            this.unsafeEnchantments = null;
        }
        if (this.lore != null) {
            this.lore = null;
        }
        if (this.cor) {
    	    this.cor = false;
        }
        if (this.c != Color.WHITE) {
    	    this.c = Color.WHITE;
        }
        if (this.inquebravel) {
    	    this.inquebravel = false;
        }
        return stack;
    }
}