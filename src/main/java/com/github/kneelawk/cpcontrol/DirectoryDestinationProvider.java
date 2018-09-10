package com.github.kneelawk.cpcontrol;

import java.io.File;

/**
 * DirectoryDestinationProvider - Provides a location for a resource by placing
 * its path within the parent directory given on construction.
 * 
 * @see DestinationProvider
 * @author Kneelawk
 *
 */
public class DirectoryDestinationProvider implements DestinationProvider {
	private File parent;
	private ResourceDeletionPolicy policy;

	public DirectoryDestinationProvider(File parent, ResourceDeletionPolicy policy) {
		this.parent = parent;
		this.policy = policy;
	}

	@Override
	public File getFile(String path) {
		return CPControl4.enactResourceDeletionPolicy(new File(parent, path), policy);
	}
}