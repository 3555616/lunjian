if (!window.g_obj_map || !window.g_obj_map.get('msg_attrs') || !window.is_fighting) {
	return null;
}
var name, pos;
if (arguments.length > 0) {
	name = arguments[0];
} else {
	name = window.g_obj_map.get('msg_attrs').get('name');
}
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
return pos;