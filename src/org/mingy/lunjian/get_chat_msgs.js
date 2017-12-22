if (window.channel_messages) {
	var msgs = channel_messages;
	channel_messages = [];
	return msgs;
}
return [];