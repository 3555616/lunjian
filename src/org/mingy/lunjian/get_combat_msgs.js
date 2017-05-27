var vs1 = [], vs2 = [], msgs = [];
if (!window.is_fighting) {
	return null;
}
$('td#vs11,td#vs12,td#vs13,td#vs14').each(
		function() {
			vs1.push($($(this).contents()[0]).text());
		});
$('td#vs21,td#vs22,td#vs23,td#vs24').each(
		function() {
			vs2.push($($(this).contents()[0]).text());
		});
var line = '';
var process = function(e) {
	$(e).contents().each(function() {
		if (this.nodeType == 3) {
			line += this.textContent;
		} else if (this.nodeType == 1) {
			var $e = $(this);
			if ($e.is('br')) {
				msgs.push(line);
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
return {me: window.g_obj_map.get('msg_attrs').get('name'), vs1: vs1, vs2: vs2, msgs: msgs};