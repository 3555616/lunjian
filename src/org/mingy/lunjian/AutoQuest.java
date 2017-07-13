package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.List;

public class AutoQuest {

	
	private static class Area {
		
		private List<Room> rooms;

		public Area(List<Room> rooms) {
			this.rooms = rooms;
		}
		
		public Room getRoom(int index) {
			return rooms.get(index);
		}
		
		public Room getRoom(String name) {
			for (Room room : rooms) {
				if (room.getName().equals(name)) {
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
			List<Integer> list = getPrevAll();
			String path = forward;
			for (int i = prev; i > 0;) {
				if (list.contains(i)) {
					for (int j = list.indexOf(i) - 1; j >= 0; j--) {
						Room room = area.getRoom(j);
						path = room.backward + ";" + path;
					}
					break;
				}
				Room room = area.getRoom(i);
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
