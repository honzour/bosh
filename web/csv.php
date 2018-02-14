<?php

include("db.php");
include("utils.php");

	loginUser();

	if (!$person_id) {
		errorHeader(403, "Username or password invalid or missing");
	}

	header('Content-Type: text/plain; charset=utf-8');

	
	if ($worker && !$admin && !$boss) {
		$query = "select concat_ws('$', b.name, s.street, s.city, s.id, s.lon, s.lat) from shops s left join brands b on s.brand = b.id left join people p on p.id = s.worker where p.id='" .
			$person_id . "' order by s.id";
	} else {
		$query = "select concat_ws('$ ', s.boss, k.name, s.worker, o.name, b.name, s.id, s.city, s.street, s.lon, s.lat, s.channel) from shops s left join brands b on b.id = s.brand left join people k on k.id = s.boss left join people o on o.id = s.worker order by s.id";
	}

	$res = mysql_query($query);
	if (!$res) {
		$ok = false;
		echo(mysql_error());
	} else {
		while ($s = mysql_fetch_row($res)[0]) {
			echo($s);
			echo("\n");
		}
	}
?>
