package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QinglongTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("^青龙会组织：(.*)正在(.*)施展力量，本会愿出(.*)的战利品奖励给本场战斗的最终获胜者。这是.*第(\\d+)个.*青龙。$");
	private static final Pattern PATTERN1 = Pattern
			.compile("^【系统】青龙会组织：(.*)正在(.*)施展力量，本会愿出(.*)的战利品奖励给本场战斗的最终获胜者。这是.*第(\\d+)个.*青龙。$");
	private static final Pattern PATTERN2 = Pattern
			.compile("^【系统】跨服：\\[(.*)\\](.*)逃到了跨服时空(.*)之中，青龙会组织悬赏(.*)惩治恶人，众位英雄快来诛杀。这是.*第(\\d+)个.*青龙。$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"sys".equals(type) && !"local".equals(type)) {
			return false;
		}
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			m = PATTERN1.matcher(message);
		}
		if (m.find()) {
			String npc = m.group(1);
			String place = m.group(2);
			String reward = m.group(3);
			int count = Integer.parseInt(m.group(4));
			String kuafu = cmdline.getProperty("kuafu.area");
			if (kuafu == null || kuafu.length() == 0) {
				kuafu = "1-5区";
			}
			if (!npc.startsWith("[") || npc.startsWith("[" + kuafu + "]")) {
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
				process(cmdline, npc, place, reward, !pass, count, seq);
			}
			return true;
		}
		m = PATTERN2.matcher(message);
		if (m.find()) {
			String kuafu = cmdline.getProperty("kuafu.area");
			if (kuafu == null || kuafu.length() == 0) {
				kuafu = "1-5区";
			}
			if (kuafu.equals(m.group(1))) {
				String npc = "[" + m.group(1) + "]" + m.group(2);
				String place = m.group(3);
				String reward = m.group(4);
				int count = Integer.parseInt(m.group(4));
				process(cmdline, npc, place, reward, false, count, 0);
				return true;
			}
		}
		return false;
	}

	protected void process(CommandLine cmdline, String npc, String place,
			String reward, boolean ignore, int count, long seq) {
		if (seq <= 0) {
			String message = "[青龙] " + npc + " at " + place + " rewards "
					+ reward + " (" + count + ")";
			cmdline.notify(message, !ignore, true);
		}
	}

	@Override
	public void cleanup() {

	}
}
