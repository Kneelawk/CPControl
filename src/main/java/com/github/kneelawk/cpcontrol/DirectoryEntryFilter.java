package com.github.kneelawk.cpcontrol;

import java.io.IOException;

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