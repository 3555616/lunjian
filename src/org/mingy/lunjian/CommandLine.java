package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.IOUtils;

public class CommandLine {

	private static final Map<String, String> MAP_IDS = new HashMap<String, String>();

	private WebDriver webdriver;
	private File aliasFile;
	private Properties aliases;
	private Timer timer;
	private TimerTask task;

	static {
		MAP_IDS.put("xueting", "1");
		MAP_IDS.put("xt", "1");
		MAP_IDS.put("luoyang", "2");
		MAP_IDS.put("ly", "2");
		MAP_IDS.put("huashancun", "3");
		MAP_IDS.put("hsc", "3");
		MAP_IDS.put("huashan", "4");
		MAP_IDS.put("hs", "4");
		MAP_IDS.put("yangzhou", "5");
		MAP_IDS.put("yz", "5");
		MAP_IDS.put("gaibang", "6");
		MAP_IDS.put("gb", "6");
		MAP_IDS.put("qiaoyin", "7");
		MAP_IDS.put("qy", "7");
		MAP_IDS.put("emei", "8");
		MAP_IDS.put("em", "8");
		MAP_IDS.put("hengshan", "9");
		MAP_IDS.put("hs2", "9");
		MAP_IDS.put("wudang", "10");
		MAP_IDS.put("wd", "10");
		MAP_IDS.put("wanyue", "11");
		MAP_IDS.put("wy", "11");
		MAP_IDS.put("shuiyan", "12");
		MAP_IDS.put("sy", "12");
		MAP_IDS.put("shaolin", "13");
		MAP_IDS.put("sl", "13");
		MAP_IDS.put("tangmen", "14");
		MAP_IDS.put("tm", "14");
		MAP_IDS.put("qingcheng", "15");
		MAP_IDS.put("qc", "15");
		MAP_IDS.put("xiaoyao", "16");
		MAP_IDS.put("xy", "16");
		MAP_IDS.put("kaifang", "17");
		MAP_IDS.put("kf", "17");
		MAP_IDS.put("guangmingding", "18");
		MAP_IDS.put("gmd", "18");
		MAP_IDS.put("mingjiao", "18");
		MAP_IDS.put("mj", "18");
		MAP_IDS.put("quanzhen", "19");
		MAP_IDS.put("qz", "19");
		MAP_IDS.put("gumu", "20");
		MAP_IDS.put("gm", "20");
		MAP_IDS.put("baituo", "21");
		MAP_IDS.put("bt", "21");
		MAP_IDS.put("songshan", "22");
		MAP_IDS.put("ss", "22");
		MAP_IDS.put("meizhuang", "23");
		MAP_IDS.put("mz", "23");
		MAP_IDS.put("taishan", "24");
		MAP_IDS.put("ts", "24");
		MAP_IDS.put("daqi", "25");
		MAP_IDS.put("dq", "25");
		MAP_IDS.put("dazhao", "26");
		MAP_IDS.put("dz", "26");
		MAP_IDS.put("heimuya", "27");
		MAP_IDS.put("hmy", "27");
		MAP_IDS.put("riyue", "27");
		MAP_IDS.put("ry", "27");
		MAP_IDS.put("xingxiu", "28");
		MAP_IDS.put("xx", "28");
		MAP_IDS.put("maoshan", "29");
		MAP_IDS.put("ms", "29");
		MAP_IDS.put("taohuadao", "30");
		MAP_IDS.put("thd", "30");
		MAP_IDS.put("tiexue", "31");
		MAP_IDS.put("tx", "31");
		MAP_IDS.put("murong", "32");
		MAP_IDS.put("mr", "32");
		MAP_IDS.put("dali", "33");
		MAP_IDS.put("dl", "33");
	}

	public static void main(String[] args) throws Exception {
		CommandLine cmdline = new CommandLine();
		cmdline.start();
	}

	private void start() throws Exception {
		Properties properties = new Properties();
		properties.load(CommandLine.class
				.getResourceAsStream("/robot.properties"));
		String browser = properties.getProperty("webdriver.browser");
		if (browser == null || "firefox".equalsIgnoreCase(browser)) {
			System.setProperty("webdriver.firefox.bin",
					properties.getProperty("webdriver.firefox.bin"));
			webdriver = new FirefoxDriver();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			System.setProperty("webdriver.chrome.driver",
					properties.getProperty("webdriver.chrome.driver"));
			webdriver = new ChromeDriver();
		}
		String size = properties.getProperty("browser.size");
		int i = size.indexOf('*');
		if (i > 0) {
			webdriver
					.manage()
					.window()
					.setSize(
							new Dimension(Integer.parseInt(size.substring(0, i)
									.trim()), Integer.parseInt(size.substring(
									i + 1).trim())));
		}
		webdriver.navigate().to(properties.getProperty("lunjian.url"));
		webdriver.switchTo().defaultContent();
		timer = new Timer(true);
		timer.schedule(new ChannelTask(), 1000, 1000);
		loadAlias(properties.getProperty("alias.properties"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in, "gbk"));
		for (;;) {
			String line = reader.readLine();
			if (line == null
					|| "#quit".equals((line = line.toLowerCase().trim()))) {
				break;
			}
			execute(line);
		}
		timer.cancel();
		System.out.println("over!");
	}

	private void execute(String line) throws IOException {
		if (line.startsWith("#loop ")) {
			line = line.substring(6).trim();
			if (line.length() > 0) {
				stopTask();
				System.out.println("starting loop...");
				task = new LoopTask(line);
				timer.schedule(task, 0, 500);
			}
		} else if (line.startsWith("#kill ")) {
			String name = line.substring(6).trim();
			if (name.length() > 0) {
				stopTask();
				System.out.println("starting auto kill...");
				task = new KillTask(name);
				timer.schedule(task, 0, 200);
			}
		} else if (line.equals("#lc")) {
			stopTask();
			System.out.println("starting loot corpse...");
			task = new LootTask();
			timer.schedule(task, 0, 200);
		} else if (line.equals("#combat")) {
			String pos = getCombatPosition();
			if (pos != null) {
				stopTask();
				System.out.println("starting auto combat...");
				task = new CombatTask(pos, new String[] { "乾坤一阳指", "金玉拳" },
						"枯荣禅功", 70, 5000);
				timer.schedule(task, 0, 500);
			}
		} else if (line.equals("#stop")) {
			stopTask();
		} else if (line.startsWith("#alias ")) {
			String alias = line.substring(7).trim();
			String key;
			String value;
			int i = alias.indexOf(' ');
			if (i >= 0) {
				key = alias.substring(0, i).trim();
				value = alias.substring(i + 1).trim();
			} else {
				key = alias;
				value = null;
			}
			if (value != null) {
				if (!value.equals(aliases.getProperty(key))) {
					aliases.setProperty(key, value);
					saveAlias();
					System.out.println("set alias ok.");
				}
			} else {
				if (aliases.containsKey(key)) {
					aliases.remove(key);
					saveAlias();
					System.out.println("alias removed.");
				}
			}
		} else if (line.length() > 0 && line.charAt(0) != '#') {
			ProcessedCommand pc = process(line);
			if (pc.isChat) {
				send("go_chat");
			} else {
				send("quit_chat");
			}
			if (pc.command != null) {
				send(pc.command);
			}
		}
	}

	private ProcessedCommand process(String line) {
		ProcessedCommand pc = new ProcessedCommand();
		pc.isChat = true;
		StringBuilder sb = new StringBuilder();
		for (String cmd : line.split(";")) {
			if (cmd.length() > 0) {
				ProcessedCommand c = translate(cmd);
				if (!c.isChat) {
					pc.isChat = false;
				}
				if (c.command != null) {
					if (sb.length() > 0) {
						sb.append(';');
					}
					sb.append(c.command);
				}
			}
		}
		pc.command = sb.length() > 0 ? sb.toString() : null;
		return pc;
	}

	private ProcessedCommand translate(String line) {
		int i = line.indexOf(' ');
		String[] cmd = new String[2];
		if (i > 0) {
			cmd[0] = line.substring(0, i).trim();
			cmd[1] = line.substring(i + 1).trim();
		} else {
			cmd[0] = line;
		}
		String alias = aliases.getProperty(cmd[0]);
		if (alias != null) {
			line = alias.toLowerCase().trim();
			if (cmd[1] != null) {
				line += " " + cmd[1];
			}
			return process(line);
		}
		ProcessedCommand pc = new ProcessedCommand();
		pc.isChat = translate(cmd);
		if (cmd[0] != null) {
			if (cmd[1] != null) {
				pc.command = cmd[0] + " " + cmd[1];
			} else {
				pc.command = cmd[0];
			}
		}
		return pc;
	}

	private boolean translate(String[] cmd) {
		boolean isChat = false;
		if ("look".equals(cmd[0])) {
			if (cmd[1] == null) {
				cmd[0] = "golook_room";
			} else {
				String[] target = findTarget(new String[] { "npc", "item",
						"user" }, cmd[1]);
				if (target != null) {
					if ("npc".equals(target[1])) {
						cmd[0] = "look_npc";
						cmd[1] = target[0];
					} else if ("item".equals(target[1])) {
						cmd[0] = "look_item";
						cmd[1] = target[0];
					} else {
						cmd[0] = "score";
						cmd[1] = target[0];
					}
				} else {
					cmd[0] = null;
				}
			}
		} else if ("fight".equals(cmd[0]) || "watch".equals(cmd[0])) {
			if ("watch".equals(cmd[0])) {
				cmd[0] = "watch_vs";
			}
			String[] target = findTarget(new String[] { "npc", "user" }, cmd[1]);
			if (target != null) {
				cmd[1] = target[0];
			} else {
				cmd[0] = null;
			}
		} else if ("kill".equals(cmd[0]) || "ask".equals(cmd[0])
				|| "give".equals(cmd[0])) {
			String[] target = findTarget(new String[] { "npc" }, cmd[1]);
			if (target != null) {
				cmd[1] = target[0];
			} else {
				cmd[0] = null;
			}
		} else if ("get".equals(cmd[0])) {
			String[] target = findTarget(new String[] { "item" }, cmd[1]);
			if (target != null) {
				cmd[1] = target[0];
			} else {
				cmd[0] = null;
			}
		} else if ("east".equals(cmd[0]) || "south".equals(cmd[0])
				|| "west".equals(cmd[0]) || "north".equals(cmd[0])
				|| "southeast".equals(cmd[0]) || "southwest".equals(cmd[0])
				|| "northeast".equals(cmd[0]) || "northwest".equals(cmd[0])
				|| "up".equals(cmd[0]) || "down".equals(cmd[0])) {
			cmd[1] = cmd[0];
			cmd[0] = "go";
		} else if ("fly".equals(cmd[0])) {
			cmd[0] = "jh";
			String id = MAP_IDS.get(cmd[1]);
			cmd[1] = id != null ? id : cmd[1];
		} else if ("tu".equals(cmd[0])) {
			cmd[0] = "cangbaotu_op1";
			cmd[1] = null;
		} else if ("quest".equals(cmd[0])) {
			cmd[0] = "task_quest";
			cmd[1] = null;
		} else if ("dig".equals(cmd[0])) {
			cmd[0] = "dig go";
			cmd[1] = null;
		} else if ("halt".equals(cmd[0])) {
			cmd[0] = "escape";
			cmd[1] = null;
		} else if ("heal".equals(cmd[0])) {
			cmd[0] = "recovery";
			cmd[1] = null;
		} else if ("chat".equals(cmd[0]) || "rumor".equals(cmd[0])) {
			if (cmd[1] == null) {
				cmd[0] = null;
			}
			isChat = true;
		}
		return isChat;
	}

	private String[] findTarget(String[] types, String pattern) {
		String name = null;
		int index = 1;
		int i = pattern.lastIndexOf(' ');
		if (i >= 0) {
			try {
				index = Integer.parseInt(pattern.substring(i + 1).trim());
				name = pattern.substring(0, i).trim();
			} catch (NumberFormatException e) {
				name = pattern;
			}
		} else {
			try {
				index = Integer.parseInt(pattern.trim());
			} catch (NumberFormatException e) {
				name = pattern;
			}
		}
		for (String[] target : getTargets(types)) {
			boolean match = false;
			if (name != null) {
				if ("corpse".equals(name)) {
					if (target[0].startsWith("corpse")) {
						match = true;
					}
				} else if (target[1] != null) {
					if (target[1].contains(name)) {
						match = true;
					} else {
						for (String pinyin : Pinyin.convertToPinyin(target[1])) {
							if (pinyin.contains(name)) {
								match = true;
								break;
							}
						}
						if (!match) {
							if (Pinyin.convertToFirstPinyin(target[1])
									.contains(name)) {
								match = true;
							}
						}
					}
				}
			} else {
				match = true;
			}
			if (match && (--index) == 0) {
				return new String[] { target[0], target[2] };
			}
		}
		return null;
	}

	private String[] findTargets(String type, String name) {
		List<String> list = new ArrayList<String>();
		for (String[] target : getTargets(type)) {
			boolean match = false;
			if (name != null) {
				if ("corpse".equals(name)) {
					if (target[0].startsWith("corpse")) {
						match = true;
					}
				} else if (target[1] != null) {
					if (target[1].contains(name)) {
						match = true;
					} else {
						for (String pinyin : Pinyin.convertToPinyin(target[1])) {
							if (pinyin.contains(name)) {
								match = true;
								break;
							}
						}
						if (!match) {
							if (Pinyin.convertToFirstPinyin(target[1])
									.contains(name)) {
								match = true;
							}
						}
					}
				}
			} else {
				match = true;
			}
			if (match) {
				list.add(target[0]);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	@SuppressWarnings("unchecked")
	private List<String[]> getTargets(String... types) {
		List<List<String>> targets = (List<List<String>>) js(
				load("get_targets.js"), new Object[] { types });
		List<String[]> result = new ArrayList<String[]>(targets.size());
		for (List<String> target : targets) {
			String text = target.get(1);
			if (text != null) {
				target.set(1, removeSGR(text));
			}
			result.add(target.toArray(new String[3]));
		}
		return result;
	}

	private String removeSGR(String text) {
		for (int i = text.indexOf("\u001b["); i >= 0; i = text
				.indexOf("\u001b[")) {
			int j = text.indexOf('m', i + 2);
			if (j >= 0) {
				text = text.substring(0, i) + text.substring(j + 1);
			}
		}
		return text;
	}

	private void loadAlias(String location) throws IOException {
		if (location != null) {
			aliasFile = new File(location);
		}
		if (aliasFile == null || !aliasFile.isFile()) {
			aliasFile = new File(System.getProperty("user.home"),
					"alias.properties");
		}
		aliases = new Properties();
		if (aliasFile.exists()) {
			aliases.load(new FileInputStream(aliasFile));
		} else {
			aliases.load(CommandLine.class
					.getResourceAsStream("/alias.properties"));
		}
	}

	private void saveAlias() throws IOException {
		aliases.store(new FileOutputStream(aliasFile), null);
	}

	private void send(String command) {
		command = command.replace(';', '\n');
		js("clickButton(arguments[0]);", command);
	}

	private String load(String file) {
		try {
			return IOUtils.readFully(CommandLine.class
					.getResourceAsStream(file));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Object js(String script, Object... args) {
		return ((JavascriptExecutor) webdriver).executeScript(script, args);
	}

	private String getCombatPosition() {
		return (String) js(load("get_combat_position.js"));
	}

	private void stopTask() {
		if (task != null) {
			task.cancel();
			task = null;
			System.out.println("task stopped.");
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
				ProcessedCommand pc = process(originCmd);
				if (pc.command != null) {
					processedCmd = pc;
				}
			}
			if (processedCmd != null) {
				send(processedCmd.command);
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
				ProcessedCommand pc = process("kill " + name);
				if (pc.command != null) {
					send(pc.command);
					state = 1;
				}
			} else if (state == 1) {
				state = getCombatPosition() != null ? 2 : 0;
			} else if (state == 2) {
				try {
					webdriver
							.findElement(By
									.xpath("//span[translate(normalize-space(text()),' ','')='战斗结束']"));
					send("prev_combat");
					state = 3;
				} catch (NoSuchElementException e) {
					// ignore
				}
			} else if (state == 3) {
				String[] corpses = findTargets("item", "corpse");
				if (corpses.length > 0) {
					StringBuilder sb = new StringBuilder();
					for (String corpse : corpses) {
						if (sb.length() > 0) {
							sb.append(';');
						}
						sb.append("get " + corpse);
					}
					send(sb.toString());
					state = 4;
					System.out.println("ok!");
					this.cancel();
					if (task == this) {
						task = null;
					}
				}
			}
		}
	}

	private class LootTask extends TimerTask {

		private int state;

		@Override
		public void run() {
			if (state == 0) {
				try {
					webdriver
							.findElement(By
									.xpath("//span[translate(normalize-space(text()),' ','')='战斗结束']"));
					send("prev_combat");
					state = 1;
				} catch (NoSuchElementException e) {
					// ignore
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
					send(sb.toString());
					state = 2;
					System.out.println("ok!");
					this.cancel();
					if (task == this) {
						task = null;
					}
				}
			}
		}
	}

	private class CombatTask extends TimerTask {

		private String pos;
		private String[] performs;
		private String heal;
		private double safePercent;
		private int fastKillHp;
		private List<Object> context = new ArrayList<Object>(4);

		public CombatTask(String pos, String[] performs, String heal,
				double safePercent, int fastKillHp) {
			super();
			this.pos = pos;
			this.performs = performs;
			this.heal = heal;
			this.safePercent = safePercent;
			this.fastKillHp = fastKillHp;
			this.context.add(0);
			this.context.add(false);
			this.context.add(false);
			this.context.add(null);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				context = (List<Object>) js(load("auto_fight.js"), pos,
						performs, heal, safePercent, fastKillHp, context);
				if (context != null) {
					if (context.get(3) != null) {
						System.out.println(context.get(3));
						context.set(3, null);
					}
				} else {
					System.out.println("ok!");
					this.cancel();
					if (task == this) {
						task = null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.cancel();
				if (task == this) {
					task = null;
				}
			}
		}
	}

	private class ChannelTask extends TimerTask {

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			List<String> msgs = (List<String>) js(load("get_chat_msgs.js"));
			for (String msg : msgs) {
				msg = removeSGR(msg);
				System.out.print(msg);
				js("notify_fail(arguments[0]);", msg);
			}
		}
	}

	public static class ProcessedCommand {
		String command;
		boolean isChat;
	}
}
