package org.mingy.lunjian;

import java.io.IOException;
import java.util.TimerTask;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class Power extends CommandLine {

	private WebDriver webdriver2;

	public static void main(String[] args) throws Exception {
		Power cmdline = new Power();
		cmdline.start(args);
	}

	@Override
	protected void start(String[] args) throws Exception {
		super.start(args);
		if (Boolean.parseBoolean(properties.getProperty("notify.webqq"))) {
			String browser = properties.getProperty("webdriver.browser");
			if (browser == null || "firefox".equalsIgnoreCase(browser)) {
				webdriver2 = new FirefoxDriver();
			} else if ("chrome".equalsIgnoreCase(browser)) {
				webdriver2 = new ChromeDriver();
			}
			webdriver2.navigate().to("http://web2.qq.com");
			webdriver2.switchTo().defaultContent();
		}
	}

	@Override
	protected void registerTriggers() {
		super.registerTriggers();
		TriggerManager.register("youxia", PowerYouxiaTrigger.class);
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
		TriggerManager.register("guanfu", GuanfuTrigger.class);
	}

	@Override
	protected void finish() throws Exception {
		if (webdriver2 != null) {
			webdriver2.quit();
		}
		super.finish();
	}

	@Override
	protected void execute(String line) throws IOException {
		if (line.startsWith("#loop ")) {
			line = line.substring(6).trim();
			if (line.length() > 0) {
				System.out.println("starting loop...");
				executeTask(new LoopTask(line), 500);
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
		} else {
			super.execute(line);
		}
	}

	@Override
	protected void notify(String message, boolean important) {
		super.notify(message, important);
		if (important && webdriver2 != null) {
			try {
				WebElement e = webdriver2.findElement(By.id("chat_textarea"));
				e.clear();
				e.sendKeys(message);
				webdriver2.findElement(By.id("send_chat_btn")).click();
			} catch (NoSuchElementException e) {
				// ignore
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
