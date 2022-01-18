package com.br.gabrielsilva.prismamc.commons.bukkit.hologram;

import java.text.DecimalFormat;

public class PlayerTop {

	private String nome, ranking, valor;
	private int valorNotReformuled;
	
	public PlayerTop(String ranking, String nome, String valor, boolean reformular) {
		this.ranking = reformular(Integer.valueOf(ranking));
		this.nome = nome;
		
		this.valor = valor;
		try {
			this.valorNotReformuled = Integer.valueOf(valor);
		} catch (NumberFormatException ex) {}
		
		if (reformular) {
		    this.valor = reformularValor(Integer.valueOf(valor));
		    this.valorNotReformuled = Integer.valueOf(valor);
		}
	}
	
	public PlayerTop(String ranking, String nome, String valor) {
		this.ranking = reformular(Integer.valueOf(ranking));
		this.nome = nome;
		this.valor = reformularValor(Integer.valueOf(valor));
	    this.valorNotReformuled = Integer.valueOf(valor);
	}
	
	public String reformular(int ranking) {
		return "§a" + ranking + ". ";
	}
	
    public static String reformularValor(int valor) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
        return decimalFormat.format(valor);
    }
	
	public String getRanking() {
		return ranking;
	}

	public String getNome() {
		return nome;
	}

	public String getValor() {
		return valor;
	}
	
	public int getValorNotReformuled() {
		return valorNotReformuled;
	}
}