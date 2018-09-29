package com.github.kneelawk.cpcontrol;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.kneelawk.cpcontrol.util.RuntimeConstants;

public class SeparateProcessLauncher implements Launcher {

	private List<String> command;

	public SeparateProcessLauncher(Collection<String> classpath, Collection<String> modulepath,
			Collection<String> nativepath, String mainModule, String mainClass) {
		command = new ArrayList<>();

		command.add(RuntimeConstants.JAVA_EXECUTIBLE);
		classpath.stream().reduce((a, b) -> a + File.pathSeparator + b).ifPresent(str -> {
			command.add("-classpath");
			command.add(str);
		});
		modulepath.stream().reduce((a, b) -> a + File.pathSeparator + b).ifPresent(str -> {
			command.add("--module-path");
			command.add(str);
		});
		String nativepathString = nativepath.stream().reduce(RuntimeConstants.JAVA_LIBRARY_PATH,
				(a, b) -> a + File.pathSeparator + b);
		command.add("Djava.library.path=" + nativepathString);

		if (mainModule != null && !"".equals(mainModule)) {
			command.add(mainModule + "/" + mainClass);
		} else {
			command.add(mainClass);
		}
	}

	@Override
	public void launch(String[] args) throws IOException, InterruptedException {
		List<String> runtimeCommand = new ArrayList<>(command);
		for (String argument : args)
			runtimeCommand.add(argument);

		ProcessBuilder builder = new ProcessBuilder(runtimeCommand);
		builder.directory(new File(RuntimeConstants.USER_DIR));
		builder.inheritIO();

		Process process = builder.start();
		process.waitFor();
	}

}
