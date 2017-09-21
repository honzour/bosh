<?php

header('Content-Type: text/plain; charset=utf-8');

include("db.php");
include("utils.php");

	$db = mysql_connect($db_host, $db_user, $db_password);
	mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);
	
	if ((array_key_exists("login", $_POST))) {
		$query = "select concat_ws(',', b.name, s.city, s.street, s.lon, s.lat) from shops s left join brands b on s.brand = b.id left join people p on p.id = s.oz where p.login='" .
			mysql_escape_string($_POST["login"]) . "'and p.password='" . mysql_escape_string($_POST["password"]) . "' order by s.id";
	} else {
		$query = "select concat_ws(', ', s.boss, k.name, s.worker, o.name, b.name, s.id, s.city, s.street, s.lon, s.lat) from shops s left join brands b on b.id = s.brand left join people k on k.id = s.boss left join people o on o.id = s.worker order by s.id";
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
