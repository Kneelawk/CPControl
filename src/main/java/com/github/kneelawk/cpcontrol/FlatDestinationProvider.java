package com.github.kneelawk.cpcontrol;

import java.io.File;

/**
 * FlatDestinationProvider - Provides a destination for a resource by placing
 * the path's filename directly within the parent directory.
 * 
 * @see DestinationProvider
 * @author Kneelawk
 *
 */
public class FlatDestinationProvider implements DestinationProvider {
	private File parent;
	private ResourceDeletionPolicy policy;

	public FlatDestinationProvider(File parent, ResourceDeletionPolicy policy) {
		this.parent = parent;
		this.policy = policy;
	}

	@Override
	public File getFile(String path) {
		return CPControl4.enactResourceDeletionPolicy(new File(parent, CPControl4.getPathName(path)), policy);
	}
}