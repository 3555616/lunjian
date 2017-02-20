package org.mingy.lunjian;

import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.io.IOUtils;

public class CommandLine {

	private static final DateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
	private static final Map<String, String> MAP_IDS = new HashMap<String, String>();

	protected WebDriver webdriver;
	private WebDriver webdriver2;
	private File aliasFile;
	protected Properties properties;
	private Properties defaultAliases;
	private Properties userAliases;
	protected Timer timer;
	private SnoopTask snoopTask;
	private WebqqTask webqqTask;
	private TimerTask task;
	private BlockingQueue<String> webqqQueue;
	private Thread webqqThread;
	private TriggerManager triggerManager;
	private Map<String, String> jslibs = new HashMap<String, String>();
	private Toolkit toolkit = new Frame().getToolkit();

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
		MAP_IDS.put("mojiao", "27");
		MAP_IDS.put("mj2", "27");
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
		cmdline.run(args);
	}

	public void run(String[] args) throws Exception {
		start(args);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in, "gbk"));
		try {
			for (;;) {
				String line = reader.readLine();
				if (line == null
						|| "#quit".equals((line = line.toLowerCase().trim()))) {
					break;
				}
				execute(line);
			}
		} finally {
			finish();
			System.out.println("over!");
		}
	}

	protected void start(String[] args) throws Exception {
		properties = new Properties();
		if (args.length > 0) {
			properties.load(new InputStreamReader(new FileInputStream(args[0]),
					"utf-8"));
		} else {
			properties.load(CommandLine.class
					.getResourceAsStream("/lunjian.properties"));
		}
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
		webdriver.manage().timeouts()
				.setScriptTimeout(1000, TimeUnit.MILLISECONDS);
		loadAliases(properties.getProperty("alias.properties"));
		registerTriggers();
		String triggers = properties.getProperty("snoop.triggers");
		loadTriggers(triggers != null && triggers.length() > 0 ? triggers
				.split(",") : new String[0]);
		timer = new Timer(true);
		String keywords = properties.getProperty("snoop.keywords");
		snoopTask = new SnoopTask(
				keywords != null && keywords.length() > 0 ? keywords.split(",")
						: new String[0]);
		timer.schedule(snoopTask, 1000, 1000);
		if (Boolean.parseBoolean(properties.getProperty("notify.webqq"))) {
			if (browser == null || "firefox".equalsIgnoreCase(browser)) {
				webdriver2 = new FirefoxDriver();
			} else if ("chrome".equalsIgnoreCase(browser)) {
				webdriver2 = new ChromeDriver();
			}
			webdriver2.manage().window().setSize(new Dimension(1052, 768));
			webdriver2.navigate().to("http://web2.qq.com");
			webdriver2.switchTo().defaultContent();
			final String groupId = properties.getProperty("notify.group");
			final String pingId = properties.getProperty("notify.ping");
			long interval = Long.parseLong(properties
					.getProperty("notify.interval"));
			if (groupId != null && groupId.length() > 0 && pingId != null
					&& pingId.length() > 0) {
				webqqTask = new WebqqTask();
				timer.schedule(webqqTask, interval, interval);
			}
			webqqQueue = new LinkedBlockingDeque<String>();
			webqqThread = new Thread() {
				@Override
				public void run() {
					while (!isInterrupted()) {
						try {
							String message = webqqQueue.take();
							if ("__ping__".equals(message)) {
								switchWebqq(pingId);
								sendWebqq("1");
								switchWebqq(groupId);
							} else {
								sendWebqq(message);
							}
						} catch (NoSuchElementException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}
			};
			webqqThread.start();
		}
	}

	protected void registerTriggers() {
		TriggerManager.register("youxia", YouxiaTrigger.class);
		TriggerManager.register("qinglong", QinglongTrigger.class);
		TriggerManager.register("zhengxie", ZhengxieTrigger.class);
		TriggerManager.register("baozang", BaozangTrigger.class);
	}

	protected void finish() throws Exception {
		timer.cancel();
		if (webqqThread != null) {
			webqqThread.interrupt();
		}
		webdriver.quit();
		if (webdriver2 != null) {
			webdriver2.quit();
		}
	}

	protected void execute(String line) throws IOException {
		if (line.equals("#combat")) {
			String[] settings = properties.getProperty("auto.fight", "").split(
					",");
			if (settings.length < 1) {
				System.out.println("property auto.fight not set");
			} else {
				String[] pfms = settings[0].split("\\|");
				int wait = settings.length > 1 && settings[1].length() > 0 ? Integer
						.parseInt(settings[1]) : 0;
				String heal = settings.length > 2 && settings[2].length() > 0 ? settings[2]
						: null;
				int safe = settings.length > 3 && settings[3].length() > 0 ? Integer
						.parseInt(settings[3]) : 0;
				int fast = settings.length > 4 && settings[4].length() > 0 ? Integer
						.parseInt(settings[4]) : 0;
				if (wait < pfms.length * 20) {
					wait = pfms.length * 20;
				}
				String pos = getCombatPosition();
				if (pos != null) {
					System.out.println("starting auto combat...");
					executeTask(new CombatTask(pos, pfms, wait, heal, safe,
							fast), 500);
				}
			}
		} else if (line.equals("#combat continue")) {
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
				int fast = settings.length > 4 && settings[4].length() > 0 ? Integer
						.parseInt(settings[4]) : 0;
				String fastpfm = settings.length > 5
						&& settings[5].length() > 0 ? settings[5] : null;
				if (wait < pfms.length * 20) {
					wait = pfms.length * 20;
				}
				System.out.println("starting continue combat...");
				executeTask(new ContinueCombatTask(pfms, wait, heal, safe,
						fast, fastpfm), 500);
			}
		} else if (line.startsWith("#findway ")) {
			line = line.substring(9).trim();
			if (line.length() > 0) {
				System.out.println("starting find way...");
				executeTask(new FindWayTask(line), 100);
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
				if (!value.equals(userAliases.getProperty(key))) {
					userAliases.setProperty(key, value);
					saveAliases();
					System.out.println("set alias ok.");
				}
			} else {
				if (userAliases.containsKey(key)) {
					userAliases.remove(key);
					saveAliases();
					System.out.println("alias removed.");
				}
			}
		} else if (line.equals("#set")) {
			properties.list(System.out);
		} else if (line.startsWith("#set ")) {
			String alias = line.substring(5).trim();
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
				if (!value.equals(properties.getProperty(key))) {
					properties.setProperty(key, value);
					// saveConfig();
					System.out.println("set property ok.");
				}
			} else {
				if (properties.containsKey(key)) {
					properties.remove(key);
					// saveConfig();
					System.out.println("property removed.");
				}
			}
		} else if (line.startsWith("#snoop add ")) {
			snoopTask.add(line.substring(11).trim());
		} else if (line.startsWith("#snoop del ")) {
			snoopTask.remove(line.substring(11).trim());
		} else if (line.startsWith("#s+ ")) {
			snoopTask.add(line.substring(4).trim());
		} else if (line.startsWith("#s- ")) {
			snoopTask.remove(line.substring(4).trim());
		} else if (line.startsWith("#trigger add ")) {
			triggerManager.add(line.substring(13).trim());
		} else if (line.startsWith("#trigger del ")) {
			triggerManager.remove(line.substring(13).trim());
		} else if (line.startsWith("#t+ ")) {
			triggerManager.add(line.substring(4).trim());
		} else if (line.startsWith("#t- ")) {
			triggerManager.remove(line.substring(4).trim());
		} else if (line.startsWith("#show ")) {
			triggerManager.process(this, line.substring(6).trim());
		} else if (line.startsWith("#sh ")) {
			triggerManager.process(this, line.substring(4).trim());
		} else if (webqqQueue != null && line.startsWith("#send ")) {
			webqqQueue.offer(line.substring(6).trim());
		} else if (webqqQueue != null && line.equals("#ping")) {
			webqqQueue.offer("__ping__");
		} else if (line.length() > 0 && line.charAt(0) == '#') {
			int i = line.indexOf(' ');
			if (i >= 0) {
				try {
					int times = Integer.parseInt(line.substring(1, i));
					line = line.substring(i + 1).trim();
					if (times > 0 && line.length() > 0) {
						for (int j = 0; j < times; j++) {
							executeCmd(line);
						}
					}
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		} else if (line.length() > 0) {
			executeCmd(line);
		}
	}

	protected String getProperty(String key) {
		return properties.getProperty(key);
	}

	protected boolean executeCmd(String command) {
		ProcessedCommand pc = processCmd(command);
		if (pc.isChat) {
			sendCmd("go_chat");
		} else {
			sendCmd("quit_chat");
		}
		if (pc.command != null) {
			sendCmd(pc.command);
			return true;
		} else {
			return false;
		}
	}

	protected void executeTask(TimerTask task, int interval) {
		stopTask();
		this.task = task;
		timer.schedule(task, 0, interval);
	}

	protected ProcessedCommand processCmd(String line) {
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
		String alias = userAliases.getProperty(cmd[0]);
		if (alias == null) {
			alias = defaultAliases.getProperty(cmd[0]);
		}
		if (alias != null) {
			line = alias.toLowerCase().trim();
			if (cmd[1] != null) {
				line += " " + cmd[1];
			}
			return processCmd(line);
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
				|| "give".equals(cmd[0]) || "buy".equals(cmd[0])) {
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
		} else if ("dig".equals(cmd[0])) {
			cmd[0] = "dig go";
			cmd[1] = null;
		} else if ("halt".equals(cmd[0])) {
			cmd[0] = "escape";
			cmd[1] = null;
		} else if ("pfm".equals(cmd[0]) || "perform".equals(cmd[0])) {
			String skills = cmd[1];
			if (skills == null) {
				String[] settings = properties.getProperty("auto.fight", "")
						.split(",");
				if (settings.length > 0) {
					skills = settings[0];
				}
			}
			if (skills != null) {
				StringBuilder sb = new StringBuilder();
				String[] pfms = skills.split("\\|");
				List<String> list = getCombatSkills();
				if (list != null && !list.isEmpty()) {
					for (String pfm : pfms) {
						for (int i = 0; i < list.size(); i++) {
							String text = list.get(i);
							if (matchText(text, pfm)) {
								if (sb.length() > 0) {
									sb.append(';');
								}
								sb.append("playskill ").append(i + 1);
							}
						}
					}
				}
				cmd[0] = sb.length() > 0 ? sb.toString() : null;
			} else {
				cmd[0] = null;
			}
		} else if ("1".equals(cmd[0])) {
			cmd[0] = "playskill 1";
			cmd[1] = null;
		} else if ("2".equals(cmd[0])) {
			cmd[0] = "playskill 2";
			cmd[1] = null;
		} else if ("3".equals(cmd[0])) {
			cmd[0] = "playskill 3";
			cmd[1] = null;
		} else if ("4".equals(cmd[0])) {
			cmd[0] = "playskill 4";
			cmd[1] = null;
		} else if ("heal".equals(cmd[0])) {
			cmd[0] = "recovery";
			cmd[1] = null;
		} else if ("quest".equals(cmd[0])) {
			cmd[0] = "family_quest";
			cmd[1] = null;
		} else if ("task".equals(cmd[0])) {
			cmd[0] = "task_quest";
			if ("cancel".equals(cmd[1])) {
				cmd[0] = "auto_tasks";
			}
		} else if ("map".equals(cmd[0])) {
			cmd[0] = "client_map";
			cmd[1] = null;
		} else if ("chat".equals(cmd[0]) || "rumor".equals(cmd[0])) {
			if (cmd[1] == null) {
				cmd[0] = null;
			}
			isChat = true;
		}
		return isChat;
	}

	protected boolean isFighting() {
		return getCombatPosition() != null;
	}

	protected boolean isCombatOver() {
		try {
			webdriver
					.findElement(By
							.xpath("//span[translate(normalize-space(text()),' ','')='战斗结束']"));
			sendCmd("prev_combat");
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	protected void notify(String message, boolean important, boolean send) {
		if (important) {
			new Thread() {
				@Override
				public void run() {
					for (int i = 0; i < 5; i++) {
						toolkit.beep();
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// ignore
						}
					}
				}
			}.start();
		}
		String details = message + " (" + FORMAT_TIME.format(new Date()) + ")";
		System.out.println(details);
		js("notify_fail(arguments[0]);", message);
		if (send && webdriver2 != null) {
			if (important) {
				details += " @全体成员";
			}
			webqqQueue.offer(details);
		}
	}

	protected String[] findTarget(String[] types, String pattern) {
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
		List<String[]> targets = getTargets(types);
		if (index < 0) {
			index = -index;
			Collections.reverse(targets);
		}
		for (String[] target : targets) {
			boolean match = false;
			if (name != null) {
				if ("corpse".equals(name)) {
					if (target[0].startsWith("corpse")) {
						match = true;
					}
				} else if (target[1] != null) {
					match = matchText(target[1], name);
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

	protected String[] findTargets(String type, String name) {
		List<String> list = new ArrayList<String>();
		for (String[] target : getTargets(type)) {
			boolean match = false;
			if (name != null) {
				if ("corpse".equals(name)) {
					if (target[0].startsWith("corpse")) {
						match = true;
					}
				} else if (target[1] != null) {
					match = matchText(target[1], name);
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

	protected boolean matchText(String text, String name) {
		if (text == null || text.length() == 0) {
			return false;
		}
		if (text.contains(name)) {
			return true;
		} else {
			for (String pinyin : Pinyin.convertToPinyin(text)) {
				if (pinyin.contains(name)) {
					return true;
				}
			}
			if (Pinyin.convertToFirstPinyin(text).contains(name)) {
				return true;
			}
		}
		return false;
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

	protected static String removeSGR(String text) {
		for (int i = text.indexOf("\u001b["); i >= 0; i = text
				.indexOf("\u001b[")) {
			int j = text.indexOf('m', i + 2);
			if (j >= 0) {
				text = text.substring(0, i) + text.substring(j + 1);
			}
		}
		return text;
	}

	private void loadAliases(String location) throws IOException {
		if (location != null) {
			aliasFile = new File(location);
		}
		if (aliasFile == null || !aliasFile.isFile()) {
			aliasFile = new File(System.getProperty("user.home"),
					"alias.properties");
		}
		defaultAliases = new Properties();
		defaultAliases.load(CommandLine.class
				.getResourceAsStream("/alias.properties"));
		userAliases = new Properties();
		if (aliasFile.exists()) {
			userAliases.load(new FileInputStream(aliasFile));
		}
	}

	private void saveAliases() throws IOException {
		userAliases.store(new FileOutputStream(aliasFile), null);
	}

	private void loadTriggers(String[] triggers) {
		triggerManager = new TriggerManager();
		for (String trigger : triggers) {
			triggerManager.add(trigger);
		}
	}

	protected void sendCmd(String command) {
		command = command.replace(';', '\n');
		js("clickButton(arguments[0]);", command);
	}

	protected String load(String file) {
		String js = jslibs.get(file);
		if (js == null) {
			try {
				js = IOUtils.readFully(CommandLine.class
						.getResourceAsStream(file));
				jslibs.put(file, js);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return js;
	}

	protected Object js(String script, Object... args) {
		return ((JavascriptExecutor) webdriver).executeScript(script, args);
	}

	protected String getCombatPosition() {
		return (String) js(load("get_combat_position.js"));
	}

	@SuppressWarnings("unchecked")
	protected List<String> getCombatSkills() {
		List<String> skills = (List<String>) js(load("get_combat_skills.js"));
		for (int i = 0; i < skills.size(); i++) {
			skills.set(i, skills.get(i).trim());
		}
		return skills;
	}

	protected void stopTask(TimerTask task) {
		task.cancel();
		if (this.task == task) {
			this.task = null;
		}
	}

	private void stopTask() {
		if (task != null) {
			task.cancel();
			task = null;
			System.out.println("task stopped.");
		}
	}

	private void switchWebqq(String id) {
		webdriver2
				.findElement(
						By.xpath("//ul[@id='current_chat_list']/li[@_uin='"
								+ id + "']")).click();
	}

	private void sendWebqq(String message) {
		WebElement e = webdriver2.findElement(By.id("chat_textarea"));
		e.clear();
		e.sendKeys(message);
		webdriver2.findElement(By.id("send_chat_btn")).click();
	}

	private class CombatTask extends TimerTask {

		private String pos;
		private int waitPoint;
		private String[] performs;
		private String heal;
		private double safePercent;
		private int fastKillHp;
		private List<Object> context = new ArrayList<Object>(4);

		public CombatTask(String pos, String[] performs, int waitPoint,
				String heal, double safePercent, int fastKillHp) {
			super();
			this.pos = pos;
			this.performs = performs;
			this.waitPoint = waitPoint;
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
						performs, waitPoint, heal, safePercent, fastKillHp,
						context);
				if (context != null) {
					if (context.get(3) != null) {
						// System.out.println(context.get(3));
						context.set(3, null);
					}
				} else {
					System.out.println("ok!");
					stopTask(this);
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class ContinueCombatTask extends TimerTask {

		private int waitPoint;
		private String[] performs;
		private String heal;
		private int safeHp;
		private int fastKillHp;
		private String fastPerform;
		private List<Object> context = new ArrayList<Object>(5);

		public ContinueCombatTask(String[] performs, int waitPoint,
				String heal, int safeHp, int fastKillHp, String fastPerform) {
			super();
			this.performs = performs;
			this.waitPoint = waitPoint;
			this.heal = heal;
			this.safeHp = safeHp;
			this.fastKillHp = fastKillHp;
			this.fastPerform = fastPerform;
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
						performs, waitPoint, heal, safeHp, fastKillHp,
						fastPerform, context);
				if (ctx != null) {
					context = ctx;
					if (context.get(3) != null) {
						// System.out.println(context.get(3));
						context.set(3, null);
					}
				} else if ((Boolean) context.get(4)) {
					ProcessedCommand pc = processCmd("get corpse; get corpse 2;get corpse 3;get corpse 4");
					if (pc.command != null) {
						sendCmd(pc.command);
					}
					context.set(0, 0);
					context.set(1, false);
					context.set(2, false);
					context.set(3, null);
					context.set(4, false);
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class FindWayTask extends TimerTask {

		private String target;
		private String current;

		public FindWayTask(String target) {
			super();
			this.target = target;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				List<List<String>> rooms = (List<List<String>>) js(load("get_rooms.js"));
				if (!rooms.isEmpty()) {
					String room = removeSGR(rooms.get(0).get(1));
					if (current == null || !current.equals(room)) {
						current = room;
						if (!current.equals(target)) {
							for (int i = 1; i < rooms.size(); i++) {
								if (target
										.equals(removeSGR(rooms.get(i).get(1)))) {
									sendCmd("go " + rooms.get(i).get(0));
									return;
								}
							}
							int i = (int) Math.floor(Math.random()
									* (rooms.size() - 1)) + 1;
							sendCmd("go " + rooms.get(i).get(0));
						} else {
							System.out.println("ok!");
							stopTask(this);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class SnoopTask extends TimerTask {

		private List<String> keywords;

		private SnoopTask(String[] keywords) {
			this.keywords = new ArrayList<String>(Arrays.asList(keywords));
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (keywords.isEmpty()) {
				return;
			}
			List<String> msgs = (List<String>) js(load("get_chat_msgs.js"),
					keywords);
			for (String msg : msgs) {
				msg = removeSGR(msg);
				triggerManager.process(CommandLine.this, msg);
			}
		}

		public void add(String keyword) {
			if (!keywords.contains(keyword)) {
				keywords.add(keyword);
				System.out.println("ok!");
			}
		}

		public void remove(String keyword) {
			if (keywords.contains(keyword)) {
				keywords.remove(keyword);
				System.out.println("ok!");
			}
		}
	}

	private class WebqqTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("send ping");
			webqqQueue.offer("__ping__");
		}
	}

	public static class ProcessedCommand {
		String command;
		boolean isChat;
	}
}
