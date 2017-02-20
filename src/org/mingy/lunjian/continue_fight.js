var pfms = arguments[0], wait = arguments[1], heal = arguments[2], safe = arguments[3], fast = arguments[4], fastpfm = arguments[5], ctx = arguments[6], point, hp;
if (!window.g_obj_map || !window.g_obj_map.get('msg_attrs')) {
	return null;
}
var name = window.g_obj_map.get('msg_attrs').get('name'), pos;
$('td#vs11,td#vs12,td#vs13,td#vs14,td#vs21,td#vs22,td#vs23,td#vs24').each(
		function() {
			if ($($(this).contents()[0]).text() == name) {
				pos = $(this).attr('id').substr(2, 2);
				return false;
			}
		});
if (!pos) {
	return null;
}
ctx[4] = true;
var style = $('#barxdz_bar').attr('style');
if (!style) {
	return null;
}
var k = style.indexOf('width:')
if (k >= 0) {
	style = style.substr(k + 6);
} else {
	return null;
}
k = style.indexOf(';');
var width = k >= 0 ? $.trim(style.substring(0, k)) : $.trim(style);
if (width && width.charAt(width.length - 1) == '%') {
	point = parseFloat(width.substr(0, width.length - 1));
} else {
	return null;
}
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
	var hp = 0, $t = pos.charAt(0) == '1' ? $('#vs_hp21,#vs_hp22,vs_hp23，vs_hp24') : $('#vs_hp11,#vs_hp12,vs_hp13，vs_hp14');
	$t.each(function () {
		hp += parseInt($('> i > span', this).text());
	});
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
			ctx[0] = ++index < pfms.length ? index : 0;
			ctx[1] = true;
			ctx[3] = 'perform ' + pfm;
		}
		return ctx;
	}
}
return ctx;