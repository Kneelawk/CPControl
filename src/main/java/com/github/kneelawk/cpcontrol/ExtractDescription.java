package com.github.kneelawk.cpcontrol;

/**
 * ExtractDescription - This describes a the details for a basic resource
 * extraction operation, in which a resource is found using the EntryFilter,
 * getSearchFor(), in an archive provided elsewhere, is placed in a specific
 * directory, named by getDirName(), that is a subdirectory of a base directory
 * supplied elsewhere, and is governed by the ResourceDeletionPolicy,
 * getDeletionPolicy().
 * 
 * @author Kneelawk
 *
 */
public interface ExtractDescription {
	public String getDirName();

	public EntryFilter getSearchFor();

	public ResourceDeletionPolicy getDeletionPolicy();
}