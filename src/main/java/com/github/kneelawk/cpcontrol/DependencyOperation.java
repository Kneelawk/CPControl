package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

public interface DependencyOperation {
	public void perform(ClassPath cp, File baseDir) throws IOException;
}