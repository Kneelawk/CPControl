package com.github.kneelawk.cpcontrol;

public interface ExtractDescription {
	public String getDirName();

	public EntryFilter getSearchFor();

	public ResourceDeletionPolicy getDeletionPolicy();
}