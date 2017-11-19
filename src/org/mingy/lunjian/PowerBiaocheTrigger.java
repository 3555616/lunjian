package org.mingy.lunjian;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PowerBiaocheTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^荣威镖局:\\[(.*)\\](.*)押运镖车行至跨服\\-(.*)，忽入\\[(.*)\\](.*)埋伏之中，哪位好汉能伸出援手，我荣威镖局必有重谢！");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"local".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String kuafu = cmdline.getProperty("kuafu.area");
		if (kuafu == null || kuafu.length() == 0) {
			kuafu = "1-5区";
		}
		if (!kuafu.equals(m.group(1))) {
			return false;
		}
		String npc = Boolean.parseBoolean(cmdline
				.getProperty("biaoche.target.master")) ? m.group(2) : m
				.group(5);
		npc = "[" + kuafu + "]" + npc;
		cmdline.js(cmdline.load("click_link.js"), seq);
		try {
			cmdline.execute("#loop 100 kill " + npc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void cleanup() {

	}
}
