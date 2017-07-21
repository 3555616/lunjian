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

/*
 店小二道：我有个事情想找雪亭镇-醉汉，壮士可否替我走一趟？
 苦力道：上次我不小心，竟然吃了雪亭镇-王铁匠的亏，壮士去杀了他！
 王铁匠脚一蹬，死了。现在可以回去找苦力交差了。
 刘安禄道：我将铁锤藏在了雪亭镇-城隍庙内室，壮士可前去寻找。
 你一番搜索，果然找到了，回去告诉刘安禄吧。
 庙祝道：雪亭镇-老农夫好大胆，竟敢拿走了我的铁手镯，去替我要回来可好？
 老农夫老老实实将东西交了出来，现在可以回去找庙祝交差了。
 庙祝道：我想找雪亭镇-庙祝商量一点事情，壮士替我找一下？
 庙祝说道：好，我知道了。你回去转告庙祝吧。
 疯狗道：雪亭镇-武馆弟子看上去好生奇怪，壮士可前去打探一番。
 武馆弟子道：我十分讨厌那雪亭镇-醉汉，壮士替我去教训教训他罢！
 醉汉说道：好，好，好，我知错了……你回去转告武馆弟子吧。
 农夫道：我有个铁手镯被洛阳-疯狗抢走了，去替我要回来吧！
 疯狗道：雪亭镇-农夫竟敢得罪我，壮士去让他尝尝厉害吧！
 魏无极道：突然想要一顶皮帽，壮士可否帮忙找来？
 刘安禄道：华山村-野兔竟对我横眉瞪眼的，真想杀掉他！
 李火狮道：雪亭镇-庙祝昨天捡到了我几十辆银子，拒不归还。钱是小事，但人品可不好。壮士去杀了他！
 李火狮道：唉，好想要一顶白缨冠啊。
 王铁匠道：雪亭镇-疯狗十分嚣张，去让他见识见识厉害！
 黑衣女子道：雪亭镇-老农夫鬼鬼祟祟的叫人生疑，婆婆去打探打探情况。
 */

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
		MAP_NAMES.add("明教");
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
	private Map<String, Seller> sellers = new HashMap<String, Seller>();

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
		InputStream in = CommandLine.class
				.getResourceAsStream("quests/seller.list");
		if (in != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
				Seller seller = null;
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						if (line.startsWith("[") && line.endsWith("]")) {
							seller = new Seller();
							sellers.put(line.substring(1, line.length() - 1)
									.trim(), seller);
						} else if (seller != null) {
							int i = line.indexOf(',');
							if (i >= 0) {
								seller.addItem(line.substring(0, i).trim(),
										line.substring(i + 1).trim());
							} else {
								System.out.println("error: " + line);
								return false;
							}
						} else {
							System.out.println("error: " + line);
							return false;
						}
					}
				}
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
		return true;
	}

	public Area getArea(String name) {
		return maps.get(name);
	}

	public Area getArea(int index) {
		return maps.get(MAP_NAMES.get(index));
	}

	public Seller getSeller(String id) {
		return sellers.get(id);
	}

	public static class Area {

		private List<Room> rooms = new ArrayList<Room>();

		private Area() {

		}

		public boolean addRoom(String line) {
			String[] arr = line.split(",");
			if (arr.length < 4) {
				System.out.println("error: " + line);
				return false;
			}
			try {
				Room room = new Room(this, arr[0].trim(),
						Integer.parseInt(arr[1].trim()), arr[2].trim(),
						arr[3].trim(), arr.length > 4 ? arr[4].trim().split(
								"\\|") : null, arr.length > 5 ? arr[5].trim()
								.split("\\|") : null);
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
				if (room.hasNpc(name)) {
					list.add(room);
				}
			}
			return list;
		}

		public List<Room> findItem(String name) {
			List<Room> list = new ArrayList<Room>();
			for (Room room : rooms) {
				if (room.hasItem(name)) {
					list.add(room);
				}
			}
			return list;
		}
	}

	public static class Room {

		private Area area;
		private String name;
		private int prev;
		private String forward;
		private String backward;
		private String[] npc;
		private String[] items;

		private Room(Area area, String name, int prev, String forward,
				String backward, String[] npc, String[] items) {
			this.area = area;
			this.name = name;
			this.prev = prev;
			this.forward = forward;
			this.backward = backward;
			this.npc = npc;
			this.items = items;
		}

		public String getPath() {
			String path = forward;
			for (int i = prev; i > 0;) {
				Room room = area.getRoom(i - 1);
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
			list.add(0, area.rooms.indexOf(this) + 1);
			String path = dest.forward;
			for (int i = dest.prev; i > 0;) {
				if (list.contains(i)) {
					for (int j = 0; j < list.indexOf(i); j++) {
						Room room = area.getRoom(list.get(j) - 1);
						path = room.backward + ";" + path;
					}
					break;
				}
				Room room = dest.area.getRoom(i - 1);
				path = room.forward + ";" + path;
				i = room.prev;
			}
			return path;
		}

		private List<Integer> getPrevAll() {
			List<Integer> list = new ArrayList<Integer>();
			for (int i = prev; i > 0;) {
				list.add(i);
				i = area.getRoom(i - 1).prev;
			}
			return list;
		}

		public boolean hasNpc(String name) {
			if (npc != null) {
				for (String n : npc) {
					if (n.equals(name)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean hasItem(String name) {
			if (items != null) {
				for (String item : items) {
					if (item.equals(name)) {
						return true;
					}
				}
			}
			return false;
		}

		public String getName() {
			return name;
		}

		public String[] getNpc() {
			return npc;
		}

		public String[] getItems() {
			return items;
		}
	}

	public static class Seller {

		private Map<String, String> list = new HashMap<String, String>();

		private Seller() {

		}

		private void addItem(String name, String id) {
			list.put(name, id);
		}

		public String getItemId(String name) {
			return list.get(name);
		}
	}
}
