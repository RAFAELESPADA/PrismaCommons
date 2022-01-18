package com.br.gabrielsilva.prismamc.commons.core.server.types;

public enum Stages {
	
	CARREGANDO("Carregando"),
	PREJOGO("PreJogo"),
	INVENCIBILIDADE("Invencibilidade"),
	JOGANDO("Jogando"),
	OFFLINE("Offline"),
	FIM("Fim");
	
	private String nome;
	
	Stages(String nome) {
		this.nome = nome;
	}

	public String getNome() {
		return nome;
	}

	public static Stages getStageByName(String nome) {
		Stages finded = Stages.OFFLINE;
		for (Stages estagios : Stages.values()) {
			 if (nome.equalsIgnoreCase(estagios.getNome())) {
			     finded = estagios;
			     break;
			 }
		}
		return finded;
	}
}