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
		works.add(new Work("public_op3", 301000));
	}

	@Override
	protected void registerTriggers() {
		super.registerTriggers();
		TriggerManager.register("youxia", PowerYouxiaTrigger.class);
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
		TriggerManager.register("baozang", PowerBaozangTrigger.class);
		TriggerManager.register("guanfu", GuanfuTrigger.class);
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
				executeCmd("prepare_kill");
				executeTask(new KillTask(name), 200);
			}
		} else if (line.equals("#lc")) {
			System.out.println("starting loot corpse...");
			executeTask(new LootTask(), 200);
		} else if (line.equals("#work")) {
			System.out.println("starting auto work...");
			executeTask(new WorkTask(works), 1000);
		} else {
			super.execute(line);
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
			long timestamp = System.currentTimeMillis();
			for (Work work : works) {
				if (timestamp - work.lasttime >= work.cooldown) {
					sendCmd(work.command);
					work.lasttime = timestamp;
				}
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
			if (processedCmd == null) {
				ProcessedCommand pc = processCmd(originCmd);
				if (pc.command != null) {
					processedCmd = pc;
				}
			}
			if (processedCmd != null) {
				sendCmd(processedCmd.command);
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
		}
	}

	private class LootTask extends TimerTask {

		private int state;

		@Override
		public void run() {
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
		}
	}
}
