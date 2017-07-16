package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YouxiaTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】游侠会：听说(.*)出来闯荡江湖了，目前正在前往(.*)的路上。$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type) {
		if (!"system".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		process(cmdline, npc, place);
		return true;
	}

	protected void process(CommandLine cmdline, String npc, String place) {
		cmdline.notify("[游侠] " + npc + " at " + place, true, true);
	}

	@Override
	public void cleanup() {
		
	}
}
