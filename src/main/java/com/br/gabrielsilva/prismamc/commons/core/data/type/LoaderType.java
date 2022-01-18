package com.br.gabrielsilva.prismamc.commons.core.data.type;

import lombok.Getter;

public enum LoaderType {

	REDIS("(Redis)"),
	MYSQL("(MySQL)");
	
	@Getter
	private String name;
	
	LoaderType(String name) {
		this.name = name;
	}
}