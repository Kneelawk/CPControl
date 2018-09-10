package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

/**
 * AndFileFilter - Takes multiple FileFilters as constructor arguments. Only
 * accepts a file that is accepted by all FileFilters within it.
 * 
 * @see FileFilter
 * @author Kneelawk
 *
 */
public class AndFileFilter implements FileFilter {
	private FileFilter[] filters;

	public AndFileFilter(FileFilter... filters) {
		this.filters = filters;
	}

	@Override
	public boolean accept(File file) throws IOException {
		for (FileFilter filter : filters) {
			if (!filter.accept(file))
				return false;
		}
		return true;
	}
}