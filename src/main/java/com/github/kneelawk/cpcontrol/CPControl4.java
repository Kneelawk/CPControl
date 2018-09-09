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
 * CPControl v3.1.2<br>
 * Sorry about the mess. This should be an entire library or at least a package,
 * but is stuffed into one class for ease of copy-and-paste.
 */
public class CPControl4 {
	protected File baseDir;
	protected String mainClassName;

	protected List<DependencyOperation> operations = new ArrayList<>();

	protected URLClassLoader loader;

	protected ErrorCallback errorCallback = DEFAULT_ERROR_CALLBACK;

	public CPControl4(String mainClassName) {
		this(mainClassName, createBaseDir());
	}

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

	public File getBaseDir() {
		return baseDir;
	}

	public void addOperation(DependencyOperation operation) {
		operations.add(operation);
	}

	public void addLibrary(File library) {
		operations.add(new LibraryAddOperation(library));
	}

	public void addNativeDir(File nativeDir) {
		operations.add(new NativeAddOperation(nativeDir));
	}

	public LibraryExtractFromClasspathOperation addExtractingFromClasspathLibrary() {
		LibraryExtractFromClasspathOperation operation = new LibraryExtractFromClasspathOperation();
		operations.add(operation);
		return operation;
	}

	public NativeExtractFromClasspathOperation addExtractingFromClasspathNativeDir() {
		NativeExtractFromClasspathOperation operation = new NativeExtractFromClasspathOperation();
		operations.add(operation);
		return operation;
	}

	public LibraryExtractFromFileOperation addExtractingFromFileLibrary(File file) {
		LibraryExtractFromFileOperation operation = new LibraryExtractFromFileOperation(file);
		operations.add(operation);
		return operation;
	}

	public NativeExtractFromFileOperation addExtractingFromFileNativeDir(File file) {
		NativeExtractFromFileOperation operation = new NativeExtractFromFileOperation(file);
		operations.add(operation);
		return operation;
	}

	public void setErrorCallback(ErrorCallback callback) {
		errorCallback = callback;
	}

	public void launch(String[] args) throws IOException, InterruptedException {
		ClassPath path = new ClassPath();

		for (DependencyOperation operation : operations) {
			operation.perform(path, baseDir);
		}

		for (String dir : path.nativeDirs) {
			addNativesDir(dir);
		}

		URL[] urls = copyFilesToClassPath(path.classpath);
		loader = new URLClassLoader(urls);

		Launcher launcher = new Launcher(loader, mainClassName, args, errorCallback);
		launcher.start();
	}

	public static final String LIBS_DIR_NAME = "libs";
	public static final String NATIVES_DIR_NAME = "natives";

	public static final File ME = new File(
			CPControl4.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	public static final File PARENT = ME.getParentFile();

	public static final FileFilter IS_JAR_FILE = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().toLowerCase().endsWith(".jar");
		}
	};

	public static final FileFilter IS_NATIVE_FILE = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String name = file.getName().toLowerCase();
			return name.endsWith(".so") || name.endsWith(".dll") || name.endsWith(".jnilib") || name.endsWith(".dylib");
		}
	};

	public static final FileFilter IS_ME = new FileFilter() {
		@Override
		public boolean accept(File file) throws IOException {
			return file.getCanonicalPath().equals(ME.getCanonicalPath());
		}
	};

	public static final EntryFilter IS_JAR_ENTRY = new EntryFilter() {
		@Override
		public boolean accept(String path) throws IOException {
			return path.toLowerCase().endsWith(".jar");
		}
	};

	public static final EntryFilter IS_NATIVE_ENTRY = new EntryFilter() {
		@Override
		public boolean accept(String path) throws IOException {
			String lower = path.toLowerCase();
			return lower.endsWith(".so") || lower.endsWith(".dll") || lower.endsWith(".jnilib")
					|| lower.endsWith(".dylib");
		}
	};

	public static final ResourceDeletionPolicy ALWAYS_DELETE = new ResourceDeletionPolicy() {
		@Override
		public boolean shouldDeleteOnExit(File resource) {
			return true;
		}
	};

	public static final ResourceDeletionPolicy NEVER_DELETE = new ResourceDeletionPolicy() {
		@Override
		public boolean shouldDeleteOnExit(File resource) {
			return false;
		}
	};

	public static final ErrorCallback DEFAULT_ERROR_CALLBACK = new ErrorCallback() {
		@Override
		public void error(Throwable t) {
			t.printStackTrace();
		}
	};

	private static Set<File> librariesOnClasspath;

	public static File createBaseDir() {
		try {
			return Files.createTempDirectory(ME.getName()).toFile();
		} catch (IOException e) {
			return PARENT;
		}
	}

	public static String getPathName(String path) {
		return path.substring(path.lastIndexOf('/') + 1);
	}

	public static File inactResourceDeletionPolicy(File resource, ResourceDeletionPolicy policy) {
		if (policy.shouldDeleteOnExit(resource))
			resource.deleteOnExit();
		return resource;
	}

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

	public static Set<File> recalculateLibrariesOnClasspath() throws IOException {
		return librariesOnClasspath = findLibrariesOnClasspath();
	}

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

	public static Set<File> filterFiles(Collection<File> files, FileFilter filter) throws IOException {
		Set<File> filteredFiles = new HashSet<>();

		for (File inputFile : files) {
			if (filter.accept(inputFile)) {
				filteredFiles.add(inputFile);
			}
		}

		return filteredFiles;
	}

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

	public static Set<File> extractFilesMatching(Collection<File> archives, EntryFilter filter,
			DestinationProvider destinations) throws IOException {
		Set<File> extractedFiles = new HashSet<>();
		for (File archive : archives) {
			extractedFiles.addAll(extractFilesMatching(archive, filter, destinations));
		}
		return extractedFiles;
	}

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

	public static void extractFileFromSystemClasspath(String path, File to) throws IOException {
		extractFileFromSystemClasspath(CPControl4.class, path, to);
	}

	public static void extractFileFromSystemClasspath(Class<?> relative, String path, File to) throws IOException {
		InputStream is = relative.getResourceAsStream(path);
		if (is == null)
			throw new IOException("File: " + path + " not found on classapth");
		FileOutputStream fos = new FileOutputStream(to);
		copy(is, fos);
	}

	public static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[8192];
		int read;
		while ((read = is.read(buf)) >= 0) {
			os.write(buf, 0, read);
		}
	}

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
