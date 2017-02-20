package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhengxieTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("【系统】(段老大|二娘|岳老三|云老四|流寇|恶棍|剧盗)对着(.*)(淫笑|叫道)");

	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String bad_npc = m.group(1);
		String good_npc = m.group(2);
		process(cmdline, good_npc, bad_npc);
		return true;
	}

	protected void process(CommandLine cmdline, String good_npc, String bad_npc) {
		cmdline.notify("[正邪] " + good_npc + " vs " + bad_npc, false, false);
	}
}
