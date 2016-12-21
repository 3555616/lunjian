if (!window.g_obj_map) {
	return null;
}
var msgs = window.g_obj_map.get('msg_npc');
if (!msgs) {
	return null;
}
var desc = {
		id : msgs.get('id'),
		name : msgs.get('name'),
		desc : msgs.get('long'),
		level : msgs.get('lvl'),
		cmds : []
};
for (var i = 1; ; i++) {
	var label = msgs.get('cmd' + i + '_name');
	if (label) {
		desc.cmds.push({name : label, action : msgs.get('cmd' + i)});
	} else {
		break;
	}
}
return desc;