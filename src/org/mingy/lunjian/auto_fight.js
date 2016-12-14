var pfms = arguments[0], heal = arguments[1], safe = arguments[2], fast = arguments[3], ctx = arguments[4];
var me = g_obj_map.get('msg_attrs').get('name'), pos, point, hp;
$('td#vs11,td#vs12,td#vs13,td#vs14,td#vs21,td#vs22,td#vs23,td#vs24').each(
		function() {
			if ($($(this).contents()[0]).text() == me) {
				pos = $(this).attr('id').substr(2, 2);
				return false;
			}
		});
if (!pos) {
	return false;
}
var width = $('#barxdz_bar').css('width');
if (width && width.charAt(width.length - 1) == '%') {
	point = parseFloat(width.substr(0, width.length - 1));
} else {
	return false;
}
if (point < 20) {
	ctx[1] = false;
	return ctx;
}
if (!ctx[2] && heal && safe && safe > 0) {
	width = $('#barvader' + pos).css('width');
	if (width && width.charAt(width.length - 1) == '%') {
		hp = parseFloat(width.substr(0, width.length - 1));
		if (hp < safe) {
			ctx[1] = false;
			ctx[2] = true;
		}
	} else {
		return false;
	}
}
var findButton = function(name) {
	var $b = null;
	$('button.cmd_skill_button').each(function() {
		if ($(this).text == name) {
			$b = $(this);
			return false;
		}
	});
	return $b;
};
if (ctx[2]) {
	var $b = findButton(heal);
	if ($b) {
		String onclick = $b.attr('onclick');
		if (onclick != 'clickButton(\'0\', 0)') {
			$b.click();
			ctx[1] = false;
			ctx[2] = false;
			ctx[3] = "perform " + heal;
		}
		return ctx;
	}
}
var getTargetHp = function() {
	var hp = 0, $t = pos.charAt(0) == '1' ? $('#vs_hp21,#vs_hp22,vs_hp23，vs_hp24') : $('#vs_hp11,#vs_hp12,vs_hp13，vs_hp14');
	%t.each(function () {
		hp += parseInt($('> i > span', this).text());
	});
	return hp;
};
if (!ctx[1] && !ctx[2]) {
	if (getTargetHp() < fast) {
		if (point < 40) {
			return ctx;
		}
	} else {
		if (point < 100) {
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
		String onclick = $b.attr('onclick');
		if (onclick != 'clickButton(\'0\', 0)') {
			$b.click();
			ctx[0] = ++index < pfms.length ? index : 0;
			ctx[2] = true;
			ctx[3] = "perform " + pfm;
		}
		return ctx;
	}
}
return true;