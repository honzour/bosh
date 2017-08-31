<?php

header('Content-Type: text/plain; charset=utf-8');

include("db.php");
include("utils.php");

	$db = mysql_connect($db_host, $db_user, $db_password);
	mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);
	$res = mysql_query("select concat_ws(', ', s.kam, k.name, s.oz, o.name, b.name, s.id, s.city, s.street, s.lon, s.lat) from shops s left join brands b on b.id = s.brand left join people k on k.id = s.kam left join people o on o.id = s.oz order by s.id");
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
