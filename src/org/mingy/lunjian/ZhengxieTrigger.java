package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhengxieTrigger implements Trigger {

	private static final Pattern PATTERN = Pattern
			.compile("【系统】(段延庆|叶二娘|南海鳄神|云中鹤)对着(.*)(淫笑|叫道)");
	private static final Map<String, String> PATHS = new HashMap<String, String>();

	static {
		PATHS.put("王铁匠", "wangtiejiang");
		PATHS.put("杨掌柜", "yangzhanggui");
		PATHS.put("柳绘心", "liuhuixin");
		PATHS.put("客商", "keshang");
		PATHS.put("柳小花", "liuxiaohua");
		PATHS.put("卖花姑娘", "maihua");
		PATHS.put("刘守财", "liushoucai");
		PATHS.put("方老板", "fanglaoban");
		PATHS.put("朱老伯", "zhulaobo");
		PATHS.put("方寡妇", "fangguafu");
	}

	@Override
	public boolean match(CommandExecutor executor, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String bad_npc = m.group(1);
		String good_npc = m.group(2);
		executor.notify("[正邪] " + good_npc + " vs " + bad_npc);
		if (!executor.isFighting()) {
			String path = PATHS.get(good_npc);
			if (path == null) {
				System.out.println("path not found: " + good_npc);
			} else {
				System.out.println("goto " + path);
				executor.executeCmd(path);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
				executor.executeCmd("watch " + bad_npc);
			}
		}
		return true;
	}
}
