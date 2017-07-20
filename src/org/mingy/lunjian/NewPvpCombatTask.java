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

import org.mingy.lunjian.AutoQuest.Area;
import org.mingy.lunjian.AutoQuest.Room;
import org.mingy.lunjian.skills.Skills;

public class NewPvpCombatTask extends TimerTask {

	private static Pattern[] PART_FINISH_PATTERNS = new Pattern[] {
			Pattern.compile("^(.*)顿时被冲开老远，失去了攻击之势！$"),
			Pattern.compile("^(.*)被(.*)的真气所迫，只好放弃攻击！$"),
			Pattern.compile("^(.*)衣裳鼓起，真气直接将(.*)逼开了！$"),
			Pattern.compile("^(.*)找到了闪躲的空间！$"),
			Pattern.compile("^(.*)朝边上一步闪开！$"),
			Pattern.compile("^面对(.*)的攻击，(.*)毫不为惧！$"),
			Pattern.compile("^（(.*)似乎受了点轻伤，不过光从外表看不大出来。）$"),
			Pattern.compile("^（(.*)受了几处伤，不过似乎并不碍事。）$"),
			Pattern.compile("^（(.*)看起来可能受了点轻伤。）$"),
			Pattern.compile("^（(.*)受伤不轻，看起来状况并不太好。）$"),
			Pattern.compile("^（(.*)气息粗重，动作开始散乱，看来所受的伤着实不轻。）$"),
			Pattern.compile("^（(.*)已经伤痕累累，正在勉力支撑着不倒下去。）$"),
			Pattern.compile("^（(.*)受了相当重的伤，只怕会有生命危险。）$"),
			Pattern.compile("^（(.*)伤重之下已经难以支撑，眼看就要倒在地上。）$"),
			Pattern.compile("^（(.*)受伤过重，已经奄奄一息，命在旦夕了。）$"),
			Pattern.compile("^（(.*)受伤过重，已经有如风中残烛，随时都可能断气。）$") };

	private static Pattern TAIJI_PATTERN = Pattern
			.compile("^(.*)运起太极神功的“卸力诀”，将(.*)的力道卸去大半……$");
	private static Pattern DAMAGE_PATTERN = Pattern.compile("^（.*）$");

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
	private String vs_npc1;
	private String vs_npc2;
	private boolean npc_attack;
	private String npc_attack_target;
	private boolean is_first;
	private boolean in_fighting;
	private MapId mapId;
	private String room;

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
				vs_npc1 = null;
				vs_npc2 = null;
				npc_attack = false;
				npc_attack_target = null;
				is_first = true;
				if (in_fighting) {
					in_fighting = false;
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// ignore
					}
					cmdline.sendCmd("golook_room");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// ignore
					}
					if (cmdline.isKuafu() && mapId != null
							&& mapId.ordinal() > 0
							&& cmdline.getMapId() == MapId.kuafu) {
						System.out.println("try to back " + mapId.name() + "-"
								+ room);
						AutoQuest quest = new AutoQuest(cmdline);
						if (quest.init()) {
							Area area = quest.getArea(mapId.ordinal() - 1);
							if (area != null) {
								final List<Room> rooms = area.findRoom(room);
								if (!rooms.isEmpty()) {
									Runnable callback = new Runnable() {
										@Override
										public void run() {
											RecoveryTask task = new RecoveryTask(
													cmdline, rooms.get(0));
											cmdline.executeTask(task, 1000);
										}
									};
									cmdline.walk(
											new String[] { "jh 1;e;n;n;n;w" },
											"桑邻药铺", null, callback, 200);
								}
							}
						}
					}
					mapId = null;
					room = null;
				}
				return;
			}
			if (!in_fighting) {
				in_fighting = true;
				Map<String, Object> map = (Map<String, Object>) cmdline.js(
						cmdline.load("get_msgs.js"), "msg_room", false);
				String id = (String) map.get("map_id");
				if (id != null) {
					try {
						mapId = MapId.valueOf(id);
						room = CommandLine.removeSGR((String) map.get("short"));
					} catch (Exception e) {
						mapId = null;
					}
				}
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
				Part part = parts.get(i);
				if (time - part.alive > 2500) {
					System.out.println("[VS] " + part.attacker + " vs "
							+ part.defender + " timeout");
					if (isNpc(part.attacker, vs2) || isNpc(part.defender, vs2)) {
						vs_npc2 = null;
						npc_attack = false;
						npc_attack_target = null;
					}
					parts.remove(i);
				}
			}
			List<String> msgs = (List<String>) result.get("msgs");
			Stack<String> skills = new Stack<String>();
			Part current = null;
			for (int i = 0; i < msgs.size(); i++) {
				String msg = msgs.get(i);
				String[] r;
				Matcher m = TAIJI_PATTERN.matcher(msg);
				if (m.find()) {
					r = new String[] { m.group(1), m.group(2),
							m.group(1) + " damaged by " + m.group(2) };
					for (i = i + 1; i < msgs.size(); i++) {
						m = DAMAGE_PATTERN.matcher(msgs.get(i));
						if (m.find()) {
							i++;
							break;
						}
					}
				} else {
					r = checkPartFinish(msg);
				}
				if (r != null) {
					System.out.println("[VS] " + r[2]);
					if (vs_npc1 != null
							&& (vs_npc1.equals(r[0]) || vs_npc1.equals(r[1]) || !isNpc(
									vs_npc1, vs1))) {
						vs_npc1 = null;
					}
					if (vs_npc2 != null
							&& (vs_npc2.equals(r[0]) || vs_npc2.equals(r[1]) || !isNpc(
									vs_npc2, vs2))) {
						for (int j = 0; j < parts.size(); j++) {
							Part part = parts.get(j);
							if (vs_npc2.equals(part.attacker)
									|| vs_npc2.equals(part.defender)) {
								parts.remove(j);
								break;
							}
						}
						vs_npc2 = null;
						npc_attack = false;
						npc_attack_target = null;
					}
					if ("你".equals(r[0])) {
						for (int j = 0; j < parts.size(); j++) {
							Part part = parts.get(j);
							if ("你".equals(part.defender)
									&& (r[1] == null || r[1]
											.equals(part.attacker))) {
								if (isNpc(part.attacker, vs2)) {
									vs_npc2 = null;
									npc_attack = false;
									npc_attack_target = null;
								}
								parts.remove(j);
								break;
							}
						}
					} else if ("你".equals(r[1])) {
						for (int j = 0; j < parts.size(); j++) {
							Part part = parts.get(j);
							if ("你".equals(part.attacker)
									&& (r[0] == null || r[0]
											.equals(part.defender))) {
								if (isNpc(part.defender, vs2)) {
									vs_npc2 = null;
									npc_attack = false;
									npc_attack_target = null;
								}
								parts.remove(j);
								break;
							}
						}
					}
					is_first = false;
					continue;
				}
				m = SKILL_CHAIN_PATTERN.matcher(msg);
				if (m.find()) {
					// 技能链记录技能
					skills.push(m.group(2));
					skills.push(m.group(1));
					System.out.println("[VS] " + m.group());
					is_first = false;
					continue;
				}
				PoZhao p = checkPoZhao(msg);
				if (p != null) {
					// 破招
					System.out.println("[VS] " + p.attacker + " po "
							+ p.defender + " " + (p.success ? "ok" : "fail"));
					if (vs_npc1 == null) {
						if (isNpc(p.attacker, vs1) && p.success) {
							vs_npc1 = p.attacker;
						} else if (isNpc(p.defender, vs1)) {
							vs_npc1 = p.defender;
						}
					}
					if (vs_npc2 == null) {
						if (isNpc(p.attacker, vs2) && p.success) {
							vs_npc2 = p.attacker;
							npc_attack = true;
							npc_attack_target = p.defender;
						} else if (isNpc(p.defender, vs2)) {
							vs_npc2 = p.defender;
							if (!p.success) {
								npc_attack = true;
							}
						}
					}
					current = null;
					if (p.success) {
						if ("你".equals(p.attacker)) {
							Part part = null;
							for (int k = parts.size() - 1; k >= 0; k--) {
								if (p.defender.equals(parts.get(k).attacker)) {
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
								if (p.attacker.equals(parts.get(k).defender)) {
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
								if (p.defender.equals(parts.get(k).attacker)) {
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
								if (p.attacker.equals(parts.get(k).defender)) {
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
					is_first = false;
					continue;
				}
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
						System.out.println("[VS] " + attacker + " attack "
								+ (defender != null ? defender + " " : "")
								+ "combo");
						if (vs_npc1 == null && isNpc(defender, vs1)) {
							vs_npc1 = defender;
						}
						if (vs_npc2 == null && isNpc(defender, vs2)) {
							vs_npc2 = defender;
						}
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
								if (attacker.equals(parts.get(k).attacker)
										&& "你".equals(parts.get(k).defender)) {
									part = parts.remove(k);
									parts.add(part);
									break;
								}
							}
						}
						if ("你".equals(attacker) || "你".equals(defender)) {
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
						is_first = false;
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
									StringBuilder sb = new StringBuilder(msg);
									for (int j = 1; j < posture.lines; j++) {
										sb.append("\n").append(msgs.get(i + j));
									}
									text = sb.toString();
								}
								m = posture.pattern.matcher(text);
								if (m.find()) {
									skills.add(name);
									part = new Part();
									if (posture.source > 0) {
										part.attacker = m.group(posture.source);
									}
									if (posture.target > 0) {
										part.defender = m.group(posture.target);
									}
									part.combo_attack = 0;
									part.alive = time;
									boolean is_combo = false;
									if (i + posture.lines < msgs.size()) {
										String next = msgs.get(i
												+ posture.lines);
										for (Pattern pattern : COMBO_ATTACK_PATTERNS) {
											m = pattern.matcher(next);
											if (m.find()) {
												is_combo = true;
												break;
											}
										}
									}
									if (!is_combo) {
										if (vs_npc1 == null) {
											if (isNpc(part.attacker, vs1)) {
												vs_npc1 = part.attacker;
											} else if (isNpc(part.defender, vs1)) {
												vs_npc1 = part.defender;
											}
										}
										if (vs_npc2 == null) {
											if (isNpc(part.attacker, vs2)) {
												vs_npc2 = part.attacker;
												npc_attack = true;
												npc_attack_target = part.defender;
											} else if (isNpc(part.defender, vs2)) {
												vs_npc2 = part.defender;
											}
										}
									}
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
						is_first = false;
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
			// do perform
			int point = me.point;
			if (hasNpc(vs2)) {
				if (vs_npc2 != null) {
					if (point >= 8) {
						int a = 0, b = 0;
						for (VsInfo info : vs2) {
							Matcher m = USER_ID_PATTERN.matcher(info.id);
							if (m.find()) {
								if (info.max_qi >= 30000
										&& !isFriend(info.name)) {
									a++;
								} else {
									b++;
								}
							}
						}
						if (a < b) {
							String pfm = perform(pfms);
							if (pfm != null) {
								System.out.println("[VS] perform " + pfm
										+ " to " + vs_npc2);
								point = calcPoint(point, pfm);
							}
						}
					}
					return;
				}
			}
			if (vs_npc1 != null) {
				if (point >= 3) {
					String pfm = comboAttack(vs2, pfms);
					if (pfm != null) {
						point = calcPoint(point, pfm);
					} else {
						int a = 0, b = 0;
						for (VsInfo info : vs2) {
							Matcher m = USER_ID_PATTERN.matcher(info.id);
							if (m.find()) {
								if (info.max_qi >= 30000
										&& !isFriend(info.name)) {
									a++;
								} else {
									b++;
								}
							}
						}
						if (a >= b && a > 0) {
							pfm = perform(pfms);
							if (pfm != null) {
								System.out.println("[VS] perform " + pfm);
								point = calcPoint(point, pfm);
							}
						}
					}
				}
			} else if (hasNpc(vs1) || hasNpc(vs2)) {
				if (!is_first && point >= 3) {
					int a = 0, b = 0;
					for (VsInfo info : vs2) {
						Matcher m = USER_ID_PATTERN.matcher(info.id);
						if (m.find()) {
							if (info.max_qi >= 30000 && !isFriend(info.name)) {
								a++;
							} else {
								b++;
							}
						}
					}
					if (a >= b && a > 0) {
						String pfm = perform(pfms);
						if (pfm != null) {
							System.out.println("[VS] perform " + pfm);
							point = calcPoint(point, pfm);
						}
					}
				}
			} else {
				normalCombat(vs1, vs2, pfms, point);
			}
		} catch (Exception e) {
			e.printStackTrace();
			cmdline.stopTask(this);
		}
	}

	private void normalCombat(List<VsInfo> vs1, List<VsInfo> vs2,
			List<String> pfms, int point) {
		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			if ("你".equals(part.defender)) {
				String pfm = perform(part.skills, pfms);
				if (pfm != null) {
					System.out.println("[VS] perform " + pfm + " to "
							+ part.attacker);
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
								&& isPlayer(part.attacker, vs2)
								&& (part.combo_attack > 1 || getHp(
										part.attacker, vs2) >= 100000)) {
							pfm = combo(pfm, pfms);
							if (pfm != null) {
								System.out.println("[VS] perform " + pfm
										+ " to " + part.attacker);
								point = calcPoint(point, pfm);
							}
							break;
						}
					}
				} else {
					break;
				}
			}
		}
		if (point >= 3) {
			String pfm = comboAttack(vs2, pfms);
			if (pfm != null) {
				point = calcPoint(point, pfm);
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
	}

	private String comboAttack(List<VsInfo> vs2, List<String> pfms) {
		for (int i = 0; i < parts.size(); i++) {
			Part part = parts.get(i);
			if ("你".equals(part.attacker) && part.defender != null
					&& !isFriend(part.defender) && isPlayer(part.defender, vs2)) {
				long hp = getHp(part.defender, vs2);
				if ((hp >= 100000 && part.combo_attack < 2)
						|| (hp >= 40000 && part.combo_attack == 0)) {
					String pfm = combo(part.skills, pfms);
					if (pfm != null) {
						System.out.println("[VS] perform " + pfm + " to "
								+ part.defender);
						return pfm;
					}
				}
				return "";
			}
		}
		return null;
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
						cmdline.sendCmd("playskill " + (i + 1));
						return pfm;
					}
				}
			}
		}
		return perform(pfms);
	}

	private String perform(List<String> pfms) {
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
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
						cmdline.sendCmd("playskill " + (i + 1));
						return pfm;
					}
				}
			}
		}
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
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
					cmdline.sendCmd("playskill " + (i + 1));
					return pfm;
				}
			}
		}
		for (String pfm : performs) {
			int i = pfms.indexOf(pfm);
			if (i >= 0) {
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
		if (name == null || name.length() == 0 || "你".equals(name)) {
			return true;
		}
		for (VsInfo info : vs) {
			if (name.equals(info.name)) {
				Matcher m = USER_ID_PATTERN.matcher(info.id);
				return m.find();
			}
		}
		return false;
	}

	private boolean isNpc(String name, List<VsInfo> vs) {
		if (name == null || name.length() == 0 || "你".equals(name)) {
			return false;
		}
		for (VsInfo info : vs) {
			if (name.equals(info.name)) {
				Matcher m = USER_ID_PATTERN.matcher(info.id);
				return !m.find();
			}
		}
		return false;
	}

	private boolean hasNpc(List<VsInfo> vs) {
		for (VsInfo info : vs) {
			Matcher m = USER_ID_PATTERN.matcher(info.id);
			if (!m.find()) {
				return true;
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
		if (name == null || name.length() == 0) {
			return false;
		}
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

	private String[] checkPartFinish(String msg) {
		Matcher m = PART_FINISH_PATTERNS[0].matcher(msg);
		if (m.find()) {
			return new String[] { null, m.group(1),
					m.group(1) + " attack failed" };
		}
		m = PART_FINISH_PATTERNS[1].matcher(msg);
		if (m.find()) {
			return new String[] { m.group(2), m.group(1),
					m.group(1) + " attack " + m.group(2) + " failed" };
		}
		m = PART_FINISH_PATTERNS[2].matcher(msg);
		if (m.find()) {
			return new String[] { m.group(1), m.group(2),
					m.group(1) + " attack " + m.group(2) + " failed" };
		}
		m = PART_FINISH_PATTERNS[3].matcher(msg);
		if (m.find()) {
			return new String[] { m.group(1), null, m.group(1) + " dodged" };
		}
		m = PART_FINISH_PATTERNS[4].matcher(msg);
		if (m.find()) {
			return new String[] { m.group(1), null, m.group(1) + " dodged" };
		}
		m = PART_FINISH_PATTERNS[5].matcher(msg);
		if (m.find()) {
			return new String[] { m.group(2), m.group(1),
					m.group(1) + " attack " + m.group(2) + " failed" };
		}
		for (int i = 0; i < 10; i++) {
			m = PART_FINISH_PATTERNS[i + 6].matcher(msg);
			if (m.find()) {
				return new String[] { m.group(1), null, m.group(1) + " damaged" };
			}
		}
		return null;
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
		int point;
	}

	private static class RecoveryTask extends TimerTask {

		private CommandLine cmdline;
		private Room room;

		public RecoveryTask(CommandLine cmdline, Room room) {
			this.cmdline = cmdline;
			this.room = room;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				Map<String, Object> attrs = (Map<String, Object>) cmdline.js(
						cmdline.load("get_msgs.js"), "msg_attrs", false);
				if (attrs != null) {
					long force = Long.parseLong((String) attrs.get("force"));
					long max_force = Long.parseLong((String) attrs
							.get("max_force"));
					long kee = Long.parseLong((String) attrs.get("kee"));
					long max_kee = Long
							.parseLong((String) attrs.get("max_kee"));
					if (force < max_force) {
						StringBuilder sb = new StringBuilder();
						int n = (int) (max_force - force) / 5000 + 1;
						for (int i = 0; i < Math.min(n, 5); i++) {
							sb.append("buy /map/snow/obj/qiannianlingzhi from snow_herbalist\nitems use snow_qiannianlingzhi\n");
						}
						sb.append("attrs");
						cmdline.js("clickButton(arguments[0]);", sb.toString());
					} else if (kee < max_kee) {
						StringBuilder sb = new StringBuilder();
						for (int i = 0; i < 5; i++) {
							sb.append("recovery\n");
						}
						sb.append("attrs");
						cmdline.js("clickButton(arguments[0]);", sb.toString());
					} else {
						Runnable callback = new Runnable() {
							@Override
							public void run() {
								try {
									cmdline.execute("#pk");
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						};
						cmdline.walk(new String[] { room.getPath() },
								room.getName(), null, callback, 200);
					}
				} else {
					System.out.println("failed to get attrs");
					cmdline.stopTask(this);
				}
			} catch (Exception e) {
				e.printStackTrace();
				cmdline.stopTask(this);
			}
		}
	}
}
