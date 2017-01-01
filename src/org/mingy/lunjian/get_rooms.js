if (!window.g_obj_map) {
	return [];
}
var dirs = arguments[0], msg = window.g_obj_map.get('msg_room'), targets = [];
if (!msg) {
	return targets;
}
var check = function(key) {
	if (key != 'east' && key != 'south' && key != 'west' && key != 'north'
			&& key != 'southeast' && key != 'northeast' && key != 'southwest'
			&& key != 'northwest' && key != 'up' && key != 'down'
			&& key != 'enter' && key != 'out' && key != 'eastup'
			&& key != 'southup' && key != 'westup' && key != 'northup'
			&& key != 'eastdown' && key != 'southdown' && key != 'westdown'
			&& key != 'northdown') {
		return false;
	}
	if (dirs && $.inArray(key, dirs) < 0) {
		return false;
	}
	return true;
};
targets.push([ 'this', msg.get('short') ]);
var keys = msg.keys();
for ( var i = 0; i < keys.length; i++) {
	if (check(keys[i])) {
		targets.push([ keys[i], msg.get(keys[i]) ]);
	}
}
return targets;