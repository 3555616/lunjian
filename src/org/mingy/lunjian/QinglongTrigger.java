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
	public boolean match(CommandExecutor executor, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		String reward = m.group(3);
		executor.notify("[青龙] " + npc + " at " + place + " rewards " + reward);
		if (!executor.isFighting()) {
			String path = PATHS.get(place);
			if (path == null) {
				System.out.println("path not found: " + place);
			} else {
				System.out.println("goto " + path);
				executor.executeCmd(path);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
				executor.executeCmd("watch " + npc);
			}
		}
		return true;
	}
}
