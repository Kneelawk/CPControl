package com.github.kneelawk.cpcontrol;

import java.io.IOException;

/**
 * DirectoryEntryFilter - Filters entries based on if their path starts with the
 * directory path given in the constructor.
 * 
 * @see EntryFilter
 * @author Kneelawk
 *
 */
public class DirectoryEntryFilter implements EntryFilter {
	private String dir;

	public DirectoryEntryFilter(String dir) {
		if (!dir.startsWith("/")) {
			dir = "/" + dir;
		}

		this.dir = dir;
	}

	@Override
	public boolean accept(String path) throws IOException {
		if (!path.startsWith("/"))
			path = "/" + path;
		return path.startsWith(dir);
	}
}