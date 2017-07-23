var re = arguments[0] ? new RegExp(arguments[0]) : null, msgs = [];
if (!window.g_obj_map) {
	return msgs;
}
if (window.local_seq == undefined) {
	window.local_seq = 0;
}
$($('#out2 span.out2:not([local_seq])').toArray()).each(
	function() {
		var seq = ++window.local_seq;
		$(this).attr('local_seq', seq);
		var text = $(this).text();
		if (!re || re.test(text)) {
			msgs.push(seq + "," + text);
		}
	});
return msgs;