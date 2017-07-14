package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AutoQuest {

	private static final List<String> MAP_NAMES = new ArrayList<String>();

	static {
		MAP_NAMES.add("雪亭镇");
		MAP_NAMES.add("洛阳");
		MAP_NAMES.add("华山村");
		MAP_NAMES.add("华山");
		MAP_NAMES.add("扬州");
		MAP_NAMES.add("丐帮");
		MAP_NAMES.add("乔阴县");
		MAP_NAMES.add("峨眉山");
		MAP_NAMES.add("恒山");
		MAP_NAMES.add("武当山");
		MAP_NAMES.add("晚月庄");
		MAP_NAMES.add("水烟阁");
		MAP_NAMES.add("少林寺");
		MAP_NAMES.add("唐门");
		MAP_NAMES.add("青城山");
		MAP_NAMES.add("逍遥林");
		MAP_NAMES.add("开封");
		MAP_NAMES.add("光明顶");
		MAP_NAMES.add("全真教");
		MAP_NAMES.add("古墓");
		MAP_NAMES.add("白驮山");
		MAP_NAMES.add("嵩山");
		MAP_NAMES.add("寒梅庄");
		MAP_NAMES.add("泰山");
		MAP_NAMES.add("大旗门");
		MAP_NAMES.add("大昭寺");
		MAP_NAMES.add("魔教");
		MAP_NAMES.add("星宿海");
		MAP_NAMES.add("茅山");
		MAP_NAMES.add("桃花岛");
		MAP_NAMES.add("铁雪山庄");
		MAP_NAMES.add("慕容山庄");
		MAP_NAMES.add("大理");
		MAP_NAMES.add("断剑山庄");
		MAP_NAMES.add("冰火岛");
		MAP_NAMES.add("侠客岛");
	}

	private static final Pattern[] KILL_GO_PATTERNS = new Pattern[] {
			Pattern.compile("^上次我不小心，竟然吃了(.+)-(.+)的亏，壮士去杀了他！$"),
			Pattern.compile("^(.+)-(.+)竟对我横眉瞪眼的，真想杀掉他！$"),
			Pattern.compile("^(.+)-(.+)昨天捡到了我几十辆银子，拒不归还。钱是小事，但人品可不好。壮士去杀了他！$") };

	private CommandLine cmdline;
	private Map<String, Area> maps = new HashMap<String, Area>();

	public AutoQuest(CommandLine cmdline) {
		this.cmdline = cmdline;
	}

	public boolean init() {
		for (int i = 0; i < MAP_NAMES.size(); i++) {
			InputStream in = CommandLine.class.getResourceAsStream("quests/"
					+ (i + 1) + ".map");
			if (in != null) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(in,
							"utf-8"));
					Area area = new Area();
					String line;
					while ((line = reader.readLine()) != null) {
						if (!area.addRoom(line.trim())) {
							return false;
						}
					}
					maps.put(MAP_NAMES.get(i), area);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}
		}
		return true;
	}

	private static class Area {

		private List<Room> rooms = new ArrayList<Room>();

		public boolean addRoom(String line) {
			String[] arr = line.split(",");
			if (arr.length < 4) {
				System.out.println("error: " + line);
				return false;
			}
			try {
				Room room = new Room(this, arr[0], Integer.parseInt(arr[1]),
						arr[2], arr[3], arr.length > 4 ? arr[4].split("\\|")
								: null);
				rooms.add(room);
				return true;
			} catch (Exception e) {
				System.out.println("error: " + line);
				return false;
			}
		}

		public Room getRoom(int index) {
			return rooms.get(index);
		}

		public List<Room> findRoom(String name) {
			List<Room> list = new ArrayList<Room>();
			for (Room room : rooms) {
				if (room.getName().equals(name)) {
					list.add(room);
				}
			}
			return list;
		}

		public List<Room> findNpc(String name) {
			List<Room> list = new ArrayList<Room>();
			for (Room room : rooms) {
				if (room.npc != null) {
					boolean b = false;
					for (String npc : room.npc) {
						if (npc.equals(name)) {
							b = true;
							break;
						}
					}
					if (b) {
						list.add(room);
					}
				}
			}
			return list;
		}
	}

	private static class Room {

		private Area area;
		private String name;
		private int prev;
		private String forward;
		private String backward;
		private String[] npc;

		public Room(Area area, String name, int prev, String forward,
				String backward, String[] npc) {
			this.area = area;
			this.name = name;
			this.prev = prev;
			this.forward = forward;
			this.backward = backward;
			this.npc = npc;
		}

		public String getPath() {
			String path = forward;
			for (int i = prev; i > 0;) {
				Room room = area.getRoom(i);
				path = room.forward + ";" + path;
				i = room.prev;
			}
			return path;
		}

		public String getPathTo(Room dest) {
			if (!dest.area.equals(area)) {
				return dest.getPath();
			}
			List<Integer> list = getPrevAll();
			String path = forward;
			for (int i = dest.prev; i > 0;) {
				if (list.contains(i)) {
					for (int j = list.indexOf(i) - 1; j >= 0; j--) {
						Room room = area.getRoom(j);
						path = room.backward + ";" + path;
					}
					break;
				}
				Room room = dest.area.getRoom(i);
				path = room.forward + ";" + path;
				i = room.prev;
			}
			return path;
		}

		private List<Integer> getPrevAll() {
			List<Integer> list = new ArrayList<Integer>();
			for (int i = prev; i > 0;) {
				list.add(i);
				i = area.getRoom(i).prev;
			}
			return list;
		}

		public String getName() {
			return name;
		}

		public String[] getNpc() {
			return npc;
		}
	}
}
