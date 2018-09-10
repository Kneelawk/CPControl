package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

/**
 * FileFilter - Used for filtering a collection of files.
 * 
 * @author Kneelawk
 *
 */
public interface FileFilter {
	public boolean accept(File file) throws IOException;
}