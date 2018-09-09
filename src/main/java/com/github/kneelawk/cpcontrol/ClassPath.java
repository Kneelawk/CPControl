package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClassPath {
	public Set<File> classpath = new HashSet<>();
	public Set<String> nativeDirs = new HashSet<>();

	public void addLibrary(File lib) {
		classpath.add(lib);
	}

	public void addLibraries(Collection<File> libs) {
		classpath.addAll(libs);
	}

	public void addNativeDir(String dir) {
		nativeDirs.add(dir);
	}

	public void addNativeDirs(Collection<String> dirs) {
		nativeDirs.addAll(dirs);
	}
}