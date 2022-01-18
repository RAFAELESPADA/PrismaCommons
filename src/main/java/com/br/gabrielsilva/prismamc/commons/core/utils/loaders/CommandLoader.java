package com.br.gabrielsilva.prismamc.commons.core.utils.loaders;

import com.br.gabrielsilva.prismamc.commons.core.Core;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework;
import com.br.gabrielsilva.prismamc.commons.core.utils.ClassGetter;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommandLoader {

	@NonNull
	private CommandFramework framework;
	
	public int loadCommandsFromPackage(String pkgname) {
		int i = 0;
		for (Class<?> clazz : ClassGetter.getClassesForPackage(framework.getPlugin(), pkgname)) {
			if (CommandClass.class.isAssignableFrom(clazz)) {
				try {
					CommandClass command = (CommandClass) clazz.newInstance();
					framework.registerCommands(command);
					command = null;
				} catch (Exception e) {
					Core.console("Erro ao carregar comando da classe " + clazz.getSimpleName() + "!");
				}
				++i;
			}
		}
		return i;
	}
}
