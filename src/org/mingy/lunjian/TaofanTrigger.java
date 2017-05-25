package org.mingy.lunjian;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaofanTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^【系统】官府：\\[1\\-5区\\](.*)慌不择路，逃往了(.*)\\-(.*)$");

	@SuppressWarnings("unchecked")
	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String map = m.group(2);
		String place = m.group(3);
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
		if (npc.equals(taofan)) {
			Map<String, Object> msgs = (Map<String, Object>) cmdline.js(
					cmdline.load("get_msgs.js"), "msg_room", false);
			if (msgs == null || !"雪亭驿".equals(msgs.get("short"))) {
				process(cmdline, npc, map, place);
			}
		}
		return true;
	}

	protected void process(CommandLine cmdline, String npc, String map,
			String place) {
		cmdline.notify("[逃犯] " + npc + " at " + map + " - " + place, false,
				false);
	}
}
