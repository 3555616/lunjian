package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class PowerPintuTrigger extends PintuTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();

	static {
		PATHS.put("巫蛊王", "dishi;n");
		PATHS.put("夜千麟", "dishi;s");
		PATHS.put("百毒旗主", "dishi;w");
		PATHS.put("十方恶神", "dishi;e");
	}

	@Override
	protected void process(CommandLine cmdline, String bad_npc, String good_npc) {
		super.process(cmdline, bad_npc, good_npc);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			cmdline.executeCmd("halt");
			String path = PATHS.get(bad_npc);
			if (path == null) {
				System.out.println("path not found: " + bad_npc);
				return;
			}
			cmdline.walk(new String[] { path }, null, null, (String) null, 100);
		}
	}
}
