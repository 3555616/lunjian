package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaofanTrigger implements Trigger {

	private static final Map<String, String> LOCATIONS = new HashMap<String, String>();
	private static final Pattern PATTERN = Pattern
			.compile("跨服：\\[1\\-5区\\](.*)逃到了跨服时空(.*)之中，众位英雄快来诛杀。");

	static {
		LOCATIONS.put("雪亭镇", "饮风客栈");
		LOCATIONS.put("洛阳", "龙门石窟");
		LOCATIONS.put("华山村", "华山村村口");
		LOCATIONS.put("华山", "华山山脚");
		LOCATIONS.put("扬州", "安定门");
		LOCATIONS.put("丐帮", "树洞内部");
		LOCATIONS.put("乔阴县", "乔阴县城北门");
		LOCATIONS.put("峨眉山", "十二盘");
		LOCATIONS.put("恒山", "大字岭");
		LOCATIONS.put("武当山", "林中小路");
		LOCATIONS.put("晚月庄", "竹林");
		LOCATIONS.put("水烟阁", "青石官道");
		LOCATIONS.put("少林寺", "丛林山径");
		LOCATIONS.put("唐门", "蜀道");
		LOCATIONS.put("青城山", "北郊");
		LOCATIONS.put("逍遥林", "青石大道");
		LOCATIONS.put("开封", "朱雀门");
		LOCATIONS.put("光明顶", "小村");
		LOCATIONS.put("全真教", "终南山路");
		LOCATIONS.put("古墓", "山路");
		LOCATIONS.put("白驮山", "戈壁");
		LOCATIONS.put("嵩山", "太室阙");
		LOCATIONS.put("寒梅庄", "柳树林");
		LOCATIONS.put("泰山", "岱宗坊");
		LOCATIONS.put("大旗门", "小路");
		LOCATIONS.put("大昭寺", "草原");
		LOCATIONS.put("魔教", "驿道");
		LOCATIONS.put("星宿海", "天山下");
		LOCATIONS.put("茅山", "无名山脚");
		LOCATIONS.put("桃花岛", "海滩");
		LOCATIONS.put("铁雪山庄", "羊肠小道");
		LOCATIONS.put("慕容山庄", "回望桥");
		LOCATIONS.put("大理", "官道");
	}

	@Override
	public boolean match(CommandLine cmdline, String message) {
		Matcher m = PATTERN.matcher(message);
		if (!m.find()) {
			return false;
		}
		String npc = m.group(1);
		String place = m.group(2);
		String map = LOCATIONS.get(place);
		process(cmdline, npc, map, place);
		return true;
	}

	protected void process(CommandLine cmdline, String npc, String map,
			String place) {
		cmdline.notify("[逃犯] " + npc + " at " + map + " - " + place, false,
				true);
	}
}
