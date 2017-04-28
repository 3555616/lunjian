package org.mingy.lunjian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PowerZhengxieTrigger extends ZhengxieTrigger {

	private static final Map<String, String> PATHS = new HashMap<String, String>();

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

	@Override
	protected void process(final CommandLine cmdline, final String good_npc,
			final String bad_npc) {
		super.process(cmdline, good_npc, bad_npc);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			String path = PATHS.get(good_npc);
			if (path == null) {
				System.out.println("path not found: " + good_npc);
			} else {
				System.out.println("goto " + path);
				cmdline.executeCmd("halt");
				Runnable callback = null;
				if (Boolean.parseBoolean(cmdline.getProperty("zhengxie.auto"))) {
					callback = new Runnable() {
						@Override
						public void run() {
							String[] target = null;
							List<String[]> targets = cmdline.getTargets("npc");
							for (int i = targets.size() - 1; i > 0; i--) {
								if (bad_npc.equals(targets.get(i)[1])
										&& good_npc
												.equals(targets.get(i - 1)[1])) {
									target = new String[] { targets.get(i)[0],
											targets.get(i)[1],
											targets.get(i - 1)[0],
											targets.get(i - 1)[1] };
									break;
								}
							}
							if (target != null) {
								System.out.println("start auto zhengxie...");
								String priority = cmdline
										.getProperty("zhengxie.priority");
								if (priority == null || priority.length() == 0) {
									priority = "+-";
								}
								ZhengxieTask task = new ZhengxieTask(cmdline,
										target, priority, 0, 100);
								cmdline.executeTask(task, 100);
							} else {
								System.out.println("npc not found: " + bad_npc);
							}
						}
					};
				}
				cmdline.walk(new String[] { path }, null, null, callback, 400);
			}
		}
	}

	private static class ZhengxieTask extends TimerTaskDelegate {

		private CommandLine cmdline;
		private String[] target;
		private String priority;
		private int state;
		private String npc;

		public ZhengxieTask(CommandLine cmdline, String[] target,
				String priority, int state, int tick) {
			super(tick);
			this.cmdline = cmdline;
			this.target = target;
			this.priority = priority;
			this.state = state;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onTimer() throws Exception {
			if (state == 0) {
				cmdline.sendCmd("watch_vs " + target[0]);
				setTick(500);
				state = 1;
			} else if (state == 1) {
				int a = priority.indexOf('+');
				int b = priority.indexOf('-');
				if (a >= 0) {
					if ("段老大".equals(target[1])) {
						String pos = (String) cmdline.js(
								cmdline.load("get_combat_position.js"),
								target[1]);
						if (pos != null) {
							npc = target[0];
							state = 2;
						} else {
							state = 0;
						}
					} else if ("二娘".equals(target[1])) {
						String pos = (String) cmdline.js(
								cmdline.load("get_combat_position.js"),
								target[1]);
						if (pos != null) {
							Map<String, Object> map = (Map<String, Object>) cmdline
									.js(cmdline.load("get_msgs.js"),
											"msg_vs_info", false);
							long hp = Long.parseLong(String.valueOf(map
									.get("vs" + pos.substring(0, 1)
											+ "_max_kee" + pos.substring(1))));
							System.out.println(target[1] + ": " + hp);
							if (hp >= 429960 && hp < 450000) {
								npc = target[0];
								state = 2;
							}
						} else {
							state = 0;
						}
					}
				}
				if (b >= 0 && (npc == null || b < a)) {
					String pos = (String) cmdline.js(
							cmdline.load("get_combat_position.js"), target[3]);
					if (pos != null) {
						Map<String, Object> map = (Map<String, Object>) cmdline
								.js(cmdline.load("get_msgs.js"), "msg_vs_info",
										false);
						long hp = Long.parseLong(String.valueOf(map.get("vs"
								+ pos.substring(0, 1) + "_max_kee"
								+ pos.substring(1))));
						System.out.println(target[3] + ": " + hp);
						if (hp >= 700000 && hp < 750000) {
							npc = target[2];
							state = 2;
						}
					} else {
						state = 0;
					}
				}
				if (state == 1) {
					cmdline.sendCmd("escape;home");
					System.out.println("cancel.");
					cmdline.stopTask(this);
				} else {
					setTick(100);
				}
			} else if (state == 2) {
				cmdline.sendCmd("escape;kill " + npc);
				setTick(2000);
				state = 3;
			} else if (state == 3) {
				if (cmdline.getCombatPosition() != null) {
					if (Boolean.parseBoolean(cmdline
							.getProperty("zhengxie.auto.combat"))) {
						Runnable callback = new Runnable() {
							@Override
							public void run() {
								cmdline.sendCmd("home");
							}
						};
						cmdline.autoCombat(callback);
					}
				} else {
					System.out.println("failed to kill");
					cmdline.stopTask(this);
				}
			}
		}
	}
}
