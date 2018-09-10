package com.github.kneelawk.cpcontrol;

/**
 * ErrorCallback - Implementing this interface allows one to listen for errors
 * when invoking the CPControl's target.
 * 
 * @author Kneelawk
 *
 */
public interface ErrorCallback {
	public void error(Throwable t);
}