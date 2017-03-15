package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PowerTaofanTrigger extends TaofanTrigger {

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
		MAPS.add("光明顶");
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

	@SuppressWarnings("unchecked")
	@Override
	protected void process(CommandLine cmdline, String npc, String map,
			String place) {
		super.process(cmdline, npc, map, place);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			Map<String, Object> msgs = (Map<String, Object>) cmdline.js(
					cmdline.load("get_msgs.js"), "msg_room", true);
			if (msgs != null && "武林广场10".equals(msgs.get("short"))) {
				for (int i = 0; i < MAPS.size(); i++) {
					if (place.startsWith(MAPS.get(i))) {
						System.out.println("goto map " + (i + 1));
						cmdline.executeCmd("halt;heal;heal;heal;heal;heal;prepare_kill;fly "
								+ (i + 1));
						return;
					}
				}
				System.out.println("map not found: " + place);
			}
		}
	}
}
