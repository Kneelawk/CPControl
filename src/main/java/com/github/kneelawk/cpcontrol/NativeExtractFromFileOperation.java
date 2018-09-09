package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NativeExtractFromFileOperation implements DependencyOperation {
	private File file;
	private List<ExtractDescription> descs = new ArrayList<>();

	public NativeExtractFromFileOperation(File file) {
		this.file = file;
	}

	public NativeExtractFromFileOperation addNative(ExtractDescription desc) {
		descs.add(desc);
		return this;
	}

	public NativeExtractFromFileOperation addNative(String dirName, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		addNative(new ExtractFromFileDescription(dirName, searchFor, deletionPolicy));
		return this;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		File nativesDir = new File(baseDir, CPControl4.NATIVES_DIR_NAME);

		OwnedObjectExtractionHandler handler = new OwnedObjectExtractionHandler(nativesDir, descs);

		Set<File> extracted = CPControl4.extractFilesMatching(file, handler, handler);

		for (File f : extracted) {
			cp.getNativeDirs().add(f.getParentFile().getCanonicalPath());
		}
	}
}