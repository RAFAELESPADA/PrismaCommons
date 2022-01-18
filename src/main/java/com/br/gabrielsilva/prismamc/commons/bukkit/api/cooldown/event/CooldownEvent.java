package com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.cooldown.types.Cooldown;

@RequiredArgsConstructor
public abstract class CooldownEvent extends Event {

    @Getter
    @NonNull
    private Player player;

    @Getter
    @NonNull
    private Cooldown cooldown;
}