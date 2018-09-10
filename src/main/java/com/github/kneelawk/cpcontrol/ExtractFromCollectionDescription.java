package com.github.kneelawk.cpcontrol;

/**
 * ExtractFromCollectionDescription - This describes the details for extracting
 * a resource from a collection of archives. The FileFilter, getToSearch(),
 * filters which archives will be searched for the resource.
 * 
 * @see ExtractDescription
 * @author Kneelawk
 *
 */
public interface ExtractFromCollectionDescription extends ExtractDescription {
	public FileFilter getToSearch();
}