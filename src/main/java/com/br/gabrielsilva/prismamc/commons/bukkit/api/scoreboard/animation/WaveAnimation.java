package com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.animation;

import java.util.ArrayList;
import java.util.List;

public class WaveAnimation {

	private String string;
	private List<String> strings;
	private int index;
	private boolean bool;

	public WaveAnimation(String text, String c1, String c2, String c3) {
		this(text, c1, c2, c3, 12);
	}

	public WaveAnimation(String text, String c1, String c2, String c3, int p) {
		this.string = text;
		this.strings = new ArrayList<>();
		createFrames(c1, c2, c3, p);
	}

	private void createFrames(String c1, String c2, String c3, int p) {
		if (string != null && !string.isEmpty()) {
			for (int i = 0; i < string.length(); i++)
				if (string.charAt(i) != ' ')
					strings.add(c1 + string.substring(0, i) + c2 + string.charAt(i) + c3 + string.substring(i + 1));

			for (int i = 0; i < p; i++)
				strings.add(c1 + string);

			for (int i = 0; i < string.length(); i++)
				if (string.charAt(i) != ' ')
					strings.add(c3 + string.substring(0, i) + c2 + string.charAt(i) + c1 + string.substring(i + 1));

			for (int i = 0; i < p; i++)
				strings.add(c3 + string);
		}
	}

	public String next() {
		if (strings.isEmpty())
			return "";

		if (bool) {
			index--;
			if (index <= 0)
				bool = false;
		} else {
			index++;
			if (index >= strings.size()) {
				bool = true;
				return next();
			}
		}

		return strings.get(index);
	}
}