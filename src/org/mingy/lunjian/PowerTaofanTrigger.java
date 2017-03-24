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
		String taofan = cmdline.getProperty("taofan.target");
		if (taofan == null) {
			taofan = "岳老三";
		} else if (taofan.equals("1")) {
			taofan = "段老大";
		} else if (taofan.equals("2")) {
			taofan = "二娘";
		} else if (taofan.equals("3")) {
			taofan = "岳老三";
		} else if (taofan.equals("4")) {
			taofan = "云老四";
		}
		if (npc.equals(taofan)
				&& !Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			int id = MAPS.indexOf(map) + 1;
			if (id == 0) {
				System.out.println("map not found: " + place);
				return;
			}
			System.out.println("goto map " + id);
			String cmds = "halt;heal;heal;heal;heal;heal;prepare_kill;fly "
					+ id;
			Map<String, Object> msgs = (Map<String, Object>) cmdline.js(
					cmdline.load("get_msgs.js"), "msg_room", false);
			if (msgs != null && "武林广场10".equals(msgs.get("short"))) {
				cmdline.executeCmd(cmds);
			} else {
				cmdline.walk(
						new String[] { "fly 1;e;n;n;n;n;w;event_1_36344468;e;e;e;e;e;e;e;e;e" },
						"武林广场10", null, cmds, 100);
			}
		}
	}
}
