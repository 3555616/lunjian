package org.mingy.lunjian;

public interface Trigger {

	boolean match(CommandLine cmdline, String message, String type);
	
	void cleanup();
}
