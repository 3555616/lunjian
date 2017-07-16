package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhengxieTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】(段老大|二娘|岳老三|云老四|流寇|恶棍|剧盗)(不怀好意地对着|对着)(.*)(笑道|叫道)");
	private static final Pattern TIMES_PATTERN = Pattern
			.compile("^这是你今天完成的第(.*)/10场正邪之战！$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type) {
		Matcher m = PATTERN.matcher(message);
		if (!"system".equals(type) || !m.find()) {
			m = TIMES_PATTERN.matcher(message);
			if (!"local".equals(type) || !m.find()) {
				return false;
			}
			timesChanged(cmdline, Integer.parseInt(m.group(1)));
		}
		String bad_npc = m.group(1);
		String good_npc = m.group(3);
		process(cmdline, good_npc, bad_npc);
		return true;
	}

	protected void process(CommandLine cmdline, String good_npc, String bad_npc) {
		cmdline.notify("[正邪] " + good_npc + " vs " + bad_npc, false, false);
	}

	protected void timesChanged(CommandLine cmdline, int times) {
		System.out.println(times + "/10 completed");
	}

	@Override
	public void cleanup() {

	}
}
