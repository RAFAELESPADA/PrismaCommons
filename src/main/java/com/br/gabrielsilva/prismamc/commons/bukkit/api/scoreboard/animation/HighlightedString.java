package com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.animation;

public class HighlightedString extends FrameAnimatedString {
	
	protected String context, normalFormat, highlightFormat;
	  
	public HighlightedString(String context, String normalFormat, String highlightFormat) {
		this.context = context;
	    this.normalFormat = normalFormat;
	    this.highlightFormat = highlightFormat;
	    generateFrames();
	}    	  
	  
	protected void generateFrames() {
		for (int i = 0; i < this.context.length(); i++) {
			 if (this.context.charAt(i) != ' ') {
	             String str1 = String.valueOf(this.normalFormat) + this.context.substring(0, i) + 
	             this.highlightFormat + this.context.charAt(i) + this.normalFormat + 
	             this.context.substring(i + 1, this.context.length());
	             addFrame(str1); 
			 } else {
				 addFrame(this.normalFormat + this.context + " ");
			 }
		}
	}
	
	public String getContext() {
		return this.context;
	}
	  
	public String getNormalColor() {
	    return this.normalFormat;
	}
	  
	public String getHighlightColor() {
	    return this.highlightFormat;
	}
}