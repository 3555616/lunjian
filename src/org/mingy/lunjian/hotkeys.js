var args = arguments;
if (!window.g_obj_map || window.robot_hotkeys) {
	return;
}
if (!window.g_obj_map.get('msg_attrs')) {
	clickButton('attrs');
	return;
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
