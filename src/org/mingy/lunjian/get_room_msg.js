if (!window.g_obj_map) {
	return null;
}
var walk = arguments.length > 0 ? arguments[0] : false, msg = window.g_obj_map.get('msg_room');
if (!msg || (walk && msg.get('_robot_'))) {
	return null;
}
var ret = {};
var keys = msg.keys();
for ( var i = 0; i < keys.length; i++) {
	ret[keys[i]] = msg.get(keys[i]);
}
if (walk) {
	msg.put('_robot_', true);
}
return ret;