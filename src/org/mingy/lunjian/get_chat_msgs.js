var msgs = [];
$(chatMsg).each(
		function() {
			if (!this.get('snoop')) {
				this.put('snoop', true);
				if (this.get('type') == 'channel'
						&& this.get('subtype') == 'sys') {
					var msg = this.get('msg');
					if (msg.indexOf('青龙会组织') >= 0 || msg.indexOf('游侠会') >= 0
							|| msg.indexOf('山河宝藏图') >= 0
							|| msg.indexOf('段延庆') >= 0) {
						msgs.push(msg);
					}
				}
			}
		});
return msgs;