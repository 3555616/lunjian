var perform_list = arguments.length > 0 ? arguments[0] : null;
var friend_list = arguments.length > 1 ? arguments[1] : null;
if (!window.g_obj_map || window.robot_hotkeys) {
	return;
}
if (!window.g_obj_map.get('msg_attrs')) {
	clickButton('attrs');
	return;
}
var _dispatch_message = window.gSocketMsg.dispatchMessage;
var join_combat_target = null;
var show_attack_target = true;
var auto_attack = false;
var vs_text1 = '', vs_text2 = '';
var user_id_pattern1 = /^u[0-9]+$/;
var user_id_pattern2 = /^u[0-9]+\-/;
var kuafu_name_pattern = /^\[[0-9]+\]/;
var ansi_color_pattern = /\u001b\[[;0-9]+m/g;
var skills = new Map();
skills.put('九天龙吟剑法', ['排云掌法', '雪饮狂刀']);
skills.put('覆雨剑法', ['翻云刀法', '如来神掌']);
skills.put('织冰剑法', ['孔雀翎', '飞刀绝技']);
skills.put('排云掌法', ['九天龙吟剑法', '雪饮狂刀']);
skills.put('如来神掌', ['覆雨剑法', '孔雀翎']);
skills.put('雪饮狂刀', ['九天龙吟剑法', '排云掌法']);
skills.put('翻云刀法', ['覆雨剑法', '飞刀绝技']);
skills.put('飞刀绝技', ['翻云刀法', '织冰剑法']);
skills.put('孔雀翎', ['如来神掌', '织冰剑法']);
var skill_chains = ['九天龙吟剑法', '覆雨剑法', '织冰剑法', '排云掌法', '如来神掌', '雪饮狂刀', '翻云刀法', '飞刀绝技', '孔雀翎', '道种心魔经', '生生造化功', '幽影幻虚步', '万流归一'];
var defence_patterns = [/(.*)顿时被冲开老远，失去了攻击之势！/, /(.*)被(.*)的真气所迫，只好放弃攻击！/, /(.*)衣裳鼓起，真气直接将(.*)逼开了！/, /(.*)找到了闪躲的空间！/, /(.*)朝边上一步闪开！/, /面对(.*)的攻击，(.*)毫不为惧！/, /(.*)使出“(.*)”，希望扰乱(.*)的视线！/];
window.gSocketMsg.dispatchMessage = function(msg) {
	_dispatch_message.apply(this, arguments);
	if (join_combat_target && msg.get('type') == 'vs') {
		if (msg.get('subtype') == 'vs_info' || (msg.get('subtype') == 'die' && msg.get('uid') != join_combat_target)) {
			var vs_info = window.g_obj_map.get('msg_vs_info');
			try_join_combat(vs_info, join_combat_target);
		}
	}
	if ((show_attack_target || auto_attack) && msg.get('type') == 'vs') {
		if (msg.get('subtype') == 'text') {
			vs_text1 = vs_text2;
			vs_text2 = msg.get('msg');
		} else if (msg.get('subtype') == 'playskill' && parseInt(msg.get('ret')) == 0) {
			var my_id = window.g_obj_map.get('msg_attrs').get('id');
			if (msg.get('uid') == my_id) {
				var vid = msg.get('vid');
				var vs_info = window.g_obj_map.get('msg_vs_info');
				if (vs_info) {
					var v1, v2, p1, p2;
					for (var i = 1; i <= 4; i++) {
						if (vs_info.get('vs1_pos_v' + i) == vid) {
							v1 = 'vs1';
							p1 = i;
							v2 = 'vs2';
							break;
						}
					}
					if (!v1) {
						for (var i = 1; i <= 4; i++) {
							if (vs_info.get('vs2_pos_v' + i) == vid) {
								v1 = 'vs2';
								p1 = i;
								v2 = 'vs1';
								break;
							}
						}
					}
					for (var i = 1; i <= 4; i++) {
						var name = vs_info.get(v2 + '_name' + i);
						if (name) {
							if (my_id.indexOf('-') >= 0 && kuafu_name_pattern.test(name)) {
								var j = name.indexOf(']');
								name = name.substr(0, j) + '区' + name.substr(j, name.length - j);
							}
							var pfm = msg.get('name').replace(ansi_color_pattern, '');
							vs_text = skill_chains.indexOf(pfm) >= 0 ? vs_text1 + vs_text2 : vs_text2;
							if (vs_text.indexOf(name) >= 0) {
								var is_defence = false;
								for (var j = 0; j < defence_patterns.length; j++) {
									if (defence_patterns[j].test(vs_text)) {
										is_defence = true;
										break;
									}
								}
								if (!is_defence) {
									if (show_attack_target) {
										notify_fail(HIG + 'ATTACK: ' + name);
									}
									p2 = i;
									var id = vs_info.get(v2 + '_pos' + i);
									if (auto_attack && is_player(id) && get_friend_index(id) < 0) {
										auto_pfm(vs_info, pfm, v1, p1, v2, p2);
									}
								}
								break;
							}
						}
					}
				}
			}
		}
	}
};
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
	for (var i = 1; i <= 4; i++) {
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
		clickButton('fight ' + player_id);
	} else if (t - last_kill_time >= 1000) {
		last_kill_time = t;
		clickButton('kill ' + target);
	}
	return true;
}
function check_pos(vs_info, target) {
	if (vs_info) {
		for (var i = 1; i <= 4; i++) {
			if (vs_info.get('vs1_pos' + i) == target) {
				return ['vs1', i];
			} else if (vs_info.get('vs2_pos' + i) == target) {
				return ['vs2', i];
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
		var buttons = [];
		for (var i = 0; i < 4; i++) {
			var button = window.g_obj_map.get('skill_button' + (i + 1));
			if (button && parseInt(button.get('xdz')) <= xdz) {
				buttons.push(button.get('name').replace(ansi_color_pattern, ''));
			} else {
				buttons.push('');
			}
		}
		if (k == 1) {
			var pfms = skills.get(pfm);
			if (pfms) {
				for (var i = 0; i < buttons.length; i++) {
					if (buttons[i] && pfms.indexOf(buttons[i]) >= 0) {
						clickButton('playskill ' + (i + 1));
						return;
					}
				}
			}
			for (var i = 0; i < buttons.length; i++) {
				if (buttons[i] && skills.containsKey(buttons[i])) {
					clickButton('playskill ' + (i + 1));
					break;
				}
			}
		} else if (k == 2) {
			var pfms = skills.get(pfm);
			if (pfms) {
				for (var i = 0; i < buttons.length; i++) {
					if (buttons[i] && pfms.indexOf(buttons[i]) >= 0) {
						clickButton('playskill ' + (i + 1));
						return;
					}
				}
			}
			for (var i = 0; i < buttons.length; i++) {
				if (buttons[i]) {
					pfms = skills.get(buttons[i]);
					if (pfms) {
						for (var j = i + 1; j < buttons.length; j++) {
							if (buttons[j] && pfms.indexOf(buttons[j]) >= 0) {
								clickButton('playskill ' + (i + 1) + '\nplayskill ' + (j + 1));
								return;
							}
						}
					}
				}
			}
			for (var i = 0; i < buttons.length; i++) {
				if (buttons[i] && skills.containsKey(buttons[i])) {
					clickButton('playskill ' + (i + 1));
					break;
				}
			}
		}
	}
}
window.send_cmd = function(cmds, k) {
	var arr = cmds.split('\n');
	if (arr.length > 4) {
		_send_cmd(arr, k, 0);
	} else {
		clickButton(cmds, k);
	}
};
var _send_cmd = function(cmds, k, i) {
	clickButton(cmds[i], k);
	if (++i < cmds.length) {
		setTimeout(function() {
			_send_cmd(cmds, k, i);
		}, Math.floor(100 + Math.random() * 20));
	}
};
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
		for (var i = 1; i <= 4; i++) {
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
			var max_force = parseInt(vs_info.get(pos[0] + '_max_force' + pos[1]));
			if (max_force - force >= 20000) {
				for (var i = 0; i < 3; i++) {
					cmd += 'items use snow_qiannianlingzhi\n';
				}
			}
			cmd += 'kill ' + npc_id;
			clickButton(cmd);
		}
	}
}
var perform = function(pfm_str) {
	var skills = [];
	$('button.cmd_skill_button').each(function() {
		skills.push($(this).text());
	});
	if (skills.length == 0) {
		return;
	}
	var cmds = '';
	var pfms = pfm_str.split('|');
	for (var i = 0; i < pfms.length; i++) {
		var pfm = pfms[i];
		for (var j = 0; j < skills.length; j++) {
			if (skills[j].indexOf(pfm) >= 0) {
				if (cmds.length > 0) {
					cmds += '\n';
				}
				cmds += 'playskill ' + (j + 1);
				break;
			}
		}
	}
	if (cmds.length > 0) {
		clickButton(cmds);
	}
};
var h_interval, is_started = false;
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
			join_combat_target = null;
		}
		join_combat_target = npc;
		last_kill_time = new Date().getTime();
		clickButton('kill ' + npc + '\nwatch_vs ' + npc);
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
					clearInterval(h_interval);
					h_interval = undefined;
					is_started = false;
					join_combat_target = null;
				} else {
					try_join_combat(vs_info, npc, true);
				}
			} else if (is_started) {
				clearInterval(h_interval);
				h_interval = undefined;
				is_started = false;
				join_combat_target = null;
			} else {
				last_kill_time = new Date().getTime();
				clickButton('kill ' + npc + '\nwatch_vs ' + npc);
			}
		}, 150);
	}
};
var cmdline = $('<div id="cmdline" style="position: fixed; left: 0px; top: 0px; width: 503px; height: 44px; border: 1px solid rgb(53, 37, 21);"><table align="center" border="0" style="width:100%"><tr><td style="width:65%" align="left"><input id="cmd_box" class="chat_input" type="text" value=""></td><td style="width:35%" align="left"><button type="button" cellpadding="0" cellspacing="0" onclick="sendCommand();" class="cmd_click3"><span class="out2">发送</span></button></td></tr></table></div>');
var cmdbox = $(':text', cmdline);
var history_cmds = [];
var select_index = -1;
cmdline.keydown(function(e) {
	if (e.which == 27) { // ESC
		cmdline.detach();
	} else if (e.which == 13) { // ENTER
		cmdline.detach();
		sendCommand();
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
function sendCommand() {
	var cmd = $.trim(cmdbox.val());
	if (cmd == '') {
		return;
	}
	if (history_cmds.length == 0 || history_cmds[history_cmds.length - 1] != cmd) {
		history_cmds.push(cmd);
		if (history_cmds.length > 20) {
			history_cmds = history_cmds.slice(-20);
		}
	}
}
$(document).keydown(function(e) {
	if (e.which == 120) { // F9
		kill();
	} else if (e.which == 121) { // F10
		if (h_interval) {
			clearInterval(h_interval);
			h_interval = undefined;
			is_started = false;
			join_combat_target = null;
		}
	} else if (e.which == 115) { // F4
		rejoin(false);
	} else if (e.which == 118) { // F7
		rejoin(true);
	} else if (e.which == 119) { // F8
		auto_attack = !auto_attack;
		notify_fail('auto attack ' + (auto_attack ? 'starting' : 'stopped'));
	} else if (e.which == 123) { // F12
		show_attack_target = !show_attack_target;
	} else if (!e.isDefaultPrevented() && e.which == 13) { // ENTER
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
	} else if (perform_list) {
		for (var i = 0; i < perform_list.length; i++) {
			if (e.which == 112 + i) {
				perform(perform_list[i]);
				return false;
			}
		}
	}
	return true;
});
var qixia_id_pattern = /^(langfuyu|wangrong|pangtong|liyufei|bujinghong|fengxingzhui|guoji|wuzhen|fengnan|huoyunxieshen|niwufeng|hucangyan|huzhu)_/;
var _show_npc = window.gSocketMsg2.show_npc;
window.gSocketMsg2.show_npc = function() {
	_show_npc.apply(this, arguments);
	var id = window.g_obj_map.get('msg_npc').get('id');
	if (qixia_id_pattern.test(id)) {
		var cmd = 'ask ' + id + '\\n' + 'ask ' + id + '\\n' + 'ask ' + id + '\\n' + 'ask ' + id + '\\n' + 'ask ' + id;
		var $td = $('<td align="center"><button type="button" onclick="clickButton(\''
				+ cmd + '\', 1)" class="cmd_click2">领朱果</button></td>');
		var $tr = $('#out > span.out button.cmd_click2:last').parent('td').parent();
		if ($('> td', $tr).length >= 4) {
			$tr = $tr.parent().append('<tr></tr>');
		}
		$tr.append($td);
	}
};
window.robot_hotkeys = 1;
notify_fail('hotkey loaded');
