package com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import lombok.Getter;

@Getter
public class Sidebar {

	private Scoreboard scoreboard;
	private Objective objective;
	
	private boolean hided = true;
	
	public Sidebar(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}
	
	public void show() {
		if (!hided)
			return;
		
		hided = false;
		this.objective = scoreboard.registerNewObjective("sidebar", "dummy");
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	public void hide() {
		if (hided)
			return;
		
		hided = true;
		Objective sidebar = scoreboard.getObjective("sidebar");
		if (sidebar != null) {
			sidebar.unregister();
			sidebar = null;
		}
		for (int i = 1; i < 16; i++) {
			 Team team = scoreboard.getTeam("sidebar-" + i);
			 if (team != null) {
				 team.unregister();
				 team = null;
			 }
		}
	}
	
	public void setTitle(String name) {
		objective.setDisplayName(name);
	}
	
	public void setText(int index, String text) {
		Team team = getScoreboard().getTeam("sidebar-" + index);
		
		String prefix = "", 
				suffix = "";
		
		if (team == null) {
			team = getScoreboard().registerNewTeam("sidebar-" + index);			
			String score = ChatColor.values()[index - 1].toString();
			
			getObjective().getScore(score).setScore(index);		
			if (!team.hasEntry(score)) {
				team.addEntry(score);
			}
		}
		
		if (text.length() <= 16) {
			prefix = text;
			
			while (prefix.endsWith("§"))
				prefix = prefix.substring(0, prefix.length() - 1);
		} else {
			prefix = text.substring(0, 16);
			
			while (prefix.endsWith("§"))
				prefix = prefix.substring(0, prefix.length() - 1);
			
			suffix = ChatColor.getLastColors(prefix) + text.substring(16);
			
			if (suffix.length() > 16) 
				suffix = suffix.substring(0, 16);		
		}
		
		team.setPrefix(prefix);
		team.setSuffix(suffix);
		
		suffix = null;
		prefix = null;
		team = null;
	}
}