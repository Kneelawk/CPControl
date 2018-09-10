package com.github.kneelawk.cpcontrol;

/**
 * ExtractFromClasspathDescription - This object is used for describing how to
 * find a resource somewhere within all the archives on the classpath and how to
 * store the resource once extracted.
 * 
 * @author Kneelawk
 *
 */
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