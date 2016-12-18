var callback = arguments[0];
if (!window.writeToScreen) {
	setTimeout(function() {
		callback([]);
	}, 500);
}
window.snoop_loop_count = 0;
if (!window.lunjian_snoops) {
	window.lunjian_snoops = [];
	var fn = window.writeToScreen;
	window.writeToScreen = function(a, e, g, f) {
		if (e == 2) {
			var t = $('<div>' + a + '</div>').text();
			if (t && t.charAt(t.length - 1) == '\n') {
				t = t.substr(0, t.length - 1);
			}
			if (t) {
				window.lunjian_snoops.push(t);
				window.snoop_loop_count = 10;
			}
		}
		fn.call(window, a, e, g, f);
	};
}
var loop = function() {
	if (++window.snoop_loop_count < 1) {
		setTimeout(loop, 200);
	} else {
		var p = window.lunjian_snoops;
		window.lunjian_snoops = [];
		window.snoop_loop_count = 0;
		callback(p);
	}
};
setTimeout(loop, 100);