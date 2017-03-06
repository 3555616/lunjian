var pos = arguments[0], pfms = arguments[1], wait = arguments[2], heal = arguments[3], safe = arguments[4], fast = arguments[5], ctx = arguments[6], point, hp;
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
	style = $('#barvader' + pos).attr('style');
	if (!style) {
		return null;
	}
	k = style.indexOf('width:')
	if (k >= 0) {
		style = style.substr(k + 6);
	} else {
		return null;
	}
	k = style.indexOf(';');
	width = k >= 0 ? $.trim(style.substring(0, k)) : $.trim(style);
	if (width && width.charAt(width.length - 1) == '%') {
		hp = parseFloat(width.substr(0, width.length - 1));
		if (hp < safe) {
			ctx[1] = false;
			ctx[2] = true;
		}
	} else {
		return null;
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
	var hp = 0, $t = pos.charAt(0) == '1' ? $('#vs_hp21,#vs_hp22,#vs_hp23,#vs_hp24') : $('#vs_hp11,#vs_hp12,#vs_hp13,#vs_hp14');
	$t.each(function () {
		hp += parseInt($('> i > span', this).text());
	});
	return hp;
};
var pfms_point = pfms.length * 20;
$(pfms).each(function() {
	if (this == '覆雨剑法' || this == '织冰剑法' || this == '翻云刀法' || this == '飞刀绝技'
		|| this == '九天龙吟剑法' || this == '排云掌法' || this == '孔雀翎' || this == '如来神掌'
		|| this == '雪饮狂刀') {
		pfms_point += 10;
	}
});
if (!ctx[1] && !ctx[2]) {
	if (getTargetHp() < fast) {
		if (point < pfms_point) {
			return ctx;
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