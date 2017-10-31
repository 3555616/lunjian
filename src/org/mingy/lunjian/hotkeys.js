var args = arguments;
if (!window.g_obj_map || window.robot_hotkeys) {
	return;
}
if (!window.g_obj_map.get('msg_attrs')) {
	clickButton('attrs');
	return;
}
var _dispatch_message = window.gSocketMsg.dispatchMessage;
var show_attack_target = true;
var auto_attack = false;
var vs_text1 = '', vs_text2 = '';
var user_id_pattern = /^u[0-9]+/;
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
									if (auto_attack && user_id_pattern.test(id)) {
										autopfm(vs_info, pfm, v1, p1, v2, p2);
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
function autopfm(vs_info, pfm, v1, p1, v2, p2) {
	var xdz = parseInt(vs_info.get(v1 + '_xdz' + p1));
	var max_kee = vs_info.get(v2 + '_max_kee' + p2);
	var kee = vs_info.get(v2 + '_kee' + p2);
	var k = 0;
	if (max_kee < 30000) {
		k = 0;
	} else if (max_kee < 100000) {
		k = 1;
	} else if (max_kee < 300000) {
		k = kee < 100000 ? 1 : 2;
	} else {
		k = 2;
	}
	if (skills.containsKey(pfm)) {
		k = k > 0 ? k - 1 : 0;
	}
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
var h_interval, is_started = false, timestamp;
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
		}
		timestamp = new Date().getTime();
		clickButton('kill ' + npc + '\nwatch_vs ' + npc);
		var my_id = window.g_obj_map.get('msg_attrs').get('id');
		h_interval = setInterval(function() {
			var is_fighting = false, do_kill = false;
			if (window.is_fighting) {
				var vs_info = window.g_obj_map.get('msg_vs_info');
				if (vs_info) {
					is_started = true;
					var n1 = 0, n2 = 0, side = 0;
					for (var i = 1; i <= 4; i++) {
						var qi = vs_info.get('vs1_kee' + i);
						if (!qi) {
							continue;
						}
						qi = parseInt(qi);
						if (qi <= 0) {
							continue;
						}
						n1++;
						if (!is_fighting && vs_info.get('vs1_pos' + i) == my_id) {
							is_fighting = true;
						}
						if (!side && vs_info.get('vs1_pos' + i) == npc) {
							side = 1;
						}
					}
					for (var i = 1; i <= 4; i++) {
						var qi = vs_info.get('vs2_kee' + i);
						if (!qi) {
							continue;
						}
						qi = parseInt(qi);
						if (qi <= 0) {
							continue;
						}
						n2++;
						if (!is_fighting && vs_info.get('vs2_pos' + i) == my_id) {
							is_fighting = true;
						}
						if (!side && vs_info.get('vs2_pos' + i) == npc) {
							side = 2;
						}
					}
					if (side == 1) {
						do_kill = n2 < 4;
					} else if (side == 2) {
						do_kill = n1 < 4
					} else {
						is_fighting = true;
					}
				}
				if (is_fighting) {
					clearInterval(h_interval);
					h_interval = undefined;
					is_started = false;
				} else if (do_kill) {
					var t = new Date().getTime();
					if (t - timestamp >= 1000) {
						timestamp = t;
						clickButton('kill ' + npc + '\nwatch_vs ' + npc);
					}
				}
			} else if (is_started) {
				clearInterval(h_interval);
				h_interval = undefined;
				is_started = false;
			} else {
				clickButton('kill ' + npc + '\nwatch_vs ' + npc);
			}
		}, 50);
	}
};
$(document).keydown(function(e) {
	if (e.which == 120) {
		kill();
	} else if (e.which == 121) {
		if (h_interval) {
			clearInterval(h_interval);
			h_interval = undefined;
			is_started = false;
		}
	} else if (e.which == 119) {
		auto_attack = !auto_attack;
		notify_fail('auto attack ' + (auto_attack ? 'starting' : 'stopped'));
	} else if (e.which == 123) {
		show_attack_target = !show_attack_target;
	} else {
		for (var i = 0; i < args.length; i++) {
			if (e.which == 112 + i) {
				perform(args[i]);
				return false;
			}
		}
	}
	return true;
});
window.robot_hotkeys = 1;
notify_fail('hotkey loaded');
