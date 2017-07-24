package org.mingy.lunjian;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.AutoQuest.Area;
import org.mingy.lunjian.AutoQuest.Room;
import org.mingy.lunjian.AutoQuest.Seller;

public class AutoPartyTrigger implements Trigger {

	private static final Pattern PATTERN1 = Pattern
			.compile("^.+道：给我在.+内(战胜|杀|寻找)(.+)。任务所在地方好像是：(.+)你已经连续完成了\\d+个任务。你今天已完成(\\d+)/(\\d+)个任务。$");
	private static final Pattern PATTERN2 = Pattern
			.compile("^你现在的任务是(战胜|杀|寻找)(.+)。任务所在地方好像是：(.+)你还剩下.+去完成。你已经连续完成了\\d+个任务。你今天已完成(\\d+)/(\\d+)个任务。$");
	private static final Pattern PATTERN3 = Pattern
			.compile("^恭喜你完成师门任务，这是你连续完成的第\\d+个师门任务！");

	private AutoQuest quest;

	@Override
	public boolean match(CommandLine cmdline, String message, String type,
			long seq) {
		if (!"local".equals(type)) {
			return false;
		}
		if ("今天做的师门任务已过量，明天再来。".equals(message)) {
			cmdline.closeTrigger("party");
			return true;
		}
		Matcher m = PATTERN1.matcher(message);
		if (!m.find()) {
			m = PATTERN2.matcher(message);
			if (!m.find()) {
				m = PATTERN3.matcher(message);
				if (m.find()) {
					cmdline.sendCmd("home;family_quest");
					return true;
				} else {
					return false;
				}
			}
		}
		String action = m.group(1);
		String target = m.group(2);
		String place = m.group(3);
		int times = Integer.parseInt(m.group(4));
		int limit = Integer.parseInt(m.group(5));
		if (times + 1 == limit) {
			cmdline.sendCmd("vip finish_family");
		}
		process(cmdline, action, target, place);
		return true;
	}

	protected void process(CommandLine cmdline, String action, String target,
			String place) {
		if (this.quest == null) {
			AutoQuest quest = new AutoQuest(cmdline);
			if (!quest.init()) {
				cmdline.closeTrigger("party");
				return;
			}
			this.quest = quest;
		}
		String[] arr = place.split("\\-");
		if (arr[1].startsWith("*") && arr[1].endsWith("*")) {
			String[] tmp = new String[arr.length - 1];
			tmp[0] = arr[0];
			System.arraycopy(arr, 2, tmp, 1, arr.length - 2);
			arr = tmp;
		}
		String map = arr[0];
		String room = arr[1].trim();
		String npc, item;
		if ("寻找".equals(action)) {
			npc = arr.length > 2 ? arr[2].trim() : null;
			item = target.trim();
		} else {
			npc = target.trim();
			item = null;
		}
		boolean spec = false;
		if ("华山村".equals(map) && "黑狗".equals(npc)) {
			spec = true;
		} else if ("全真教".equals(map) && "小道童".equals(npc)) {
			spec = true;
		} else if ("古墓".equals(map) && "玉蜂".equals(npc)) {
			spec = true;
		} else if ("大旗门".equals(map) && "宾奴".equals(npc)) {
			spec = true;
		} else if ("桃花岛".equals(map) && "桃花岛弟子".equals(npc)) {
			spec = true;
		} else if ("大理".equals(map) && "采笋人".equals(npc)) {
			spec = true;
		}
		Area area = quest.getArea(map);
		if (area == null) {
			System.out.println("map not found: " + map);
			return;
		}
		List<Room> rooms;
		if (npc != null) {
			rooms = area.findNpc(npc);
		} else {
			rooms = area.findItem(item, false);
		}
		if (spec) {
			for (int i = rooms.size() - 1; i >= 0; i--) {
				if (!rooms.get(i).getName().equals(room)) {
					rooms.remove(i);
				}
			}
		}
		if (rooms.isEmpty()) {
			System.out.println("room not found: " + room);
			return;
		}
		AutoPartyTask task = new AutoPartyTask(cmdline, action, rooms, npc,
				item, 0, 500, null);
		cmdline.executeTask(task, 100);
	}

	@Override
	public void cleanup() {

	}

	private class AutoPartyTask extends TimerTaskDelegate {

		private CommandLine cmdline;
		private String action;
		private List<Room> rooms;
		private String npc;
		private String item;
		private int state;
		private Room current;

		public AutoPartyTask(CommandLine cmdline, String action,
				List<Room> rooms, String npc, String item, int state,
				long tick, Room current) {
			super(tick);
			this.cmdline = cmdline;
			this.action = action;
			this.rooms = rooms;
			this.npc = npc;
			this.item = item;
			this.state = state;
			this.current = current;
		}

		@Override
		protected void onTimer() throws Exception {
			if (state == 0) {
				if (rooms.isEmpty()) {
					System.out.println("no room found");
					cmdline.stopTask(this);
				} else {
					final Room room = rooms.remove(0);
					String path = current != null ? current.getPathTo(room)
							: room.getPath();
					System.out.println("path: " + path);
					try {
						Thread.sleep(Math.round(Math.random() * 500) + 800);
					} catch (InterruptedException e) {
						// ignore
					}
					Runnable callback = new Runnable() {
						@Override
						public void run() {
							AutoPartyTask task = new AutoPartyTask(cmdline,
									action, rooms, npc, item, 1, 500, room);
							cmdline.executeTask(task, 100);
						}
					};
					cmdline.walk(new String[] { path }, room.getName(), null,
							callback, 200);
				}
			} else if (state == 1) {
				if ("战胜".equals(action)) {
					String target = getTarget("npc", npc);
					if (target != null) {
						cmdline.sendCmd("fight " + target);
						state = 2;
					} else {
						state = 0;
					}
				} else if ("杀".equals(action)) {
					String target = getTarget("npc", npc);
					if (target != null) {
						cmdline.sendCmd("kill " + target);
						state = 3;
					} else {
						state = 0;
					}
				} else if (npc == null) {
					String target = getTarget("item", item);
					if (target != null) {
						cmdline.sendCmd("get " + target);
						cmdline.sendCmd("home;give " + getMasterId());
						cmdline.stopTask(this);
					} else {
						state = 0;
					}
				} else {
					String target = getTarget("npc", npc);
					if (target != null) {
						Seller seller = quest.getSeller(target);
						if (seller != null) {
							String id = seller.getItemId(item);
							if (id != null) {
								cmdline.sendCmd("buy " + id + " from " + target);
								cmdline.sendCmd("home;give " + getMasterId());
								cmdline.stopTask(this);
							} else {
								cmdline.sendCmd("kill " + target);
								state = 4;
							}
						} else {
							cmdline.sendCmd("kill " + target);
							state = 4;
						}
					} else {
						state = 0;
					}
				}
			} else if (state == 2) {
				if (cmdline.getCombatPosition() != null) {
					cmdline.fastCombat(false, false, true, null);
				} else {
					System.out.println("failed to fight");
					cmdline.stopTask(this);
				}
			} else if (state == 3) {
				if (cmdline.getCombatPosition() != null) {
					cmdline.fastCombat(false, false, true, null);
				} else {
					System.out.println("failed to kill");
					cmdline.stopTask(this);
				}
			} else if (state == 4) {
				if (cmdline.getCombatPosition() != null) {
					Runnable callback = new Runnable() {
						@Override
						public void run() {
							cmdline.sendCmd("home;give " + getMasterId());
						}
					};
					cmdline.fastCombat(false, true, true, callback);
				} else {
					System.out.println("failed to kill");
					cmdline.stopTask(this);
				}
			}
		}

		@SuppressWarnings("unchecked")
		private String getMasterId() {
			Map<String, Object> map = (Map<String, Object>) cmdline.js(
					cmdline.load("get_msgs.js"), "msg_attrs", false);
			return (String) map.get("master_id");
		}

		private String getTarget(String type, String name) {
			List<String[]> targets = cmdline.getTargets(type);
			for (String[] target : targets) {
				if (name.equals(target[1].trim())
						&& (!"npc".equals(type) || !target[0]
								.startsWith("bad_target_"))) {
					return target[0];
				}
			}
			return null;
		}
	}
}
