package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * LibraryExtractFromClasspathOperation - Finds a jar resource within the
 * archives on the classpath and extracts it to a location where it itself can
 * be added to the classpath.
 * 
 * @see DependencyOperation
 * @author Kneelawk
 *
 */
public class LibraryExtractFromClasspathOperation implements DependencyOperation {

	private List<ExtractFromCollectionDescription> descs = new ArrayList<>();

	public LibraryExtractFromClasspathOperation addLibrary(ExtractFromCollectionDescription desc) {
		descs.add(desc);
		return this;
	}

	public LibraryExtractFromClasspathOperation addLibrary(String dirName, FileFilter toSearch, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		addLibrary(new ExtractFromClasspathDescription(dirName, toSearch, searchFor, deletionPolicy));
		return this;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		final File libsDir = new File(baseDir, CPControl4.LIBS_DIR_NAME);
		if (!libsDir.exists())
			libsDir.mkdirs();

		Set<File> libs = new HashSet<>();
		libs.addAll(CPControl4.getLibrariesOnClasspath());
		libs.addAll(cp.getClasspath());

		final Map<File, List<ExtractFromCollectionDescription>> whoWantsWhat = new HashMap<>();

		Iterator<File> it = libs.iterator();
		while (it.hasNext()) {
			File lib = it.next();
			List<ExtractFromCollectionDescription> who = new ArrayList<>();
			for (ExtractFromCollectionDescription desc : descs) {
				if (desc.getToSearch().accept(lib)) {
					who.add(desc);
				}
			}
			if (who.isEmpty()) {
				it.remove();
			} else {
				whoWantsWhat.put(lib, who);
			}
		}

		for (final File lib : libs) {
			OwnedObjectExtractionHandler handler = new OwnedObjectExtractionHandler(libsDir, whoWantsWhat.get(lib));
			Set<File> extracted = CPControl4.extractFilesMatching(lib, handler, handler);

			cp.getClasspath().addAll(extracted);
		}
	}
}