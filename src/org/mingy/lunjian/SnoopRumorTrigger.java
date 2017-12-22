package org.mingy.lunjian;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.CommandLine.SnoopRumorTask;

public class SnoopRumorTrigger implements Trigger {

	private List<String> keywords;
	private List<String> ignores;

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"rumor".equals(type)) {
			return false;
		}
		if (keywords == null || ignores == null) {
			String str = cmdline.getProperty("snoop.rumor.keywords");
			str = str != null ? str.trim() : "";
			keywords = Arrays.asList(str.split(","));
			str = cmdline.getProperty("snoop.rumor.ignores");
			str = str != null ? str.trim() : "";
			ignores = Arrays.asList(str.split(","));
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String killer = m.group(1);
		String target = m.group(2);
		process(cmdline, killer, target);
		return true;
	}

	protected void process(CommandLine cmdline, String killer, String target) {
		if ("你".equals(killer) || "你".equals(target)) {
			NewPvpCombatTask task = new NewPvpCombatTask(cmdline);
			if (task.init()) {
				System.out.println("starting auto pvp ...");
				cmdline.executeTask(task, 100);
			}
		} else {
			boolean f1 = false, f2 = false;
			String include = cmdline.getProperty("friends.include");
			if (include != null && include.length() > 0) {
				for (String s : include.split(",")) {
					if (killer.contains(s)) {
						f1 = true;
					}
					if (target.contains(s)) {
						f2 = true;
					}
				}
			}
			String exclude = cmdline.getProperty("friends.exclude");
			if (exclude != null && exclude.length() > 0) {
				for (String s : exclude.split(",")) {
					if (f1 && killer.contains(s)) {
						f1 = false;
					}
					if (f2 && target.contains(s)) {
						f2 = false;
					}
				}
			}
			if (f1 != f2) {
				String name = f1 ? target : killer;
				String[] tar = cmdline
						.findTarget(new String[] { "user" }, name);
				if (tar != null) {
					cmdline.sendCmd("fight " + tar[0]);
					NewPvpCombatTask task = new NewPvpCombatTask(cmdline);
					if (task.init()) {
						System.out.println("starting auto pvp ...");
						cmdline.executeTask(task, 100);
					}
				}
			}
		}
	}

	@Override
	public void cleanup() {

	}
}
