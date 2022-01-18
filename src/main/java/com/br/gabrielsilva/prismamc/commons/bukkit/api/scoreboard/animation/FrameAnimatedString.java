package com.br.gabrielsilva.prismamc.commons.bukkit.api.scoreboard.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FrameAnimatedString implements AnimatableString {

	protected List<String> frames;
    protected int currentFrame;

    public FrameAnimatedString() {
    	this.frames = new ArrayList<>();
        this.currentFrame = -1;
    }

    public FrameAnimatedString(String... VarArgs) {
    	this.frames = new ArrayList<>();
        this.currentFrame = -1;
        this.frames = Arrays.asList(VarArgs);
    }

    public FrameAnimatedString(List<String> List) {
    	this.frames = new ArrayList<>();
    	this.currentFrame = -1;
    	this.frames = List;
    }

    public void addFrame(String String) {
    	this.frames.add(String);
    }

    public void setFrame(int Int, String String) {
    	this.frames.set(Int, String);
    }

    public void removeFrame(String String) {
    	this.frames.remove(String);
    }

    public int getCurrentFrame() {
    	return this.currentFrame;
    }

    public void setCurrentFrame(int Int) {
    	this.currentFrame = Int;
    }

    public int getTotalLength() {
    	return this.frames.size();
    }

    public String getString(int Int) {
    	return (String)this.frames.get(Int);
    }

    public String current() {
    	if (this.currentFrame == -1) {
    		return null;
    	}
    	return (String)this.frames.get(this.currentFrame);
    }

    public String next() {
    	this.currentFrame += 1;
    	if (this.currentFrame == this.frames.size()) {
    		this.currentFrame = 0;
    	}
    	return (String)this.frames.get(this.currentFrame);
    }

    public String previous() {
    	this.currentFrame -= 1;
    	if (this.currentFrame == -1) {
    		this.currentFrame = (this.frames.size() - 1);
    	}
    	return (String)this.frames.get(this.currentFrame);
    }
}