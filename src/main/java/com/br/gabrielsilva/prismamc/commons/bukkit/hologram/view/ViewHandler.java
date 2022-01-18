package com.br.gabrielsilva.prismamc.commons.bukkit.hologram.view;

import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.Hologram;

import javax.annotation.Nonnull;

public interface ViewHandler {

	/**
	 * Called when a {@link Hologram} is viewed by a player
	 *
	 * @param hologram viewed {@link Hologram}
	 * @param player   viewer
	 * @param string   content of the hologram
	 * @return The new/modified content of the hologram
	 */
	public String onView(@Nonnull Hologram hologram, @Nonnull Player player, @Nonnull String string);

}
