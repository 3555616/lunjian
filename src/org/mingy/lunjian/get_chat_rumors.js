var keywords = arguments[0], ignores = arguments[1], msgs = [];
if (!window.sock && window.chatMsg && window.chatMsg.length > 0) {
	window.chatMsg = [];
	window.gSocketMsg2.save_channel();
}
if (window.chatMsg) {
	$(window.chatMsg).each(function() {
		if (!this.get('snoop_rumor')) {
			this.put('snoop_rumor', true);
			if (this.get('type') == 'channel' && this.get('subtype') == 'rumor') {
				var msg = this.get('msg');
				$(keywords).each(function() {
					if (msg.indexOf(this) >= 0) {
						var b = false;
						$(ignores).each(function() {
							if (msg.indexOf(this) >= 0) {
								b = true;
								return false;
							}
						});
						if (!b) {
							msgs.push(msg);
						}
						return false;
					}
				});
			}
		}
	});
}
return msgs;