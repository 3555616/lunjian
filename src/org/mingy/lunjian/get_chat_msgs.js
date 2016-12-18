var keywords = arguments[0], msgs = [];
if (window.chatMsg) {
	$(window.chatMsg).each(function() {
		if (!this.get('snoop')) {
			this.put('snoop', true);
			if (this.get('type') == 'channel' && this.get('subtype') == 'sys') {
				var msg = this.get('msg');
				$(keywords).each(function() {
					if (msg.indexOf(this) >= 0) {
						msgs.push(msg);
						return false;
					}
				});
			}
		}
	});
}
return msgs;