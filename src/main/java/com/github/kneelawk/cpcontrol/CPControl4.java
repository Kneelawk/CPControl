package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * CPControl v4.0.0<br>
 * This is an almost monolithic utility that allows a launcher application to
 * load a target application with all its dependencies.
 * 
 * @author Kneelawk
 *
 */
public class CPControl4 {
	/**
	 * The file in which to store the extracted resources.
	 */
	protected File baseDir;
	/**
	 * The fully qualified class name of the main class of the target application.
	 */
	protected String mainClassName;

	/**
	 * A list of every {@link DependencyOperation} to be performed before the target
	 * application launches.
	 */
	protected List<DependencyOperation> operations = new ArrayList<>();

	/**
	 * The class loader used as the context class loader for the application thread.
	 */
	protected URLClassLoader loader;

	/**
	 * The {@link ErrorCallback} to be used.
	 */
	protected ErrorCallback errorCallback = DEFAULT_ERROR_CALLBACK;

	/**
	 * Creates a {@link CPControl4} with the fully qualified class name of the
	 * target application's main class.
	 * 
	 * @param mainClassName
	 *            the fully qualified class name of the target application's main
	 *            class.
	 */
	public CPControl4(String mainClassName) {
		this(mainClassName, createBaseDir());
	}

	/**
	 * Creates a {@link CPControl4} with the fully qualified class name of the
	 * target application's main class and with a custom directory for storing
	 * resource files.
	 * 
	 * @param mainClassName
	 *            the fully qualified class name of the target application's main
	 *            class
	 * @param baseDir
	 *            the directory for storing resource files
	 */
	public CPControl4(String mainClassName, File baseDir) {
		this.mainClassName = mainClassName;
		this.baseDir = baseDir;

		Thread hook = new Thread(new Runnable() {
			@Override
			public void run() {
				if (loader != null) {
					try {
						loader.close();
					} catch (IOException e) {
						System.err.println("Error closing class loader");
						e.printStackTrace();
					}
				}
			}
		});

		Runtime.getRuntime().addShutdownHook(hook);
	}

	/**
	 * Gets the directory that all the resources are copied into.
	 * 
	 * @return the directory that all the resources are copied into
	 */
	public File getBaseDir() {
		return baseDir;
	}

	/**
	 * Adds a custom {@link DependencyOperation} to the list of operations.
	 * 
	 * @param operation
	 *            the {@link DependencyOperation} to be added
	 */
	public void addOperation(DependencyOperation operation) {
		operations.add(operation);
	}

	/**
	 * Adds an operation that adds a file to the list of libraries on the classpath.
	 * 
	 * @param library
	 *            the library file to be added to the classpath
	 */
	public void addLibrary(File library) {
		operations.add(new LibraryAddOperation(library));
	}

	/**
	 * Adds an operation that adds a directory to the list of directories containing
	 * natives.
	 * 
	 * @param nativeDir
	 *            the directory to be added to the list of directories containing
	 *            natives
	 */
	public void addNativeDir(File nativeDir) {
		operations.add(new NativeAddOperation(nativeDir));
	}

	/**
	 * Creates a {@link LibraryExtractFromClasspathOperation}, adds it to the list
	 * of operations, and returns it to be configured.
	 * 
	 * @return the {@link LibraryExtractFromClasspathOperation} to be configured
	 */
	public LibraryExtractFromClasspathOperation addExtractingFromClasspathLibrary() {
		LibraryExtractFromClasspathOperation operation = new LibraryExtractFromClasspathOperation();
		operations.add(operation);
		return operation;
	}

	/**
	 * Creates a {@link NativeExtractFromClasspathOperation}, adds it to the list of
	 * operations, and returns it to be configured.
	 * 
	 * @return the {@link NativeExtractFromClasspathOperation} to be configured
	 */
	public NativeExtractFromClasspathOperation addExtractingFromClasspathNativeDir() {
		NativeExtractFromClasspathOperation operation = new NativeExtractFromClasspathOperation();
		operations.add(operation);
		return operation;
	}

	/**
	 * Creates a {@link LibraryExtractFromFileOperation}, adds it to the list of
	 * operations, and returns it to be configured.
	 * 
	 * @param file
	 *            the file that the {@link LibraryExtractFromFileOperation} extracts
	 *            from
	 * @return the {@link LibraryExtractFromFileOperation} to be configured
	 */
	public LibraryExtractFromFileOperation addExtractingFromFileLibrary(File file) {
		LibraryExtractFromFileOperation operation = new LibraryExtractFromFileOperation(file);
		operations.add(operation);
		return operation;
	}

	/**
	 * Creates a {@link NativeExtractFromFileOperation}, adds it to the list of
	 * operations, and returns it to be configured.
	 * 
	 * @param file
	 *            the file that the {@link NativeExtractFromFileOperation} extracts
	 *            from
	 * @return the {@link NativeExtractFromFileOperation} to be configured
	 */
	public NativeExtractFromFileOperation addExtractingFromFileNativeDir(File file) {
		NativeExtractFromFileOperation operation = new NativeExtractFromFileOperation(file);
		operations.add(operation);
		return operation;
	}

	/**
	 * Sets the {@link CPControl4}'s error callback to something other than the
	 * default.
	 * 
	 * @param callback
	 *            the new {@link ErrorCallback} to set.
	 */
	public void setErrorCallback(ErrorCallback callback) {
		errorCallback = callback;
	}

	/**
	 * Start the target application. Every {@link DependencyOperation} is performed
	 * in the order they were added, and the {@link ClassPath} object resulting from
	 * the compiled operations of each {@link DependencyOperation} is applied to the
	 * environment. Each of the {@link ClassPath} object's native directories are
	 * added to the {@link ClassLoader}'s list of directories containing natives.
	 * Then a thread is started with a {@link URLClassLoader} as its context class
	 * loader, constructed from all the libraries in the {@link ClassPath} object.
	 * 
	 * @param args
	 *            the main arguments to be supplied to the target application
	 * @throws IOException
	 *             if there is an error setting up the target application to be
	 *             launched
	 * @throws InterruptedException
	 *             if this thread receives an interrupt while waiting for the target
	 *             application to finish
	 */
	public void launch(String[] args) throws IOException, InterruptedException {
		ClassPath path = new ClassPath();

		for (DependencyOperation operation : operations) {
			operation.perform(path, baseDir);
		}

		for (String dir : path.getNativeDirs()) {
			addNativesDir(dir);
		}

		URL[] urls = copyFilesToClassPath(path.getClasspath());
		loader = new URLClassLoader(urls);

		Launcher launcher = new Launcher(loader, mainClassName, args, errorCallback);
		launcher.start();
	}

	/**
	 * The name of the directory in which to store the libraries.
	 */
	public static final String LIBS_DIR_NAME = "libs";
	/**
	 * The name of the directory in which to store the natives.
	 */
	public static final String NATIVES_DIR_NAME = "natives";

	/**
	 * A file pointing to the archive that contains this class.
	 */
	public static final File ME = new File(
			CPControl4.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	/**
	 * A file pointing to the parent directory of the archive that contains this
	 * class.
	 */
	public static final File PARENT = ME.getParentFile();

	/**
	 * A {@link FileFilter} for testing if a file is a jar file.
	 */
	public static final FileFilter IS_JAR_FILE = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".jar");
		}
	};

	/**
	 * A {@link FileFilter} for testing if a file is a native file.
	 */
	public static final FileFilter IS_NATIVE_FILE = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String name = file.getName().toLowerCase();
			return name.endsWith(".so") || name.endsWith(".dll") || name.endsWith(".jnilib") || name.endsWith(".dylib");
		}
	};

	/**
	 * A {@link FileFilter} for testing if a file is the archive this class is
	 * stored in.
	 */
	public static final FileFilter IS_ME = new FileFilter() {
		@Override
		public boolean accept(File file) throws IOException {
			return file.getCanonicalPath().equals(ME.getCanonicalPath());
		}
	};

	/**
	 * An {@link EntryFilter} for testing if a path string describes a jar entry.
	 */
	public static final EntryFilter IS_JAR_ENTRY = new EntryFilter() {
		@Override
		public boolean accept(String path) throws IOException {
			return path.toLowerCase().endsWith(".jar");
		}
	};

	/**
	 * An {@link EntryFilter} for testing if a path string describes a native entry.
	 */
	public static final EntryFilter IS_NATIVE_ENTRY = new EntryFilter() {
		@Override
		public boolean accept(String path) throws IOException {
			String lower = path.toLowerCase();
			return lower.endsWith(".so") || lower.endsWith(".dll") || lower.endsWith(".jnilib")
					|| lower.endsWith(".dylib");
		}
	};

	/**
	 * A {@link ResourceDeletionPolicy} that deletes every file when the application
	 * exits.
	 */
	public static final ResourceDeletionPolicy ALWAYS_DELETE = new ResourceDeletionPolicy() {
		@Override
		public boolean shouldDeleteOnExit(File resource) {
			return true;
		}
	};

	/**
	 * A {@link ResourceDeletionPolicy} that does not delete any file when the
	 * application exits.
	 */
	public static final ResourceDeletionPolicy NEVER_DELETE = new ResourceDeletionPolicy() {
		@Override
		public boolean shouldDeleteOnExit(File resource) {
			return false;
		}
	};

	/**
	 * An {@link ErrorCallback} that simply prints a stack trace when an error
	 * occurs.
	 */
	public static final ErrorCallback DEFAULT_ERROR_CALLBACK = new ErrorCallback() {
		@Override
		public void error(Throwable t) {
			t.printStackTrace();
		}
	};

	private static Set<File> librariesOnClasspath;

	/**
	 * Creates a temporary directory based on the file containing this class's name.
	 * 
	 * @return the temporary directory for storing resource files
	 */
	public static File createBaseDir() {
		try {
			return Files.createTempDirectory(ME.getName()).toFile();
		} catch (IOException e) {
			return PARENT;
		}
	}

	/**
	 * Finds the filename associated with the path string.
	 * 
	 * @param path
	 *            the path string to find the filename of
	 * @return the filename of the path string
	 */
	public static String getPathName(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * If the supplied {@link ResourceDeletionPolicy} determines the file should be
	 * deleted on exit, this method flags that file for deletion on exit.
	 * 
	 * @param resource
	 *            the file on which the {@link ResourceDeletionPolicy} is enacted
	 *            upon
	 * @param policy
	 *            the policy used to determine whether the resource file should be
	 *            deleted on exit
	 * @return the resource file
	 */
	public static File enactResourceDeletionPolicy(File resource, ResourceDeletionPolicy policy) {
		if (policy.shouldDeleteOnExit(resource))
			resource.deleteOnExit();
		return resource;
	}

	/**
	 * Generates an array of all the classpath entries.
	 * 
	 * @return an array of all the classpath entries.
	 */
	public static String[] getClassPath() {
		String classPath = System.getProperty("sun.boot.class.path") + File.pathSeparator
				+ System.getProperty("java.ext.path") + File.pathSeparator + System.getProperty("java.class.path");
		return classPath.split(File.pathSeparator);
	}

	private static Set<File> findLibrariesOnClasspath() throws IOException {
		Set<File> found = new HashSet<>();

		String[] classPath = getClassPath();
		for (String path : classPath) {
			File file = new File(path);
			if (file.exists())
				recursiveSearch(found, new HashSet<>(), file, IS_JAR_FILE);
		}

		return found;
	}

	/**
	 * Forces recreation of the list of all the jar files on the classpath.
	 * 
	 * @return the newly created list of all the jars on the classpath
	 * @throws IOException
	 *             if there is an error finding the jars
	 */
	public static Set<File> recalculateLibrariesOnClasspath() throws IOException {
		return librariesOnClasspath = findLibrariesOnClasspath();
	}

	/**
	 * Returns a list of all the jars on the classpath. Creates the list if it is
	 * empty.
	 * 
	 * @return a set of all the jar files on the classpath
	 * @throws IOException
	 *             if there is an error while finding the jars
	 */
	public static Set<File> getLibrariesOnClasspath() throws IOException {
		if (librariesOnClasspath == null) {
			librariesOnClasspath = findLibrariesOnClasspath();
		}
		return librariesOnClasspath;
	}

	private static void recursiveSearch(Collection<File> found, Set<File> searched, File dir, FileFilter filter)
			throws IOException {
		if (searched.contains(dir))
			return;
		if (dir.isDirectory()) {
			searched.add(dir);
			File[] children = dir.listFiles();
			for (File child : children) {
				if (child.isDirectory()) {
					if (searched.contains(child))
						continue;
					recursiveSearch(found, searched, child, filter);
				} else if (filter.accept(child)) {
					found.add(child);
				}
			}
		} else {
			if (filter.accept(dir)) {
				found.add(dir);
			}
		}
	}

	/**
	 * Filters a collection of files based on a {@link FileFilter}.
	 * 
	 * @param files
	 *            the collection of files to be filtered
	 * @param filter
	 *            only files accepted by this filter are in the returned set
	 * @return a set of filtered files
	 * @throws IOException
	 *             if there is an error while filtering
	 */
	public static Set<File> filterFiles(Collection<File> files, FileFilter filter) throws IOException {
		Set<File> filteredFiles = new HashSet<>();

		for (File inputFile : files) {
			if (filter.accept(inputFile)) {
				filteredFiles.add(inputFile);
			}
		}

		return filteredFiles;
	}

	/**
	 * Converts a Collection of files into an array of path URLs.
	 * 
	 * @param files
	 *            the collection of files to be converted
	 * @return the array of converted path urls
	 * @throws MalformedURLException
	 *             if there are invalid file paths being converted
	 */
	public static URL[] copyFilesToClassPath(Collection<File> files) throws MalformedURLException {
		int size = files.size();
		URL[] urls = new URL[size];

		Iterator<File> it = files.iterator();
		for (int i = 0; i < size; i++) {
			File file = it.next();
			urls[i] = file.toURI().toURL();
		}

		return urls;
	}

	/**
	 * Extracts all files accepted by the {@link EntryFilter} to locations provided
	 * by the {@link DestinationProvider}.
	 * 
	 * @param archives
	 *            a collection of archives to extract resources from
	 * @param filter
	 *            only extracts resources accepted by this filter
	 * @param destinations
	 *            provides destination locations for each extracted resource
	 * @return the set of all resource files extracted
	 * @throws IOException
	 *             if there is an error while extracting
	 */
	public static Set<File> extractFilesMatching(Collection<File> archives, EntryFilter filter,
			DestinationProvider destinations) throws IOException {
		Set<File> extractedFiles = new HashSet<>();
		for (File archive : archives) {
			extractedFiles.addAll(extractFilesMatching(archive, filter, destinations));
		}
		return extractedFiles;
	}

	/**
	 * Extracts all files accepted by the {@link EntryFilter} to locations provided
	 * by the {@link DestinationProvider}.
	 * 
	 * @param archive
	 *            the archive to extract resources from
	 * @param filter
	 *            only extracts resources accepted by this filter
	 * @param destinations
	 *            provides destination locations for each extracted resource
	 * @return the set of all resource files extracted
	 * @throws IOException
	 *             if there is an error while extracting
	 */
	public static Set<File> extractFilesMatching(File archive, EntryFilter filter, DestinationProvider destinations)
			throws IOException {
		Set<File> extractedFiles = new HashSet<>();

		if (archive.isDirectory()) {
			extractFilesMatchingFromDirectory(archive, new HashSet<>(), extractedFiles, "/", filter, destinations);
		} else {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(archive));
			ZipEntry entry;

			while ((entry = zis.getNextEntry()) != null) {
				String path = entry.getName();
				File dest;
				if (!entry.isDirectory() && filter.accept(path) && (dest = destinations.getFile(path)) != null) {
					// make sure parent dirs exist
					File parent = dest.getParentFile();
					if (!parent.exists())
						parent.mkdirs();

					// copy the file
					FileOutputStream fos = new FileOutputStream(dest);
					copy(zis, fos);
					fos.close();

					// keep track of where we put the files
					extractedFiles.add(dest);
				}
				zis.closeEntry();
			}

			zis.close();
		}

		return extractedFiles;
	}

	private static void extractFilesMatchingFromDirectory(File dir, Set<File> visited, Collection<File> extracted,
			String path, EntryFilter filter, DestinationProvider prov) throws IOException {
		if (visited.contains(dir))
			return;
		visited.add(dir);
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File child : children) {
				path += child.getName();
				if (child.isDirectory()) {
					path += "/";
					extractFilesMatchingFromDirectory(child, visited, extracted, path, filter, prov);
				} else {
					File to;
					if (filter.accept(path) && (to = prov.getFile(path)) != null) {
						FileInputStream fis = new FileInputStream(child);
						FileOutputStream fos = new FileOutputStream(to);

						copy(fis, fos);

						fis.close();
						fos.close();
					}
				}
			}
		} else {
			File to = null;
			if (filter.accept(path) && (to = prov.getFile(path)) != null) {
				FileInputStream fis = new FileInputStream(dir);
				FileOutputStream fos = new FileOutputStream(to);

				copy(fis, fos);

				fis.close();
				fos.close();
			}
		}
	}

	/**
	 * Gets a resource from the classpath relative to CPControl4.class and copies it
	 * to a file.
	 * 
	 * @param path
	 *            the path to the resource relative to CPControl4.class
	 * @param to
	 *            the file to copy the resource to
	 * @throws IOException
	 *             if there is a problem while copying the resource
	 */
	public static void extractFileFromSystemClasspath(String path, File to) throws IOException {
		extractFileFromSystemClasspath(CPControl4.class, path, to);
	}

	/**
	 * Gets a resource from the classpath and copies it to a file.
	 * 
	 * @param relative
	 *            the class that the path is relative to
	 * @param path
	 *            the path to the resource
	 * @param to
	 *            the file to copy the resource to
	 * @throws IOException
	 *             if there is a problem while copying the resource
	 */
	public static void extractFileFromSystemClasspath(Class<?> relative, String path, File to) throws IOException {
		InputStream is = relative.getResourceAsStream(path);
		if (is == null)
			throw new IOException("File: " + path + " not found on classapth");
		FileOutputStream fos = new FileOutputStream(to);
		copy(is, fos);
	}

	/**
	 * Copies all data from an {@link InputStream} to an {@link OutputStream}.
	 * 
	 * @param is
	 *            the stream to be copied from
	 * @param os
	 *            the stream to be copied to
	 * @throws IOException
	 *             if there is an error while copying
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[8192];
		int read;
		while ((read = is.read(buf)) >= 0) {
			os.write(buf, 0, read);
		}
	}

	/**
	 * Adds a directory to the classloader's list of directories containing natives.
	 * 
	 * @param dirName
	 *            the path of the directory to be added
	 * @throws IOException
	 *             if there is a problem adding the directory
	 */
	public static void addNativesDir(String dirName) throws IOException {
		try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at
			// http://forums.sun.com/thread.jspa?threadID=707176
			//
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (dirName.equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = dirName;
			field.set(null, tmp);
			System.setProperty("java.library.path",
					System.getProperty("java.library.path") + File.pathSeparator + dirName);
		} catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
		} catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
		}
	}
}
