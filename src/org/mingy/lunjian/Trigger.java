package org.mingy.lunjian;

public interface Trigger {

	boolean match(CommandExecutor executor, String message);
}
