<?php
include("db.php");
include("utils.php");

	$title = "Přidání obrázku";

	loginUser();

	if (!$worker) {
		errorHeader(403, "Please ogin as a worker.");		
	}


	if (array_key_exists("lon", $_POST)) {


		if (array_key_exists('file', $_FILES) && strlen($tmpName = $_FILES['file']['tmp_name']) > 0	) {

			$fp = fopen($tmpName, 'r');
			if (!$fp) {
				errorHeader(500, "Cannot open uploaded file");
			}

			$filesize = filesize($tmpName);

			if ($filesize > 1024 * 1024) {
				fclose($fp);
				errorHeader(406, "File too long");
			}

			$bufsize = 131072;

			if ($filesize < $bufsize) {

				$data = fread($fp, $filesize);
				if (!$data) {
					errorHeader(406, "Empty uploaded file");
				}
				$data = mysql_escape_string($data);
				fclose($fp);

				$r = mysql_query("INSERT INTO photos (lon, lat, acc, photo, note, note2, istourplan, isorder) VALUES (" . 
					"'" . mysql_escape_string($_POST["lon"]) .  "', " .
					"'" . mysql_escape_string($_POST["lat"]) .  "', " .
					"'" . mysql_escape_string($_POST["acc"]) .  "', " .
					"'" . $data .  "'," .
					"'" . mysql_escape_string($_POST["note"]) .  "', " .
					"'" . mysql_escape_string($_POST["note2"]) .  "', " .
					"'" . (isset($_POST["istourplan"]) ? 1 : 0) .  "', " .
					"'" . (isset($_POST["isorder"]) ? 1 : 0) .  "'" .
					")");
				if (!$r) {
					errorHeader(500, "Cannot insert uploaded file into db " . mysql_error($db));
				}
			} else {
				$r = mysql_query("INSERT INTO photos (lon, lat, acc, photo, note, note2, istourplan, isorder) VALUES (" . 
					"'" . mysql_escape_string($_POST["lon"]) .  "', " .
					"'" . mysql_escape_string($_POST["lat"]) .  "', " .
					"'" . mysql_escape_string($_POST["acc"]) .  "', '', " . // acc + photo
					"'" . mysql_escape_string($_POST["note"]) .  "', " .
					"'" . mysql_escape_string($_POST["note2"]) .  "', " .
					"'" . (isset($_POST["istourplan"]) ? 1 : 0) .  "', " .
					"'" . (isset($_POST["isorder"]) ? 1 : 0) .  "'" .
				")");
				if (!$r) {
					fclose($fp);
					errorHeader(500, "Cannot insert into db " . mysql_error($db));
				}
				$id = mysql_insert_id();
				while (!feof($fp)) {
					$data = fread($fp, $bufsize);
					if (!$data) {
						fclose($fp);
						mysql_query("DELETE FROM photos WHERE id = $id");
						errorHeader(406, "Cannot read file");
					}
					$data = mysql_escape_string($data);
					$r = mysql_query("UPDATE photos SET photo = CONCAT(photo, '$data') WHERE id = $id");
					if (!$r) {
						fclose($fp);
						mysql_query("DELETE FROM photos WHERE id = $id");
						errorHeader(500, "Cannot update into db " . mysql_error($db));
					}
						
				}
				fclose($fp);
			}

		} else {
			errorHeader(406, "No uploaded file found");

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
<H2><?php echo($title);?></H2>
<FORM action="add.php" method="post" enctype="multipart/form-data">
<TABLE>

<TR><TD>Obchod:</TD><TD><SELECT name="shop">
<?php
	$result = mysql_query("select s.id, concat(b.name, ' ', s.city, ' ', s.street) from shops s left join brands b on s.brand = b.id where s.oz = $person_id", $db);
	while ($row = mysql_fetch_row($result))
		echo("<OPTION value=\"". $row[0] ."\">" . $row[1] ."</OPTION>");

?>
</SELECT>
</TR>
<TR><TD>Poznámka:</TD><TD><INPUT type="text" name="note"></TD></TR>
<TR><TD>Koment k promotérovi:</TD><TD><INPUT type="text" name="note2"></TD></TR>
<TR><TD>Tourplán:</TD><TD><INPUT type="checkbox" name="istourplan" value="1"></TD></TR>
<TR><TD>Objednávka:</TD><TD><INPUT type="checkbox" name="isorder" value="1"></TD></TR>
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
