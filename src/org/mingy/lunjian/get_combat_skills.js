var skills = [];
$('button.cmd_skill_button').each(function() {
	skills.push($(this).text());
});
return skills;