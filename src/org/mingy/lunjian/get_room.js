if (!window.g_obj_map) {
	return null;
}
var msg = window.g_obj_map.get('msg_room');
if (!msg || msg.get('_robot_')) {
	return null;
}
var ret = {};
var keys = msg.keys();
for ( var i = 0; i < keys.length; i++) {
	ret[keys[i]] = msg.get(keys[i]);
}
msg.put('_robot_', true);
return ret;