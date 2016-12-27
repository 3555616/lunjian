package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuanfuTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("官府：(.*)慌不择路，逃往了(.*)-(.*)");
	private static final List<String> MAPS = new ArrayList<String>();

	static {
		MAPS.add("雪亭镇");
		MAPS.add("洛阳");
		MAPS.add("华山村");
		MAPS.add("华山");
		MAPS.add("扬州");
		MAPS.add("丐帮");
		MAPS.add("乔阴县");
		MAPS.add("峨眉山");
		MAPS.add("恒山");
		MAPS.add("武当山");
		MAPS.add("晚月庄");
		MAPS.add("水烟阁");
		MAPS.add("少林寺");
		MAPS.add("唐门");
		MAPS.add("青城山");
		MAPS.add("逍遥林");
		MAPS.add("开封");
		MAPS.add("明教");
		MAPS.add("全真教");
		MAPS.add("古墓");
		MAPS.add("白驮山");
		MAPS.add("嵩山");
		MAPS.add("寒梅庄");
		MAPS.add("泰山");
		MAPS.add("大旗门");
		MAPS.add("大昭寺");
		MAPS.add("魔教");
		MAPS.add("星宿海");
		MAPS.add("茅山");
		MAPS.add("桃花岛");
		MAPS.add("铁雪山庄");
		MAPS.add("慕容山庄");
		MAPS.add("大理");
	}

	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		String room = m.group(3);
		cmdline.notify("[官府] " + npc + " at " + place + " - " + room, true);
		if (!cmdline.isFighting()) {
			for (int i = 0; i < MAPS.size(); i++) {
				if (place.startsWith(MAPS.get(i))) {
					System.out.println("goto map " + (i + 1));
					cmdline.executeCmd("halt;fly " + (i + 1) + ";watch " + npc
							+ " -1");
					return true;
				}
			}
			System.out.println("map not found: " + place);
		}
		return true;
	}
}
