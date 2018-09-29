/**
 * CPControl module. This module enables a launcher application to load target
 * application with extra dependencies and run it.
 */

module com.github.kneelawk.cpcontrol {
	// This is the package that pretty much everything happens in.
	exports com.github.kneelawk.cpcontrol;
	exports com.github.kneelawk.cpcontrol.util;

	// Requires java.base because basic java stuff is needed.
	requires java.base;
}
