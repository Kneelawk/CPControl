package com.github.kneelawk.cpcontrol;

import java.io.File;

public class FlatDestinationProvider implements DestinationProvider {
	private File parent;
	private ResourceDeletionPolicy policy;

	public FlatDestinationProvider(File parent, ResourceDeletionPolicy policy) {
		this.parent = parent;
		this.policy = policy;
	}

	@Override
	public File getFile(String path) {
		return CPControl4.inactResourceDeletionPolicy(new File(parent, CPControl4.getPathName(path)), policy);
	}
}