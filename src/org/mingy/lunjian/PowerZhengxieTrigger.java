package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class PowerZhengxieTrigger extends ZhengxieTrigger {

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
	protected void process(CommandLine cmdline, String good_npc, String bad_npc) {
		super.process(cmdline, good_npc, bad_npc);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			String path = PATHS.get(good_npc);
			if (path == null) {
				System.out.println("path not found: " + good_npc);
			} else {
				System.out.println("goto " + path);
				cmdline.executeCmd("halt;" + path);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
				cmdline.executeCmd("watch " + bad_npc + " -1");
			}
		}
	}
}
