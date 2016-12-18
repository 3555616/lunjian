if (!window.g_obj_map) {
	return [];
}
var types = arguments[0], msg = window.g_obj_map.get('msg_room'), targets = [];
if (!msg) {
	return targets;
}
var check = function(t, n) {
	if (types && $.inArray(t, types) < 0) {
		return false;
	}
	var s = n.split(',');
	if (s.length > 1) {
		return [s[0], s[1], t];
	} else {
		return [s[0], null, t];
	}
};
for (var t, i = 1; (t = msg.get('npc' + i)) != undefined; i++) {
	var r = check('npc', t);
	if (r) {
		targets.push(r);
	}
}
for (var t, i = 1; (t = msg.get('item' + i)) != undefined; i++) {
	var r = check('item', t);
	if (r) {
		targets.push(r);
	}
}
for (var t, i = 1; (t = msg.get('user' + i)) != undefined; i++) {
	var r = check('user', t);
	if (r) {
		targets.push(r);
	}
}
for (var t, i = 1; (t = msg.get('cmd' + i)) != undefined; i++) {
	var r = check('cmd', t);
	if (r) {
		targets.push(r);
	}
}
return targets;