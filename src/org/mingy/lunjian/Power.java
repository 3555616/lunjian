package org.mingy.lunjian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class Power extends CommandLine {

	private List<Work> works;

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
		if (hotkeys != null && hotkeys.trim().length() > 0) {
			HotkeyTask hotkeyTask = new HotkeyTask(hotkeys.trim());
			timer.schedule(hotkeyTask, 1000, 3000);
		}
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
	}

	@Override
	protected void registerTriggers() {
		super.registerTriggers();
		TriggerManager.register("youxia", PowerYouxiaTrigger.class);
		TriggerManager.register("location", YouxiaLocationTrigger.class);
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
		TriggerManager.register("baozang", PowerBaozangTrigger.class);
		TriggerManager.register("guanfu", PowerGuanfuTrigger.class);
		TriggerManager.register("taofan", PowerTaofanTrigger.class);
		TriggerManager.register("hongbao", PowerHongbaoTrigger.class);
	}

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
		} else {
			super.execute(line);
		}
	}

	private class HotkeyTask extends TimerTask {

		private Object[] args;

		public HotkeyTask(String hotkeys) {
			super();
			String[] pfms = hotkeys.split(",");
			args = new Object[pfms.length];
			for (int i = 0; i < pfms.length; i++) {
				args[i] = pfms[i].trim();
			}
		}

		@Override
		public void run() {
			try {
				js(load("hotkeys.js"), args);
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
						performs, waitPoint, heal, safeHp, 0, null, context);
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
}
