package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

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