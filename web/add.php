<?php
include("db.php");
include("utils.php");

	$title = "Přidání obrázku";

	function errorHeader($code, $desc) {
		$protocol = (isset($_SERVER['SERVER_PROTOCOL']) ? $_SERVER['SERVER_PROTOCOL'] : 'HTTP/1.0');
		header($protocol . ' ' . $code . ' ' . $desc);
		die("Error $code, $desc");
	}


	if (array_key_exists("lon", $_POST)) {

		$db = mysql_connect($db_host, $db_user, $db_password);

		if (!$db) {
			errorHeader(500, "Cannot connect to the database " . mysql_error());
		}

		if (!mysql_select_db($db_db, $db)) {
			errorHeader(500, "Cannot select the database " . mysql_error($db));
		}

		if (array_key_exists('file', $_FILES) && strlen($tmpName = $_FILES['file']['tmp_name']) > 0	) {

			$fp = fopen($tmpName, 'r');
			if (!$fp) {
				errorHeader(500, "Cannot open uploaded file");
			}

			$data = fread($fp, filesize($tmpName));
			if (!$data) {
				errorHeader(406, "Empty uploaded file");
			}
			$data = mysql_escape_string($data);
			fclose($fp);

			$r = mysql_query("INSERT INTO photos (lon, lat, acc, photo) VALUES (" . 
				"'" . mysql_escape_string($_POST["lon"]) .  "', " .
				"'" . mysql_escape_string($_POST["lat"]) .  "', " .
				"'" . mysql_escape_string($_POST["acc"]) .  "', " .
				"'" . $data .  "'" .
				")");
			if (!$r) {
				errorHeader(500, "Cannot insert uploaded file into db " . mysql_error($db));
			}


		} else {
			errorHeader(406, "No uploaded file found");

/*
			$insertstring = "INSERT INTO photos (lon, lat, acc) VALUES (" . 
				"'" . mysql_escape_string($_POST["lon"]) .  "', " .
				"'" . mysql_escape_string($_POST["lat"]) .  "', " .
				"'" . mysql_escape_string($_POST["acc"]) . "')";
			mysql_query($insertstring);
*/
		}
		mysql_close($db);

		htmlHeader($title);

		?>
Fotka přidána.<BR>
<A HREF="index.php">Zpět</A>
		<?php
	}
	else {
		htmlHeader($title);
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
