<?php
include("db.php");
include("utils.php");

htmlHeader("Seznam obrázků");

?>
<?php
	

	$db = mysql_connect($db_host, $db_user, $db_password);
    mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);

	if (array_key_exists("action", $_GET) && $_GET["action"] == "delete") {
		$id = mysql_escape_string($_GET["id"]);

		mysql_query("DELETE FROM photos WHERE id = '" . $id . "'", $db);
	}

	$result = mysql_query("SELECT id, lon, lat, acc, photo, note, note2, istourplan, isorder FROM photos",$db);

	while ($row = mysql_fetch_row($result)) {
?>
<P>
<?php
		echo("<H3> obrázek " . $row[0] . "</H3>\n");
        echo("délka: " . $row[1] . "<BR>\n");
        echo("šířka: " . $row[2] . "<BR>\n");
        echo("přesnost: " . $row[3] . "<BR><BR>\n");
        echo("poznámka: " . htmlspecialchars($row[5]) . "<BR><BR>\n");
        echo("koment k promotérovi: " . htmlspecialchars($row[6]) . "<BR><BR>\n");
        echo("tourplan: " . ($row[7] ? "ano" : "ne") . "<BR><BR>\n");
        echo("objednávka: " . ($row[8] ? "ano" : "ne") . "<BR><BR>\n");
		echo("<IMG src=\"data:image/png;base64,". base64_encode($row[4]) ."\" alt=\"fotka\"><BR>\n");
		echo("<A href=\"index.php?action=delete&amp;id=" . $row[0] ."\">smazat</A>\n<BR><BR>");
?>
</P>
<HR>
<?php
	}
	mysql_free_result($result);
	mysql_close($db)

 ?>
</BODY>
</HTML>
