package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import java.util.LinkedList;
import java.util.List;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;

public class LogFilterBungee {

	private static List<InjectableFilter> filters = new LinkedList<>();
	
	public static void load(BungeeMain plugin) {
		 filters.clear();
		 
	     filters.add(new Filters(plugin));
	     
	     for (InjectableFilter filter : filters) {
	          filter.inject();
	     }
	}
	
	public static void unload() {
        for (InjectableFilter filter : filters) {
             filter.reset();
        }
	}
}