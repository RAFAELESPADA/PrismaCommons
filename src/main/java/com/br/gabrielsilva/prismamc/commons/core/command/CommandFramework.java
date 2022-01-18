package com.br.gabrielsilva.prismamc.commons.core.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.br.gabrielsilva.prismamc.commons.core.group.Groups;

public interface CommandFramework {

	Object getPlugin();

    void registerCommands(CommandClass commandClass);

    @Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Command {

		String name();

		Groups[] groupsToUse() default {Groups.MEMBRO};

		String permission() default "";

		String[] aliases() default {};

		String description() default "";

		String usage() default "";

		boolean runAsync() default false;
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Completer {

		String name();

		String[] aliases() default {};
	}
}
