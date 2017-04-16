var args = arguments;
if (args.length == 0) {
	return;
}
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
$(document).keydown(function(e) {
	for (var i = 0; i < args.length; i++) {
		if (e.which == 112 + i) {
			perform(args[i]);
			return false;
		}
	}
	return true;
});
window.robot_hotkeys = 1;
notify_fail('hotkey loaded');