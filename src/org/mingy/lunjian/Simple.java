package org.mingy.lunjian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class Simple extends CommandLine {

	private List<WebDriver> webdrivers;

	public static void main(String[] args) throws Exception {
		Simple cmdline = new Simple();
		cmdline.run(args);
	}

	@Override
	protected void start(String[] args) throws Exception {
		super.start(args);
		webdrivers = new ArrayList<WebDriver>();
		for (int i = 1;; i++) {
			String dummy = properties.getProperty("dummy" + i);
			if (dummy != null && dummy.trim().length() > 0) {
				WebDriver webdriver = openUrl(dummy);
				timer.schedule(new HotkeyTask(webdriver), 1000, 3000);
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
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
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
		} else if (line.equals("#pk")) {
			PvpCombatTask task = new PvpCombatTask(this);
			if (task.init()) {
				System.out.println("starting auto pvp ...");
				executeTask(task, 100);
			}
		} else if (line.equals("#combat") || line.startsWith("#combat ")
				|| line.startsWith("#findway ")) {
		} else {
			super.execute(line);
		}
	}

	private class HotkeyTask extends TimerTask {

		private WebDriver webdriver;

		public HotkeyTask(WebDriver webdriver) {
			super();
			this.webdriver = webdriver;
		}

		@Override
		public void run() {
			try {
				((JavascriptExecutor) webdriver)
						.executeScript(load("hotkeys.js"));
			} catch (Exception e) {
				e.printStackTrace();
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
}
