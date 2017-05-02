<HTML>
<HEAD><meta charset="utf-8"></HEAD>
<BODY>
<TABLE border="3">
<TR><TD>id</TD><TD>délka</TD><TD>šířka</TD><TD>přesnost</TD><TD>funkce</TD></TR>
<TR><TD colspan="5">obrázek</TD></TR>
<?php 
$db = mysql_connect("localhost", "root", "korinek");
mysql_select_db("bosh",$db);

if ($_GET["action"] == "delete") {
	$id = mysql_escape_string($_GET["id"]);

	echo "DELETE FROM photos WHERE id = '" . $id . "'";
	mysql_query("DELETE FROM photos WHERE id = '" . $id . "'", $db);
}

$result = mysql_query("SELECT * FROM photos",$db);

while ($row = mysql_fetch_row($result)) {
	echo("<TR><TD>" . $row[0] . "</TD><TD>" . $row[1] . "</TD><TD>" . $row[2] . "</TD><TD>" . $row[3] . "</TD><TD><A href=\"index.php?action=delete&amp;id=" . $row[0] ."\">smazat</A></TD></TR>");
}
mysql_free_result($result);
mysql_close($db)

 ?>
</TABLE>
</BODY>
</HTML>
