package com.github.kneelawk.cpcontrol;

import java.io.File;

public interface DestinationProvider {
	public File getFile(String path);
}