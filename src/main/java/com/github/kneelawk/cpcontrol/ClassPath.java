package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ClassPath {
	private Set<File> classpath = new HashSet<>();
	private Set<String> nativeDirs = new HashSet<>();

	public ClassPath() {
	}

	public ClassPath(Set<File> classpath, Set<String> nativeDirs) {
		super();
		this.classpath = classpath;
		this.nativeDirs = nativeDirs;
	}

	public Set<File> getClasspath() {
		return classpath;
	}

	public Set<String> getNativeDirs() {
		return nativeDirs;
	}

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