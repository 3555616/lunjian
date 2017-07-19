package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaofanTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】\\[(.*)\\](.*)慌不择路，逃往了(.*)\\-(.*)$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type) {
		if (!"system".equals(type)) {
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
		String npc = m.group(2);
		String map = m.group(3);
		String place = m.group(4);
		String taofan = cmdline.getProperty("taofan.target");
		if (taofan == null) {
			taofan = "段老大";
		} else if (taofan.equals("1")) {
			taofan = "段老大";
		} else if (taofan.equals("2")) {
			taofan = "二娘";
		} else if (taofan.equals("3")) {
			taofan = "岳老三";
		} else if (taofan.equals("4")) {
			taofan = "云老四";
		}
		if (npc.equals(taofan) && cmdline.isKuafu()) {
			process(cmdline, kuafu, npc, map, place);
		}
		return true;
	}

	protected void process(CommandLine cmdline, String kuafu, String npc,
			String map, String place) {
		cmdline.notify("[逃犯] " + npc + " at " + map + " - " + place, false,
				false);
	}

	@Override
	public void cleanup() {

	}
}
