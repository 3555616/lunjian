var pfms = arguments[0], wait = arguments[1], heal = arguments[2], safe = arguments[3], fast = arguments[4], fastpfm = arguments[5], halt = arguments[6], ctx = arguments[7], point, hp;
if (!window.is_fighting || !window.g_obj_map || !window.g_obj_map.get('msg_attrs')) {
	return null;
}
var my_id = window.g_obj_map.get('msg_attrs').get('id'), pos;
var vs_info = window.g_obj_map.get('msg_vs_info');
if (!vs_info) {
	return null;
}
for (var i = 1; i <= 4; i++) {
	var qi = vs_info.get('vs1_kee' + i);
	if (!qi) {
		continue;
	}
	qi = parseInt(qi);
	if (qi <= 0) {
		continue;
	}
	if (vs_info.get('vs1_pos' + i) == my_id) {
		pos = '1' + i;
		point = parseInt(vs_info.get('vs1_xdz' + i)) * 10;
		break;
	}
}
if (!pos) {
	for (var i = 1; i <= 4; i++) {
		var qi = vs_info.get('vs2_kee' + i);
		if (!qi) {
			continue;
		}
		qi = parseInt(qi);
		if (qi <= 0) {
			continue;
		}
		if (vs_info.get('vs2_pos' + i) == my_id) {
			pos = '2' + i;
			point = parseInt(vs_info.get('vs2_xdz' + i)) * 10;
			break;
		}
	}
}
if (!pos) {
	return null;
}
ctx[4] = true;
if (point < 20) {
	ctx[1] = false;
	return ctx;
}
if (!ctx[2] && heal && safe && safe > 0) {
	var hp = parseInt($('> i > span', '#vs_hp' + pos).text());
	if (hp < safe) {
		ctx[1] = false;
		ctx[2] = true;
	}
}
var findButton = function(name) {
	var $b = null;
	$('button.cmd_skill_button').each(function() {
		if ($(this).text() == name) {
			$b = $(this);
			return false;
		}
	});
	return $b;
};
if (ctx[2]) {
	var $b = findButton(heal);
	if ($b) {
		var onclick = $b.attr('onclick');
		if (onclick != 'clickButton(\'0\', 0)') {
			$b.click();
			ctx[1] = false;
			ctx[2] = false;
			ctx[3] = 'perform ' + heal;
		}
		return ctx;
	}
}
var getTargetHp = function() {
	var hp = 0;
	for (var i = 1; i <= 4; i++) {
		var qi = vs_info.get((pos.charAt(0) == '1' ? 'vs2_kee' : 'vs1_kee') + i);
		if (!qi) {
			continue;
		}
		qi = parseInt(qi);
		if (qi <= 0) {
			continue;
		}
		hp += qi;
	}
	return hp;
};
if (!ctx[1] && !ctx[2]) {
	if (fastpfm && getTargetHp() < fast) {
		if (point < 20) {
			return ctx;
		} else {
			var $b = findButton(fastpfm);
			if ($b) {
				var onclick = $b.attr('onclick');
				if (onclick != 'clickButton(\'0\', 0)') {
					$b.click();
					if (halt) {
						clickButton('escape');
					}
					ctx[3] = 'perform ' + fastpfm;
				}
				return ctx;
			}
		}
	} else {
		if (point < wait) {
			return ctx;
		}
	}
}
var cycle = [];
for (var i = ctx[0]; i < pfms.length; i++) {
	cycle.push(i);
}
for (var i = 0; i < ctx[0]; i++) {
	cycle.push(i);
}
for (var i = 0; i < cycle.length; i++) {
	var index = cycle[i], pfm = pfms[index];
	var $b = findButton(pfm);
	if ($b) {
		var onclick = $b.attr('onclick');
		if (onclick != 'clickButton(\'0\', 0)') {
			$b.click();
			if (halt) {
				clickButton('escape');
			}
			ctx[0] = ++index < pfms.length ? index : 0;
			ctx[1] = true;
			ctx[3] = 'perform ' + pfm;
		}
		return ctx;
	}
}
return ctx;