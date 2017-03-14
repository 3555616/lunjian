if (!window.g_obj_map) {
	return null;
}
var fresh = arguments.length > 1 ? arguments[1] : false, msg = window.g_obj_map.get(arguments[0]);
if (!msg || (fresh && msg.get('_robot_'))) {
	return null;
}
var ret = {};
var keys = msg.keys();
for ( var i = 0; i < keys.length; i++) {
	ret[keys[i]] = msg.get(keys[i]);
}
if (fresh) {
	msg.put('_robot_', true);
}
return ret;