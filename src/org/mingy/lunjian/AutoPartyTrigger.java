package org.mingy.lunjian;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoPartyTrigger implements Trigger {

	private static final Pattern PATTERN1 = Pattern
			.compile("^.+道：给我在.+内(战胜|杀|寻找)(.+)。任务所在地方好像是：(.+)你已经连续完成了\\d+个任务。你今天已完成(\\d+)/(\\d+)个任务。$");
	private static final Pattern PATTERN2 = Pattern
	.compile("^你现在的任务是(战胜|杀|寻找)(.+)。任务所在地方好像是：(.+)你还剩下.+去完成。你已经连续完成了\\d+个任务。你今天已完成(\\d+)/(\\d+)个任务。$");
	
	private AutoQuest quest;
	
	@Override
	public boolean match(CommandLine cmdline, String message, String type) {
		if (!"local".equals(type)) {
			return false;
		}
		Matcher m = PATTERN1.matcher(message);
		if (!m.find()) {
			m = PATTERN2.matcher(message);
			if (!m.find()) {
				return false;
			}
		}
		String action = m.group(1);
		String target = m.group(2);
		String place = m.group(3);
		int times = Integer.parseInt(m.group(4));
		int limit = Integer.parseInt(m.group(5));
		if (times + 1 == limit) {
			cmdline.sendCmd("vip finish_family");
		}
		process(cmdline, action, target, place);
		return true;
	}

	protected void process(CommandLine cmdline, String action, String target,
			String place) {
		if (this.quest == null) {
			AutoQuest quest = new AutoQuest(cmdline);
			if (!quest.init()) {
				cmdline.closeTrigger("party");
				return;
			}
			this.quest = quest;
		}
		System.out.println(action);
		System.out.println(target);
		System.out.println(place);
	}

	@Override
	public void cleanup() {
		
	}
}
