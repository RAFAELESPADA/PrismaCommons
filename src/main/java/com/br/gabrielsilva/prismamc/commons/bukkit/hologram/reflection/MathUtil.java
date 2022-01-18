package com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection;

public abstract class MathUtil {

	public static int floor(double d1) {
		int i = (int) d1;
		return d1 >= i ? i : i - 1;
	}

}
