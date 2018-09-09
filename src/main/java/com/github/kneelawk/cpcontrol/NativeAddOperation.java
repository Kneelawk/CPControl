package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

public class NativeAddOperation implements DependencyOperation {
	private File nativeDir;

	public NativeAddOperation(File nativeDir) {
		this.nativeDir = nativeDir;
	}

	@Override
	public void perform(ClassPath cp, File baseDir) throws IOException {
		cp.addNativeDir(nativeDir.getCanonicalPath());
	}
}