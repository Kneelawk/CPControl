package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

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