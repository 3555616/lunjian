package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PintuTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】(.+)对着(.+)叫道：.+，今天你可是在我的地盘，看来你是在劫难逃！$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"sys".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		process(cmdline, m.group(1), m.group(2));
		return true;
	}

	protected void process(CommandLine cmdline, String bad_npc, String good_npc) {
		cmdline.notify("[拼图] " + bad_npc + " vs " + good_npc, false, false);
	}

	@Override
	public void cleanup() {

	}
}
