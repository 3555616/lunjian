package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QinglongTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("青龙会组织：(.*)正在(.*)施展力量，本会愿出(.*)的战利品奖励给本场战斗的最终获胜者。");

	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		String reward = m.group(3);
		String ignores = cmdline.getProperty("qinglong.ignore");
		boolean pass = true;
		if (ignores != null) {
			for (String ignore : ignores.split(",")) {
				if (reward.contains(ignore)) {
					pass = false;
					break;
				}
			}
		}
		process(cmdline, npc, place, reward, !pass);
		return true;
	}

	protected void process(CommandLine cmdline, String npc, String place,
			String reward, boolean ignore) {
		cmdline.notify("[青龙] " + npc + " at " + place + " rewards " + reward,
				!ignore);
	}
}
