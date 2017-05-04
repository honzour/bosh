<HTML>
<HEAD><meta charset="utf-8"></HEAD>
<BODY>

<?php 
	include "db.php";

	if (array_key_exists("lon", $_POST)) {
		$db = mysql_connect($db_host, $db_user, $db_password);
		mysql_select_db($db_db, $db);

		if (array_key_exists('file', $_FILES) && strlen($tmpName = $_FILES['file']['tmp_name']) > 0	) {

			$fp = fopen($tmpName, 'r');
			$data = fread($fp, filesize($tmpName));
			$data = mysql_escape_string($data);
			fclose($fp);

			mysql_query("INSERT INTO photos (lon, lat, acc, photo) VALUES (" . 
				"'" . mysql_escape_string($_POST["lon"]) .  "', " .
				"'" . mysql_escape_string($_POST["lat"]) .  "', " .
				"'" . mysql_escape_string($_POST["acc"]) .  "', " .
				"'" . $data .  "'" .
				")");
		} else {
			mysql_query("INSERT INTO photos (lon, lat, acc) VALUES (" . 
				"'" . mysql_escape_string($_POST["lon"]) .  "', " .
				"'" . mysql_escape_string($_POST["lat"]) .  "', " .
				"'" . mysql_escape_string($_POST["acc"]) . "')");
		}
		mysql_close($db);

		?>
<A HREF="index.php">Zpět</A>
		<?php
	}
	else {
?>
<FORM action="add.php" method="post" enctype="multipart/form-data">
<TABLE>
<TR><TD>Délka:</TD><TD><INPUT type="text" name="lon"></TD></TR>
<TR><TD>Šířka:</TD><TD><INPUT type="text" name="lat"></TD></TR>
<TR><TD>Přesnost:</TD><TD><INPUT type="text" name="acc"></TD></TR>
<TR><TD>Fotka:</TD><TD><INPUT type = "file" name="file"></TD></TR>
</TABLE>
<P>
<INPUT type = "submit">
</FORM>
<?php 
}
?>
</BODY>
</HTML>
