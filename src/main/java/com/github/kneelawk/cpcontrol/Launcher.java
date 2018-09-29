package com.github.kneelawk.cpcontrol;

import java.io.IOException;

public interface Launcher {
	public void launch(String[] args) throws IOException, InterruptedException;
}
