package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;

/**
 * DependencyOperation - Interface that all Dependency Operations must
 * implement. When the CPControl launches, it performs each Dependency Operation
 * in the order in which they were added to the CPControl. After each Dependency
 * Operation has been performed, the ClassPath object of the compiled actions of
 * all the Dependency Operations is applied to the environment. First all the
 * ClassPath object's native directories are added to the environment, then a
 * Thread is constructed and launched with a URLClassLoader as its Context
 * ClassLoader, created from all the class path elements within the ClassPath
 * object.
 * 
 * @author Kneelawk
 *
 */
public interface DependencyOperation {
	public void perform(ClassPath cp, File baseDir) throws IOException;
}