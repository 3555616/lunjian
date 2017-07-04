var vs1 = [], vs2 = [], msgs = [], pfms = [], pt = 0;
if (!window.is_fighting) {
	return null;
}
var my_id = window.g_obj_map.get('msg_attrs').get('id');
var vs_info = window.g_obj_map.get('msg_vs_info');
if (!vs_info) {
	return null;
}
for (var i = 1; i <= 4; i++) {
	var qi = vs_info.get('vs1_kee' + i);
	if (!qi) {
		continue;
	}
	qi = parseInt(qi);
	if (qi <= 0) {
		continue;
	}
	vs1.push({id: vs_info.get('vs1_pos' + i),
			name: vs_info.get('vs1_name' + i),
			qi: qi,
			max_qi: parseInt(vs_info.get('vs1_max_kee' + i)),
			neili: parseInt(vs_info.get('vs1_force' + i)),
			pt: parseInt(vs_info.get('vs1_xdz' + i))});
}
for (var i = 1; i <= 4; i++) {
	var qi = vs_info.get('vs2_kee' + i);
	if (!qi) {
		continue;
	}
	qi = parseInt(qi);
	if (qi <= 0) {
		continue;
	}
	vs2.push({id: vs_info.get('vs2_pos' + i),
			name: vs_info.get('vs2_name' + i),
			qi: qi,
			max_qi: parseInt(vs_info.get('vs2_max_kee' + i)),
			neili: parseInt(vs_info.get('vs2_force' + i)),
			pt: parseInt(vs_info.get('vs2_xdz' + i))});
}
var me;
$(vs1).each(function() {
	if (this.id == my_id) {
		me = this;
		return false;
	}
});
if (!me) {
	$(vs2).each(function() {
		if (this.id == my_id) {
			me = this;
			return false;
		}
	});
	if (me) {
		var tmp = vs1;
		vs1 = vs2;
		vs2 = tmp;
	} else {
		return null;
	}
}
var line = '';
var process = function(e) {
	$(e).contents().each(function() {
		if (this.nodeType == 3) {
			line += this.textContent;
		} else if (this.nodeType == 1) {
			var $e = $(this);
			if ($e.is('br')) {
				if (line.length > 0) {
					msgs.push(line);
				}
				line = '';
			} else if (!$e.is('.out3')) {
				process(this);
			}
		}
	});
};
$($('#out span.out[robot!="1"]:visible').toArray()).each(
	function() {
		$(this).attr('robot', '1');
		process(this);
	});
if (line.length > 0) {
	msgs.push(line);
}
$('button.cmd_skill_button').each(function() {
	var $b = $(this);
	var onclick = $b.attr('onclick');
	if (onclick != 'clickButton(\'0\', 0)') {
		pfms.push($b.text());
	} else {
		pfms.push('');
	}
});
return {me: me, vs1: vs1, vs2: vs2, msgs: msgs, pfms: pfms};