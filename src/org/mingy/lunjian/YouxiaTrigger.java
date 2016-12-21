package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.CommandLine.ProcessedCommand;

public class YouxiaTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("游侠会：听说(.*)出来闯荡江湖了，目前正在前往(.*)的路上。");
	private static final Pattern DESC_PATTERN = Pattern
			.compile("武功看上去(.*)，出手似乎(.*)。");
	private static final List<String> MAPS = new ArrayList<String>();

	static {
		MAPS.add("雪亭镇");
		MAPS.add("洛阳");
		MAPS.add("华山村");
		MAPS.add("华山");
		MAPS.add("扬州");
		MAPS.add("丐帮");
		MAPS.add("乔银县");
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
		cmdline.notify("[游侠] " + npc + " at " + place);
		if (!cmdline.isFighting()) {
			for (int i = 0; i < MAPS.size(); i++) {
				if (place.startsWith(MAPS.get(i))) {
					System.out.println("goto map " + (i + 1));
					cmdline.executeCmd("halt;fly " + (i + 1));
					System.out.println("start auto youxia...");
					YouxiaTask task = new YouxiaTask(cmdline, npc);
					cmdline.executeTask(task, 500);
					return true;
				}
			}
			System.out.println("map not found: " + place);
		}
		return true;
	}

	private static class YouxiaTask extends TimerTask {

		private CommandLine cmdline;
		private String name;
		private int state;
		private String id;
		private String corpse;

		public YouxiaTask(CommandLine cmdline, String name) {
			this.cmdline = cmdline;
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (state == 0) {
				ProcessedCommand pc = cmdline.processCmd("look " + name);
				if (pc.command != null) {
					cmdline.sendCmd(pc.command);
					state = 1;
				}
			} else if (state == 1) {
				Map<String, Object> map = (Map<String, Object>) cmdline
						.js(cmdline.load("get_npc_desc.js"));
				if (map != null
						&& name.equals(CommandLine.removeSGR((String) map
								.get("name")))) {
					id = (String) map.get("id");
					List<Map<String, String>> cmds = (List<Map<String, String>>) map
							.get("cmds");
					for (Map<String, String> cmd : cmds) {
						if ("跟班".equals(cmd.get("name"))) {
							cmdline.sendCmd(cmd.get("action"));
							state = 2;
							break;
						}
					}
				}
				if (state == 1) {
					state = 0;
				}
			} else if (state == 2) {
				cmdline.sendCmd("look_npc " + id);
				state = 3;
			} else if (state == 3) {
				Map<String, Object> map = (Map<String, Object>) cmdline
						.js(cmdline.load("get_npc_desc.js"));
				if (map != null
						&& name.equals(CommandLine.removeSGR((String) map
								.get("name")))) {
					Matcher m = DESC_PATTERN.matcher(CommandLine
							.removeSGR((String) map.get("desc")));
					if (m.find()) {
						if ("可毁天灭地".equals(m.group(2))
								|| "可开山裂石".equals(m.group(2))
								|| "惊天动地".equals(m.group(2))) {
							System.out.println("fear :(");
							state = 100;
						} else {
							state = 5;
						}
					}
				}
				if (state == 3) {
					state = 2;
				}
			} else if (state == 4) {
				cmdline.sendCmd("look_npc " + id);
				state = 5;
			} else if (state == 5) {
				Map<String, Object> map = (Map<String, Object>) cmdline
						.js(cmdline.load("get_npc_desc.js"));
				if (map != null
						&& name.equals(CommandLine.removeSGR((String) map
								.get("name")))) {
					List<Map<String, String>> cmds = (List<Map<String, String>>) map
							.get("cmds");
					for (Map<String, String> cmd : cmds) {
						if ("观战".equals(cmd.get("name"))) {
							cmdline.sendCmd(cmd.get("action"));
							state = 6;
							break;
						}
					}
				}
				if (state == 5) {
					state = 4;
				}
			} else if (state == 6) {
				Map<String, Object> map = (Map<String, Object>) cmdline
						.js(cmdline.load("get_npc_desc.js"));
				if (map != null
						&& name.equals(CommandLine.removeSGR((String) map
								.get("name")))) {
					List<Map<String, String>> cmds = (List<Map<String, String>>) map
							.get("cmds");
					for (Map<String, String> cmd : cmds) {
						if ("取消跟班".equals(cmd.get("name"))) {
							cmdline.sendCmd(cmd.get("action"));
							cmdline.executeCmd("prepare_kill");
							cmdline.sendCmd("kill " + id);
							state = 7;
							break;
						}
					}
				}
				if (state == 6) {
					state = 4;
				}
			} else if (state == 7) {
				state = cmdline.getCombatPosition() != null ? 8 : 0;
			} else if (state == 8) {
				if (cmdline.isCombatOver()) {
					state = 9;
				}
			} else if (state == 9) {
				String[] corpses = cmdline.findTargets("item", name + "的尸体");
				if (corpses.length > 0) {
					corpse = corpses[corpses.length - 1];
					cmdline.sendCmd("get " + corpse);
					state = 10;
				} else {
					state = 100;
				}
			} else if (state == 10) {
				cmdline.sendCmd("get " + corpse);
			} else if (state == 100) {
				System.out.println("ok!");
				cmdline.stopTask(this);
			}
		}
	}
}
