package org.mingy.lunjian;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PvpCombatTask extends TimerTask {

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
			Pattern.compile("^(.*)在旁边开始观看这场战斗！$"),
			Pattern.compile("^(.*)一看势头不对，溜了！$"),
			Pattern.compile("^(.*)一看势头不对想要逃跑，结果(.*)一转身就挡在了前面！$"),
			Pattern.compile("^(.*)深深吸了几口气，脸色看起来好多了。$"),
			Pattern.compile("^(.*)双目赤红，四处寻找目标攻击！！$"),
			Pattern.compile("^(.*)手脚速度加快，处于极度敏捷之中！$"),
			Pattern.compile("^(.*)全身衣裳鼓起，防御力极度增高！$"),
			Pattern.compile("^(.*)双掌随意游动，将(.*)的攻击之势直接攻向了自身！$"),
			Pattern.compile("^(.*)施展凌波微步，(.*)顿时被迷惑，手下绵软无力！$"),
			Pattern.compile("^(.*)只觉得脸上一阵痒痛，用手一抹，又没发现什么东西。$"),
			Pattern.compile("^(.*)手脚无力，出手毫无力气……$"),
			Pattern.compile("^(.*)手脚迟缓，处于极度迟钝之中！$"),
			Pattern.compile("^(.*)头昏目眩，几乎无法动弹……$"),
			Pattern.compile("^(.*)脸上泛起一阵绿光，身子不由自主地摇晃了一下……$"),
			Pattern.compile("^(.*)打了个寒颤，眼前迷乎了一下。$"),
			Pattern.compile("^(.*)的身子突然晃了两晃，牙关格格地响了起来。$") };

	private static Pattern[] COMBO_ATTACK_PATTERNS = new Pattern[] {
			Pattern.compile("^(.*)招式之间组合成了更为凌厉的攻势！$"),
			Pattern.compile("^(.*)这几招配合起来，威力更为惊人！$"),
			Pattern.compile("^(.*)将招式连成一片，令(.*)眼花缭乱！$")};

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

	private static Pattern SKILL_CHAIN_PATTERN = Pattern
			.compile("^\\-\\-(.*)\\-\\-(.*)\\-\\-$");
	
	private static String[] AUTO_ATTACK_NPCS = new String[] { "段老大", "二娘",
			"岳老三", "云老四", "剧盗", "流寇", "恶棍", "王铁匠", "杨掌柜", "柳绘心", "客商", "柳小花",
			"卖花姑娘", "刘守财", "方老板", "朱老伯", "方寡妇", "无一", "铁二", "追三", "冷四" };
	
	private static Map<String, String[]> SKILL_MAP = new HashMap<String, String[]>();
	
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

	static {
		SKILL_MAP.put("九天龙吟剑法", new String[] {"排云掌法","如来神掌"});
		SKILL_MAP.put("覆雨剑法", new String[] {"排云掌法","如来神掌"});
		SKILL_MAP.put("织冰剑法", new String[] {"排云掌法","如来神掌"});
		SKILL_MAP.put("排云掌法", new String[] {"雪饮狂刀","翻云刀法"});
		SKILL_MAP.put("如来神掌", new String[] {"雪饮狂刀","翻云刀法"});
		SKILL_MAP.put("雪饮狂刀", new String[] {"九天龙吟剑法","覆雨剑法","织冰剑法"});
		SKILL_MAP.put("翻云刀法", new String[] {"九天龙吟剑法","覆雨剑法","织冰剑法"});
	}

	private CommandLine cmdline;
	private String[] performs;
	private String[] dodges;
	private Part part;
	
	public PvpCombatTask(CommandLine cmdline) {
		this.cmdline = cmdline;
	}
	
	public boolean init() {
		String str = cmdline.getProperty("pk.perform");
		if (str != null && str.trim().length() > 0) {
			performs = str.trim().split(",");
		} else {
			System.out.println("property pk.perform not set");
			return false;
		}
		str = cmdline.getProperty("pk.dodge");
		if (str != null && str.trim().length() > 0) {
			dodges = str.trim().split(",");
		} else {
			System.out.println("property pk.dodge not set");
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			Map<String, Object> result = (Map<String, Object>) cmdline
					.js(cmdline.load("get_combat_msgs.js"));
			if (result == null) {
				part = null;
				return;
			}
			List<String> msgs = (List<String>) result.get("msgs");
			//for (String msg : msgs) {
			//	System.out.println(msg);
			//}
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
			boolean done = false;
			if (!msgs.isEmpty()) {
				if (part == null) {
					part = new Part();
				}
				Stack<String> skills = new Stack<String>();
				for (String msg : msgs) {
					Matcher m = SKILL_CHAIN_PATTERN.matcher(msg);
					if (m.find()) {
						skills.push(m.group(2));
						skills.push(m.group(1));
						System.out.println("[VS] " + m.group());
					} else {
						PoZhao p = checkPoZhao(msg);
						if (p != null) {
							Boolean last = part.attacker != null ? part.attacker_in_my_side : null;
							if ("你".equals(p.attacker)) {
								p.attacker = (String) result.get("me");
							} else if ("你".equals(p.defender)) {
								p.defender = (String) result.get("me");
							}
							System.out.println("[VS] " + p.attacker
									+ " po " + p.defender + " "
									+ (p.success ? "ok" : "fail"));
							part.attacker = p.attacker;
							part.attack_success = p.success;
							part.combo_attack = false;
							part.attacker_is_friend = isFriend(part.attacker);
							part.attacker_in_my_side = inMySide(part.attacker, result);
							if (last == null || !last.equals(part.attacker_in_my_side)) {
								part.skills.clear();
								part.performed = false;
							}
							if (!part.attacker_is_friend && !part.attacker_in_my_side && part.attack_success) {
								part.skills.addAll(skills);
							}
							skills.clear();
						} else {
							for (Pattern pattern : COMBO_ATTACK_PATTERNS) {
								m = pattern.matcher(msg);
								if (m.find()) {
									Boolean last = part.attacker != null ? part.attacker_in_my_side : null;
									part.attacker = m.group(1);
									part.attack_success = true;
									part.combo_attack = true;
									if ("你".equals(part.attacker)) {
										part.attacker = (String) result.get("me");
									}
									System.out.println("[VS] " + part.attacker
											+ " attack combo");
									part.attacker_is_friend = isFriend(part.attacker);
									part.attacker_in_my_side = inMySide(part.attacker, result);
									if (last == null || !last.equals(part.attacker_in_my_side)) {
										part.skills.clear();
										part.performed = false;
									}
									if (!part.attacker_is_friend && !part.attacker_in_my_side && part.attack_success) {
										part.skills.addAll(skills);
									}
									skills.clear();
									break;
								}
							}
						}
					}
				}
			}
			if (part != null && !part.performed) {
				if (!part.attacker_in_my_side && !part.attacker_is_friend && part.attack_success) {
					List<String> pfms = (List<String>) result.get("pfms");
					while (!part.performed && !part.skills.isEmpty()) {
						String[] choose = SKILL_MAP.get(part.skills.pop());
						if (choose != null) {
							for (String pfm : choose) {
								int i = pfms.indexOf(pfm);
								if (i >= 0) {
									System.out.println("[VS] perform " + pfm);
									cmdline.sendCmd("playskill " + (i + 1));
									part.performed = true;
									done = true;
									break;
								}
							}
						}
					}
				}
				if (!part.performed
						&& ((!part.attacker_in_my_side
								&& !part.attacker_is_friend && part.attack_success) || (part.attacker_in_my_side
								&& part.attacker_is_friend && !part.attack_success))) {
					boolean ignore = false;
					if (!part.combo_attack) {
						for (String npc : AUTO_ATTACK_NPCS) {
							if (npc.equals(part.attacker)) {
								ignore = true;
								break;
							}
						}
					}
					if (!ignore) {
						List<String> pfms = (List<String>) result.get("pfms");
						for (String pfm : performs) {
							int i = pfms.indexOf(pfm);
							if (i >= 0) {
								System.out.println("[VS] perform " + pfm);
								cmdline.sendCmd("playskill " + (i + 1));
								part.performed = true;
								done = true;
								break;
							}
						}
					} else {
						part.performed = true;
					}
				}
			}
			if (!done
					&& Math.round(Double.parseDouble(String.valueOf(result
							.get("pt")))) >= 9) {
				boolean b = false;
				List<String> vs1 = (List<String>) result.get("vs1");
				List<String> vs2 = (List<String>) result.get("vs2");
				for (String npc : AUTO_ATTACK_NPCS) {
					if (vs1.contains(npc) || vs2.contains(npc)) {
						b = true;
						break;
					}
				}
				if (b) {
					int t1, t2;
					if (vs1.contains(result.get("me"))) {
						t1 = vs1.size();
						t2 = vs2.size();
					} else {
						t1 = vs2.size();
						t2 = vs1.size();
					}
					if (t1 - t2 > 1) {
						List<String> pfms = (List<String>) result
								.get("pfms");
						for (String pfm : dodges) {
							int i = pfms.indexOf(pfm);
							if (i >= 0) {
								System.out.println("[VS] perform " + pfm);
								cmdline.sendCmd("playskill " + (i + 1));
								done = true;
								break;
							}
						}
					}
				}
			}
			
			
/*				
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
				if (part != null) {
					for (String msg : msgs) {
						Matcher m = SKILL_CHAIN_PATTERN.matcher(msg);
						if (m.find()) {
							part.skill1.add(m.group(1));
							part.skill2.add(m.group(2));
							System.out.println("[VS] " + m.group());
						} else {
							PoZhao p = checkPoZhao(msg);
							if (p != null) {
								if ("你".equals(p.attacker)) {
									p.attacker = (String) result.get("me");
								} else if ("你".equals(p.defender)) {
									p.defender = (String) result.get("me");
								}
								System.out.println("[VS] " + p.attacker
										+ " po " + p.defender + " "
										+ (p.success ? "ok" : "fail"));
								List<String> vs = (List<String>) result
										.get("vs1");
								if (vs.contains(part.defender)
										&& vs.contains(p.attacker)) {
									part.attack_success = !p.success;
								} else {
									part.attack_success = p.success;
								}
							}
						}
					}
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			cmdline.stopTask(this);
		}
	}

/*
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
			part = null;
		}
	}*/

	private boolean isFriend(String name) {
		if ("你".equals(name)) {
			return true;
		}
		boolean ok = false;
		String include = cmdline.getProperty("friends.include");
		if (include != null && include.length() > 0) {
			for (String s : include.split(",")) {
				if (name.contains(s)) {
					ok = true;
					break;
				}
			}
		}
		if (ok) {
			String exclude = cmdline.getProperty("friends.exclude");
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
			p.attacker = m.group(2);
			p.defender = m.group(1);
			return p;
		}
		m = POZHAO_PATTERN2.matcher(msg);
		if (m.find()) {
			PoZhao p = new PoZhao();
			p.success = true;
			p.attacker = m.group(1);
			p.defender = m.group(2);
			return p;
		}
		m = POZHAO_PATTERN3.matcher(msg);
		if (m.find()) {
			PoZhao p = new PoZhao();
			p.success = true;
			p.attacker = m.group(2);
			p.defender = m.group(1);
			return p;
		}
		m = POZHAO_PATTERN4.matcher(msg);
		if (m.find()) {
			PoZhao p = new PoZhao();
			p.success = false;
			p.attacker = m.group(1);
			p.defender = m.group(2);
			return p;
		}
		m = POZHAO_PATTERN5.matcher(msg);
		if (m.find()) {
			PoZhao p = new PoZhao();
			p.success = false;
			p.attacker = m.group(2);
			p.defender = m.group(1);
			return p;
		}
		m = POZHAO_PATTERN6.matcher(msg);
		if (m.find()) {
			PoZhao p = new PoZhao();
			p.success = false;
			p.attacker = m.group(1);
			p.defender = m.group(2);
			return p;
		}
		return null;
	}

	private static class Part {
		List<String> msgs;
		String attacker;
		String defender;
		boolean attacker_is_friend;
		boolean defender_is_friend;
		boolean attacker_in_my_side;
		boolean attack_success;
		boolean combo_attack;
		Stack<String> skills = new Stack<String>();
		boolean performed;
	}

	private static class PoZhao {
		boolean success;
		String attacker;
		String defender;
	}
}
