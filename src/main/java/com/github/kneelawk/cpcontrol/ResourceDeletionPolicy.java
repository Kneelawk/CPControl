package com.github.kneelawk.cpcontrol;

import java.io.File;

public interface ResourceDeletionPolicy {
	public boolean shouldDeleteOnExit(File resource);
}