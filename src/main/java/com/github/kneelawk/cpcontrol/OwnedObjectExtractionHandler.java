package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OwnedObjectExtractionHandler implements EntryFilter, DestinationProvider {
	private File baseDir;
	private Collection<? extends ExtractDescription> descs;

	private Map<String, ExtractDescription> acceptedDirDescriptions = new HashMap<>();

	public OwnedObjectExtractionHandler(File baseDir, Collection<? extends ExtractDescription> descs) {
		this.baseDir = baseDir;
		this.descs = descs;
	}

	@Override
	public File getFile(String path) {
		// getFile should not be called until the path has already been
		// accepted
		if (!acceptedDirDescriptions.containsKey(path))
			throw new RuntimeException("File destination requested before the path has been accepted");
		ExtractDescription desc = acceptedDirDescriptions.get(path);
		File dir = new File(baseDir, desc.getDirName());
		if (!dir.exists())
			dir.mkdir();
		return CPControl4.enactResourceDeletionPolicy(new File(dir, CPControl4.getPathName(path)), desc.getDeletionPolicy());
	}

	@Override
	public boolean accept(String path) throws IOException {
		boolean keep = false;
		for (ExtractDescription desc : descs) {
			if (desc.getSearchFor().accept(path)) {
				keep = true;
				acceptedDirDescriptions.put(path, desc);
				break;
			}
		}
		return keep;
	}

}