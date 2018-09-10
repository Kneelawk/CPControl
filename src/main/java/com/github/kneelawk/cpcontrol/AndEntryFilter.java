package com.github.kneelawk.cpcontrol;

import java.io.IOException;

/**
 * AndEntryFilter - Takes multiple EntryFilters as constructor arguments. Only
 * accepts an entry when all the EntryFilters within it accept the entry.
 * 
 * @see EntryFilter
 * @author Kneelawk
 *
 */
public class AndEntryFilter implements EntryFilter {
	private EntryFilter[] filters;

	public AndEntryFilter(EntryFilter... filters) {
		this.filters = filters;
	}

	@Override
	public boolean accept(String path) throws IOException {
		for (EntryFilter filter : filters) {
			if (!filter.accept(path))
				return false;
		}
		return true;
	}
}