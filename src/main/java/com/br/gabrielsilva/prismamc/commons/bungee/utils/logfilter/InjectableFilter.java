package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import java.util.logging.Filter;
import java.util.logging.Logger;

public interface InjectableFilter extends Filter {
	
    Logger getLogger();
    Filter getPreviousFilter();
    boolean isInjected();
    Filter inject();
    boolean reset();
}