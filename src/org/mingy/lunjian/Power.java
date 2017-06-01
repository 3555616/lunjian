package org.mingy.lunjian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class Power extends CommandLine {

	private List<Work> works;
	private List<WebDriver> webdrivers;

	public static void main(String[] args) throws Exception {
		if (args.length > 1 && "--no_power".equalsIgnoreCase(args[1])) {
			CommandLine cmdline = new CommandLine();
			cmdline.run(args);
		} else {
			Power cmdline = new Power();
			cmdline.run(args);
		}
	}

	@Override
	protected void start(String[] args) throws Exception {
		super.start(args);
		String hotkeys = properties.getProperty("hotkey.performs");
		HotkeyTask hotkeyTask = new HotkeyTask(webdriver,
				hotkeys != null ? hotkeys.trim() : null);
		timer.schedule(hotkeyTask, 1000, 3000);
		works = new ArrayList<Work>();
		works.add(new Work("work click maikuli", 5500));
		works.add(new Work("work click duancha", 10500));
		works.add(new Work("work click dalie", 301000));
		works.add(new Work("work click baobiao", 301000));
		works.add(new Work("work click maiyi", 301000));
		works.add(new Work("work click xuncheng", 301000));
		works.add(new Work("work click datufei", 301000));
		works.add(new Work("work click dalei", 301000));
		works.add(new Work("work click kangjijinbin", 301000));
		works.add(new Work("work click zhidaodiying", 301000));
		works.add(new Work("work click dantiaoqunmen", 301000));
		works.add(new Work("work click shenshanxiulian", 301000));
		works.add(new Work("work click jianmenlipai", 301000));
		works.add(new Work("public_op3", 301000));
		webdrivers = new ArrayList<WebDriver>();
		for (int i = 1;; i++) {
			String dummy = properties.getProperty("dummy" + i);
			if (dummy != null && dummy.trim().length() > 0) {
				WebDriver webdriver = openUrl(dummy);
				timer.schedule(new HotkeyTask(webdriver, null), 1000, 3000);
				webdrivers.add(webdriver);
			} else {
				break;
			}
		}
	}

	@Override
	protected void finish() throws Exception {
		super.finish();
		for (WebDriver webdriver : webdrivers) {
			webdriver.quit();
		}
	}

	@Override
	protected void registerTriggers() {
		super.registerTriggers();
		TriggerManager.register("youxia", PowerYouxiaTrigger.class);
		TriggerManager.register("location", YouxiaLocationTrigger.class);
		TriggerManager.register("qinglong", PowerQinglongTrigger.class);
		TriggerManager.register("zhengxie", PowerZhengxieTrigger.class);
		TriggerManager.register("baozang", PowerBaozangTrigger.class);
		TriggerManager.register("guanfu", PowerGuanfuTrigger.class);
		TriggerManager.register("taofan", PowerTaofanTrigger.class);
		TriggerManager.register("hongbao", PowerHongbaoTrigger.class);
	}

	@Override
	protected void execute(String line) throws IOException {
		if (line.startsWith("#loop ")) {
			line = line.substring(6).trim();
			if (line.length() > 0) {
				int interval = 500;
				int i = line.indexOf(' ');
				if (i >= 0) {
					try {
						interval = Integer.parseInt(line.substring(0, i));
						line = line.substring(i + 1).trim();
					} catch (NumberFormatException e) {
						// ignore
					}
				}
				if (line.length() > 0) {
					System.out.println("starting loop...");
					executeTask(new LoopTask(line), interval);
				}
			}
		} else if (line.startsWith("#kill ")) {
			String name = line.substring(6).trim();
			if (name.length() > 0) {
				System.out.println("starting auto kill...");
				// executeCmd("prepare_kill");
				executeTask(new KillTask(name), 200);
			}
		} else if (line.equals("#lc")) {
			System.out.println("starting loot corpse...");
			executeTask(new LootTask(), 200);
		} else if (line.equals("#work")) {
			System.out.println("starting auto work...");
			executeTask(new WorkTask(works), 1000);
		} else if (line.equals("#tianjiangu")) {
			String[] settings = properties.getProperty("continue.fight", "")
					.split(",");
			if (settings.length < 1) {
				System.out.println("property continue.fight not set");
			} else {
				String[] pfms = settings[0].split("\\|");
				int wait = settings.length > 1 && settings[1].length() > 0 ? Integer
						.parseInt(settings[1]) : 0;
				String heal = settings.length > 2 && settings[2].length() > 0 ? settings[2]
						: null;
				int safe = settings.length > 3 && settings[3].length() > 0 ? Integer
						.parseInt(settings[3]) : 0;
				if (wait < pfms.length * 20) {
					wait = pfms.length * 20;
				}
				System.out.println("starting tianjiangu combat...");
				executeTask(new TianjianguCombatTask(pfms, wait, heal, safe),
						500);
			}
		} else if (line.equals("#pk")) {
			System.out.println("starting auto pvp ...");
			executeTask(new PvpCombatTask(), 100);
		} else {
			super.execute(line);
		}
	}

	private class HotkeyTask extends TimerTask {

		private WebDriver webdriver;
		private Object[] args = new Object[0];

		public HotkeyTask(WebDriver webdriver, String hotkeys) {
			super();
			this.webdriver = webdriver;
			if (hotkeys != null && hotkeys.length() > 0) {
				String[] pfms = hotkeys.split(",");
				args = new Object[pfms.length];
				for (int i = 0; i < pfms.length; i++) {
					args[i] = pfms[i].trim();
				}
			}
		}

		@Override
		public void run() {
			try {
				((JavascriptExecutor) webdriver).executeScript(
						load("hotkeys.js"), args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class Work {

		String command;
		long cooldown;
		long lasttime;

		public Work(String command, long cooldown) {
			this.command = command;
			this.cooldown = cooldown;
		}
	}

	private class WorkTask extends TimerTask {

		private List<Work> works;

		private WorkTask(List<Work> works) {
			this.works = works;
		}

		@Override
		public void run() {
			try {
				long timestamp = System.currentTimeMillis();
				for (Work work : works) {
					if (timestamp - work.lasttime >= work.cooldown) {
						sendCmd(work.command);
						work.lasttime = timestamp;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class LoopTask extends TimerTask {

		private String originCmd;
		private ProcessedCommand processedCmd;

		public LoopTask(String cmd) {
			originCmd = cmd;
		}

		@Override
		public void run() {
			try {
				if (processedCmd == null) {
					ProcessedCommand pc = processCmd(originCmd);
					if (pc.command != null) {
						processedCmd = pc;
					}
				}
				if (processedCmd != null) {
					sendCmd(processedCmd.command);
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class KillTask extends TimerTask {

		private String name;
		private int state;

		public KillTask(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			try {
				if (state == 0) {
					ProcessedCommand pc = processCmd("kill " + name);
					if (pc.command != null) {
						sendCmd(pc.command);
						state = 1;
					}
				} else if (state == 1) {
					state = getCombatPosition() != null ? 2 : 0;
				} else if (state == 2) {
					if (isCombatOver()) {
						state = 3;
					}
				} else if (state == 3) {
					String[] corpses = findTargets("item", "corpse");
					if (corpses.length > 0) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// ignore
						}
						sendCmd("get " + corpses[corpses.length - 1]);
						state = 4;
						System.out.println("ok!");
						stopTask(this);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class LootTask extends TimerTask {

		private int state;

		@Override
		public void run() {
			try {
				if (state == 0) {
					if (isCombatOver()) {
						state = 1;
					}
				} else if (state == 1) {
					String[] corpses = findTargets("item", "corpse");
					if (corpses.length > 0) {
						StringBuilder sb = new StringBuilder();
						for (String corpse : corpses) {
							if (sb.length() > 0) {
								sb.append(';');
							}
							sb.append("get " + corpse);
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// ignore
						}
						sendCmd(sb.toString());
						state = 2;
						System.out.println("ok!");
						stopTask(this);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private class TianjianguCombatTask extends TimerTask {

		private int waitPoint;
		private String[] performs;
		private String heal;
		private int safeHp;
		private List<Object> context = new ArrayList<Object>(5);

		public TianjianguCombatTask(String[] performs, int waitPoint,
				String heal, int safeHp) {
			super();
			this.performs = performs;
			this.waitPoint = waitPoint;
			this.heal = heal;
			this.safeHp = safeHp;
			this.context.add(0);
			this.context.add(false);
			this.context.add(false);
			this.context.add(null);
			this.context.add(false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				List<Object> ctx = (List<Object>) js(load("continue_fight.js"),
						performs, waitPoint, heal, safeHp, 0, null, context);
				if (ctx != null) {
					context = ctx;
					if (context.get(3) != null) {
						context.set(3, null);
					}
				} else {
					if ((Boolean) context.get(4)) {
						context.set(0, 0);
						context.set(1, false);
						context.set(2, false);
						context.set(3, null);
						context.set(4, false);
						sendCmd("prev_combat");
					}
					List<String[]> targets = getTargets("npc");
					if (!targets.isEmpty()) {
						String npc = null;
						for (String[] target : targets) {
							if ("天剑谷卫士".equals(target[1])) {
								if (npc == null) {
									npc = target[0];
								}
							} else if ("天剑真身".equals(target[1])
									|| "天剑".equals(target[1])
									|| "虹风".equals(target[1])
									|| "虹雨".equals(target[1])
									|| "虹雷".equals(target[1])
									|| "虹电".equals(target[1])) {
								npc = target[0];
							}
						}
						if (npc != null) {
							sendCmd("kill " + npc);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}
	}

	private static Pattern[] PART_FINISH_PATTERNS = new Pattern[] {
			Pattern.compile("^（.*）$"),
			Pattern.compile("^(.*)顿时被冲开老远，失去了攻击之势！$"),
			Pattern.compile("^(.*)被(.*)的真气所迫，只好放弃攻击！$"),
			Pattern.compile("^(.*)衣裳鼓起，真气直接将(.*)逼开了！$"),
			Pattern.compile("^(.*)找到了闪躲的空间！$"),
			Pattern.compile("^(.*)朝边上一步闪开！$"),
			Pattern.compile("^面对(.*)的攻击，(.*)毫不为惧！$"),
			Pattern.compile("^但(.*)心有定力，并没有受到任何影响！$"),
			Pattern.compile("^(.*)被(.*)的身影所惑，一时失去了方向！$") };

	private static Pattern[] IGNORE_PATTERNS = new Pattern[] {
			Pattern.compile("^(.*)对著(.*)喝道：「.*」$"),
			Pattern.compile("^(.*)对著(.*)说道：.*！$"),
			Pattern.compile("^(.*)加入了战团！$"),
			Pattern.compile("^(.*)一看势头不对，溜了！$"),
			Pattern.compile("^(.*)一看势头不对想要逃跑，结果(.*)一转身就挡在了前面！$"),
			Pattern.compile("^(.*)双目赤红，四处寻找目标攻击！！$"),
			Pattern.compile("^(.*)手脚速度加快，处于极度敏捷之中！$"),
			Pattern.compile("^(.*)全身衣裳鼓起，防御力极度增高！$"),
			Pattern.compile("^(.*)手脚无力，出手毫无力气……$"),
			Pattern.compile("^(.*)手脚迟缓，处于极度迟钝之中！$"),
			Pattern.compile("^(.*)头昏目眩，几乎无法动弹……$"),
			Pattern.compile("^(.*)脸上泛起一阵绿光，身子不由自主地摇晃了一下……$"),
			Pattern.compile("^(.*)打了个寒颤，眼前迷乎了一下。$"),
			Pattern.compile("^(.*)的身子突然晃了两晃，牙关格格地响了起来。$") };

	private static Pattern POZHAO_PATTERN1 = Pattern
			.compile("^(.*)的招式尽数被(.*)所破！$");
	private static Pattern POZHAO_PATTERN2 = Pattern
			.compile("^(.*)这一招正好击向了(.*)的破绽！$");
	private static Pattern POZHAO_PATTERN3 = Pattern
			.compile("^(.*)一不留神，招式被(.*)所破！$");
	private static Pattern POZHAO_PATTERN4 = Pattern
			.compile("^(.*)的对攻无法击破(.*)的攻势，处于明显下风！$");
	private static Pattern POZHAO_PATTERN5 = Pattern
			.compile("^(.*)的招式并未有明显破绽，(.*)只好放弃对攻！$");
	private static Pattern POZHAO_PATTERN6 = Pattern
			.compile("^(.*)这一招并未奏效，仍被(.*)招式紧逼！$");

	private class PvpCombatTask extends TimerTask {
		// 你招式之间组合成了更为凌厉的攻势！
		// 你这几招配合起来，威力更为惊人！
		// 你将招式连成一片，令地府-摩诃王眼花缭乱！
		// 你使出“天邪神功”，一股内劲涌向店小二左手！
		// 你使出“天邪神功”，一股内劲涌向店小二后心！
		// 你使出“天邪神功”，一股内劲涌向逄义右耳！
		// 你使出“天邪神功”，一股内劲涌向店小二两肋！
		// 你使出“天邪神功”，一股内劲涌向店小二左肩！
		// 你使出“天邪神功”，一股内劲涌向店小二左腿！
		// 你使出“天邪神功”，一股内劲涌向店小二右臂！
		// 你使出“天邪神功”，一股内劲涌向店小二左脚！
		// 你使出“天邪神功”，一股内劲涌向店小二腰间！
		// 你使出“天邪神功”，一股内劲涌向店小二右脸！
		// 店小二使出“内功心法”，一股内劲涌向你小腹！
		// 店小二使出“内功心法”，一股内劲涌向你颈部！
		// 店小二使出“内功心法”，一股内劲涌向你头顶！
		// 但地府-无量王心有定力，并没有受到任何影响！
		// 地府-无量王被你的身影所惑，一时失去了方向！
		// 你使出“乾坤大挪移”，希望扰乱地府-无量王的视线！

		private Part part;

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				Map<String, Object> result = (Map<String, Object>) js(load("get_combat_msgs.js"));
				if (result == null) {
					part = null;
					return;
				}
				List<String> msgs = (List<String>) result.get("msgs");
				for (int i = msgs.size() - 1; i >= 0; i--) {
					String msg = msgs.get(i);
					for (Pattern pattern : IGNORE_PATTERNS) {
						if (pattern.matcher(msg).matches()) {
							msgs.remove(i);
							break;
						}
					}
				}
				for (int i = msgs.size() - 1; i >= 0; i--) {
					String msg = msgs.get(i);
					boolean matched = false;
					for (Pattern pattern : PART_FINISH_PATTERNS) {
						if (pattern.matcher(msg).matches()) {
							msgs = msgs.subList(i + 1, msgs.size());
							part = null;
							matched = true;
							break;
						}
					}
					if (matched) {
						break;
					}
				}
				if (!msgs.isEmpty()) {
					if (part == null) {
						part = new Part();
						part.msgs = msgs;
						initPart(result);
					} else {
						part.msgs = msgs;
					}
				}
				for (int i = msgs.size() - 1; i >= 0; i--) {
					String msg = msgs.get(i);
					PoZhao p = checkPoZhao(msg);
					if (p != null) {
						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				stopTask(this);
			}
		}

		@SuppressWarnings("unchecked")
		private void initPart(Map<String, Object> result) {
			List<String> vs = new ArrayList<String>();
			vs.addAll((List<String>) result.get("vs1"));
			vs.addAll((List<String>) result.get("vs2"));
			vs.add("你");
			int i = -1;
			for (String msg : part.msgs) {
				for (String name : vs) {
					int k = msg.indexOf(name);
					if (k >= 0) {
						if (part.defender == null) {
							part.defender = name;
							i = k;
						} else if (part.attacker == null) {
							if (k < i) {
								part.attacker = name;
							} else {
								part.attacker = part.defender;
								part.defender = name;
								i = k;
							}
							break;
						}
					}
				}
				if (part.attacker != null) {
					break;
				}
			}
			if ("你".equals(part.attacker)) {
				part.attacker = (String) result.get("me");
			} else if ("你".equals(part.defender)) {
				part.defender = (String) result.get("me");
			}
			if (part.defender != null) {
				System.out.println("[VS] " + part.attacker + " attack "
						+ part.defender);
				part.attack_success = true;
				part.attacker_is_friend = part.attacker != null ? isFriend(part.attacker)
						: false;
				part.defender_is_friend = isFriend(part.defender);
				part.attacker_in_my_side = !inMySide(part.defender, result);
			} else {
				System.out.println("[VS] attacker and defender can not detect");
			}
		}

		private boolean isFriend(String name) {
			if ("你".equals(name)) {
				return true;
			}
			boolean ok = false;
			String include = getProperty("friends.include");
			if (include != null && include.length() > 0) {
				for (String s : include.split(",")) {
					if (name.contains(s)) {
						ok = true;
						break;
					}
				}
			}
			if (ok) {
				String exclude = getProperty("friends.exclude");
				if (exclude != null && exclude.length() > 0) {
					for (String s : exclude.split(",")) {
						if (name.contains(s)) {
							ok = false;
							break;
						}
					}
				}
			}
			return ok;
		}

		@SuppressWarnings("unchecked")
		private boolean inMySide(String name, Map<String, Object> result) {
			if ("你".equals(name)) {
				return true;
			}
			String me = (String) result.get("me");
			List<String> vs = (List<String>) result.get("vs1");
			if (vs.contains(me) && vs.contains(name)) {
				return true;
			}
			vs = (List<String>) result.get("vs2");
			if (vs.contains(me) && vs.contains(name)) {
				return true;
			}
			return false;
		}

		private PoZhao checkPoZhao(String msg) {
			Matcher m = POZHAO_PATTERN1.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = true;
				p.attacker = m.group(1);
				p.defender = m.group(2);
				return p;
			}
			m = POZHAO_PATTERN2.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = true;
				p.attacker = m.group(2);
				p.defender = m.group(1);
				return p;
			}
			m = POZHAO_PATTERN3.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = true;
				p.attacker = m.group(1);
				p.defender = m.group(2);
				return p;
			}
			m = POZHAO_PATTERN4.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = false;
				p.attacker = m.group(2);
				p.defender = m.group(1);
				return p;
			}
			m = POZHAO_PATTERN5.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = false;
				p.attacker = m.group(1);
				p.defender = m.group(2);
				return p;
			}
			m = POZHAO_PATTERN6.matcher(msg);
			if (m.find()) {
				PoZhao p = new PoZhao();
				p.success = false;
				p.attacker = m.group(2);
				p.defender = m.group(1);
				return p;
			}
			return null;
		}
	}

	private static class Part {
		List<String> msgs;
		String attacker;
		String defender;
		boolean attacker_is_friend;
		boolean defender_is_friend;
		boolean attacker_in_my_side;
		boolean attack_success;
		String perform;
		int rank;
	}

	private static class PoZhao {
		boolean success;
		String attacker;
		String defender;
	}
}
