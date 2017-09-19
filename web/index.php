<?php
include("db.php");
include("utils.php");

	loginUser();

	htmlHeader("Seznam obrázků");

?>
	<H2>Seznam obrázků</H2>
<?php

	if ($name) {
		echo("Přihlášen " . $name);	
	}
	if ($error) {
		echo($error);
	}
	if (!$person_id)
	{
?>

	<FORM method="post" action = "index.php">
		Login: <INPUT type="text" name = "<?php echo($var_login); ?>"><BR>
		Heslo: <INPUT type="text" name = "<?php echo($var_password); ?>"><BR>
		<INPUT type="submit" value="Odeslat">
	</FORM>

<?php
	}
    else
	{

		if (array_key_exists("action", $_GET) && $_GET["action"] == "delete") {
			$id = mysql_escape_string($_GET["id"]);

			mysql_query("DELETE FROM photos WHERE id = '" . $id . "'", $db);
		}

		$result = mysql_query("SELECT id, lon, lat, acc, photo, note, note2, istourplan, isorder FROM photos",$db);
		if (!$result) {
			echo(mysql_error());
		}


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
		mysql_close($db);
?>
<P>
<A HREF="add.php">Přidání obrázku</A><BR>
<A HREF="people.php">Editace osob</A><BR>
<A HREF="admin.php">Administrace</A><BR>
<A HREF="csv.php">CSV export</A><BR>
<A HREF="logout.php">Odhlášení</A>
</P>

<?php
	}


 ?>
</BODY>
</HTML>
