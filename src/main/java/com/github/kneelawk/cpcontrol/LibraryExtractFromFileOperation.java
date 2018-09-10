package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * LibraryExtractFromFileOperation - Finds a jar resource within a specific
 * archive. Then it extracts that jar resource and adds it to the classpath.
 * 
 * @see DependencyOperation
 * @author Kneelawk
 *
 */
public class LibraryExtractFromFileOperation implements DependencyOperation {
	private File file;
	private List<ExtractDescription> descs = new ArrayList<>();

	public LibraryExtractFromFileOperation(File file) {
		this.file = file;
	}

	public LibraryExtractFromFileOperation addLibrary(ExtractDescription desc) {
		descs.add(desc);
		return this;
	}

	public LibraryExtractFromFileOperation addLibrary(String dirName, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		addLibrary(new ExtractFromFileDescription(dirName, searchFor, deletionPolicy));
		return this;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		File libsDir = new File(baseDir, CPControl4.LIBS_DIR_NAME);

		OwnedObjectExtractionHandler handler = new OwnedObjectExtractionHandler(libsDir, descs);

		Set<File> extracted = CPControl4.extractFilesMatching(file, handler, handler);

		cp.getClasspath().addAll(extracted);
	}
}