package com.github.kneelawk.cpcontrol;

import java.io.IOException;

/**
 * EntryFilter - Used when filtering the contents of an archive like a jar.
 * 
 * @author Kneelawk
 *
 */
public interface EntryFilter {
	public boolean accept(String path) throws IOException;
}