package com.github.kneelawk.cpcontrol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Launcher - Used for invoking and managing the running target class.
 * 
 * @author Kneelawk
 *
 */
public class Launcher {
	protected ClassLoader loader;
	protected String mainClass;
	protected String[] args;
	protected ErrorCallback error;

	public Launcher(ClassLoader loader, String mainClass, String[] args, ErrorCallback error) {
		this.loader = loader;
		this.mainClass = mainClass;
		this.args = args;
		this.error = error;
	}

	public void start() throws InterruptedException {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(mainClass);
					Method main = clazz.getMethod("main", String[].class);
					main.invoke(null, new Object[] { args });
				} catch (ClassNotFoundException e) {
					error.error(e);
				} catch (NoSuchMethodException e) {
					error.error(e);
				} catch (SecurityException e) {
					error.error(e);
				} catch (IllegalAccessException e) {
					error.error(e);
				} catch (IllegalArgumentException e) {
					error.error(e);
				} catch (InvocationTargetException e) {
					error.error(e);
				} catch (Exception e) {
					error.error(e);
				}
			}
		}, "Application");
		t.setContextClassLoader(loader);
		t.start();
		t.join();
	}
}