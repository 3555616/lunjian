package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TellTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern.compile("^(.+)告诉你：(.+)$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"local".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (m.find()) {
			String text = m.group(2);
			if (text.startsWith("完成谜题")) {
				cmdline.notify(text, false, true);
			}
			return true;
		}
		return false;
	}

	@Override
	public void cleanup() {

	}
}
