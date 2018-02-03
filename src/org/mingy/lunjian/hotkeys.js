var alias_list = arguments.length > 0 ? arguments[0] : [];
var friend_list = arguments.length > 1 ? arguments[1] : [];
if (!window.gSocketMsg || !window.gSocketMsg2 || window.mingy_addon) {
	return;
}
if (!window.g_obj_map.get('msg_attrs')) {
	clickButton('attrs');
	return;
}
var show_target = true;
var auto_attack = false;
var auto_defence = false;
var auto_fight = false;
var user_id_pattern1 = /^u[0-9]+$/;
var user_id_pattern2 = /^u[0-9]+\-/;
var kuafu_name_pattern = /^\[[0-9]+\]/;
var skills = new Map();
skills.put('九天龙吟剑法', [ '排云掌法', '雪饮狂刀' ]);
skills.put('覆雨剑法', [ '翻云刀法', '如来神掌' ]);
skills.put('织冰剑法', [ '孔雀翎', '飞刀绝技' ]);
skills.put('排云掌法', [ '九天龙吟剑法', '雪饮狂刀' ]);
skills.put('如来神掌', [ '覆雨剑法', '孔雀翎' ]);
skills.put('雪饮狂刀', [ '九天龙吟剑法', '排云掌法' ]);
skills.put('翻云刀法', [ '覆雨剑法', '飞刀绝技' ]);
skills.put('飞刀绝技', [ '翻云刀法', '织冰剑法' ]);
skills.put('孔雀翎', [ '如来神掌', '织冰剑法' ]);
var skill_chains = [ '九天龙吟剑法', '覆雨剑法', '织冰剑法', '排云掌法', '如来神掌', '雪饮狂刀',
		'翻云刀法', '飞刀绝技', '孔雀翎', '道种心魔经', '生生造化功', '幽影幻虚步', '万流归一' ];
var force_skills = [ '道种心魔经', '生生造化功', '不动明王诀', '八荒功', '易筋经神功', '天邪神功',
		'紫霞神功', '葵花宝典', '九阴真经', '茅山道术', '蛤蟆神功' ];
var dodge_skills = [ '万流归一', '幽影幻虚步', '乾坤大挪移', '凌波微步', '无影毒阵', '九妙飞天术' ];
var defence_patterns = [ /(.*)顿时被冲开老远，失去了攻击之势！/, /(.*)被(.*)的真气所迫，只好放弃攻击！/,
		/(.*)衣裳鼓起，真气直接将(.*)逼开了！/, /(.*)找到了闪躲的空间！/, /(.*)朝边上一步闪开！/,
		/面对(.*)的攻击，(.*)毫不为惧！/, /(.*)使出“(.*)”，希望扰乱(.*)的视线！/ ];
var aliases = new Map();
for (var i = 0; i < alias_list.length; i++) {
	var k = alias_list[i].indexOf('=');
	if (k >= 0) {
		aliases.put(alias_list[i].substr(0, k), alias_list[i].substr(k + 1));
	}
}
var map_ids = new Map();
map_ids.put('xueting', '1');
map_ids.put('xt', '1');
map_ids.put('luoyang', '2');
map_ids.put('ly', '2');
map_ids.put('huashancun', '3');
map_ids.put('hsc', '3');
map_ids.put('huashan', '4');
map_ids.put('hs', '4');
map_ids.put('yangzhou', '5');
map_ids.put('yz', '5');
map_ids.put('gaibang', '6');
map_ids.put('gb', '6');
map_ids.put('qiaoyin', '7');
map_ids.put('qy', '7');
map_ids.put('emei', '8');
map_ids.put('em', '8');
map_ids.put('hengshan', '9');
map_ids.put('hs2', '9');
map_ids.put('wudang', '10');
map_ids.put('wd', '10');
map_ids.put('wanyue', '11');
map_ids.put('wy', '11');
map_ids.put('shuiyan', '12');
map_ids.put('sy', '12');
map_ids.put('shaolin', '13');
map_ids.put('sl', '13');
map_ids.put('tangmen', '14');
map_ids.put('tm', '14');
map_ids.put('qingcheng', '15');
map_ids.put('qc', '15');
map_ids.put('xiaoyao', '16');
map_ids.put('xy', '16');
map_ids.put('kaifang', '17');
map_ids.put('kf', '17');
map_ids.put('mingjiao', '18');
map_ids.put('mj', '18');
map_ids.put('quanzhen', '19');
map_ids.put('qz', '19');
map_ids.put('gumu', '20');
map_ids.put('gm', '20');
map_ids.put('baituo', '21');
map_ids.put('bt', '21');
map_ids.put('songshan', '22');
map_ids.put('ss', '22');
map_ids.put('meizhuang', '23');
map_ids.put('mz', '23');
map_ids.put('taishan', '24');
map_ids.put('ts', '24');
map_ids.put('daqi', '25');
map_ids.put('dq', '25');
map_ids.put('dazhao', '26');
map_ids.put('dz', '26');
map_ids.put('heimuya', '27');
map_ids.put('hmy', '27');
map_ids.put('mojiao', '27');
map_ids.put('mj2', '27');
map_ids.put('xingxiu', '28');
map_ids.put('xx', '28');
map_ids.put('maoshan', '29');
map_ids.put('ms', '29');
map_ids.put('taohuadao', '30');
map_ids.put('thd', '30');
map_ids.put('tiexue', '31');
map_ids.put('tx', '31');
map_ids.put('murong', '32');
map_ids.put('mr', '32');
map_ids.put('dali', '33');
map_ids.put('dl', '33');
map_ids.put('duanjian', '34');
map_ids.put('dj', '34');
map_ids.put('binghuodao', '35');
map_ids.put('bhd', '35');
map_ids.put('xiakedao', '36');
map_ids.put('xkd', '36');
map_ids.put('jueqinggu', '37');
map_ids.put('jqg', '37');
map_ids.put('bihai', '38');
map_ids.put('bh', '38');
map_ids.put('tianshan', '39');
map_ids.put('ts2', '39');
map_ids.put('miaojiang', '40');
map_ids.put('mj3', '40');
var secrets = new Map();
secrets.put("lvshuige", 1255);
secrets.put("daojiangu", 1535);
secrets.put("taohuadu", 1785);
secrets.put("lvzhou", 2035);
secrets.put("luanshishan", 2350);
secrets.put("dilongling", 2385);
secrets.put("fomenshiku", 2425);
secrets.put("tianlongshan", 3100);
secrets.put("dafuchuan", 3090);
secrets.put("binhaigucheng", 3385);
secrets.put("baguamen", 3635);
secrets.put("nanmanzhidi", 3890);
secrets.put("fengduguicheng", 3890);
var message_listeners = [];
var listener_seq = 0;
function add_listener(type, subtype, fn, is_pre) {
	var listener = {
		'id' : ++listener_seq,
		'type' : type,
		'subtype' : subtype,
		'fn' : fn,
		'is_pre' : !!is_pre
	};
	message_listeners.push(listener);
	return listener.id;
}
function remove_listener(id) {
	for ( var i = 0; i < message_listeners.length; i++) {
		if (message_listeners[i].id == id) {
			message_listeners.splice(i, 1);
		}
	}
}
var _dispatch_message = window.gSocketMsg.dispatchMessage;
window.channel_messages = [];
window.gSocketMsg.dispatchMessage = function(msg) {
	for ( var i = 0; i < message_listeners.length; i++) {
		var listener = message_listeners[i];
		if (listener.is_pre
				&& (listener.type == msg.get('type') || (listener.type instanceof Array && $
						.inArray(msg.get('type'), listener.type) >= 0))
				&& (!listener.subtype
						|| listener.subtype == msg.get('subtype') || (listener.subtype instanceof Array && $
						.inArray(msg.get('subtype'), listener.subtype) >= 0))) {
			listener.fn(msg);
		}
	}
	_dispatch_message.apply(this, arguments);
	for ( var i = 0; i < message_listeners.length; i++) {
		var listener = message_listeners[i];
		if (!listener.is_pre
				&& (listener.type == msg.get('type') || (listener.type instanceof Array && $
						.inArray(msg.get('type'), listener.type) >= 0))
				&& (!listener.subtype
						|| listener.subtype == msg.get('subtype') || (listener.subtype instanceof Array && $
						.inArray(msg.get('subtype'), listener.subtype) >= 0))) {
			listener.fn(msg);
		}
	}
	if (msg.get('type') == 'channel') {
		channel_messages.push(msg.get('subtype') + ',' + msg.get('msg'));
		if (channel_messages.length > 50) {
			channel_messages = channel_messages.slice(-25);
		}
	}
};
var _click_button = window.clickButton;
window.clickButton = function(cmd, k) {
	if (cmd.substr(0, 1) == '#') {
		execute_cmd(cmd);
	} else {
		_click_button.apply(this, arguments);
	}
};
var _make_cmd = window.gSocketMsg.make_cmd;
window.gSocketMsg.make_cmd = function(a, b, c, d) {
	if (c == 'combat_auto_fight') {
		clickButton('auto_fight 0');
	}
	if (c == 'combat_auto_fight' || c == 'combat_no_auto_fight') {
		if (auto_fight) {
			arguments[1] = '#stop';
			arguments[2] = 'combat_auto_fight';
		} else {
			arguments[1] = '#combat';
			arguments[2] = 'combat_no_auto_fight';
		}
	}
	return _make_cmd.apply(this, arguments);
};
var vs_text1 = '', vs_text2 = '', defence_performed = false;
add_listener(
		'vs',
		'',
		function(msg) {
			if (!show_target && !auto_attack && !auto_defence) {
				return;
			}
			var subtype = msg.get('subtype');
			if (subtype == 'text') {
				vs_text1 = vs_text2;
				vs_text2 = msg.get('msg');
			} else if (subtype == 'add_xdz') {
				var my_id = window.g_obj_map.get('msg_attrs').get('id');
				if (msg.get('uid') == my_id) {
					defence_performed = false;
				}
			} else if (subtype == 'playskill' && parseInt(msg.get('ret')) == 0) {
				var my_id = window.g_obj_map.get('msg_attrs').get('id');
				var vs_info = window.g_obj_map.get('msg_vs_info');
				var pfm = removeSGR(msg.get('name'));
				var vs_text = skill_chains.indexOf(pfm) >= 0 ? vs_text1
						+ vs_text2
						: vs_text2;
				if (!is_defence(vs_text)) {
					if (msg.get('uid') == my_id) {
						var vid = msg.get('vid');
						if (vs_info) {
							var v1, v2, p1, p2;
							for ( var i = 1; i <= 8; i++) {
								if (vs_info.get('vs1_pos_v' + i) == vid) {
									v1 = 'vs1';
									p1 = i;
									v2 = 'vs2';
									break;
								}
							}
							if (!v1) {
								for ( var i = 1; i <= 8; i++) {
									if (vs_info.get('vs2_pos_v' + i) == vid) {
										v1 = 'vs2';
										p1 = i;
										v2 = 'vs1';
										break;
									}
								}
							}
							for ( var i = 1; i <= 8; i++) {
								if (is_attack_target(vs_info, my_id, [v2, i], pfm, vs_text)) {
									if (show_target) {
										notify_fail(HIG + 'ATTACK: '
												+ vs_info.get(v2 + '_name' + i));
									}
									p2 = i;
									var id = vs_info.get(v2 + '_pos'
											+ i);
									if (auto_attack && is_player(id)
											&& get_friend_index(id) < 0) {
										auto_pfm(vs_info, pfm, v1, p1,
												v2, p2);
									}
									break;
								}
							}
						}
					} else if (is_player(msg.get('uid'))) {
						var pos1 = check_pos(vs_info, my_id);
						if (pos1) {
							var pos2 = check_pos(vs_info, msg.get('uid'));
							if (pos2 && pos1[0] != pos2[0]) {
								var target = 0;
								for ( var i = 1; i <= 8; i++) {
									if (i == pos1[1]) {
										continue;
									}
									if (is_attack_target(vs_info, my_id, [pos1[0], i], pfm, vs_text)) {
										target = i;
										break;
									}
								}
								if (target == 0 && vs_text.indexOf('你') >= 0) {
									if (show_target) {
										notify_fail(HIG + 'DEFENCE: '
												+ vs_info.get(pos2[0] + '_name' + pos2[1]));
									}
									if (auto_defence && !defence_performed && has_npc(vs_info, pos1[0]) && has_npc(vs_info, pos2[0])) {
										var max_kee1 = parseInt(window.g_obj_map.get('msg_attrs').get('max_kee'));
										var max_kee2 = parseInt(vs_info.get(pos2[0] + '_max_kee' + pos2[1]));
										if (max_kee2 > max_kee1 * 0.8) {
											var xdz = parseInt(vs_info.get(pos1[0] + '_xdz' + pos1[1]));
											var buttons = get_skill_buttons(xdz);
											for ( var i = 0; i < dodge_skills.length; i++) {
												var k = $.inArray(dodge_skills[i], buttons);
												if (k >= 0) {
													clickButton('playskill ' + (k + 1));
													defence_performed = true;
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		});
function is_defence(vs_text) {
	for ( var i = 0; i < defence_patterns.length; i++) {
		if (defence_patterns[i]
				.test(vs_text)) {
			return true;
		}
	}
	return false;
}
function is_attack_target(vs_info, my_id, pos, pfm, vs_text) {
	var name = vs_info.get(pos[0] + '_name' + pos[1]);
	if (!name) {
		return false;
	}
	if (my_id.indexOf('-') >= 0
			&& kuafu_name_pattern.test(name)) {
		var j = name.indexOf(']');
		name = name.substr(0, j)
				+ '区'
				+ name.substr(j, name.length
						- j);
	}
	return vs_text.indexOf(name) >= 0;
}
var last_fight_time = 0;
var last_kill_time = 0;
function try_join_combat(vs_info, target, ignore_check) {
	var pos = check_pos(vs_info, target);
	if (!pos) {
		return false;
	} else if (parseInt(vs_info.get(pos[0] + '_kee' + pos[1])) <= 0) {
		return false;
	}
	var side = pos[0] == 'vs1' ? 'vs2' : 'vs1';
	var has_npc = false;
	var has_pos = false;
	var player_id = null;
	var k = -1;
	var pos1_name = removeSGR(vs_info.get(pos[0] + '_name' + pos[1]));
	if (pos1_name == '天剑真身' || pos1_name == '年兽') {
		has_npc = true;
	}
	for ( var i = 1; i <= 8; i++) {
		if (!has_npc || !has_pos) {
			var kee = vs_info.get(side + '_kee' + i);
			if (kee && parseInt(kee) > 0) {
				if (!has_npc && !is_player(vs_info.get(side + '_pos' + i))) {
					has_npc = true;
				}
			} else {
				has_pos = true;
			}
		}
		if (friend_list) {
			var kee = vs_info.get(pos[0] + '_kee' + i);
			if (kee && parseInt(kee) > 0) {
				var id = vs_info.get(pos[0] + '_pos' + i);
				if (is_player(id)) {
					if (!player_id) {
						player_id = id;
					}
					var j = get_friend_index(id);
					if (j >= 0 && (k < 0 || j < k)) {
						player_id = id;
						k = j;
					}
				}
			}
		}
	}
	if (!has_npc || (!ignore_check && !has_pos)) {
		return false;
	}
	var t = new Date().getTime();
	if (!ignore_check && player_id && t - last_fight_time >= 3000) {
		last_fight_time = t;
		send_cmd('fight ' + player_id);
	} else if (t - last_kill_time >= 1000) {
		last_kill_time = t;
		send_cmd('kill ' + target);
	}
	return true;
}
function check_pos(vs_info, target) {
	if (vs_info) {
		for ( var i = 1; i <= 8; i++) {
			if (vs_info.get('vs1_pos' + i) == target) {
				if (parseInt(vs_info.get('vs1_kee' + i)) > 0) {
					return [ 'vs1', i ];
				}
			} else if (vs_info.get('vs2_pos' + i) == target) {
				if (parseInt(vs_info.get('vs2_kee' + i)) > 0) {
					return [ 'vs2', i ];
				}
			}
		}
	}
	return null;
}
function is_player(id) {
	return user_id_pattern1.test(id) || user_id_pattern2.test(id);
}
function get_friend_index(id) {
	if (!friend_list) {
		return -1;
	}
	var i = id.indexOf('-');
	if (i >= 0) {
		id = id.substr(0, i);
	}
	return friend_list.indexOf(id);
}
function removeSGR(text) {
	return text ? text.replace(/\u001b\[[;0-9]+m/g, '') : '';
}
function auto_pfm(vs_info, pfm, v1, p1, v2, p2) {
	var xdz = parseInt(vs_info.get(v1 + '_xdz' + p1));
	var max_kee = parseInt(vs_info.get(v2 + '_max_kee' + p2));
	var kee = parseInt(vs_info.get(v2 + '_kee' + p2));
	var my_max_kee = parseInt(vs_info.get(v1 + '_max_kee' + p1));
	var k = 0;
	if (my_max_kee > 500000) {
		if (max_kee < 100000) {
			k = 0;
		} else if (max_kee < 200000) {
			k = 1;
		} else if (max_kee < 350000) {
			k = kee < 150000 ? 1 : 2;
		} else {
			k = 2;
		}
	} else if (my_max_kee > 300000) {
		if (max_kee < 30000) {
			k = 0;
		} else if (max_kee < 100000) {
			k = 1;
		} else if (max_kee < 300000) {
			k = kee < 100000 ? 1 : 2;
		} else {
			k = 2;
		}
	} else {
		k = 0;
	}
	if (skills.containsKey(pfm)) {
		k = k > 0 ? k - 1 : 0;
	}
	if (k > 0) {
		var buttons = get_skill_buttons(xdz);
		if (k == 1) {
			var pfms = skills.get(pfm);
			if (pfms) {
				for ( var i = 0; i < buttons.length; i++) {
					if (buttons[i] && pfms.indexOf(buttons[i]) >= 0) {
						clickButton('playskill ' + (i + 1));
						return;
					}
				}
			}
			for ( var i = 0; i < buttons.length; i++) {
				if (buttons[i] && skills.containsKey(buttons[i])) {
					clickButton('playskill ' + (i + 1));
					break;
				}
			}
		} else if (k == 2) {
			var pfms = skills.get(pfm);
			if (pfms) {
				for ( var i = 0; i < buttons.length; i++) {
					if (buttons[i] && pfms.indexOf(buttons[i]) >= 0) {
						clickButton('playskill ' + (i + 1));
						return;
					}
				}
			}
			select_perform(buttons);
		}
	}
}
function rejoin(change_side) {
	if (!window.is_fighting) {
		return;
	}
	var vs_info = window.g_obj_map.get('msg_vs_info');
	var my_id = window.g_obj_map.get('msg_attrs').get('id');
	var pos = check_pos(vs_info, my_id);
	if (pos) {
		var side = pos[0];
		if (!change_side) {
			side = side == 'vs1' ? 'vs2' : 'vs1';
		}
		var npc_id = null;
		for ( var i = 1; i <= 8; i++) {
			var kee = vs_info.get(side + '_kee' + i);
			if (kee && parseInt(kee) > 0) {
				var id = vs_info.get(side + '_pos' + i);
				if (!is_player(id)) {
					npc_id = id;
					break;
				}
			}
		}
		if (npc_id) {
			var cmd = 'escape\n';
			var force = parseInt(vs_info.get(pos[0] + '_force' + pos[1]));
			var max_force = parseInt(vs_info.get(pos[0] + '_max_force'
					+ pos[1]));
			if (max_force - force >= 20000) {
				for ( var i = 0; i < 3; i++) {
					cmd += 'items use snow_qiannianlingzhi\n';
				}
			}
			cmd += 'kill ' + npc_id;
			send_cmd(cmd);
		}
	}
}
var perform = function() {
	if (!window.is_fighting) {
		return false;
	}
	var my_id = window.g_obj_map.get('msg_attrs').get('id');
	var vs_info = window.g_obj_map.get('msg_vs_info');
	var pos = check_pos(vs_info, my_id);
	if (!pos) {
		return false;
	}
	var xdz = parseInt(vs_info.get(pos[0] + '_xdz' + pos[1]));
	return select_perform(get_skill_buttons(xdz));
};
function get_skill_buttons(xdz) {
	var buttons = [];
	for ( var i = 0; i < 4; i++) {
		var button = window.g_obj_map.get('skill_button' + (i + 1));
		if (button && parseInt(button.get('xdz')) <= xdz) {
			buttons.push(removeSGR(button.get('name')));
		} else {
			buttons.push('');
		}
	}
	return buttons;
}
function select_perform(buttons) {
	for ( var i = 0; i < buttons.length; i++) {
		if (buttons[i]) {
			var pfms = skills.get(buttons[i]);
			if (pfms) {
				for ( var j = i + 1; j < buttons.length; j++) {
					if (buttons[j] && pfms.indexOf(buttons[j]) >= 0) {
						clickButton('playskill ' + (i + 1) + '\nplayskill '
								+ (j + 1));
						return true;
					}
				}
			}
		}
	}
	for ( var i = 0; i < buttons.length; i++) {
		if (buttons[i] && skills.containsKey(buttons[i])) {
			clickButton('playskill ' + (i + 1));
			return true;
		}
	}
	return false;
}
function has_npc(vs_info, side) {
	for (var i = 1; i <= 8; i++) {
		if (!is_player(vs_info.get(side + '_pos' + i))) {
			var kee = vs_info.get(side + '_kee' + i);
			if (kee && parseInt(kee) > 0) {
				return true;
			}
		}
	}
	return false;
}
function has_power_player(vs_info, side) {
	for (var i = 1; i <= 8; i++) {
		if (is_player(vs_info.get(side + '_pos' + i))) {
			var kee = vs_info.get(side + '_kee' + i);
			if (kee && parseInt(kee) > 0) {
				var max_kee = vs_info.get(side + '_max_kee' + i);
				if (max_kee > 500000) {
					return true;
				}
			}
		}
	}
	return false;
}
var h_interval, h_listener, is_started = false;
var kill = function() {
	var npc = null;
	$('#out > span.out button.cmd_click2').each(function() {
		$e = $(this);
		if ($e.text() == '杀死') {
			var onclick = $e.attr('onclick');
			var i = onclick.indexOf('\'');
			var j = onclick.indexOf('\'', i + 1);
			npc = onclick.substring(i + 6, j);
			return false;
		}
	});
	if (npc) {
		if (h_interval) {
			clearInterval(h_interval);
			h_interval = undefined;
			is_started = false;
			remove_listener(h_listener);
			h_listener = undefined;
		}
		h_listener = add_listener('vs', '',
				function(msg) {
					if (msg.get('subtype') == 'vs_info'
							|| (msg.get('subtype') == 'die' && msg
									.get('uid') != npc)) {
						var vs_info = window.g_obj_map.get('msg_vs_info');
						try_join_combat(vs_info, npc);
					}
				});
		last_kill_time = new Date().getTime();
		send_cmd('kill ' + npc + '\nwatch_vs ' + npc);
		var my_id = window.g_obj_map.get('msg_attrs').get('id');
		h_interval = setInterval(function() {
			var is_fighting = false;
			if (window.is_fighting) {
				var vs_info = window.g_obj_map.get('msg_vs_info');
				if (vs_info) {
					is_started = true;
					is_fighting = !!check_pos(vs_info, my_id);
				}
				if (is_fighting) {
					if (check_pos(vs_info, npc)) {
						clearInterval(h_interval);
						h_interval = undefined;
						is_started = false;
						remove_listener(h_listener);
						h_listener = undefined;
					} else {
						is_started = false;
						perform();
					}
				} else {
					try_join_combat(vs_info, npc, true);
				}
			} else if (is_started) {
				clearInterval(h_interval);
				h_interval = undefined;
				is_started = false;
				remove_listener(h_listener);
				h_listener = undefined;
			} else {
				last_kill_time = new Date().getTime();
				send_cmd('kill ' + npc + '\nwatch_vs ' + npc);
			}
		}, 150);
	}
};
function find_target(nameOrId, types) {
	var room = window.g_obj_map.get('msg_room');
	if (!room) {
		return null;
	}
	if (!types || $.inArray('npc', types) >= 0) {
		for ( var t, i = 1; (t = room.get('npc' + i)) != undefined; i++) {
			var s = t.split(',');
			if (s.length > 1) {
				s[1] = removeSGR(s[1]);
				if (s[0] == nameOrId || s[1] == nameOrId) {
					return [ s[0], s[1], 'npc' ];
				}
			} else {
				if (s[0] == nameOrId) {
					return [ s[0], null, 'npc' ];
				}
			}
		}
	}
	if (!types || $.inArray('item', types) >= 0) {
		for ( var t, i = 1; (t = room.get('item' + i)) != undefined; i++) {
			var s = t.split(',');
			if (s.length > 1) {
				s[1] = removeSGR(s[1]);
				if (s[0] == nameOrId || s[1] == nameOrId) {
					return [ s[0], s[1], 'item' ];
				}
			} else {
				if (s[0] == nameOrId) {
					return [ s[0], null, 'item' ];
				}
			}
		}
	}
	if (!types || $.inArray('user', types) >= 0) {
		for ( var t, i = 1; (t = room.get('user' + i)) != undefined; i++) {
			var s = t.split(',');
			if (s.length > 1) {
				s[1] = removeSGR(s[1]);
				if (s[0] == nameOrId || s[1] == nameOrId) {
					return [ s[0], s[1], 'user' ];
				}
			} else {
				if (s[0] == nameOrId) {
					return [ s[0], null, 'user' ];
				}
			}
		}
	}
	return null;
}
var task_h_timer, task_h_listener;
function stop_task() {
	if (task_h_timer) {
		clearInterval(task_h_timer);
		task_h_timer = undefined;
		console.log('task stopped.');
	} else if (task_h_listener) {
		auto_fight = false;
		var $b = $('button.cmd_combat_auto_fight');
		if ($b.length > 0) {
			$b.removeClass('cmd_combat_auto_fight');
			$b.addClass('cmd_combat_no_auto_fight');
			$b.onclick = 'clickButton("#combat", 0)';
		}
		remove_listener(task_h_listener);
		task_h_listener = undefined;
		console.log('task stopped.');
	}
}
function add_task_timer(fn, interval) {
	stop_task();
	task_h_timer = setInterval(fn, interval);
}
function add_task_listener(type, subtype, fn, is_pre) {
	stop_task();
	task_h_listener = add_listener(type, subtype, fn, is_pre);
}
function execute_cmd(cmd) {
	if (cmd.substr(0, 6) == '#loop ') {
		cmd = $.trim(cmd.substr(6));
		if (cmd) {
			var interval = 500;
			var i = cmd.indexOf(' ');
			if (i >= 0) {
				var t = parseInt(cmd.substr(0, i));
				if (!isNaN(t)) {
					interval = t;
					cmd = $.trim(cmd.substr(i + 1));
				}
			}
			if (cmd) {
				stop_task();
				console.log('starting loop...');
				var pc;
				add_task_timer(function() {
					if (!pc) {
						pc = process_cmdline(cmd);
					}
					if (pc && pc[0]) {
						send_cmd(pc[0]);
					}
				}, interval);
			}
		}
	} else if (cmd == '#stop') {
		stop_task();
		cmd_queue.splice(0, cmd_queue.length);
	} else if (cmd.substr(0, 6) == '#kill ') {
		var name = $.trim(cmd.substr(6));
		if (name) {
			console.log('starting auto kill...');
			var target = find_target(name, [ 'npc' ]);
			if (target) {
				clickButton('kill ' + target[0]);
				add_task_listener('jh', 'new_item', function(msg) {
					if (removeSGR(msg.get('name')) == target[1] + '的尸体') {
						clickButton('get ' + msg.get('id'));
						console.log('ok!');
						stop_task();
						if (target[1] == '年兽') {
							execute_cmd('#loop get ' + msg.get('id'));
						}
					}
				}, true);
			} else {
				add_task_listener(
						'jh',
						'new_npc',
						function(msg) {
							if (msg.get('id') == name
									|| removeSGR(msg.get('name')) == name) {
								name = removeSGR(msg.get('name'));
								clickButton('kill ' + msg.get('id'));
								add_task_listener(
										'jh',
										'new_item',
										function(msg) {
											if (removeSGR(msg.get('name')) == name
													+ '的尸体') {
												clickButton('get '
														+ msg.get('id'));
												console.log('ok!');
												stop_task();
												if (name == '年兽') {
													execute_cmd('#loop get ' + msg.get('id'));
												}
											}
										}, true);
							}
						}, true);
			}
		}
	} else if (cmd.substr(0, 7) == '#fight ') {
		var name = $.trim(cmd.substr(7));
		if (name) {
			console.log('starting auto fight...');
			var target = find_target(name, [ 'npc' ]);
			if (target) {
				clickButton('fight ' + target[0]);
			} else {
				add_task_listener(
						'jh',
						'new_npc',
						function(msg) {
							if (msg.get('id') == name
									|| removeSGR(msg.get('name')) == name) {
								clickButton('fight ' + msg.get('id'));
								console.log('ok!');
								stop_task();
							}
						}, true);
			}
		}
	} else if (cmd.substr(0, 5) == '#get ') {
		var name = $.trim(cmd.substr(5));
		if (name) {
			var target = find_target(name, [ 'item' ]);
			if (target) {
				clickButton('get ' + target[0]);
			} else {
				console.log('starting auto get...');
				add_task_listener('jh', 'new_item', function(msg) {
					if (msg.get('id') == name
							|| removeSGR(msg.get('name')) == name) {
						clickButton('get ' + msg.get('id'));
						console.log('ok!');
						stop_task();
					}
				}, true);
			}
		}
	} else if (cmd == '#secret' || cmd.substr(0, 8) == '#secret ') {
		var msg_room = window.g_obj_map.get('msg_room');
		var accept;
		if (cmd == '#secret') {
			accept = secrets.get(msg_room.get('map_id'));
		} else {
			accept = parseInt($.trim(cmd.substr(8)));
		}
		if (!isNaN(accept)) {
			var saodang;
			for (var i = 1; ; i++) {
				if (!msg_room.containsKey('cmd' + i + '_name')) {
					break;
				}
				if (removeSGR(msg_room.get('cmd' + i + '_name')) == '扫荡') {
					saodang = msg_room.get('cmd' + i);
					break;
				}
			}
			if (saodang) {
				add_task_listener(
						'prompt',
						'',
						function(msg) {
							var r = msg.get('msg').match(/^您已经通关过此副本，可以扫荡完成，\n扫荡完成的奖励为：玄铁令x(\d+)、朱果x(\d+)。/);
							if (r) {
								if (parseInt(r[2]) > accept) {
									console.log('ok!');
									stop_task();
								} else {
									send_cmd(saodang);
								}
							}
						}, true);
				console.log('starting clean out secret...');
				send_cmd(saodang);
			} else {
				console.log('cannot clean out secret.');
			}
		} else {
			console.log('invalid command.');
		}
	} else if (cmd == '#tianjiangu') {
		console.log('starting tianjiangu combat...');
		add_task_listener(['jh', 'vs'], '', function(msg) {
			if (msg.get('type') == 'jh' && (msg.get('subtype') == 'info'
					|| msg.get('subtype') == 'new_npc' || msg.get('subtype') == 'dest_npc')) {
				var msg_room = window.g_obj_map.get('msg_room');
				var target;
				for (var i = 1; ; i++) {
					var npc = msg_room.get('npc' + i);
					if (!npc) {
						break;
					}
					var arr = npc.split(',');
					if (arr.length > 1) {
						arr[1] = removeSGR(arr[1]);
						if (arr[1] == '天剑真身' || arr[1] == '天剑'
								|| arr[1] == '虹风' || arr[1] == '虹雨'
								|| arr[1] == '虹雷' || arr[1] == '虹电') {
							target = arr[0];
						} else if (!target && arr[1] == '天剑谷卫士') {
							target = arr[0];
						}
					}
				}
				if (target) {
					clickButton('kill ' + target);
				}
			} else if (msg.get('type') == 'vs') {
				if (msg.get('subtype') == 'combat_result') {
					clickButton('prev_combat');
				} else {
					var vs_info = window.g_obj_map.get('msg_vs_info');
					var my_id = window.g_obj_map.get('msg_attrs').get('id');
					auto_combat(vs_info, my_id, msg);
				}
			}
		});
	} else if (cmd == '#combat') {
		console.log('starting auto combat...');
		auto_fight = true;
		var $b = $('button.cmd_combat_no_auto_fight');
		if ($b.length > 0) {
			$b.removeClass('cmd_combat_no_auto_fight');
			$b.addClass('cmd_combat_auto_fight');
			$b.onclick = 'clickButton("#stop", 0)';
		}
		add_task_listener('vs', '', function(msg) {
			var vs_info = window.g_obj_map.get('msg_vs_info');
			var my_id = window.g_obj_map.get('msg_attrs').get('id');
			auto_combat(vs_info, my_id, msg);
		});
	} else if (cmd == '#pk') {
		console.log('starting auto pvp...');
		var vs_npc1 = false, vs_npc2 = false;
		add_task_listener('vs', '', function(msg) {
			var vs_info = window.g_obj_map.get('msg_vs_info');
			var my_id = window.g_obj_map.get('msg_attrs').get('id');
			var pos = check_pos(vs_info, my_id);
			if (!pos) {
				return;
			}
			var subtype = msg.get('subtype');
			var has_npc1 = has_npc(vs_info, pos[0]);
			var has_npc2 = has_npc(vs_info, pos[0] == 'vs1' ? 'vs2' : 'vs1');
			if (has_npc1 && has_npc2) {
				if (subtype == 'add_xdz' || subtype == 'playskill'
					|| subtype == 'attack') {
					var kee = parseInt(vs_info.get(pos[0] + '_kee' + pos[1]));
					var max_kee = parseInt(vs_info.get(pos[0] + '_max_kee'
							+ pos[1]));
					var percent = kee * 100 / max_kee;
					if (percent < 50 || (percent < 75 && has_power_player(vs_info, pos[0] == 'vs1' ? 'vs2' : 'vs1'))) {
						var xdz = parseInt(vs_info
								.get(pos[0] + '_xdz' + pos[1]));
						var buttons = get_skill_buttons(xdz);
						for ( var i = 0; i < force_skills.length; i++) {
							var k = $.inArray(force_skills[i], buttons);
							if (k >= 0) {
								clickButton('playskill ' + (k + 1));
								return;
							}
						}
					}
					
				}
				
			}
		});
	} else if (cmd.substr(0, 7) == '#alias ') {
		var alias = $.trim(cmd.substr(7));
		var key, value, i = alias.indexOf(' ');
		if (i >= 0) {
			key = $.trim(alias.substr(0, i));
			value = $.trim(alias.substr(i + 1));
		} else {
			key = alias;
			value = '';
		}
		if (value) {
			if (value != aliases.get(key)) {
				aliases.put(key, value);
				console.log("set alias ok.");
			}
		} else {
			if (aliases.containsKey(key)) {
				aliases.remove(key);
				console.log("alias removed.");
			}
		}
	} else if (cmd.substr(0, 1) == '#') {
		var i = cmd.indexOf(' ');
		if (i >= 0) {
			var times = parseInt(cmd.substr(1, i - 1));
			if (!isNaN(times)) {
				cmd = $.trim(cmd.substr(i + 1));
				for ( var j = 0; j < times; j++) {
					execute_cmd(cmd);
				}
			}
		}
	} else if (cmd) {
		var pc = process_cmdline(cmd);
		if (pc[1]) {
			// clickButton('go_chat');
		} else {
			// clickButton('quit_chat');
		}
		if (pc[0]) {
			send_cmd(pc[0]);
		}
	}
}
function auto_combat(vs_info, my_id, msg) {
	var pos = check_pos(vs_info, my_id);
	if (!pos) {
		return;
	}
	var subtype = msg.get('subtype');
	if (subtype == 'add_xdz'
			|| (subtype == 'attack' && msg.get('rid') == my_id)) {
		var kee = parseInt(vs_info.get(pos[0] + '_kee' + pos[1]));
		var max_kee = parseInt(window.g_obj_map.get('msg_attrs').get('max_kee'));
		if (kee * 100 / max_kee < 50) {
			var xdz = parseInt(vs_info
					.get(pos[0] + '_xdz' + pos[1]));
			var buttons = get_skill_buttons(xdz);
			for ( var i = 0; i < force_skills.length; i++) {
				var k = $.inArray(force_skills[i], buttons);
				if (k >= 0) {
					clickButton('playskill ' + (k + 1));
					break;
				}
			}
			return;
		}
	}
	if (subtype == 'add_xdz') {
		var xdz = parseInt(vs_info.get(pos[0] + '_xdz' + pos[1]));
		var buttons = get_skill_buttons(xdz);
		select_perform(buttons);
	}
}
function process_cmdline(line) {
	var pc = [ '', true ];
	var arr = line.split(';');
	for ( var i = 0; i < arr.length; i++) {
		var cmd = $.trim(arr[i]);
		if (cmd) {
			var c = process_cmd(cmd);
			if (!c[1]) {
				pc[1] = false;
			}
			if (c[0]) {
				if (pc[0]) {
					pc[0] += ';';
				}
				pc[0] += c[0];
			}
		}
	}
	return pc;
}
function process_cmd(cmd) {
	var args = [ '', '' ];
	var i = cmd.indexOf(' ');
	if (i >= 0) {
		args[0] = $.trim(cmd.substr(0, i));
		args[1] = $.trim(cmd.substr(i + 1));
	} else {
		args[0] = cmd;
	}
	var alias = aliases.get(args[0]);
	if (alias) {
		var line = alias;
		if (args[1]) {
			line += ' ' + args[1];
		}
		return process_cmdline(line);
	}
	var pc = [ '', translate(args) ];
	if (args[0]) {
		if (pc[0]) {
			pc[0] += ';';
		}
		if (args[1]) {
			pc[0] += args[0] + ' ' + args[1];
		} else {
			pc[0] += args[0];
		}
	}
	return pc;
}
function translate(args) {
	var is_chat = false;
	if (args[0] == 'look') {
		if (!args[1]) {
			args[0] = 'golook_room';
		} else {
			var target = find_target(args[1]);
			if (target) {
				if (target[2] == 'npc') {
					args[0] = 'look_npc';
					args[1] = target[0];
				} else if (target[2] == 'item') {
					args[0] = 'look_item';
					args[1] = target[0];
				} else {
					args[0] = 'score';
					args[1] = target[0];
				}
			} else {
				arg[0] = '';
			}
		}
	} else if (args[0] == 'fight' || args[0] == 'watch') {
		if (args[0] == 'watch') {
			args[0] = 'watch_vs';
		}
		var target = find_target(args[1], [ 'npc', 'user' ]);
		if (target) {
			args[1] = target[0];
		}
	} else if (args[0] == 'kill' || args[0] == 'ask' || args[0] == 'give'
			|| args[0] == 'buy') {
		var target = find_target(args[1], [ 'npc' ]);
		if (target) {
			args[1] = target[0];
		}
	} else if (args[0] == 'get') {
		var target = find_target(args[1], [ 'item' ]);
		if (target) {
			args[1] = target[0];
		}
	} else if (args[0] == 'loot') {
		var room = window.g_obj_map.get('msg_room');
		if (room) {
			args[1] = '';
			for ( var t, i = 1; (t = room.get('item' + i)) != undefined; i++) {
				var s = t.split(',');
				if (s.length > 1 && /.+的尸体$/.test(removeSGR(s[1]))) {
					if (args[1].length > 0) {
						args[1] += '\nget ';
					}
					args[1] += s[0];
				}
			}
			args[0] = args[1].length > 0 ? 'get' : '';
		}
	} else if (args[0] == 'east' || args[0] == 'south' || args[0] == 'west'
			|| args[0] == 'north' || args[0] == 'southeast'
			|| args[0] == 'southwest' || args[0] == 'northeast'
			|| args[0] == 'northwest' || args[0] == 'up'
			|| args[0] == 'down') {
		var room = window.g_obj_map.get('msg_room');
		if (room) {
			var random = room.get('go_random');
			if (random) {
				args[1] = args[0] + '.' + random;
			} else {
				args[1] = args[0];
			}
		}
		args[0] = 'go';
	} else if (args[0] == 'fly') {
		args[0] = 'jh';
		var id = map_ids.get(args[1]);
		if (id) {
			args[1] = id;
		}
	} else if (args[0] == 'tu') {
		args[0] = 'cangbaotu_op1';
		args[1] = '';
	} else if (args[0] == 'dig') {
		args[0] = 'dig go';
		args[1] = '';
	} else if (args[0] == 'halt') {
		args[0] = 'escape';
		args[1] = '';
	} else if (args[0] == 'heal') {
		args[0] = 'recovery';
		args[1] = '';
	} else if (args[0] == 'quest') {
		args[0] = 'family_quest';
		args[1] = '';
	} else if (args[0] == 'task') {
		args[0] = 'task_quest';
		if (args[1] == 'cancel') {
			args[0] = 'auto_tasks';
		}
	} else if (args[0] == 'map') {
		args[0] = 'client_map';
		args[1] = '';
	} else if (args[0] == 'chat' || args[0] == 'rumor') {
		if (!cmd[1]) {
			cmd[0] = '';
		}
		is_chat = true;
	}
	return is_chat;
}
var cmd_queue = [];
var cmd_busy = false;
window.send_cmd = function(cmd, k) {
	cmd_queue = cmd_queue.concat(cmd.split(';'));
	if (!cmd_busy) {
		_send_cmd(k);
	}
}
var _send_cmd = function(k) {
	if (cmd_queue.length > 0) {
		cmd_busy = true;
		clickButton(cmd_queue.shift(), k);
		setTimeout(function() {
			_send_cmd(k);
		}, Math.floor(120 + Math.random() * 20));
	} else {
		cmd_busy = false;
	}
};
var cmdline = $('<div id="cmdline" style="position: fixed; left: 0px; top: 0px; width: 503px; height: 44px; border: 1px solid rgb(53, 37, 21);"><table align="center" border="0" style="width:100%"><tr><td style="width:65%" align="left"><input id="cmd_box" class="chat_input" type="text" value=""></td><td style="width:35%" align="left"><button type="button" cellpadding="0" cellspacing="0" class="cmd_click3"><span class="out2">发送</span></button></td></tr></table></div>');
var cmdbox = $(':text', cmdline);
var history_cmds = [];
var select_index = -1;
cmdline.keydown(function(e) {
	if (e.which == 27) { // ESC
		cmdline.detach();
		e.preventDefault();
	} else if (e.which == 13) { // ENTER
		$(':button', cmdline).click();
		e.preventDefault();
	} else if (e.which == 38) { // UP
		if (select_index > 0) {
			cmdbox.val(history_cmds[--select_index]);
			cmdbox.select();
			cmdbox.focus();
			e.preventDefault();
		}
	} else if (e.which == 40) { // DOWN
		if (select_index < history_cmds.length - 1) {
			cmdbox.val(history_cmds[++select_index]);
			cmdbox.select();
			cmdbox.focus();
			e.preventDefault();
		}
	}
});
$(':button', cmdline).click(function(e) {
	cmdline.detach();
	sendCommand();
});
function sendCommand() {
	var cmd = $.trim(cmdbox.val());
	if (cmd == '') {
		return;
	}
	if (history_cmds.length == 0
			|| history_cmds[history_cmds.length - 1] != cmd) {
		history_cmds.push(cmd);
		if (history_cmds.length > 20) {
			history_cmds = history_cmds.slice(-20);
		}
	}
	execute_cmd(cmd);
}
$(document).keydown(
		function(e) {
			if (e.which == 120) { // F9
				kill();
				e.preventDefault();
			} else if (e.which == 121) { // F10
				if (h_interval) {
					clearInterval(h_interval);
					h_interval = undefined;
					is_started = false;
					remove_listener(h_listener);
					h_listener = undefined;
					e.preventDefault();
				}
			} else if (e.which == 114) { // F3
				rejoin(true);
				e.preventDefault();
			} else if (e.which == 115) { // F4
				rejoin(false);
				e.preventDefault();
			} else if (e.which == 117) { // F6
				show_target = !show_target;
				e.preventDefault();
			} else if (e.which == 118) { // F7
				auto_defence = !auto_defence;
				notify_fail('auto defence '
						+ (auto_defence ? 'starting' : 'stopped'));
				e.preventDefault();
			} else if (e.which == 119) { // F8
				auto_attack = !auto_attack;
				notify_fail('auto attack '
						+ (auto_attack ? 'starting' : 'stopped'));
				e.preventDefault();
			} else if (e.which == 97) {
				execute_cmd('southwest');
			} else if (e.which == 98) {
				execute_cmd('south');
			} else if (e.which == 99) {
				execute_cmd('southeast');
			} else if (e.which == 100) {
				execute_cmd('west');
			} else if (e.which == 102) {
				execute_cmd('east');
			} else if (e.which == 103) {
				execute_cmd('northwest');
			} else if (e.which == 104) {
				execute_cmd('north');
			} else if (e.which == 105) {
				execute_cmd('northeast');
			} else if (!e.isDefaultPrevented() && e.which == 13) { // ENTER
				var $e = $('#chat_msg');
				if ($e.length > 0 && document.activeElement == $e[0]) {
					return true;
				}
				$('body').append(cmdline);
				cmdline.css('top', ($('#page').height() - 60) + 'px');
				if (history_cmds.length > 0) {
					select_index = history_cmds.length - 1;
					cmdbox.val(history_cmds[select_index]);
					cmdbox.select();
					cmdbox.focus();
				} else {
					cmdbox.val('');
					cmdbox.focus();
				}
				e.preventDefault();
			} else if (e.which == 112) { // F1
				perform();
				e.preventDefault();
			}
			return true;
		});
var qixia_id_pattern = /^(langfuyu|wangrong|pangtong|liyufei|bujinghong|fengxingzhui|guoji|wuzhen|fengnan|huoyunxieshen|niwufeng|hucangyan|huzhu)_/;
var _show_npc = window.gSocketMsg2.show_npc;
window.gSocketMsg2.show_npc = function() {
	_show_npc.apply(this, arguments);
	var id = window.g_obj_map.get('msg_npc').get('id');
	if (qixia_id_pattern.test(id)) {
		var cmd = 'ask ' + id + '\\n' + 'ask ' + id + '\\n' + 'ask ' + id
				+ '\\n' + 'ask ' + id + '\\n' + 'ask ' + id;
		var $td = $('<td align="center"><button type="button" onclick="clickButton(\''
				+ cmd + '\', 1)" class="cmd_click2">领朱果</button></td>');
		var $tr = $('#out > span.out button.cmd_click2:last').parent('td')
				.parent();
		if ($('> td', $tr).length >= 4) {
			$tr = $tr.parent().append('<tr></tr>');
		}
		$tr.append($td);
	}
};
window.mingy_addon = 1;
notify_fail('addon loaded');