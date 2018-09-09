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

public class NativeExtractFromClasspathOperation implements DependencyOperation {
	private List<ExtractFromCollectionDescription> descs = new ArrayList<>();

	public NativeExtractFromClasspathOperation addNative(ExtractFromCollectionDescription desc) {
		descs.add(desc);
		return this;
	}

	public NativeExtractFromClasspathOperation addNative(String dirName, FileFilter toSearch, EntryFilter searchFor,
			ResourceDeletionPolicy deletionPolicy) {
		addNative(new ExtractFromClasspathDescription(dirName, toSearch, searchFor, deletionPolicy));
		return this;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		final File nativesDir = new File(baseDir, CPControl4.NATIVES_DIR_NAME);
		if (!nativesDir.exists())
			nativesDir.mkdirs();

		Set<File> libs = new HashSet<>();
		libs.addAll(CPControl4.getLibrariesOnClasspath());
		libs.addAll(cp.classpath);

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
			OwnedObjectExtractionHandler handler = new OwnedObjectExtractionHandler(nativesDir,
					whoWantsWhat.get(lib));
			Set<File> extracted = CPControl4.extractFilesMatching(lib, handler, handler);

			for (File f : extracted) {
				cp.nativeDirs.add(f.getParentFile().getCanonicalPath());
			}
		}
	}
}