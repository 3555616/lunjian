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
var vs_text;
window.gSocketMsg.dispatchMessage = function(msg) {
	_dispatch_message.apply(this, arguments);
	if (show_attack_target && msg.get('type') == 'vs') {
		if (msg.get('subtype') == 'text') {
			vs_text = msg.get('msg');
		} else if (msg.get('subtype') == 'playskill' && parseInt(msg.get('ret')) == 0) {
			var my_id = window.g_obj_map.get('msg_attrs').get('id');
			if (msg.get('uid') == my_id) {
				var vid = msg.get('vid');
				var vs_info = window.g_obj_map.get('msg_vs_info');
				if (vs_info) {
					var v;
					for (var i = 1; i <= 4; i++) {
						if (vs_info.get('vs1_pos_v' + i) == vid) {
							v = 'vs2';
							break;
						}
					}
					v = v || 'vs1';
					for (var i = 1; i <= 4; i++) {
						var name = vs_info.get(v + '_name' + i);
						if (name) {
							if (vs_text.indexOf(name) >= 0) {
								notify_fail(HIG + 'ATTACK: ' + name);
								break;
							}
						}
					}
				}
			}
		}
	}
};
var skills = new Map();
skills.set('九天龙吟剑法', ['排云掌法', '雪饮狂刀']);
skills.set('覆雨剑法', ['翻云刀法', '如来神掌']);
skills.set('织冰剑法', ['孔雀翎', '飞刀绝技']);
skills.set('排云掌法', ['九天龙吟剑法', '雪饮狂刀']);
skills.set('如来神掌', ['覆雨剑法', '孔雀翎']);
skills.set('雪饮狂刀', ['九天龙吟剑法', '排云掌法']);
skills.set('翻云刀法', ['覆雨剑法', '飞刀绝技']);
skills.set('飞刀绝技', ['翻云刀法', '织冰剑法']);
skills.set('孔雀翎', ['如来神掌', '织冰剑法']);
function autopfm(vs_info, msg, v, i) {
	var max_kee = vs_info.get(v + '_max_kee' + i);
	var kee = vs_info.get(v + '_kee' + i);
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
	String pfm = msg.get('name').replace(/\u001e\[[;0-9]+m/g, '');
	if (skills.has(pfm)) {
		k = k > 0 ? k - 1 : 0;
	}
	var buttons = [];
	$('button.cmd_skill_button').each(function() {
		buttons.push($(this).text().replace(/\u001e\[[;0-9]+m/g, ''));
	});
	if (k == 1) {
		var pfms = skills.get(pfm);
		if (pfms) {
			for (var j = 0; j < buttons.length; j++) {
				if (pfms.indexOf(buttons[j]) >= 0) {
					clickButton('playskill ' + (j + 1));
					break;
				}
			}
		} else {
			for (var j = 0; j < buttons.length; j++) {
				if (skills.has(buttons[j])) {
					clickButton('playskill ' + (j + 1));
					break;
				}
			}
		}
	} else if (k == 2) {
		var pfms = skills.get(pfm);
		if (pfms) {
			for (var j = 0; j < buttons.length; j++) {
				if (pfms.indexOf(buttons[j]) >= 0) {
					clickButton('playskill ' + (j + 1));
					break;
				}
			}
		} else {
			
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
notify_fail(HIG + 'hotkey loaded');
