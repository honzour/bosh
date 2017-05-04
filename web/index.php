<HTML>
<HEAD><meta charset="utf-8"></HEAD>
<BODY>
<TABLE border="3">
<TR><TD>id</TD><TD>délka</TD><TD>šířka</TD><TD>přesnost</TD><TD>funkce</TD></TR>
<TR><TD colspan="5">obrázek</TD></TR>
<?php
	include("db.php");

	$db = mysql_connect($db_host, $db_user, $db_password);
	mysql_select_db($db_db, $db);

	if (array_key_exists("action", $_GET) && $_GET["action"] == "delete") {
		$id = mysql_escape_string($_GET["id"]);

		mysql_query("DELETE FROM photos WHERE id = '" . $id . "'", $db);
	}

	$result = mysql_query("SELECT id, lon, lat, acc, photo FROM photos",$db);

	while ($row = mysql_fetch_row($result)) {
		echo("<TR><TD>" . $row[0] . "</TD><TD>" . $row[1] . "</TD><TD>" . $row[2] . "</TD><TD>" . $row[3] . "</TD><TD><A href=\"index.php?action=delete&amp;id=" . $row[0] ."\">smazat</A></TD></TR>\n");
		echo("<TR><TD colspan=\"5\"><IMG src=\"data:image/png;base64,". base64_encode($row[4]) ."\"></TD></TR>\n");
	}
	mysql_free_result($result);
	mysql_close($db)

 ?>
</TABLE>
</BODY>
</HTML>
