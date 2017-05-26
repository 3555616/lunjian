var args = arguments;
if (!window.g_obj_map || window.robot_hotkeys) {
	return;
}
if (!window.g_obj_map.get('msg_attrs')) {
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
var h_interval;
var kill = function() {
	var cmd = null;
	$('#out > span.out button.cmd_click2').each(function() {
		$e = $(this);
		if ($e.text() == '杀死') {
			var onclick = $e.attr('onclick');
			var i = onclick.indexOf('\'');
			var j = onclick.indexOf('\'', i + 1);
			cmd = onclick.substring(i + 1, j);
			return false;
		}
	});
	if (cmd) {
		h_interval = setInterval(function() {
			var is_fighting = false;
			if (window.is_fighting) {
				var name = window.g_obj_map.get('msg_attrs').get('name');
				$('td#vs11,td#vs12,td#vs13,td#vs14,td#vs21,td#vs22,td#vs23,td#vs24').each(
						function() {
							if ($($(this).contents()[0]).text() == name) {
								is_fighting = true;
								return false;
							}
						});
			}
			if (!is_fighting) {
				clickButton(cmd);
			} else {
				clearInterval(h_interval);
				h_interval = undefined;
			}
		}, 100);
	}
};
$(document).keydown(function(e) {
	if (e.which == 120) {
		kill();
	} else if (e.which == 121) {
		clearInterval(h_interval);
		h_interval = undefined;
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