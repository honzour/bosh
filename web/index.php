<?php
include("db.php");
include("utils.php");

	if (isset($_POST[$var_login]) || isset($_POST[$var_password])) {
		$login = $_POST[$var_login];
		$password = $_POST[$var_password];
		setcookie($var_login, $login, $cookie_expire, $cookie_path);
		setcookie($var_password, $password, $cookie_expire, $cookie_path);

	} else {
		if (isset($_COOKIE[$var_login]) || isset($_COOKIE[$var_password])) {
			$login = $_COOKIE[$var_login];
			$password = $_COOKIE[$var_password];
		} else {
			$login = "";
			$password = "";
		}
	}

	htmlHeader("Seznam obrázků");

	$db = mysql_connect($db_host, $db_user, $db_password);
	if (!$db) {
		echo(mysql_error());
	}
    mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);

	if ($login == "" || $password == "")
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
