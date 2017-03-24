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
		LOCATIONS.put("饮风客栈", "雪亭镇");
		LOCATIONS.put("龙门石窟", "洛阳");
		LOCATIONS.put("华山村村口", "华山村");
		LOCATIONS.put("华山山脚", "华山");
		LOCATIONS.put("安定门", "扬州");
		LOCATIONS.put("树洞内部", "丐帮");
		LOCATIONS.put("乔阴县城北门", "乔阴县");
		LOCATIONS.put("十二盘", "峨眉山");
		LOCATIONS.put("大字岭", "恒山");
		LOCATIONS.put("林中小路", "武当山");
		LOCATIONS.put("竹林", "晚月庄");
		LOCATIONS.put("青石官道", "水烟阁");
		LOCATIONS.put("丛林山径", "少林寺");
		LOCATIONS.put("蜀道", "唐门");
		LOCATIONS.put("北郊", "青城山");
		LOCATIONS.put("青石大道", "逍遥林");
		LOCATIONS.put("朱雀门", "开封");
		LOCATIONS.put("小村", "光明顶");
		LOCATIONS.put("终南山路", "全真教");
		LOCATIONS.put("山路", "古墓");
		LOCATIONS.put("戈壁", "白驮山");
		LOCATIONS.put("太室阙", "嵩山");
		LOCATIONS.put("柳树林", "寒梅庄");
		LOCATIONS.put("岱宗坊", "泰山");
		LOCATIONS.put("小路", "大旗门");
		LOCATIONS.put("草原", "大昭寺");
		LOCATIONS.put("驿道", "魔教");
		LOCATIONS.put("天山下", "星宿海");
		LOCATIONS.put("无名山脚", "茅山");
		LOCATIONS.put("海滩", "桃花岛");
		LOCATIONS.put("羊肠小道", "铁雪山庄");
		LOCATIONS.put("回望桥", "慕容山庄");
		LOCATIONS.put("官道", "大理");
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
