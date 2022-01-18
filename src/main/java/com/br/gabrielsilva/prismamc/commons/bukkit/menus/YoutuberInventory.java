package com.br.gabrielsilva.prismamc.commons.bukkit.menus;

import org.bukkit.Material;

import com.br.gabrielsilva.prismamc.commons.bukkit.api.itembuilder.ItemBuilder;
import com.br.gabrielsilva.prismamc.commons.bukkit.api.menu.MenuInventory;

public class YoutuberInventory extends MenuInventory {

	public YoutuberInventory() {
		super("Requisitos para TAGS.", 3);
		
		setItem(11, new ItemBuilder().material(Material.INK_SACK).durability(12).name("�3�lSTREAMER").lore(new String[] {
		"",
		"�fPara tornar-se �3�lSTREAMER�f, � necess�rio possuir:",
		"�fQuantidade an�nima de inscritos n�o requisitado;",
		"�fPossuir �65 �fa �615 viewers �fe�6 15 likes�f em suas lives;",
		"�fA tag tem dura��o de �6�l3DIAS �fproduza lives para renova-l�.",
		"",
		"�fApos isso requisitar sua tag no �3�lDISCORD",
		}).build());
		
		setItem(13, new ItemBuilder().material(Material.INK_SACK).durability(11).name("�6�lLEGEND").lore(new String[] {
		"",
		"�fPara tornar-se �6�lLEGEND�f, � necess�rio possuir:",
		"�fQuantidade de inscritos n�o requisitados em seu canal;",
		"�fPossuir�6 150 visualiza��es �fe�6 20 likes�f em seus v�deos;",
		"�fA tag tem dura��o de �6�l7DIAS �fproduza v�deos para renova-l�.",
		"",
		"�fApos isso requisitar sua tag no �3�lDISCORD",
		}).build());
		
		setItem(15, new ItemBuilder().material(Material.INK_SACK).durability(1).name("�b�lYOUTUBER").lore(new String[] {
		"",
		"�fPara tornar-se �b�lYOUTUBER,�f � necess�rio possuir:",
		"�fQuantidade an�nima de inscritos n�o requisitado;",
		"�fPossuir �6300 visualiza��es �fe�6 30 likes�f em seus v�deos;",
		"�fA tag tem dura��o de �6�l7DIAS �fproduza v�deos para renova-l�.",
		"",
		"�fApos isso requisitar sua tag no �3�lDISCORD",
		""}).build());
	}
}