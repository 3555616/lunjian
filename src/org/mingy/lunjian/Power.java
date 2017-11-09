package org.mingy.lunjian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.mingy.lunjian.AutoQuest.Area;
import org.mingy.lunjian.AutoQuest.Room;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class Power extends CommandLine {

	private List<Work> works;
	private List<WebDriver> webdrivers;

	public static void main(String[] args) throws Exception {
		if (args.length > 1 && "--no_power".equalsIgnoreCase(args[1])) {
			CommandLine cmdline = new CommandLine();
			cmdline.run(args);
		} else {
			Power cmdline = new Power();
			cmdline.run(args);
		}
	}

	@Override
	protected void start(String[] args) throws Exception {
		super.start(args);
		String hotkeys = properties.getProperty("hotkey.performs");
		String friends = properties.getProperty("friends.list");
		HotkeyTask hotkeyTask = new HotkeyTask(webdriver,
				hotkeys != null ? hotkeys.trim() : null,
				friends != null ? friends.trim() : null);
		timer.schedule(hotkeyTask, 1000, 3000);
		works = new ArrayList<Work>();
		works.add(new Work("work click maikuli", 5500));
		works.add(new Work("work click duancha", 10500));
		works.add(new Work("work click dalie", 301000));
		works.add(new Work("work click baobiao", 301000));
		works.add(new Work("work click maiyi", 301000));
		works.add(new Work("work click xuncheng", 301000));
		works.add(new Work("work click datufei", 301000));
		works.add(new Work("work click dalei", 301000));
		works.add(new Work("work click kangjijinbin", 301000));
		works.add(new Work("work click zhidaodiying", 301000));
		works.add(new Work("work click dantiaoqunmen", 301000));
		works.add(new Work("work click shenshanxiulian", 301000));
		works.add(new Work("work click jianmenlipai", 301000));
		works.add(new Work("public_op3", 301000));
		webdrivers = new ArrayList<WebDriver>();
		for (int i = 1;; i++) {
			String dummy = properties.getProperty("dummy" + i);
			if (dummy != null && dummy.trim().length() > 0) {
				WebDriver webdriver = openUrl(dummy);
				timer.schedule(new HotkeyTask(webdriver, null, null), 1000,
						3000);
				webdrivers.add(webdriver);
			} else {
				break;
			}
		}
	}

	@Override
	protected void finish() throws Exception {
		super.finish();
		for (WebDriver webdriver : webdrivers) {
			webdriver.quit();
		}
	}

	@Override
	protected void registerTriggers() {
		super.registerTriggers();
		TriggerManager.register("youxia", PowerYouxiaTrigger.class);
		TriggerManager.register("location", YouxiaLocationTrigger.class);
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
		TriggerManager.register("baozang", PowerBaozangTrigger.class);
		TriggerManager.register("taofan", PowerTaofanTrigger.class);
		TriggerManager.register("hongbao", PowerHongbaoTrigger.class);
		TriggerManager.register("clean", CleanZhengxieTrigger.class);
		TriggerManager.register("party", AutoPartyTrigger.class);
		TriggerManager.register("guild", AutoGuildTrigger.class);
		TriggerManager.register("task", AutoTaskTrigger.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void execute(String line) throws IOException {
		if (line.startsWith("#loop ")) {
			line = line.substring(6).trim();
			if (line.length() > 0) {
				int interval = 500;
				int i = line.indexOf(' ');
				if (i >= 0) {
					try {
						interval = Integer.parseInt(line.substring(0, i));
						line = line.substring(i + 1).trim();
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				if (line.length() > 0) {
					System.out.println("starting loop...");
					executeTask(new LoopTask(line), interval);
				}
			}
		} else if (line.startsWith("#kill ")) {
			String name = line.substring(6).trim();
			if (name.length() > 0) {
				System.out.println("starting auto kill...");
				// executeCmd("prepare_kill");
				executeTask(new KillTask(name), 200);
			}
		} else if (line.equals("#lc")) {
			System.out.println("starting loot corpse...");
			executeTask(new LootTask(), 200);
		} else if (line.equals("#work")) {
			System.out.println("starting auto work...");
			executeTask(new WorkTask(works), 1000);
		} else if (line.equals("#tianjiangu")) {
			String[] settings = properties.getProperty("continue.fight", "")
					.split(",");
			if (settings.length < 1) {
				System.out.println("property continue.fight not set");
			} else {
				String[] pfms = settings[0].split("\\|");
				int wait = settings.length > 1 && settings[1].length() > 0 ? Integer
						.parseInt(settings[1]) : 0;
				String heal = settings.length > 2 && settings[2].length() > 0 ? settings[2]
						: null;
				int safe = settings.length > 3 && settings[3].length() > 0 ? Integer
						.parseInt(settings[3]) : 0;
				if (wait < pfms.length * 20) {
					wait = pfms.length * 20;
				}
				System.out.println("starting tianjiangu combat...");
				executeTask(new TianjianguCombatTask(pfms, wait, heal, safe),
						500);
			}
		} else if (line.equals("#find tianjian")) {
			System.out.println("starting find tianjian...");
			executeTask(new FindTianjianTask(), 100);
		} else if (line.equals("#pk")) {
			NewPvpCombatTask task = new NewPvpCombatTask(this);
			if (task.init()) {
				System.out.println("starting auto pvp ...");
				executeTask(task, 100);
			}
		} else if (line.equals("#pk1")) {
			NewPvpCombatTask1 task = new NewPvpCombatTask1(this);
			if (task.init()) {
				System.out.println("starting auto pvp ...");
				executeTask(task, 100);
			}
		} else if (line.equals("#pve")) {
			PveCombatTask task = new PveCombatTask(this);
			if (task.init()) {
				System.out.println("starting auto pve ...");
				executeTask(task, 100);
			}
		} else if (line.equals("#heal")) {
			Map<String, Object> map = (Map<String, Object>) js(
					load("get_msgs.js"), "msg_room", false);
			MapId mapId = null;
			String place = null;
			String id = (String) map.get("map_id");
			if (id != null) {
				try {
					mapId = MapId.valueOf(id);
					place = removeSGR((String) map.get("short"));
				} catch (Exception e) {
					// ignore
				}
			}
			Room room = null;
			AutoQuest quest = new AutoQuest(this);
			if (quest.init()) {
				Area area = quest.getArea(mapId.ordinal() - 1);
				if (area != null) {
					final List<Room> rooms = area.findRoom(place);
					if (!rooms.isEmpty()) {
						room = rooms.get(0);
					}
				}
			}
			recovery(room, null);
		} else {
			super.execute(line);
		}
	}

	protected void recovery(final Room room, final Runnable finish_callback) {
		System.out.println("starting recovery ...");
		Runnable callback = new Runnable() {
			@Override
			public void run() {
				RecoveryTask task = new RecoveryTask(room, finish_callback);
				executeTask(task, 1000, 1000);
			}
		};
		walk(new String[] { "jh 1;e" }, "广场", null, callback, 200);
	}

	private class HotkeyTask extends TimerTask {

		private WebDriver webdriver;
		private Object[] args = new Object[2];

		public HotkeyTask(WebDriver webdriver, String hotkeys, String friends) {
			super();
			this.webdriver = webdriver;
			if (hotkeys != null && hotkeys.length() > 0) {
				String[] pfms = hotkeys.split(",");
				for (int i = 0; i < pfms.length; i++) {
					pfms[i] = pfms[i].trim();
				}
				args[0] = pfms;
			}
			if (friends != null && friends.length() > 0) {
				String[] arr = friends.split(",");
				for (int i = 0; i < arr.length; i++) {
					arr[i] = arr[i].trim();
				}
				args[1] = arr;
			}
		}

		@Override
		public void run() {
			try {
				((JavascriptExecutor) webdriver).executeScript(
						load("hotkeys.js"), args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class Work {

		String command;
		long cooldown;
		long lasttime;

		public Work(String command, long cooldown) {
			this.command = command;
			this.cooldown = cooldown;
		}
	}

	private class WorkTask extends TimerTask {

		private List<Work> works;

		private WorkTask(List<Work> works) {
			this.works = works;
		}

		@Override
		public void run() {
			try {
				long timestamp = System.currentTimeMillis();
				for (Work work : works) {
					if (timestamp - work.lasttime >= work.cooldown) {
						sendCmd(work.command);
						work.lasttime = timestamp;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class LoopTask extends TimerTask {

		private String originCmd;
		private ProcessedCommand processedCmd;

		public LoopTask(String cmd) {
			originCmd = cmd;
		}

		@Override
		public void run() {
			try {
				if (processedCmd == null) {
					ProcessedCommand pc = processCmd(originCmd);
					if (pc.command != null) {
						processedCmd = pc;
					}
				}
				if (processedCmd != null) {
					sendCmd(processedCmd.command);
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class KillTask extends TimerTask {

		private String name;
		private int state;

		public KillTask(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			try {
				if (state == 0) {
					ProcessedCommand pc = processCmd("kill " + name);
					if (pc.command != null) {
						sendCmd(pc.command);
						state = 1;
					}
				} else if (state == 1) {
					state = getCombatPosition() != null ? 2 : 0;
				} else if (state == 2) {
					if (isCombatOver()) {
						state = 3;
					}
				} else if (state == 3) {
					String[] corpses = findTargets("item", "corpse");
					if (corpses.length > 0) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// ignore
						}
						sendCmd("get " + corpses[corpses.length - 1]);
						state = 4;
						System.out.println("ok!");
						stopTask(this);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class LootTask extends TimerTask {

		private int state;

		@Override
		public void run() {
			try {
				if (state == 0) {
					if (isCombatOver()) {
						state = 1;
					}
				} else if (state == 1) {
					String[] corpses = findTargets("item", "corpse");
					if (corpses.length > 0) {
						StringBuilder sb = new StringBuilder();
						for (String corpse : corpses) {
							if (sb.length() > 0) {
								sb.append(';');
							}
							sb.append("get " + corpse);
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// ignore
						}
						sendCmd(sb.toString());
						state = 2;
						System.out.println("ok!");
						stopTask(this);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class TianjianguCombatTask extends TimerTask {

		private int waitPoint;
		private String[] performs;
		private String heal;
		private int safeHp;
		private List<Object> context = new ArrayList<Object>(5);

		public TianjianguCombatTask(String[] performs, int waitPoint,
				String heal, int safeHp) {
			super();
			this.performs = performs;
			this.waitPoint = waitPoint;
			this.heal = heal;
			this.safeHp = safeHp;
			this.context.add(0);
			this.context.add(false);
			this.context.add(false);
			this.context.add(null);
			this.context.add(false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				List<Object> ctx = (List<Object>) js(load("continue_fight.js"),
						performs, waitPoint, heal, safeHp, 0, null, false,
						context);
				if (ctx != null) {
					context = ctx;
					if (context.get(3) != null) {
						context.set(3, null);
					}
				} else {
					if ((Boolean) context.get(4)) {
						context.set(0, 0);
						context.set(1, false);
						context.set(2, false);
						context.set(3, null);
						context.set(4, false);
						sendCmd("prev_combat");
					}
					List<String[]> targets = getTargets("npc");
					if (!targets.isEmpty()) {
						String npc = null;
						for (String[] target : targets) {
							if ("天剑谷卫士".equals(target[1])) {
								if (npc == null) {
									npc = target[0];
								}
							} else if ("天剑真身".equals(target[1])
									|| "天剑".equals(target[1])
									|| "虹风".equals(target[1])
									|| "虹雨".equals(target[1])
									|| "虹雷".equals(target[1])
									|| "虹电".equals(target[1])) {
								npc = target[0];
							}
						}
						if (npc != null) {
							sendCmd("kill " + npc);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class FindTianjianTask extends TimerTask {

		private Map<String, Long> memo = new HashMap<String, Long>(2048);
		private long step;

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				List<List<String>> rooms = (List<List<String>>) js(
						load("get_rooms.js"), null, true);
				if (rooms != null && !rooms.isEmpty()) {
					String room = removeSGR(rooms.get(0).get(1));
					memo.put(room, ++step);
					String random = rooms.get(1).get(1);
					long min = Long.MAX_VALUE;
					String cmd = null;
					for (int i = 2; i < rooms.size(); i++) {
						room = removeSGR(rooms.get(i).get(1));
						Long n = memo.get(room);
						if (n == null) {
							cmd = "go " + rooms.get(i).get(0);
							break;
						} else if (n < min) {
							cmd = "go " + rooms.get(i).get(0);
							min = n;
						}
					}
					if (cmd != null) {
						if (random != null && random.length() > 0) {
							cmd += "." + random;
						}
						sendCmd(cmd);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private static Pattern HEAL_PATTERN = Pattern
			.compile("^你深深吸了几口气，脸色看起来好多了。$");
	private static String[] FORCE_SKILLS = new String[] { "道种心魔经", "生生造化功",
			"不动明王诀", "八荒功", "易筋经神功", "天邪神功", "紫霞神功", "葵花宝典", "九阴真经", "茅山道术",
			"蛤蟆神功" };

	private class RecoveryTask extends TimerTask {

		private Room room;
		private Runnable finish_callback;
		private boolean in_fighting;
		private int heal_count;

		public RecoveryTask(Room room, Runnable callback) {
			this.room = room;
			this.finish_callback = callback;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				Map<String, Object> combat = (Map<String, Object>) js(load("get_combat_info.js"));
				if (combat != null) {
					if (!in_fighting) {
						in_fighting = true;
						heal_count = 0;
					}
					Map<String, Object> map = (Map<String, Object>) combat
							.get("me");
					long kee = (Long) map.get("qi");
					long max_kee = (Long) map.get("max_qi");
					long force = (Long) map.get("neili");
					if (kee * 1.0 / max_kee >= 0.8 || force < 1000) {
						sendCmd("escape");
						return;
					}
					List<String> msgs = (List<String>) combat.get("msgs");
					for (String msg : msgs) {
						if (HEAL_PATTERN.matcher(msg).find()) {
							heal_count++;
						}
					}
					if (heal_count >= 3) {
						sendCmd("escape");
						return;
					}
					List<String> pfms = (List<String>) combat.get("pfms");
					for (String skill : FORCE_SKILLS) {
						int i = pfms.indexOf(skill);
						if (i >= 0) {
							sendCmd("playskill " + (i + 1));
							break;
						}
					}
				} else {
					in_fighting = false;
					Map<String, Object> attrs = (Map<String, Object>) js(
							load("get_msgs.js"), "msg_attrs", false);
					if (attrs != null) {
						long force = Long
								.parseLong((String) attrs.get("force"));
						long max_force = Long.parseLong((String) attrs
								.get("max_force"));
						long kee = Long.parseLong((String) attrs.get("kee"));
						long max_kee = Long.parseLong((String) attrs
								.get("max_kee"));
						if (max_force - force >= 10000) {
							final StringBuilder sb = new StringBuilder();
							int n = (int) (max_force - force) / 5000 + 1;
							for (int i = 0; i < Math.min(n, 2); i++) {
								sb.append("buy /map/snow/obj/qiannianlingzhi from snow_herbalist\nitems use snow_qiannianlingzhi\n");
							}
							sb.append("attrs");
							if ("桑邻药铺".equals(getRoom())) {
								js("clickButton(arguments[0]);", sb.toString());
							} else {
								Runnable callback = new Runnable() {
									@Override
									public void run() {
										try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											// ignore
										}
										js("clickButton(arguments[0]);",
												sb.toString());
										RecoveryTask task = new RecoveryTask(
												room, finish_callback);
										executeTask(task, 1000, 1000);
									}
								};
								walk(new String[] { "n;n;n;w" }, "桑邻药铺", null,
										callback, 200);
							}
						} else if (kee * 1.0 / max_kee < 0.8) {
							if ("广场".equals(getRoom())) {
								sendCmd("fight snow_worker");
							} else {
								Runnable callback = new Runnable() {
									@Override
									public void run() {
										sendCmd("fight snow_worker");
										RecoveryTask task = new RecoveryTask(
												room, finish_callback);
										executeTask(task, 500, 1000);
									}
								};
								walk(new String[] { "jh 1;e" }, "广场", null,
										callback, 200);
							}
						} else if (kee < max_kee) {
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < 3; i++) {
								sb.append("recovery\n");
							}
							sb.append("attrs");
							js("clickButton(arguments[0]);", sb.toString());
						} else if (room != null) {
							System.out.println("finished!");
							walk(new String[] { room.getPath() },
									room.getName(), null, finish_callback, 200);
						} else {
							System.out.println("finished!");
							stopTask(this);
							if (finish_callback != null) {
								finish_callback.run();
							}
						}
					} else {
						System.out.println("failed to get attrs");
						stopTask(this);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}
}
