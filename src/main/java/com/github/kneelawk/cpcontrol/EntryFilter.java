package com.github.kneelawk.cpcontrol;

import java.io.IOException;

public interface EntryFilter {
	public boolean accept(String path) throws IOException;
}