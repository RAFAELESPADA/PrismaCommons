package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import com.google.common.base.Preconditions;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractInjectableFilter implements InjectableFilter {
	
    private final Logger logger;
    protected Filter previousFilter = null;

    protected AbstractInjectableFilter(Logger logger) {
        Preconditions.checkNotNull(logger, "logger");
        this.logger = logger;
    }

    @Override
    public boolean isInjected() {
        return logger.getFilter() == this;
    }

    @Override
    public Filter inject() {
        if (isInjected()) {
            return logger.getFilter();
        }

        previousFilter = logger.getFilter();
        logger.setFilter(this);
        logger.log(Level.INFO, "[LogFilter] Logs sendo filtrados. by {0}", this);
        return logger.getFilter();
    }

    @Override
    public boolean reset() {
        if (!isInjected()) {
            return false;
        } else {
            logger.setFilter(previousFilter);
            return true;
        }
    }

    @Override
    public Filter getPreviousFilter() {
        return previousFilter;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}