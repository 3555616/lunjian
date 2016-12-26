package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QinglongTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("青龙会组织：(.*)正在(.*)施展力量，本会愿出(.*)的战利品奖励给本场战斗的最终获胜者。");
	private static final Map<String, String> PATHS = new HashMap<String, String>();

	static {
		PATHS.put("打铁铺子", "tiepu");
		PATHS.put("桑邻药铺", "yaopu");
		PATHS.put("书房", "shufang");
		PATHS.put("南市", "nanshi");
		PATHS.put("绣楼", "xiulou");
		PATHS.put("北大街", "beidajie");
		PATHS.put("钱庄", "qianzhuang");
		PATHS.put("杂货铺", "zahuopu");
		PATHS.put("祠堂大门", "citang");
		PATHS.put("厅堂", "tingtang");
	}

	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		String reward = m.group(3);
		cmdline.notify("[青龙] " + npc + " at " + place + " rewards " + reward);
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
		if (pass && !cmdline.isFighting()) {
			String path = PATHS.get(place);
			if (path == null) {
				System.out.println("path not found: " + place);
			} else {
				System.out.println("goto " + path);
				cmdline.executeCmd("halt;" + path + ";prepare_kill");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
				cmdline.executeCmd("watch " + npc + " -1");
			}
		}
		return true;
	}
}
