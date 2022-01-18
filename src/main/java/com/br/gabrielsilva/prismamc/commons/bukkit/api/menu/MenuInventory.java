package com.br.gabrielsilva.prismamc.commons.bukkit.api.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map.Entry;

public class MenuInventory {

    private HashMap<Integer, MenuItem> slotItem;
    private int rows;
    private String title;
    private Inventory inv;
    private boolean onePerPlayer;

    @Getter @Setter
    private MenuUpdateHandler updateHandler;

    public MenuInventory(String title, int rows) {
        this(title, rows, false);
    }

    public MenuInventory(String title, int rows, boolean onePerPlayer) {
        this.slotItem = new HashMap<>();
        this.rows = rows;
        this.title = title;
        this.onePerPlayer = onePerPlayer;
        if (!onePerPlayer) {
            this.inv = Bukkit.createInventory(new MenuHolder(this), rows * 9, this.title);
        }
    }

    public void addItem(MenuItem item) {
        setItem(firstEmpty(), item);
    }

    public void addItem(ItemStack item) {
        setItem(firstEmpty(), item);
    }

    public void setItem(ItemStack item, int slot) {
        setItem(slot, new MenuItem(item));
    }

    public void setItem(int slot, ItemStack item) {
        setItem(slot, new MenuItem(item));
    }

    public void setItem(int slot, ItemStack item, MenuClickHandler handler) {
        setItem(slot, new MenuItem(item, handler));
    }

    public void setItem(MenuItem item, int slot) {
        setItem(slot, item);
    }

    public void setItem(int slot, MenuItem item) {
        this.slotItem.put(slot, item);
        if (!onePerPlayer) {
            inv.setItem(slot, item.getStack());
        }
    }

    public int firstEmpty() {
        if (!onePerPlayer) {
            return inv.firstEmpty();
        } else {
            for (int i = 0; i < rows * 9; i++) {
                if (!slotItem.containsKey(i)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public boolean hasItem(int slot) {
        return this.slotItem.containsKey(slot);
    }

    public MenuItem getItem(int slot) {
        return this.slotItem.get(slot);
    }

    public void clear() {
        slotItem.clear();
        if (!onePerPlayer) {
            inv.clear();
        }
    }

    public void open(Player p) {
        if (!onePerPlayer) {
            p.openInventory(inv);
        } else {
            if (p.getOpenInventory() == null//
                    || p.getOpenInventory().getTopInventory().getType() != InventoryType.CHEST//
                    || p.getOpenInventory().getTopInventory().getSize() != rows * 9
                    || p.getOpenInventory().getTopInventory().getHolder() == null//
                    || !(p.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder)//
                    || !(((MenuHolder) p.getOpenInventory().getTopInventory().getHolder()).isOnePerPlayer())) {
                createAndOpenInventory(p);
            } else {
                // Update the current inventory of player
                for (int i = 0; i < rows * 9; i++) {
                    if (slotItem.containsKey(i)) {
                        p.getOpenInventory().getTopInventory().setItem(i, slotItem.get(i).getStack());
                    } else {
                        p.getOpenInventory().getTopInventory().setItem(i, null);
                    }
                }
                p.updateInventory();
            }
            ((MenuHolder) p.getOpenInventory().getTopInventory().getHolder()).setMenu(this);
        }
        //updateTitle(p);
        // Garbage Colector
        p = null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void createAndOpenInventory(Player p) {
        Inventory playerInventory = Bukkit.createInventory(new MenuHolder(this), rows * 9, this.title);
        for (Entry<Integer, MenuItem> entry : slotItem.entrySet()) {
            playerInventory.setItem(entry.getKey(), entry.getValue().getStack());
        }
        p.openInventory(playerInventory);
        p = null;
    }

    public void close(Player p) {
        if (onePerPlayer) {
            destroy(p);
            p = null;
        }
    }

    public void destroy(Player p) {
        if (p.getOpenInventory().getTopInventory().getHolder() != null
                && p.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
            ((MenuHolder) p.getOpenInventory().getTopInventory().getHolder()).destroy();
        }
    }

    // public void destroy() {
    // for (MenuItem item : slotItem.values()) {
    // item.destroy();
    // }
    // this.slotItem.clear();
    // this.slotItem = null;
    // }

    public boolean isOnePerPlayer() {
        return onePerPlayer;
    }

    public Inventory getInventory() {
        return inv;
    }
}