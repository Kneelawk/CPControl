package com.github.kneelawk.cpcontrol;

public class ExtractFromFileDescription implements ExtractDescription {
	private String dirName;
	private EntryFilter searchFor;
	private ResourceDeletionPolicy deletionPolicy;

	public ExtractFromFileDescription(String dirName, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		super();
		this.dirName = dirName;
		this.searchFor = searchFor;
		this.deletionPolicy = deletionPolicy;
	}

	public String getDirName() {
		return dirName;
	}

	public EntryFilter getSearchFor() {
		return searchFor;
	}

	public ResourceDeletionPolicy getDeletionPolicy() {
		return deletionPolicy;
	}
}