package org.mingy.lunjian;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QinglongTrigger implements Trigger {

	private static final Pattern PATTERN1 = Pattern
			.compile("^【系统】青龙会组织：(.*)正在(.*)施展力量，本会愿出(.*)的战利品奖励给本场战斗的最终获胜者。$");
	private static final Pattern PATTERN2 = Pattern
			.compile("^【系统】跨服：\\[(.*)\\](.*)逃到了跨服时空(.*)之中，青龙会组织悬赏(.*)惩治恶人，众位英雄快来诛杀。$");

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"system".equals(type)) {
			return false;
		}
		Matcher m = PATTERN1.matcher(message);
		if (m.find()) {
			String npc = m.group(1);
			String place = m.group(2);
			String reward = m.group(3);
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
				process(cmdline, npc, place, reward, !pass);
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
				process(cmdline, npc, place, reward, false);
				return true;
			}
		}
		return false;
	}

	protected void process(CommandLine cmdline, String npc, String place,
			String reward, boolean ignore) {
		int count = 0;
		String logfile = cmdline.getProperty("log.properties");
		if (logfile != null && logfile.length() > 0) {
			try {
				boolean kuafu = npc.startsWith("[");
				Properties properties = new Properties();
				properties.load(new FileInputStream(logfile));
				String s_count = properties
						.getProperty(kuafu ? "qinglong.kuafu.count"
								: "qinglong.count");
				count = s_count != null && s_count.length() > 0 ? Integer
						.parseInt(s_count) : 0;
				String s_timestamp = properties
						.getProperty(kuafu ? "qinglong.kuafu.timestamp"
								: "qinglong.timestamp");
				long timestamp = s_timestamp != null
						&& s_timestamp.length() > 0 ? Long
						.parseLong(s_timestamp) : 0;
				Calendar last = Calendar.getInstance();
				last.setTimeInMillis(timestamp);
				Calendar target = Calendar.getInstance();
				if (target.get(Calendar.HOUR_OF_DAY) < 6) {
					target.add(Calendar.DAY_OF_MONTH, -1);
				}
				target.set(Calendar.HOUR_OF_DAY, 6);
				target.set(Calendar.MINUTE, 0);
				target.set(Calendar.SECOND, 0);
				target.set(Calendar.MILLISECOND, 0);
				if (target.after(last) && Calendar.getInstance().after(target)) {
					count = 0;
				}
				count++;
				properties.setProperty(kuafu ? "qinglong.kuafu.timestamp"
						: "qinglong.timestamp", String.valueOf(System
						.currentTimeMillis()));
				properties.setProperty(kuafu ? "qinglong.kuafu.count"
						: "qinglong.count", String.valueOf(count));
				properties.store(new FileOutputStream(logfile), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String message = "[青龙] " + npc + " at " + place + " rewards " + reward;
		if (count > 0) {
			message += " (" + count + ")";
		}
		cmdline.notify(message, !ignore, true);
	}

	@Override
	public void cleanup() {

	}
}
