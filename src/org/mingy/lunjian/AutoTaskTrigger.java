package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.AutoQuest.Area;
import org.mingy.lunjian.AutoQuest.Room;

public class AutoTaskTrigger implements Trigger {

	private static final Pattern[] GO_NPC_PATTERNS = new Pattern[] {
			Pattern.compile("^(.+)道：上次我不小心，竟然吃了(.+)\\-(.+)的亏，.+去杀了.?！$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)竟对我横眉瞪眼的，真想杀掉.?！$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)昨天捡到了我几十辆银子，拒不归还。钱是小事，但人品可不好。.+去杀了.?！$"),
			Pattern.compile("^(.+)道：我十分讨厌那(.+)\\-(.+)，.+替我去教训教训.?罢！$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)竟敢得罪我，.+去让.?尝尝厉害吧！$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)十分嚣张，去让.?见识见识厉害！$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)好大胆，竟敢拿走了我的.+，去替我要回来可好？$"),
			Pattern.compile("^(.+)道：我有个.+被(.+)\\-(.+)抢走了，去替我要回来吧！$"),
			Pattern.compile("^(.+)道：我有个事情想找(.+)\\-(.+)，.+可否替我走一趟？$"),
			Pattern.compile("^(.+)道：我想找(.+)\\-(.+)商量一点事情，.+替我找一下？$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)看上去好生奇怪，.+可前去打探一番。$"),
			Pattern.compile("^(.+)道：(.+)\\-(.+)鬼鬼祟祟的叫人生疑，.+去打探打探情况。$") };

	private static final Pattern[] GO_ROOM_PATTERNS = new Pattern[] { Pattern
			.compile("^(.+)道：我将.+藏在了(.+)\\-(.+)，.+可前去寻找。$") };

	private static final Pattern[] FIND_ITEM_PATTERNS = new Pattern[] {
			Pattern.compile("^(.+)道：突然想要一.?(.+)，.+可否帮忙找来？$"),
			Pattern.compile("^(.+)道：唉，好想要一.?(.+)啊。$") };

	private static final Pattern[] BACK_NPC_PATTERNS = new Pattern[] {
			Pattern.compile("^.+脚一蹬，死了。现在可以回去找(.+)交差了。$"),
			Pattern.compile("^.+说道：好，好，好，我知错了……你回去转告(.+)吧。$"),
			Pattern.compile("^.+说道：好，我知道了。你回去转告(.+)吧。$"),
			Pattern.compile("^.+老老实实将东西交了出来，现在可以回去找(.+)交差了。$"),
			Pattern.compile("^你一番打探，果然找到了一些线索，回去告诉(.+)吧。$"),
			Pattern.compile("^你一番搜索，果然找到了，回去告诉(.+)吧。$") };

	private static final Pattern FINISH_PATTERN = Pattern
			.compile("^完成谜题\\((\\d+)/(\\d+)\\)：(.+)的谜题，获得：经验x(\\d+)潜能x(\\d+)银两x(\\d+)");

	private AutoQuest quest;
	private Map<String, Room> tasks = new HashMap<String, Room>();

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"local".equals(type)) {
			return false;
		}
		if (message.startsWith("[谜题")) {
			return false;
		}
		if ("清空谜题任务成功。".equals(message)) {
			tasks.clear();
			return true;
		}
		Matcher m = FINISH_PATTERN.matcher(message);
		if (m.find()) {
			Room room = tasks.remove(m.group(3).trim());
			if (room != null) {
				cmdline.js(cmdline.load("add_task_link.js"), seq,
						room.getPath());
			}
			return true;
		}
		if (this.quest == null) {
			AutoQuest quest = new AutoQuest(cmdline);
			if (!quest.init()) {
				cmdline.closeTrigger("task");
				return false;
			}
			this.quest = quest;
		}
		for (int i = 0; i < GO_NPC_PATTERNS.length; i++) {
			Pattern pattern = GO_NPC_PATTERNS[i];
			m = pattern.matcher(message);
			if (m.find()) {
				List<Object> args = new ArrayList<Object>();
				args.add(seq);
				String source = m.group(1).trim();
				if (!"一个声音说".equals(source)) {
					String path = null;
					Area area = quest.getArea(cmdline.getMapId());
					if (area != null) {
						Room room = area.getRoom(cmdline.getRoom(), source);
						if (room != null) {
							tasks.put(source, room);
							path = room.getPath();
						}
					}
					args.add(path);
				}
				if (i == 7) {
					args.add(null);
				}
				String path = null;
				Area area = quest.getArea(m.group(2).trim());
				if (area != null) {
					List<Room> rooms = area.findNpc(m.group(3).trim());
					if (!rooms.isEmpty()) {
						path = rooms.get(0).getPath();
					}
				}
				args.add(path);
				cmdline.js(cmdline.load("add_task_link.js"), args.toArray());
				return true;
			}
		}
		for (int i = 0; i < GO_ROOM_PATTERNS.length; i++) {
			Pattern pattern = GO_ROOM_PATTERNS[i];
			m = pattern.matcher(message);
			if (m.find()) {
				List<Object> args = new ArrayList<Object>();
				args.add(seq);
				String source = m.group(1).trim();
				if (!"一个声音说".equals(source)) {
					String path = null;
					Area area = quest.getArea(cmdline.getMapId());
					if (area != null) {
						Room room = area.getRoom(cmdline.getRoom(), source);
						if (room != null) {
							tasks.put(source, room);
							path = room.getPath();
						}
					}
					args.add(path);
				}
				args.add(null);
				String path = null;
				Area area = quest.getArea(m.group(2).trim());
				if (area != null) {
					List<Room> rooms = area.findRoom(m.group(3).trim());
					if (!rooms.isEmpty()) {
						path = rooms.get(0).getPath();
					}
				}
				args.add(path);
				cmdline.js(cmdline.load("add_task_link.js"), args.toArray());
				return true;
			}
		}
		for (int i = 0; i < FIND_ITEM_PATTERNS.length; i++) {
			Pattern pattern = FIND_ITEM_PATTERNS[i];
			m = pattern.matcher(message);
			if (m.find()) {
				List<Object> args = new ArrayList<Object>();
				args.add(seq);
				String source = m.group(1).trim();
				Area area = quest.getArea(cmdline.getMapId());
				if (!"一个声音说".equals(source)) {
					String path = null;
					if (area != null) {
						Room room = area.getRoom(cmdline.getRoom(), source);
						if (room != null) {
							tasks.put(source, room);
							path = room.getPath();
						}
					}
					args.add(path);
				}
				String path = null;
				if (area != null) {
					List<Area> list = new ArrayList<Area>();
					list.add(area);
					int j = area.getIndex();
					area = quest.getArea(j - 1);
					if (area != null) {
						list.add(area);
					}
					area = quest.getArea(j + 1);
					if (area != null) {
						list.add(area);
					}
					area = quest.getArea(j - 2);
					if (area != null) {
						list.add(area);
					}
					area = quest.getArea(j + 2);
					if (area != null) {
						list.add(area);
					}
					for (Area a : list) {
						List<Room> rooms = a.findItem(m.group(2).trim(), true);
						if (!rooms.isEmpty()) {
							path = rooms.get(0).getPath();
							break;
						}
					}
				}
				args.add(path);
				cmdline.js(cmdline.load("add_task_link.js"), args.toArray());
				return true;
			}
		}
		for (int i = 0; i < BACK_NPC_PATTERNS.length; i++) {
			Pattern pattern = BACK_NPC_PATTERNS[i];
			m = pattern.matcher(message);
			if (m.find()) {
				String source = m.group(1).trim();
				Room room = tasks.get(source);
				if (room != null) {
					cmdline.js(cmdline.load("add_task_link.js"), seq,
							room.getPath());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void cleanup() {

	}
}
