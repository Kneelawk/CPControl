package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

public interface FileFilter {
	public boolean accept(File file) throws IOException;
}