package org.mingy.lunjian;

public interface CommandExecutor {

	void executeCmd(String command);
	
	boolean isFighting();

	void notify(String message);
}
