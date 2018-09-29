package com.github.kneelawk.cpcontrol.util;

import java.io.File;

public class RuntimeConstants {
	public static final String OS_NAME = System.getProperty("os.name");
	public static final String JAVA_HOME = System.getProperty("java.home");
	public static final String JAVA_EXECUTIBLE = JAVA_HOME + File.separator
			+ (OS_NAME.contains("Windows") ? "java.exe" : "java");
	public static final String JAVA_LIBRARY_PATH = System.getProperty("java.library.path");
	public static final String USER_DIR = System.getProperty("user.dir");
}
