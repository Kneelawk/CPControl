package com.github.kneelawk.cpcontrol;

import java.io.File;

/**
 * DestinationProvider - Classes implementing this interface supply a location
 * for a resource to be stored at when given that resource's path within an
 * archive.
 * 
 * @author Kneelawk
 *
 */
public interface DestinationProvider {
	public File getFile(String path);
}