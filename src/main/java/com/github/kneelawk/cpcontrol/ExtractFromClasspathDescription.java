package com.github.kneelawk.cpcontrol;

public class ExtractFromClasspathDescription extends ExtractFromFileDescription
		implements ExtractFromCollectionDescription {
	private FileFilter toSearch;

	public ExtractFromClasspathDescription(String dirName, FileFilter toSearch, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		super(dirName, searchFor, deletionPolicy);
		this.toSearch = toSearch;
	}

	public FileFilter getToSearch() {
		return toSearch;
	}
}