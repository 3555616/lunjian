package org.mingy.lunjian;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnoopRumorTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【谣言】某人：听说(.+)被(.+)杀死了。$");
	private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");

	private List<String> keywords;
	private List<String> ignores;

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"rumor".equals(type)) {
			return false;
		}
		if (keywords == null || ignores == null) {
			String str = cmdline.getProperty("snoop.rumor.keywords");
			str = str != null ? str.trim() : "";
			keywords = Arrays.asList(str.split(","));
			str = cmdline.getProperty("snoop.rumor.ignores");
			str = str != null ? str.trim() : "";
			ignores = Arrays.asList(str.split(","));
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		if (keywords.contains(m.group(2)) && !ignores.contains(m.group(1))) {
			System.out.println("[谣言] " + m.group(1) + "被" + m.group(2)
					+ "杀死了 (" + FORMAT_TIME.format(new Date()) + ")");
		}
		return true;
	}

	@Override
	public void cleanup() {

	}
}
