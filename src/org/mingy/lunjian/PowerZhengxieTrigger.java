package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class PowerZhengxieTrigger extends ZhengxieTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();

	static {
		PATHS.put("王铁匠", "fly 1;e;n;n;w");
		PATHS.put("杨掌柜", "fly 1;e;n;n;n;w");
		PATHS.put("柳绘心", "fly 1;e;n;e;e;e;e;n");
		PATHS.put("客商", "fly 2;n;n;e");
		PATHS.put("柳小花", "fly 2;n;n;n;n;w;s;w");
		PATHS.put("卖花姑娘", "fly 2;n;n;n;n;n;n;n");
		PATHS.put("刘守财", "fly 2;n;n;n;n;n;n;n;e");
		PATHS.put("方老板", "fly 3;s;s;e");
		PATHS.put("朱老伯", "fly 3;s;s;w");
		PATHS.put("方寡妇", "fly 3;s;s;w;n");
	}

	@Override
	protected void process(CommandLine cmdline, String good_npc, String bad_npc) {
		super.process(cmdline, good_npc, bad_npc);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			String path = PATHS.get(good_npc);
			if (path == null) {
				System.out.println("path not found: " + good_npc);
			} else {
				System.out.println("goto " + path);
				cmdline.executeCmd("halt");
				cmdline.walk(new String[] { path }, null, null, "watch "
						+ bad_npc + " -1", 400);
			}
		}
	}
}
