package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.br.gabrielsilva.prismamc.commons.bungee.BungeeMain;

public class Filters extends PropagatingFilter {

    public Filters(BungeeMain plugin) {
        super(plugin.getProxy().getLogger());
    }

    Filters(Logger logger) {
        super(logger);
    }
    
    public String[] messages = {"hasconnected", "hasdisconnected", "disconnectedwith", "nstoprocess!", "readtimedout",
    		"connectionresetbypeer", "pinging", "initialhandler", "connectionwasaborted", "pluginviolation", "<->",
    		  "warning", "upstreambridge", "ioexception", "illegalthread", "bungeesecuritymanager", "plugin", "performed", 
    		  "restricted", "action", "pinghandler", "peer", "readaddress", "(..)", "native", "upstream"};
    
	@Override
    public boolean isLoggable(LogRecord record) {
		boolean canLog = true;
		
		if (record.getMessage().length() > 2) {
			//String message = (new ConciseFormatter()).formatMessage(formated).trim();
			String message = record.getMessage();
			
	    	if (message.contains(" ")) {
	    		message = message.replaceAll(" ", "");
	    	}
	    	
	    	for (String filteredsMsgs : messages) {
	    		 if (message.contains(filteredsMsgs)) {
	    			 canLog = false;
	    			 break;
	    		 }
	       		 if (message.startsWith(filteredsMsgs)) {
	    			 canLog = false;
	    			 break;
	    		 }
	       		 if (message.endsWith(filteredsMsgs)) {
	    			 canLog = false;
	    			 break;
	    		 }
	    	}
		}
		return canLog;
    }
    
    @Override
    public String toString() {
        return "LogFilter{all connects}'";
    }
}