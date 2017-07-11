package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mingy.lunjian.skills.Skills;

public class NewPvpCombatTask extends TimerTask {

	private static Pattern[] COMBO_ATTACK_PATTERNS = new Pattern[] {
			Pattern.compile("^(.*)招式之间组合成了更为凌厉的攻势！$"),
			Pattern.compile("^(.*)这几招配合起来，威力更为惊人！$"),
			Pattern.compile("^(.*)将招式连成一片，令(.*)眼花缭乱！$") };

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

	private static Pattern USER_ID_PATTERN = Pattern.compile("^u(\\d+)");

	private static String[] PREFIX_COMBO = new String[] { "紧接着，", "迅疾无比，",
			"身形再转，" };
	private static String[] AUTO_ATTACK_NPCS = new String[] { "段老大", "二娘",
			"岳老三", "云老四", "剧盗", "流寇", "恶棍", "王铁匠", "杨掌柜", "柳绘心", "客商", "柳小花",
			"卖花姑娘", "刘守财", "方老板", "朱老伯", "方寡妇", "[1-5区]段老大", "[1-5区]二娘",
			"[1-5区]岳老三", "[1-5区]云老四", "[1-5区]无一", "[1-5区]铁二", "[1-5区]追三",
			"[1-5区]冷四" };
	private static String[] SKILL_CHAINS = new String[] { "九天龙吟剑法", "覆雨剑法",
			"织冰剑法", "排云掌法", "如来神掌", "雪饮狂刀", "翻云刀法", "飞刀绝技", "孔雀翎", "道种心魔经",
			"生生造化功", "幽影幻虚步", "万流归一" };

	private static Map<String, String[]> SKILL_MAP1 = new HashMap<String, String[]>();
	private static Map<String, String[]> SKILL_MAP2 = new HashMap<String, String[]>();

	static {
		SKILL_MAP1.put("九天龙吟剑法", new String[] { "排云掌法", "如来神掌" });
		SKILL_MAP1.put("覆雨剑法", new String[] { "排云掌法", "如来神掌" });
		SKILL_MAP1.put("织冰剑法", new String[] { "排云掌法", "如来神掌" });
		SKILL_MAP1.put("排云掌法", new String[] { "雪饮狂刀", "翻云刀法" });
		SKILL_MAP1.put("如来神掌", new String[] { "雪饮狂刀", "翻云刀法" });
		SKILL_MAP1.put("雪饮狂刀", new String[] { "九天龙吟剑法", "覆雨剑法", "织冰剑法" });
		SKILL_MAP1.put("翻云刀法", new String[] { "九天龙吟剑法", "覆雨剑法", "织冰剑法" });
		SKILL_MAP2.put("九天龙吟剑法", new String[] { "排云掌法", "雪饮狂刀" });
		SKILL_MAP2.put("覆雨剑法", new String[] { "翻云刀法", "如来神掌" });
		SKILL_MAP2.put("织冰剑法", new String[] { "孔雀翎", "飞刀绝技" });
		SKILL_MAP2.put("排云掌法", new String[] { "九天龙吟剑法", "雪饮狂刀" });
		SKILL_MAP2.put("如来神掌", new String[] { "覆雨剑法", "孔雀翎" });
		SKILL_MAP2.put("雪饮狂刀", new String[] { "九天龙吟剑法", "排云掌法" });
		SKILL_MAP2.put("翻云刀法", new String[] { "覆雨剑法", "飞刀绝技" });
		SKILL_MAP2.put("飞刀绝技", new String[] { "翻云刀法", "织冰剑法" });
		SKILL_MAP2.put("孔雀翎", new String[] { "如来神掌", "织冰剑法" });
	}

	private CommandLine cmdline;
	private Map<String, List<Posture>> all_skills = new HashMap<String, List<Posture>>();
	private String[] performs;
	private String[] dodges;
	private List<Part> parts = new ArrayList<Part>();

	public NewPvpCombatTask(CommandLine cmdline) {
		this.cmdline = cmdline;
	}

	public boolean init() {
		try {
			loadSkills();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		String str = cmdline.getProperty("pk.perform");
		if (str != null && str.trim().length() > 0) {
			performs = str.trim().split(",");
		} else {
			System.out.println("property pk.perform not set");
			performs = new String[0];
		}
		str = cmdline.getProperty("pk.dodge");
		if (str != null && str.trim().length() > 0) {
			dodges = str.trim().split(",");
		} else {
			System.out.println("property pk.dodge not set");
			dodges = new String[0];
		}
		return true;
	}

	private void loadSkills() throws IOException {
		all_skills.clear();
		for (String name : Skills.SKILL_FILES.keySet()) {
			InputStream in = CommandLine.class.getResourceAsStream("skills/"
					+ Skills.SKILL_FILES.get(name) + ".desc");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					in, "utf-8"));
			List<Posture> postures = new ArrayList<Posture>();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() > 0) {
					Posture posture = new Posture();
					StringBuilder sb = new StringBuilder("^");
					int i = 0, j = 0, k;
					while ((k = line.indexOf('{', j)) >= 0) {
						int l = line.indexOf('}', k + 1);
						if (l < 0) {
							break;
						}
						String s = line.substring(k, l + 1);
						sb.append(line.substring(j, k));
						if ("{$p}".equals(s)) {
							sb.append("(左手|右手|后心|左耳|右耳|两肋|左肩|右肩|左腿|右腿|左臂|右臂|腰间|左脸|右脸|小腹|颈部|头顶|左脚|右脚)");
						} else {
							sb.append("(.*)");
						}
						j = l + 1;
						++i;
						if (posture.source == 0 && "{$s}".equals(s)) {
							posture.source = i;
						} else if (posture.target == 0 && "{$t}".equals(s)) {
							posture.target = i;
						}
					}
					sb.append(line.substring(j)).append("$");
					posture.pattern = Pattern.compile(sb.toString(),
							Pattern.CANON_EQ | Pattern.UNICODE_CASE);
					posture.lines = line.split("\\\\n").length;
					postures.add(posture);
				}
			}
			reader.close();
			all_skills.put(name, postures);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			Map<String, Object> result = (Map<String, Object>) cmdline
					.js(cmdline.load("get_combat_info.js"));
			if (result == null) {
				parts.clear();
				return;
			}
			VsInfo me = createVsInfo((Map<String, Object>) result.get("me"));
			List<VsInfo> vs1 = new ArrayList<VsInfo>(4);
			for (Map<String, Object> map : (List<Map<String, Object>>) result
					.get("vs1")) {
				vs1.add(createVsInfo(map));
			}
			List<VsInfo> vs2 = new ArrayList<VsInfo>(4);
			for (Map<String, Object> map : (List<Map<String, Object>>) result
					.get("vs2")) {
				vs2.add(createVsInfo(map));
			}
			List<String> pfms = (List<String>) result.get("pfms");
			long time = System.currentTimeMillis();
			for (int i = parts.size() - 1; i >= 0; i--) {
				if (time - parts.get(i).alive > 2500) {
					parts.remove(i);
				}
			}
			List<String> msgs = (List<String>) result.get("msgs");
			Stack<String> skills = new Stack<String>();
			Part current = null;
			for (int i = 0; i < msgs.size(); i++) {
				String msg = msgs.get(i);
				Matcher m = SKILL_CHAIN_PATTERN.matcher(msg);
				if (m.find()) {
					// 技能链记录技能
					skills.push(m.group(2));
					skills.push(m.group(1));
					System.out.println("[VS] " + m.group());
				} else {
					PoZhao p = checkPoZhao(msg);
					if (p != null) {
						// 破招
						System.out.println("[VS] " + p.attacker + " po "
								+ p.defender + " "
								+ (p.success ? "ok" : "fail"));
						current = null;
						if (p.success) {
							if ("你".equals(p.attacker)) {
								Part part = null;
								for (int k = parts.size() - 1; k >= 0; k--) {
									if (p.defender
											.equals(parts.get(k).attacker)) {
										part = parts.remove(k);
										parts.add(part);
										break;
									}
								}
								if (part == null) {
									part = new Part();
									parts.add(part);
								}
								part.attacker = p.attacker;
								part.defender = p.defender;
								part.combo_attack = 0;
								part.skills.clear();
								part.skills.addAll(skills);
								part.alive = time;
							} else if ("你".equals(p.defender)) {
								Part part = null;
								for (int k = parts.size() - 1; k >= 0; k--) {
									if (p.attacker
											.equals(parts.get(k).defender)) {
										part = parts.remove(k);
										parts.add(part);
										break;
									}
								}
								if (part == null) {
									part = new Part();
									parts.add(part);
								}
								part.attacker = p.attacker;
								part.defender = p.defender;
								part.combo_attack = 0;
								part.skills.clear();
								part.skills.addAll(skills);
								part.alive = time;
							}
						} else {
							if ("你".equals(p.attacker)) {
								Part part = null;
								for (int k = parts.size() - 1; k >= 0; k--) {
									if (p.defender
											.equals(parts.get(k).attacker)) {
										part = parts.remove(k);
										parts.add(part);
										break;
									}
								}
								if (part == null) {
									part = new Part();
									parts.add(part);
								}
								part.attacker = p.defender;
								part.defender = p.attacker;
								part.alive = time;
							} else if ("你".equals(p.defender)) {
								Part part = null;
								for (int k = parts.size() - 1; k >= 0; k--) {
									if (p.attacker
											.equals(parts.get(k).defender)) {
										part = parts.remove(k);
										parts.add(part);
										break;
									}
								}
								if (part == null) {
									part = new Part();
									parts.add(part);
								}
								part.attacker = p.defender;
								part.defender = p.attacker;
								part.alive = time;
							}
						}
						skills.clear();
					} else {
						// 连续攻击检测
						boolean combo = false;
						for (Pattern pattern : COMBO_ATTACK_PATTERNS) {
							m = pattern.matcher(msg);
							if (m.find()) {
								String attacker = m.group(1);
								String defender = null;
								if (m.groupCount() > 1) {
									defender = m.group(2);
								} else if (current != null
										&& attacker.equals(current.attacker)) {
									defender = current.defender;
								}
								System.out.println("[VS] "
										+ attacker
										+ " attack "
										+ (defender != null ? defender + " "
												: "") + "combo");
								current = null;
								Part part = null;
								if ("你".equals(attacker)) {
									for (int k = parts.size() - 1; k >= 0; k--) {
										if ("你".equals(parts.get(k).attacker)
												&& (defender == null
														|| parts.get(k).defender == null || defender
														.equals(parts.get(k).defender))) {
											part = parts.remove(k);
											if (defender != null) {
												part.defender = defender;
											}
											parts.add(part);
											break;
										}
									}
								} else if ("你".equals(defender)) {
									for (int k = parts.size() - 1; k >= 0; k--) {
										if (attacker
												.equals(parts.get(k).attacker)
												&& "你".equals(parts.get(k).defender)) {
											part = parts.remove(k);
											parts.add(part);
											break;
										}
									}
								}
								if ("你".equals(attacker)
										|| "你".equals(defender)) {
									if (part == null) {
										part = new Part();
										part.attacker = attacker;
										part.defender = defender;
										parts.add(part);
									}
									part.combo_attack++;
									part.alive = time;
									part.skills.addAll(skills);
									skills.clear();
								}
								combo = true;
								break;
							}
						}
						if (!combo) {
							// 攻击技能检测
							String msg1 = msg.replace('\u00a0', ' ');
							for (String prefix : PREFIX_COMBO) {
								if (msg1.startsWith(prefix)) {
									msg1 = msg1.substring(prefix.length());
									if (current != null) {
										System.out
												.println("[VS] "
														+ current.attacker
														+ " attack"
														+ (current.defender != null ? " "
																+ current.defender
																: ""));
										if ("你".equals(current.attacker)
												|| "你".equals(current.defender)) {
											if (current.combo_attack == 0) {
												parts.clear();
											}
											parts.add(current);
										}
										current = null;
									}
									break;
								}
							}
							Part part = null;
							for (String name : all_skills.keySet()) {
								List<Posture> postures = all_skills.get(name);
								if (postures != null) {
									for (Posture posture : postures) {
										String text = msg1;
										if (posture.lines > 1) {
											if (msgs.size() - i < posture.lines) {
												continue;
											}
											StringBuilder sb = new StringBuilder(
													msg);
											for (int j = 1; j < posture.lines; j++) {
												sb.append("\n").append(
														msgs.get(i + j));
											}
											text = sb.toString();
										}
										m = posture.pattern.matcher(text);
										if (m.find()) {
											skills.add(name);
											// System.out.println("[VS] " +
											// name);
											part = new Part();
											if (posture.source > 0) {
												part.attacker = m
														.group(posture.source);
											}
											if (posture.target > 0) {
												part.defender = m
														.group(posture.target);
											}
											part.combo_attack = 0;
											part.alive = time;
											break;
										}
									}
								}
								if (part != null) {
									break;
								}
							}
							if (part != null) {
								if (current != null) {
									System.out.println("[VS] "
											+ current.attacker
											+ " attack"
											+ (current.defender != null ? " "
													+ current.defender : ""));
									if ("你".equals(current.attacker)
											|| "你".equals(current.defender)) {
										if (current.combo_attack == 0) {
											parts.clear();
										}
										parts.add(current);
									}
								}
								current = part;
							}
						}
					}
				}
			}
			if (current != null) {
				System.out.println("[VS] "
						+ current.attacker
						+ " attack"
						+ (current.defender != null ? " " + current.defender
								: ""));
				if ("你".equals(current.attacker)
						|| "你".equals(current.defender)) {
					if (current.combo_attack == 0) {
						parts.clear();
					}
					parts.add(current);
				}
			}
			int point = me.point;
			for (int i = 0; i < parts.size(); i++) {
				Part part = parts.get(i);
				if ("你".equals(part.defender)) {
					String pfm = perform(part.skills, pfms);
					if (pfm != null) {
						point = calcPoint(point, pfm);
						if (point >= 3) {
							boolean hasNext = false;
							for (int j = i + 1; j < parts.size(); j++) {
								if ("你".equals(parts.get(j).defender)) {
									hasNext = true;
									break;
								}
							}
							if (!hasNext
									&& !isFriend(part.attacker)
									&& (part.combo_attack > 1 || getHp(
											part.attacker, vs2) >= 100000)) {
								pfm = combo(pfm, pfms);
								point = calcPoint(point, pfm);
								break;
							}
						}
					} else {
						break;
					}
				}
			}
			if (point >= 3) {
				for (int i = 0; i < parts.size(); i++) {
					Part part = parts.get(i);
					if ("你".equals(part.attacker) && part.defender != null
							&& !isFriend(part.defender)
							&& isPlayer(part.defender, vs2)) {
						long hp = getHp(part.defender, vs2);
						if ((hp >= 100000 && part.combo_attack < 2)
								|| (hp >= 40000 && part.combo_attack == 0)) {
							String pfm = combo(part.skills, pfms);
							point = calcPoint(point, pfm);
						}
						break;
					}
				}
			}
			if (point >= 9) {
				boolean b = false;
				for (String npc : AUTO_ATTACK_NPCS) {
					for (VsInfo info : vs2) {
						if (npc.equals(info.name)) {
							Matcher m = USER_ID_PATTERN.matcher(info.id);
							if (!m.find()) {
								b = true;
								break;
							}
						}
					}
					if (b) {
						break;
					}
				}
				if (b) {
					if (vs1.size() - vs2.size() < 2) {
						for (String pfm : dodges) {
							int i = pfms.indexOf(pfm);
							if (i >= 0) {
								System.out.println("[VS] perform " + pfm);
								cmdline.sendCmd("playskill " + (i + 1));
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			cmdline.stopTask(this);
		}
	}
	
	private VsInfo createVsInfo(Map<String, Object> map) {
		VsInfo info = new VsInfo();
		info.id = (String) map.get("id");
		info.name = CommandLine.removeSGR((String) map.get("name"));
		info.qi = (Long) map.get("qi");
		info.max_qi = (Long) map.get("max_qi");
		info.point = ((Long) map.get("pt")).intValue();
		return info;
	}

	private String perform(Stack<String> skills, List<String> pfms) {
		while (!skills.isEmpty()) {
			String[] choose = SKILL_MAP1.get(skills.pop());
			if (choose != null) {
				for (String pfm : choose) {
					int i = pfms.indexOf(pfm);
					if (i >= 0) {
						System.out.println("[VS] perform " + pfm);
						cmdline.sendCmd("playskill " + (i + 1));
						return pfm;
					}
				}
			}
		}
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
				System.out.println("[VS] perform " + pfm);
				cmdline.sendCmd("playskill " + (i + 1));
				return pfm;
			}
		}
		return null;
	}

	private String combo(Stack<String> skills, List<String> pfms) {
		while (!skills.isEmpty()) {
			String[] choose = SKILL_MAP2.get(skills.pop());
			if (choose != null) {
				for (String pfm : choose) {
					int i = pfms.indexOf(pfm);
					if (i >= 0) {
						System.out.println("[VS] perform " + pfm);
						cmdline.sendCmd("playskill " + (i + 1));
						return pfm;
					}
				}
			}
		}
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
				System.out.println("[VS] perform " + pfm);
				cmdline.sendCmd("playskill " + (i + 1));
				return pfm;
			}
		}
		return null;
	}

	private String combo(String skill, List<String> pfms) {
		String[] choose = SKILL_MAP2.get(skill);
		if (choose != null) {
			for (String pfm : choose) {
				int i = pfms.indexOf(pfm);
				if (i >= 0) {
					System.out.println("[VS] perform " + pfm);
					cmdline.sendCmd("playskill " + (i + 1));
					return pfm;
				}
			}
		}
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
				System.out.println("[VS] perform " + pfm);
				cmdline.sendCmd("playskill " + (i + 1));
				return pfm;
			}
		}
		return null;
	}

	private int calcPoint(int point, String pfm) {
		if (pfm == null || pfm.length() == 0) {
			return point;
		}
		for (String skill : SKILL_CHAINS) {
			if (skill.equals(pfm)) {
				return point - 3;
			}
		}
		return point - 2;
	}

	private boolean isPlayer(String name, List<VsInfo> vs) {
		for (VsInfo info : vs) {
			if (name.equals(info.name)) {
				Matcher m = USER_ID_PATTERN.matcher(info.id);
				return m.find();
			}
		}
		return false;
	}

	private long getHp(String name, List<VsInfo> vs) {
		for (VsInfo info : vs) {
			if (name.equals(info.name)) {
				return info.qi;
			}
		}
		return 0;
	}

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
		String attacker;
		String defender;
		int combo_attack;
		Stack<String> skills = new Stack<String>();
		long alive;
	}

	private static class PoZhao {
		boolean success;
		String attacker;
		String defender;
	}

	private static class Posture {
		Pattern pattern;
		int lines;
		int source;
		int target;
	}
	
	private static class VsInfo {
		String id;
		String name;
		long qi;
		long max_qi;
		long neili;
		int point;
	}
}
