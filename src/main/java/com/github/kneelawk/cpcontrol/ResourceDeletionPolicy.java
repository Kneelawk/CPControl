package com.github.kneelawk.cpcontrol;

import java.io.File;

/**
 * ResourceDeletionPolicy - Used to determine whether a resource file should be
 * deleted when the application exits.
 * 
 * @author Kneelawk
 *
 */
public interface ResourceDeletionPolicy {
	public boolean shouldDeleteOnExit(File resource);
}