package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

/**
 * NameContainsFileFilter - Filters files based on if their name contains the
 * string given in the constructor, regardless of case.
 * 
 * @see FileFilter
 * @author Kneelawk
 *
 */
public class NameContainsFileFilter implements FileFilter {
	private String contents;

	public NameContainsFileFilter(String contents) {
		this.contents = contents.toLowerCase();
	}

	@Override
	public boolean accept(File file) throws IOException {
		String name = file.getName().toLowerCase();
		return name.contains(contents);
	}
}