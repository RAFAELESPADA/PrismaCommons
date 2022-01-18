package com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.types.Cooldown;

public class CooldownStartEvent extends CooldownEvent implements Cancellable {

    public CooldownStartEvent(Player player, Cooldown cooldown) {
        super(player, cooldown);
    }

    private boolean cancelled;

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}