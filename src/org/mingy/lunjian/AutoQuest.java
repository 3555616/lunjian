package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
					Area area = new Area(i);
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
		if (index >= 0 && index < MAP_NAMES.size()) {
			return maps.get(MAP_NAMES.get(index));
		} else {
			return null;
		}
	}

	public Area getArea(MapId mapId) {
		int i = mapId != null ? mapId.ordinal() : 0;
		return i > 0 ? maps.get(MAP_NAMES.get(i - 1)) : null;
	}

	public Seller getSeller(String id) {
		return sellers.get(id);
	}

	public static class Area {

		private int index;
		private List<Room> rooms = new ArrayList<Room>();

		private Area(int index) {
			this.index = index;
		}

		public boolean addRoom(String line) {
			String[] arr = line.split(",");
			if (arr.length < 4) {
				System.out.println("error: " + line);
				return false;
			}
			try {
				Npc[] npc = null;
				if (arr.length > 4) {
					String[] ss = arr[4].trim().split("\\|");
					npc = new Npc[ss.length];
					for (int i = 0; i < ss.length; i++) {
						String s = ss[i].trim();
						int j = s.indexOf('[');
						Npc n;
						if (j < 0) {
							n = new Npc(s, null);
						} else {
							n = new Npc(s.substring(0, j).trim(), s
									.substring(j + 1, s.lastIndexOf(']'))
									.trim().split("/"));
						}
						npc[i] = n;
					}
				}
				Room room = new Room(this, arr[0].trim(),
						Integer.parseInt(arr[1].trim()), arr[2].trim(),
						arr[3].trim(), npc, arr.length > 5 ? arr[5].trim()
								.split("\\|") : null);
				rooms.add(room);
				return true;
			} catch (Exception e) {
				System.out.println("error: " + line);
				return false;
			}
		}

		public int getIndex() {
			return index;
		}

		public Room getRoom(int index) {
			return rooms.get(index);
		}

		public Room getRoom(String name, String npc) {
			List<Room> rooms = findRoom(name);
			for (Room room : rooms) {
				if (npc == null || room.hasNpc(npc)) {
					return room;
				}
			}
			return null;
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

		public List<Room> findItem(String name, boolean checkNpc) {
			List<Room> list = new ArrayList<Room>();
			for (Room room : rooms) {
				if (room.hasItem(name, checkNpc)) {
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
		private Npc[] npc;
		private String[] items;

		private Room(Area area, String name, int prev, String forward,
				String backward, Npc[] npc, String[] items) {
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
				for (Npc n : npc) {
					if (n.getName().equals(name)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean hasItem(String name, boolean checkNpc) {
			if (items != null) {
				for (String item : items) {
					if (item.equals(name)) {
						return true;
					}
				}
			}
			if (checkNpc && npc != null) {
				for (Npc n : npc) {
					String[] items = n.getItems();
					if (items != null) {
						for (String s : items) {
							if (s.equals(name)) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		public String getName() {
			return name;
		}

		public Npc[] getNpc() {
			return npc;
		}

		public String[] getItems() {
			return items;
		}
	}

	public static class Npc {

		private String name;
		private String[] items;

		private Npc(String name, String[] items) {
			this.name = name;
			this.items = items;
		}

		public String getName() {
			return name;
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
