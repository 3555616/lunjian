package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.CommandLine.ProcessedCommand;

public class PowerYouxiaTrigger extends YouxiaTrigger {

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
	protected void process(CommandLine cmdline, String npc, String place) {
		super.process(cmdline, npc, place);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			for (int i = 0; i < MAPS.size(); i++) {
				if (place.startsWith(MAPS.get(i))) {
					cmdline.closeTrigger("zhengxie");
					try {
						Thread.sleep(Math.round(Math.random() * 200) + 500);
					} catch (InterruptedException e) {
						// ignore
					}
					System.out.println("goto map " + (i + 1));
					cmdline.executeCmd("halt;heal;heal;heal;heal;heal;prepare_kill;fly "
							+ (i + 1));
					if (Boolean
							.parseBoolean(cmdline.getProperty("youxia.auto"))) {
						System.out.println("start auto youxia...");
						YouxiaTask task = new YouxiaTask(cmdline, i + 1, npc,
								0, 100);
						cmdline.executeTask(task, 100);
					} else {
						System.out.println("start manual youxia...");
						YouxiaTask task = new YouxiaTask(cmdline, i + 1, npc,
								9, 500);
						cmdline.executeTask(task, 100);
					}
					return;
				}
			}
			System.out.println("map not found: " + place);
		}
	}

	private static class YouxiaTask extends TimerTaskDelegate {

		private CommandLine cmdline;
		private int mapId;
		private String name;
		private int state;
		private String id;
		private String corpse;
		private List<String> fears;
		private List<String> rooms;
		private int step = 0;

		public YouxiaTask(CommandLine cmdline, int mapId, String name,
				int state, int tick) {
			super(tick);
			this.cmdline = cmdline;
			this.mapId = mapId;
			this.name = name;
			this.state = state;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onTimer() throws Exception {
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
					setTick(500);
					state = 200;
				} catch (Exception e) {
					System.out.println("map not found: " + mapId);
					cmdline.stopTask(this);
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
							String fear = cmdline.getProperty("youxia.fear");
							String exclude = cmdline
									.getProperty("youxia.firstkill.exclude");
							if ((fear != null && fear.length() > 0)
									|| (exclude != null && exclude.length() > 0)) {
								cmdline.sendCmd(cmd.get("action"));
								fears = Arrays.asList(fear.split(","));
								state = 2;
							} else if (Boolean.parseBoolean(cmdline
									.getProperty("youxia.firstkill"))) {
								// cmdline.executeCmd("prepare_kill");
								cmdline.sendCmd("kill " + id);
								state = 7;
							} else {
								cmdline.sendCmd(cmd.get("action"));
								state = 5;
							}
							break;
						}
					}
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
						if (fears.contains(m.group(2))) {
							System.out.println("fear :(");
							state = 100;
						} else if (Boolean.parseBoolean(cmdline
								.getProperty("youxia.firstkill"))) {
							String exclude = cmdline
									.getProperty("youxia.firstkill.exclude");
							if (exclude != null) {
								List<String> list = Arrays.asList(exclude
										.split(","));
								if (list.contains(m.group(2))) {
									System.out.println("fear to first kill");
									state = 5;
								} else {
									state = 6;
								}
							} else {
								state = 6;
							}
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
							// cmdline.executeCmd("prepare_kill");
							cmdline.sendCmd("kill " + id);
							state = 7;
							break;
						}
					}
				}
				if (state == 6) {
					cmdline.sendCmd("look_npc " + id);
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
					state = 10;
				}
			} else if (state == 10) {
				cmdline.sendCmd("get " + corpse);
			} else if (state == 100) {
				System.out.println("ok!");
				cmdline.stopTask(this);
			} else if (state == 200) {
				Map<String, Object> map = (Map<String, Object>) cmdline.js(
						cmdline.load("get_msgs.js"), "msg_room", true);
				if (map != null) {
					for (String key : map.keySet()) {
						if (key.startsWith("npc")) {
							String[] values = map.get(key).toString()
									.split(",");
							if (name.equals(CommandLine.removeSGR(values[1]))) {
								id = values[0];
								System.out.println("find " + name + " at "
										+ map.get("short"));
								cmdline.sendCmd("look_npc " + id);
								if (Boolean.parseBoolean(cmdline
										.getProperty("youxia.manual"))) {
									// cmdline.executeCmd("prepare_kill");
									state = 9;
								} else {
									state = 1;
								}
								setTick(500);
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
						if (step < 10) {
							setTick(500);
						} else if (step < 20) {
							setTick(300);
						} else if (step < 30) {
							setTick(200);
						} else {
							setTick(100);
						}
					} else {
						System.out.println("target not found :(");
						cmdline.stopTask(this);
					}
				}
			}
		}
	}
}
