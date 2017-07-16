package org.mingy.lunjian;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CleanZhengxieTrigger extends ZhengxieTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();

	private Timer timer;
	private LinkedList<CleanJob> jobs = new LinkedList<CleanJob>();
	private CommandLine cmdline;

	static {
		PATHS.put("王铁匠", "fly 1;e;n;n;w");
		PATHS.put("杨掌柜", "fly 1;e;n;n;n;w");
		PATHS.put("柳绘心", "fly 1;e;n;e;e;e;e;n");
		PATHS.put("客商", "fly 2;n;n;e");
		PATHS.put("柳小花", "fly 2;n;n;n;n;w;s;w");
		PATHS.put("卖花姑娘", "fly 2;n;n;n;n;n;n;n");
		PATHS.put("刘守财", "fly 2;n;n;n;n;n;n;n;e");
		PATHS.put("方老板", "fly 3;s;s;e");
		PATHS.put("朱老伯", "fly 3;s;s;w");
		PATHS.put("方寡妇", "fly 3;s;s;w;n");
	}

	public CleanZhengxieTrigger() {
		timer = new Timer();
		timer.schedule(new CleanTask(), 10000, 10000);
	}

	@Override
	public boolean match(CommandLine cmdline, String message, String type) {
		if ("local".equals(type) && "好在有保险卡，没有降低技能等级！".equals(message)) {
			cmdline.closeTrigger("clean");
			cmdline.sendCmd("home");
			return false;
		}
		return super.match(cmdline, message, type);
	}

	private class CleanTask extends TimerTask {

		@Override
		public void run() {
			if (jobs.isEmpty()) {
				return;
			}
			CleanJob job = jobs.getFirst();
			if (System.currentTimeMillis() - job.time < 15000 + Math.random() * 15000) {
				return;
			}
			if (cmdline.isFighting()) {
				return;
			}
			jobs.removeFirst();
			String path = PATHS.get(job.npc);
			if (path == null) {
				System.out.println("path not found: " + job.npc);
				return;
			}
			cmdline.sendCmd("escape");
			Runnable callback = new Runnable() {
				@Override
				public void run() {
					ZhengxieTask task = new ZhengxieTask(500);
					cmdline.executeTask(task, 100);
				}
			};
			cmdline.walk(new String[] { path }, null, null, callback, 500);
		}
	}

	private static class CleanJob {
		String npc;
		long time;
	}

	@Override
	protected void process(final CommandLine cmdline, final String good_npc,
			final String bad_npc) {
		super.process(cmdline, good_npc, bad_npc);
		this.cmdline = cmdline;
		if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6) {
			CleanJob job = new CleanJob();
			job.npc = good_npc;
			job.time = System.currentTimeMillis();
			jobs.add(job);
		}
	}

	@Override
	public void cleanup() {
		timer.cancel();
		super.cleanup();
	}

	private static final List<String> CHECK_USERS = Arrays.asList(new String[] {
			"u3019083", "u3004398", "u2612595", "u2627095", "u3085319",
			"u2760326", "u2622663", "u2963213", "u2734326", "u2631696",
			"u3087099" });

	private class ZhengxieTask extends TimerTaskDelegate {

		private int state;
		private List<String[]> targets;
		private int index;
		private String[] good_npc;

		public ZhengxieTask(int tick) {
			super(tick);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onTimer() throws Exception {
			if (state == 0) {
				targets = cmdline.getTargets("npc");
				for (int i = targets.size() - 1; i > 0; i--) {
					if (!targets.get(i)[0].startsWith("bad_target_")
							&& !targets.get(i)[0].startsWith("eren")) {
						targets.remove(i);
					}
				}
				index = 0;
				setTick(200);
				state = 1;
			} else if (state == 1) {
				while (index < targets.size() - 1) {
					if (targets.get(index)[0].startsWith("bad_target_")
							&& targets.get(index + 1)[0].startsWith("eren")) {
						boolean check = false;
						List<String[]> users = cmdline.getTargets("user");
						for (String[] user : users) {
							if (CHECK_USERS.contains(user[0])) {
								check = true;
								break;
							}
						}
						if (check) {
							break;
						}
						good_npc = targets.get(index);
						index += 2;
						state = 2;
						break;
					}
					index++;
				}
				if (state == 1) {
					setTick(3000);
					state = 5;
				}
			} else if (state == 2) {
				cmdline.sendCmd("watch_vs " + good_npc[0]);
				setTick(1000);
				state = 3;
			} else if (state == 3) {
				String pos = (String) cmdline.js(
						cmdline.load("get_combat_position.js"), good_npc[1]);
				if (pos != null) {
					Map<String, Object> map = (Map<String, Object>) cmdline.js(
							cmdline.load("get_msgs.js"), "msg_vs_info", false);
					long hp = Long.parseLong(String.valueOf(map.get("vs"
							+ pos.substring(0, 1) + "_max_kee"
							+ pos.substring(1))));
					if (hp < 1000000) {
						int count1 = 0, count2 = 0;
						for (String key : map.keySet()) {
							if ("vs1_pos1".equals(key)
									|| "vs1_pos2".equals(key)
									|| "vs1_pos3".equals(key)
									|| "vs1_pos4".equals(key)) {
								count1++;
							} else if ("vs2_pos1".equals(key)
									|| "vs2_pos2".equals(key)
									|| "vs2_pos3".equals(key)
									|| "vs2_pos4".equals(key)) {
								count2++;
							}
						}
						String target = null;
						if (count1 > 1 && count1 < 4 && count2 == 1) {
							target = (String) map.get("vs2_pos1");
						} else if (count2 > 1 && count2 < 4 && count1 == 1) {
							target = (String) map.get("vs1_pos1");
						} else if (count1 == 1 && count2 == 1) {
							target = Long.parseLong(String.valueOf(map
									.get("vs1_kee1"))) > Long.parseLong(String
									.valueOf(map.get("vs2_kee1"))) ? (String) map
									.get("vs2_pos1") : (String) map
									.get("vs1_pos1");
						}
						if (target != null
								&& (target.startsWith("bad_target_") || target
										.startsWith("eren"))) {
							cmdline.sendCmd("escape;kill " + target);
							setTick(1000);
							state = 4;
						} else {
							cmdline.sendCmd("escape");
							setTick(200);
							state = 1;
						}
					} else {
						cmdline.sendCmd("escape");
						setTick(200);
						state = 1;
					}
				} else {
					cmdline.sendCmd("escape");
					setTick(200);
					state = 1;
				}
			} else if (state == 4) {
				if (cmdline.getCombatPosition() != null) {
					Runnable callback = new Runnable() {
						@Override
						public void run() {
							ZhengxieTask task = new ZhengxieTask(500);
							cmdline.executeTask(task, 6000, 100);
						}
					};
					cmdline.fastCombat(true, false, true, callback);
				} else {
					System.out.println("failed to kill");
					cmdline.sendCmd("escape");
					setTick(200);
					state = 1;
				}
			} else if (state == 5) {
				cmdline.sendCmd("home");
				cmdline.stopTask(this);
			}
		}
	}
}
