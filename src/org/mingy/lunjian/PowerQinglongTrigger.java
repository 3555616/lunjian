package org.mingy.lunjian;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerQinglongTrigger extends QinglongTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();
	private static final Map<String, String> GOOD_NPCS = new HashMap<String, String>();

	static {
		PATHS.put("打铁铺子", "fly 1;e;n;n;w");
		PATHS.put("桑邻药铺", "fly 1;e;n;n;n;w");
		PATHS.put("书房", "fly 1;e;n;e;e;e;e;n");
		PATHS.put("南市", "fly 2;n;n;e");
		PATHS.put("绣楼", "fly 2;n;n;n;n;w;s;w");
		PATHS.put("北大街", "fly 2;n;n;n;n;n;n;n");
		PATHS.put("钱庄", "fly 2;n;n;n;n;n;n;n;e");
		PATHS.put("杂货铺", "fly 3;s;s;e");
		PATHS.put("祠堂大门", "fly 3;s;s;w");
		PATHS.put("厅堂", "fly 3;s;s;w;n");
		GOOD_NPCS.put("打铁铺子", "王铁匠");
		GOOD_NPCS.put("桑邻药铺", "杨掌柜");
		GOOD_NPCS.put("书房", "柳绘心");
		GOOD_NPCS.put("南市", "客商");
		GOOD_NPCS.put("绣楼", "柳小花");
		GOOD_NPCS.put("北大街", "卖花姑娘");
		GOOD_NPCS.put("钱庄", "刘守财");
		GOOD_NPCS.put("杂货铺", "方老板");
		GOOD_NPCS.put("祠堂大门", "朱老伯");
		GOOD_NPCS.put("厅堂", "方寡妇");
	}

	@Override
	protected void process(final CommandLine cmdline, final String npc,
			String place, String reward, boolean ignore) {
		super.process(cmdline, npc, place, reward, ignore);
		if (!ignore
				&& !Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			String path = PATHS.get(place);
			if (path == null) {
				System.out.println("path not found: " + place);
			} else {
				cmdline.closeTrigger("zhengxie");
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
				final String good_npc = GOOD_NPCS.get(place);
				System.out.println("goto " + path);
				cmdline.executeCmd("halt;prepare_kill");
				final int priority;
				String str = cmdline.getProperty("qinglong.auto.priority");
				if ("++".equals(str)) {
					priority = 1;
				} else if (Boolean.parseBoolean(str)) {
					priority = 0;
				} else {
					priority = -1;
				}
				Runnable callback = null;
				if (Boolean.parseBoolean(cmdline.getProperty("qinglong.auto"))) {
					boolean pass = false;
					String items = cmdline.getProperty("qinglong.auto.items");
					if (items != null) {
						for (String item : items.split(",")) {
							if (reward.equals(item)) {
								pass = true;
								break;
							}
						}
					} else {
						pass = true;
					}
					if (pass) {
						callback = new Runnable() {
							@Override
							public void run() {
								List<String[]> list = new ArrayList<String[]>();
								List<String[]> targets = cmdline
										.getTargets("npc");
								for (int i = targets.size() - 1; i > 0; i--) {
									if (npc.equals(targets.get(i)[1])
											&& good_npc.equals(targets
													.get(i - 1)[1])) {
										list.add(new String[] {
												targets.get(i)[0],
												targets.get(i)[1],
												targets.get(i - 1)[0],
												targets.get(i - 1)[1] });
									}
								}
								if (!list.isEmpty()) {
									System.out
											.println("start auto qinglong...");
									QinglongTask task = new QinglongTask(
											cmdline, list, priority, 0, 100);
									cmdline.executeTask(task, 100);
								} else {
									System.out.println("npc not found: " + npc);
								}
							}
						};
					}
				}
				try {
					if (priority == 0) {
						Thread.sleep(Math.round(Math.random() * 300) + 200);
					} else if (priority < 0) {
						Thread.sleep(Math.round(Math.random() * 500) + 500);
					} else {
						Thread.sleep(Math.round(Math.random() * 200));
					}
				} catch (InterruptedException e) {
					// ignore
				}
				cmdline.walk(new String[] { path }, place, null, callback, 200);
			}
		}
	}

	private static class QinglongTask extends TimerTaskDelegate {

		private CommandLine cmdline;
		private List<String[]> targets;
		private int priority;
		private int state;
		private boolean autoCombat;
		private boolean backHome;

		public QinglongTask(CommandLine cmdline, List<String[]> targets,
				int priority, int state, int tick) {
			super(tick);
			this.cmdline = cmdline;
			this.targets = targets;
			this.priority = priority;
			this.state = state;
			this.autoCombat = Boolean.parseBoolean(cmdline
					.getProperty("qinglong.auto.combat"));
			this.backHome = Boolean.parseBoolean(cmdline
					.getProperty("qinglong.auto.home"));
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onTimer() throws Exception {
			if (state == 0) {
				state = 10;
			} else if (state == 1) {
				String[] target = targets.get(0);
				if ("恶棍".equals(target[1]) || "流寇".equals(target[1])
						|| "剧盗".equals(target[1])) {
					if (priority > 0) {
						cmdline.sendCmd("kill " + target[0] + ";kill "
								+ target[2]);
					} else {
						cmdline.sendCmd("kill " + target[0]);
					}
				} else {
					if (priority > 0) {
						cmdline.sendCmd("kill " + target[2] + ";kill "
								+ target[0]);
					} else {
						cmdline.sendCmd("kill " + target[2]);
					}
				}
				setTick(5000);
				state = 2;
			} else if (state == 2) {
				if (cmdline.getCombatPosition() != null) {
					if (autoCombat
							&& Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 6) {
						cmdline.fastCombat(false, false, true,
								backHome ? new Runnable() {
									@Override
									public void run() {
										cmdline.sendCmd("home");
									}
								} : null);
					} else {
						cmdline.stopTask(this);
					}
				} else {
					System.out.println("failed to kill");
					cmdline.stopTask(this);
				}
			} else if (state == 10) {
				if (!targets.isEmpty()) {
					String[] target = targets.get(0);
					cmdline.sendCmd("watch_vs " + target[0]);
					setTick(500);
					state = 11;
				} else {
					if (backHome) {
						cmdline.sendCmd("home");
					}
					System.out.println("failed to find npc");
					cmdline.stopTask(this);
				}
			} else if (state == 11) {
				String[] target = targets.get(0);
				String pos = (String) cmdline.js(
						cmdline.load("get_combat_position.js"), target[3]);
				cmdline.sendCmd("escape");
				setTick(100);
				if (pos != null) {
					Map<String, Object> map = (Map<String, Object>) cmdline.js(
							cmdline.load("get_msgs.js"), "msg_vs_info", false);
					long hp = Long.parseLong(String.valueOf(map.get("vs"
							+ pos.substring(0, 1) + "_max_kee"
							+ pos.substring(1))));
					if (hp > 50000000) {
						if (autoCombat
								&& Calendar.getInstance().get(
										Calendar.HOUR_OF_DAY) < 6) {
							if (backHome) {
								cmdline.sendCmd("home");
							}
							System.out.println("npc hp = " + hp);
							cmdline.stopTask(this);
						} else {
							state = 1;
						}
					} else if (hp > 750000) {
						state = 1;
					} else {
						targets.remove(0);
						state = 10;
					}
				} else {
					targets.remove(0);
					state = 10;
				}
			}
		}
	}
}
