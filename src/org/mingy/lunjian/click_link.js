var seq = arguments[0];
$a = $('#out2 span.out2[local_seq="' + seq + '"] a');
if ($a.length > 0) {
	$a[0].click();
}