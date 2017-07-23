package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaozangTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】山河藏宝图：听说绝世高手遗落了许多宝物在秘密之所");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"system".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		process(cmdline);
		return true;
	}

	protected void process(CommandLine cmdline) {
		cmdline.notify("[宝藏图]", false, false);
		if (!cmdline.isFighting()) {
			cmdline.executeCmd("items get_store /obj/quest/cangbaotu;tu");
		}
	}

	@Override
	public void cleanup() {

	}
}
