package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class CommandLine {

	private WebDriver webdriver;
	private File aliasFile;
	private Properties aliases;
	private Timer timer;
	private TimerTask task;
	private String playerName;

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
		webdriver.manage().window().setSize(new Dimension(400, 600));
		webdriver.navigate().to(properties.getProperty("lunjian.url"));
		webdriver.switchTo().defaultContent();
		timer = new Timer(true);
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
			String pos = getCombatPos();
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
			line = process(line);
			if (line.length() > 0) {
				send(line);
			}
		}
	}

	private String process(String line) {
		StringBuilder sb = new StringBuilder();
		for (String cmd : line.split(";")) {
			if (cmd.length() > 0) {
				cmd = translate(cmd);
				if (cmd != null) {
					if (sb.length() > 0) {
						sb.append(';');
					}
					sb.append(cmd);
				}
			}
		}
		return sb.toString();
	}

	private String translate(String line) {
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
		translate(cmd);
		if (cmd[0] != null) {
			if (cmd[1] != null) {
				return cmd[0] + " " + cmd[1];
			} else {
				return cmd[0];
			}
		} else {
			return null;
		}
	}

	private void translate(String[] cmd) {
		if ("look".equals(cmd[0])) {
			if (cmd[1] == null) {
				cmd[0] = "golook_room";
			} else {
				String[] target = getTarget(new String[] { "npc", "item" },
						cmd[1]);
				if (target != null) {
					if ("npc".equals(target[1])) {
						cmd[0] = "look_npc";
						cmd[1] = target[0];
					} else {
						cmd[0] = "look_item";
						cmd[1] = target[0];
					}
				} else {
					cmd[0] = null;
				}
			}
		} else if ("kill".equals(cmd[0]) || "fight".equals(cmd[0])
				|| "watch".equals(cmd[0]) || "ask".equals(cmd[0])
				|| "give".equals(cmd[0])) {
			if ("watch".equals(cmd[0])) {
				cmd[0] = "watch_vs";
			}
			String[] target = getTarget(new String[] { "npc" }, cmd[1]);
			if (target != null) {
				cmd[1] = target[0];
			} else {
				cmd[0] = null;
			}
		} else if ("get".equals(cmd[0])) {
			String[] target = getTarget(new String[] { "item" }, cmd[1]);
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
		}
	}

	@SuppressWarnings("unchecked")
	private String[] getTarget(String[] types, String pattern) {
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
		String js = "var types = arguments[0], name = arguments[1], index = arguments[2], msg = g_obj_map.get('msg_room');\n"
				+ "if (msg == undefined) {return null;}\n"
				+ "var check = function(t, n) {if (types && $.inArray(t, types) < 0) {return false;} var s = n.split(','); if (name) {if (t == 'cmd') return false; if (s[1].indexOf(name) < 0 && (name.length < 6 || s[0].indexOf(name) < 0)) return false;} return s[0];};"
				+ "for (var t, i = 1; (t = msg.get('npc' + i)) != undefined; i++) {var id = check('npc', t); if (id && (--index) == 0) return [id, 'npc'];}\n"
				+ "for (var t, i = 1; (t = msg.get('item' + i)) != undefined; i++) {var id = check('item', t); if (id && (--index) == 0) return [id, 'item'];}\n"
				+ "for (var t, i = 1; (t = msg.get('user' + i)) != undefined; i++) {var id = check('user', t); if (id && (--index) == 0) return [id, 'user'];}\n"
				+ "for (var t, i = 1; (t = msg.get('cmd' + i)) != undefined; i++) {var id = check('cnd', t); if (id && (--index) == 0) return [id, 'cmd'];}\n"
				+ "return null;";
		List<String> target = (List<String>) js(js, types, name, index);
		return target != null ? target.toArray(new String[2]) : null;
	}

	@SuppressWarnings("unchecked")
	private String[] findTargets(String type, String name) {
		String js = "var type = arguments[0], name = arguments[1], msg = g_obj_map.get('msg_room'), targets = [];\n"
				+ "if (msg == undefined) {return targets;}\n"
				+ "var check = function(t, n) {if (type && type != t) {return false;} var s = n.split(','); if (name && s[1].indexOf(name) < 0 && (name.length < 6 || s[0].indexOf(name) < 0)) return false; return s[0];};"
				+ "for (var t, i = 1; (t = msg.get('npc' + i)) != undefined; i++) {var id = check('npc', t); if (id) targets.push(id);}\n"
				+ "for (var t, i = 1; (t = msg.get('item' + i)) != undefined; i++) {var id = check('item', t); if (id) targets.push(id);}\n"
				+ "for (var t, i = 1; (t = msg.get('user' + i)) != undefined; i++) {var id = check('user', t); if (id) targets.push(id);}\n"
				+ "return targets;";
		List<String> targets = (List<String>) js(js, type, name);
		return targets.toArray(new String[targets.size()]);
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

	private Object js(String script, Object... args) {
		return ((JavascriptExecutor) webdriver).executeScript(script, args);
	}

	private String getPlayerName() {
		if (playerName == null) {
			playerName = (String) js("return g_obj_map.get('msg_attrs').get('name');");
		}
		return playerName;
	}

	private String getCombatPos() {
		try {
			WebElement e = webdriver
					.findElement(By
							.xpath("//td[@id='vs11' or @id='vs12' or @id='vs13' or @id='vs14' or @id='vs21' or @id='vs22' or @id='vs23' or @id='vs24'][translate(normalize-space(text()),' ','')='"
									+ getPlayerName() + "']"));
			return e.getAttribute("id").substring(2);
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	private int getTargetHp(String pos) {
		String target;
		if (pos.startsWith("2")) {
			target = "@id='vs_hp11' or @id='vs_hp12' or @id='vs_hp13' or @id='vs_hp14'";
		} else {
			target = "@id='vs_hp21' or @id='vs_hp22' or @id='vs_hp23' or @id='vs_hp24'";
		}
		int hp = 0;
		for (WebElement e : webdriver.findElements(By.xpath("//span[" + target
				+ "]/i/span"))) {
			hp += Integer.parseInt(e.getText());
		}
		return hp;
	}

	private boolean autoFight(String pos, String[] performs, String heal,
			double safePercent, int fastKillHp, CombatContext context) {
		// xdz_bar
		try {
			WebElement bar = webdriver.findElement(By.id("barxdz_bar"));
			String style = bar.getAttribute("style");
			int k = style.indexOf("width:");
			if (k < 0) {
				return false;
			}
			String width = style.substring(k + 6, style.indexOf(';', k)).trim();
			if (!width.endsWith("%")) {
				return false;
			}
			double point = Double.parseDouble(width.substring(0,
					width.length() - 1));
			if (point < 20) {
				context.fast = false;
				return true;
			}
			if (!context.heal && heal != null && safePercent > 0) {
				bar = webdriver.findElement(By.id("barvader" + pos));
				style = bar.getAttribute("style");
				k = style.indexOf("width:");
				if (k < 0) {
					return false;
				}
				width = style.substring(k + 6, style.indexOf(';', k)).trim();
				if (!width.endsWith("%")) {
					return false;
				}
				double hp = Double.parseDouble(width.substring(0,
						width.length() - 1));
				if (hp < safePercent) {
					context.fast = false;
					context.heal = true;
				}
			}
			if (context.heal) {
				try {
					WebElement button = webdriver
							.findElement(By
									.xpath("//button[@class='cmd_skill_button'][span[translate(normalize-space(text()),' ','')='"
											+ heal + "']]"));
					String onclick = button.getAttribute("onclick");
					if (!"clickButton('0', 0)".equals(onclick)) {
						System.out.println("perform " + heal);
						button.click();
						context.fast = false;
						context.heal = false;
					}
					return true;
				} catch (NoSuchElementException e) {
					// no heal skill
				}
			}
			if (!context.heal && !context.fast) {
				if (getTargetHp(pos) < fastKillHp) {
					if (point < 40) {
						return true;
					}
				} else {
					if (point < 100) {
						return true;
					}
				}
			}
			Map<Integer, String> map = new LinkedHashMap<Integer, String>();
			for (int i = context.index; i < performs.length; i++) {
				map.put(i, performs[i]);
			}
			for (int i = 0; i < context.index; i++) {
				map.put(i, performs[i]);
			}
			for (Integer i : map.keySet()) {
				String perform = map.get(i);
				try {
					WebElement button = webdriver
							.findElement(By
									.xpath("//button[@class='cmd_skill_button'][span[translate(normalize-space(text()),' ','')='"
											+ perform + "']]"));
					String onclick = button.getAttribute("onclick");
					if (!"clickButton('0', 0)".equals(onclick)) {
						System.out.println("perform " + perform);
						button.click();
						context.index = ++i < performs.length ? i : 0;
						context.fast = true;
					}
					return true;
				} catch (NoSuchElementException e) {
					// ignore
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
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
		private String processedCmd;

		public LoopTask(String cmd) {
			originCmd = cmd;
		}

		@Override
		public void run() {
			if (processedCmd == null) {
				String cmd = process(originCmd);
				if (cmd.length() > 0) {
					processedCmd = cmd;
				}
			}
			if (processedCmd != null) {
				send(processedCmd);
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
				String cmd = process("kill " + name);
				if (cmd.length() > 0) {
					send(cmd);
					state = 1;
				}
			} else if (state == 1) {
				state = getCombatPos() != null ? 2 : 0;
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
				String[] corpses = findTargets("item", name + "的尸体");
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
		private CombatContext context = new CombatContext();

		public CombatTask(String pos, String[] performs, String heal,
				double safePercent, int fastKillHp) {
			super();
			this.pos = pos;
			this.performs = performs;
			this.heal = heal;
			this.safePercent = safePercent;
			this.fastKillHp = fastKillHp;
		}

		@Override
		public void run() {
			if (!autoFight(pos, performs, heal, safePercent, fastKillHp,
					context)) {
				System.out.println("ok!");
				this.cancel();
				if (task == this) {
					task = null;
				}
			}
		}
	}

	private static class CombatContext {
		int index;
		boolean fast;
		boolean heal;
	}
}
