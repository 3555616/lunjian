package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerHongbaoTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("跨服：发财树发了一个红包，赶紧.*qhb\\s(\\d+_\\d+)");
	
	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String id = m.group(1);
		cmdline.executeCmd("qhb " + id);
		return true;
	}

	@Override
	public void cleanup() {
		
	}
}
