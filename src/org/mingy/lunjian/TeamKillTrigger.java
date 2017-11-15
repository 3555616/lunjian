package org.mingy.lunjian;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamKillTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^(.*)对著(.*)喝道：「(.*)！今日不是你死就是我活！」$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"local".equals(type)) {
			return false;
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

	@SuppressWarnings("unchecked")
	protected void process(CommandLine cmdline, String killer, String target) {
		if ("你".equals(killer)) {
			cmdline.fastCombat(false, false, true, null);
		} else {
			Map<String, Object> team = (Map<String, Object>) cmdline.js(
					cmdline.load("get_msgs.js"), "msg_team", false);
			if (team != null && "1".equals(team.get("is_member_of"))) {
				String[] arr = cmdline.findTarget(new String[] { "user" },
						killer);
				if (arr != null) {
					int n = 0;
					for (int i = 0; i < Integer.parseInt(String.valueOf(team
							.get("max_member_num"))); i++) {
						String member = (String) team.get("member" + (i + 1));
						if (member.startsWith(arr[0] + ",")) {
							n = i + 1;
							break;
						}
					}
					if (n > 0) {
						try {
							Thread.sleep(((n - 1) % 3) * 1000);
						} catch (InterruptedException e) {
							// ignore
						}
						arr = cmdline.findTarget(
								new String[] { "npc", "user" }, target);
						if (arr != null) {
							cmdline.sendCmd("kill " + arr[0]);
							cmdline.fastCombat(false, false, true, null);
						}
					}
				}

			}
		}
	}

	@Override
	public void cleanup() {

	}
}
