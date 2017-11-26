var seq = arguments[0], args = arguments;
var $e = $('#out2 span.out2[local_seq="' + seq + '"]');
var $a = $('a', $e);
$a.each(
	function(i) {
		if (args.length <= i + 1) {
			return false;
		}
		var path = args[i + 1];
		if (path) {
			var $n = $('<span style="color:red;">[<a style="text-decoration:underline;color:red;" href="javascript:send_cmd(\''
					+ path + '\', 0);">GO</a>]</span>');
			$n.insertAfter(this);
		}
	});
if ($a.length < args.length - 1) {
	for (var i = $a.length; i < args.length - 1; i++) {
		var path = args[i + 1];
		if (path) {
			var $n = $('<span style="color:red;">[<a style="text-decoration:underline;color:red;" href="javascript:send_cmd(\''
					+ path + '\', 0);">GO</a>]</span>');
			$e.append($n);
		}
	}
}