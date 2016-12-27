package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class PowerQinglongTrigger extends QinglongTrigger {

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
	protected void process(CommandLine cmdline, String npc, String place,
			String reward, boolean ignore) {
		super.process(cmdline, npc, place, reward, ignore);
		if (!ignore && !cmdline.isFighting()) {
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
	}
}
