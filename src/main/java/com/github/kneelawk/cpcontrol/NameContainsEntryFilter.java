package com.github.kneelawk.cpcontrol;

public class NameContainsEntryFilter implements EntryFilter {
	private String contents;

	public NameContainsEntryFilter(String contents) {
		this.contents = contents.toLowerCase();
	}

	@Override
	public boolean accept(String path) {
		String name = CPControl4.getPathName(path).toLowerCase();
		return name.contains(contents);
	}
}