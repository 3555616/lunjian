package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.mingy.lunjian.CommandLine.ProcessedCommand;

public class YouxiaLocationTrigger extends YouxiaTrigger {

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
		MAPS.add("断剑山庄");
		MAPS.add("冰火岛");
		MAPS.add("侠客岛");
	}

	@Override
	protected void process(CommandLine cmdline, String npc, String place) {
		super.process(cmdline, npc, place);
		if (!cmdline.isFighting()) {
			for (int i = 0; i < MAPS.size(); i++) {
				if (place.startsWith(MAPS.get(i))) {
					System.out.println("goto map " + (i + 1));
					cmdline.executeCmd("halt;fly " + (i + 1));
					System.out.println("start auto youxia...");
					YouxiaTask task = new YouxiaTask(cmdline, i + 1, npc);
					cmdline.executeTask(task, 100);
					return;
				}
			}
			System.out.println("map not found: " + place);
		}
	}

	private static class YouxiaTask extends TimerTask {

		private CommandLine cmdline;
		private int mapId;
		private String name;
		private int state;
		private List<String> rooms;
		private int step = 0;

		public YouxiaTask(CommandLine cmdline, int mapId, String name) {
			super();
			this.cmdline = cmdline;
			this.mapId = mapId;
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (state == 0) {
				try {
					String data = cmdline.load("maps/" + mapId + ".map");
					BufferedReader reader = new BufferedReader(
							new StringReader(data));
					rooms = new ArrayList<String>();
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();
						if (line.length() > 0) {
							String[] cmds = line.split(";");
							for (String cmd : cmds) {
								cmd = cmd.trim();
								if (cmd.length() > 0) {
									rooms.add(cmd);
								}
							}
						}
					}
					reader.close();
					state = 1;
				} catch (Exception e) {
					System.out.println("map not found: " + mapId);
					cmdline.stopTask(this);
					cmdline.sendCmd("home");
				}
			} else if (state == 1) {
				Map<String, Object> map = (Map<String, Object>) cmdline.js(
						cmdline.load("get_msgs.js"), "msg_room", true);
				if (map != null) {
					for (String key : map.keySet()) {
						if (key.startsWith("npc")) {
							String[] values = map.get(key).toString()
									.split(",");
							if (name.equals(CommandLine.removeSGR(values[1]))) {
								String message = "find " + name + " at "
										+ map.get("short");
								System.out.println(message);
								cmdline.tell(message);
								cmdline.stopTask(this);
								cmdline.sendCmd("home");
								return;
							}
						}
					}
					if (step < rooms.size()) {
						String cmd = rooms.get(step);
						Object random = map.get("go_random");
						if (random != null) {
							ProcessedCommand pc = cmdline.processCmd(cmd);
							if (pc != null) {
								cmd = pc.command;
								if (cmd.startsWith("go ")) {
									cmdline.sendCmd(cmd + "." + random);
								} else if (cmd.startsWith("wield ")
										|| cmd.startsWith("unwield ")
										|| cmd.startsWith("ask ")) {
									cmdline.sendCmd(cmd);
									boolean ok = false;
									while (++step < rooms.size()) {
										cmd = rooms.get(step);
										pc = cmdline.processCmd(cmd);
										if (pc != null) {
											cmd = pc.command;
											cmdline.sendCmd(cmd);
											if (!cmd.startsWith("wield ")
													&& !cmd.startsWith("unwield ")
													&& !cmd.startsWith("ask ")) {
												ok = true;
												break;
											}
										}
									}
									if (!ok) {
										System.out
												.println("target not found :(");
										cmdline.stopTask(this);
										cmdline.sendCmd("home");
										return;
									}
								} else {
									cmdline.sendCmd(cmd);
								}
							}
						} else {
							cmdline.sendCmd(cmd);
						}
						step++;
					} else {
						System.out.println("target not found :(");
						cmdline.stopTask(this);
						cmdline.sendCmd("home");
					}
				}
			}
		}
	}
}
