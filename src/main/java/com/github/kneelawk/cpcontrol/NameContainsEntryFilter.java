package com.github.kneelawk.cpcontrol;

/**
 * NameContainsEntryFilter - Filters entries based on if their name contains the
 * string given in the constructor, regardless of case.
 * 
 * @see EntryFilter
 * @author Kneelawk
 *
 */
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