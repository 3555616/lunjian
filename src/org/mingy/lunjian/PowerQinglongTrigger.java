package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class PowerQinglongTrigger extends QinglongTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();

	static {
		PATHS.put("打铁铺子", "fly 1;e;n;n;w");
		PATHS.put("桑邻药铺", "fly 1;e;n;n;n;w");
		PATHS.put("书房", "fly 1;e;n;e;e;e;e;n");
		PATHS.put("南市", "fly 2;n;n;e");
		PATHS.put("绣楼", "fly 2;n;n;n;n;w;s;w");
		PATHS.put("北大街", "fly 2;n;n;n;n;n;n;n");
		PATHS.put("钱庄", "fly 2;n;n;n;n;n;n;n;e");
		PATHS.put("杂货铺", "fly 3;s;s;e");
		PATHS.put("祠堂大门", "fly 3;s;s;w");
		PATHS.put("厅堂", "fly 3;s;s;w;n");
	}

	@Override
	protected void process(CommandLine cmdline, String npc, String place,
			String reward, boolean ignore) {
		super.process(cmdline, npc, place, reward, ignore);
		if (!ignore
				&& !Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			String path = PATHS.get(place);
			if (path == null) {
				System.out.println("path not found: " + place);
			} else {
				System.out.println("goto " + path);
				cmdline.executeCmd("halt;prepare_kill");
				cmdline.walk(new String[] { path }, null, "watch " + npc
						+ " -1", 100);
			}
		}
	}
}
