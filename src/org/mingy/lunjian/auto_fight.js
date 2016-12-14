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
if (ctx[2]) {
	
}