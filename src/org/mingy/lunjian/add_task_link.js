var seq = arguments[0], args = arguments;
var $e = $('#out2 span.out2[local_seq="' + seq + '"]');
$('a', $e).each(
	function(i) {
		if (args.length <= i + 1) {
			return false;
		}
		var path = args[i + 1];
		if (path) {
			$a = $('<span style="color:red;">[<a style="text-decoration:underline;color:red;" href="javascript:clickButton(\''
					+ path.replace(/;/g, '\\n') + '\', 0);">GO</a>]</span>');
			$a.insertAfter(this);
		}
	});