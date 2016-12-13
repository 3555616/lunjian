package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class CommandLine {

	private WebDriver webdriver;
	private File aliasFile;
	private Properties aliases;
	private Timer timer;
	private TimerTask loopTask;

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
			System.setProperty("webdriver.firefox.bin", properties.getProperty("webdriver.firefox.bin"));
			webdriver = new FirefoxDriver();
		} else if ("chrome".equalsIgnoreCase(browser)) {
			System.setProperty("webdriver.chrome.driver", properties.getProperty("webdriver.chrome.driver"));
			webdriver = new ChromeDriver();
		}
		webdriver.manage().window().setSize(new Dimension(560, 840));
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
				if (loopTask != null) {
					loopTask.cancel();
					loopTask = null;
				}
				System.out.println("starting loop...");
				loopTask = new LoopTask(line);
				timer.schedule(loopTask, 0, 500);
			}
		} else if (line.startsWith("#lc ")) {

		} else if (line.equals("#stop")) {
			if (loopTask != null) {
				loopTask.cancel();
				System.out.println("loop stopped.");
			}
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
				String cmd = line.replace(';', '\n');
				js("clickButton(arguments[0]);", cmd);
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
		} else if ("kill".equals(cmd[0]) || "fight".equals(cmd[0]) || "watch".equals(cmd[0])) {
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
		} else if ("quest".equals(cmd[0])) {
			cmd[0] = "task_quest";
		} else if ("halt".equals(cmd[0])) {
			cmd[0] = "escape";
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
				+ "var check = function(t, n) {if (types && $.inArray(t, types) < 0) {return false;} var s = n.split(','); if (name) {if (t == 'cmd') return false; if (s[1].indexOf(name) < 0 && (name.length < 6 || s[0].indexOf(name) < 0)) return false; return s[0];} else {return s[0];}};"
				+ "for (var t, i = 1; (t = msg.get('npc' + i)) != undefined; i++) {var id = check('npc', t); if (id && (--index) == 0) return [id, 'npc'];}\n"
				+ "for (var t, i = 1; (t = msg.get('item' + i)) != undefined; i++) {var id = check('item', t); if (id && (--index) == 0) return [id, 'item'];}\n"
				+ "for (var t, i = 1; (t = msg.get('user' + i)) != undefined; i++) {var id = check('user', t); if (id && (--index) == 0) return [id, 'user'];}\n"
				+ "for (var t, i = 1; (t = msg.get('cmd' + i)) != undefined; i++) {var id = check('cnd', t); if (id && (--index) == 0) return [id, 'cmd'];}\n"
				+ "return null;";
		List<String> target = (List<String>) js(js, types, name, index);
		return target != null ? target.toArray(new String[2]) : null;
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

	private Object js(String script, Object... args) {
		return ((JavascriptExecutor) webdriver).executeScript(script, args);
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
					cmd = cmd.replace(';', '\n');
					processedCmd = cmd;
				}
			}
			if (processedCmd != null) {
				js("clickButton(arguments[0]);", processedCmd);
			}
		}
	}
}
