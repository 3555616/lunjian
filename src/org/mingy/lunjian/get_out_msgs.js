var re = arguments[0] ? new RegExp(arguments[0]) : null, msgs = [];
if (!window.g_obj_map) {
	return msgs;
}
$($('#out2 span.out2[robot!="1"]').toArray()).each(
	function() {
		$(this).attr('robot', '1');
		var text = $(this).text();
		if (!re || re.test(text)) {
			msgs.push(text);
		}
	});
return msgs;