package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	protected void process(final CommandLine cmdline, String npc, String map,
			String place) {
		super.process(cmdline, npc, map, place);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			int id = MAPS.indexOf(map) + 1;
			if (id == 0) {
				System.out.println("map not found: " + place);
				return;
			}
			System.out.println("goto map " + id);
			try {
				Thread.sleep(Math.round(Math.random() * 500) + 1000);
			} catch (InterruptedException e) {
				// ignore
			}
			if (!Boolean.parseBoolean(cmdline.getProperty("taofan.target.bad"))) {
				if ("段老大".equals(npc)) {
					npc = "无一";
				} else if ("二娘".equals(npc)) {
					npc = "铁二";
				} else if ("岳老三".equals(npc)) {
					npc = "追二";
				} else if ("云老四".equals(npc)) {
					npc = "冷四";
				}
			}
			final String cmd = "look [1-5区]" + npc;
			cmdline.walk(new String[] { "fly " + id }, place, null,
					new Runnable() {
						@Override
						public void run() {
							cmdline.executeCmd(cmd);
							NewPvpCombatTask task = new NewPvpCombatTask(cmdline);
							if (task.init()) {
								System.out.println("starting auto pvp ...");
								cmdline.executeTask(task, 100);
							}
						}
					}, 100);
		}
	}
}
