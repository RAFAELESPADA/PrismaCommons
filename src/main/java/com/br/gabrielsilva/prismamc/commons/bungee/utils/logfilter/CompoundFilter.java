package com.br.gabrielsilva.prismamc.commons.bungee.utils.logfilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CompoundFilter extends AbstractInjectableFilter {
	
    private final List<Filter> filters;

    public CompoundFilter(Logger logger) {
        this(logger, new LinkedList<Filter>());
    }

    public CompoundFilter(Logger logger, List<Filter> filters) {
        super(logger);
        this.filters = new ArrayList<>(filters);
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        for (Filter filter : filters) {
             if (!filter.isLoggable(record)) {
                 return false;
             }
         }
         return true;
    }

    public void addFilter(Filter filter) {
        filters.add(filter);
    }

    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }

    public boolean hasFilter(Filter filter) {
        return filters.contains(filter);
    }

    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }

    @Override
    public String toString() {
        return "CompoundFilter{" +
        "filters=" + filters +
        '}';
    }
}