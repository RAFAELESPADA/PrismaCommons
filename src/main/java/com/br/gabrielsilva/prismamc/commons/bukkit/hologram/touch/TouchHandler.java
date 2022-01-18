package com.br.gabrielsilva.prismamc.commons.bukkit.hologram.touch;

import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.Hologram;

import javax.annotation.Nonnull;

public interface TouchHandler {

	public void onTouch(@Nonnull Hologram hologram, @Nonnull Player player, @Nonnull TouchAction action);

}
