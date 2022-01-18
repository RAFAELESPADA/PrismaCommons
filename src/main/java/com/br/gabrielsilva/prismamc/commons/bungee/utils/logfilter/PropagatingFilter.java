package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import java.util.Arrays;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PropagatingFilter extends AbstractInjectableFilter {
	
    protected PropagatingFilter(Logger logger) {
        super(logger);
    }

    @Override
    public boolean isInjected() {
        return super.isInjected() || inCompound();
    }

    @Override
    public Filter inject() {
        if (isInjected()) {
        } else if (isCompound()) {
            ((CompoundFilter) getLogger().getFilter()).addFilter(this);
        } else if (getLogger().getFilter() != null) {
            previousFilter = getLogger().getFilter();
            getLogger().setFilter(new CompoundFilter(
                    getLogger(),
                    Arrays.asList(this, previousFilter)
            ));
        } else {
            return super.inject();
        }
        return getLogger().getFilter();
    }

    @Override
    public boolean reset() {
        if (inCompound()) {
            CompoundFilter compound = (CompoundFilter) getLogger().getFilter();
            compound.removeFilter(this);
            if (compound.getFilters().size() == 1 && 
                    compound.getFilters().get(0) == previousFilter) {
                getLogger().setFilter(previousFilter); 
            } else if (compound.getFilters().isEmpty()) {
                getLogger().setFilter(null);
            }
            return !isCompound();
        } else if (isCompound()) {
            getLogger().log(Level.WARNING,
                    "[QuietCord] Could not reset filter {0} because no longer in compound!",
                    new Object[]{this, getLogger().getFilter(), getLogger().getName()});
            return false;
        } else {
            return super.reset();
        }
    }

    protected boolean inCompound() {
        return isCompound() && ((CompoundFilter) getLogger().getFilter()).hasFilter(this);
    }

    protected boolean isCompound() {
        return getLogger().getFilter() instanceof CompoundFilter;
    }
}