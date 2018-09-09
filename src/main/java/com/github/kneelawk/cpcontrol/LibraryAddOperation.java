package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

public class LibraryAddOperation implements DependencyOperation {
	private File libFile;

	public LibraryAddOperation(File libFile) {
		this.libFile = libFile;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		cp.addLibrary(libFile);
	}
}