package com.github.kneelawk.cpcontrol;

import java.io.File;

public class DirectoryDestinationProvider implements DestinationProvider {
	private File parent;
	private ResourceDeletionPolicy policy;

	public DirectoryDestinationProvider(File parent, ResourceDeletionPolicy policy) {
		this.parent = parent;
		this.policy = policy;
	}

	@Override
	public File getFile(String path) {
		return CPControl4.inactResourceDeletionPolicy(new File(parent, path), policy);
	}
}