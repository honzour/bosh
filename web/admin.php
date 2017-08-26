<?php
include("db.php");
include("utils.php");

htmlHeader("Administrace");
	

if (array_key_exists("csv", $_POST))
{

	?>
<H2>Nahrávání CSV souboru</H2>
<P>
Nahrávám csv soubor...<BR>
<?php
	if (!array_key_exists('file', $_FILES) || !(strlen($tmpName = $_FILES['file']['tmp_name']) > 0)) {
		?>
		Soubor se nepodařilo nahrát na server.<BR>
		<?php
	}
	else
	{
		$csvFile = file_get_contents($tmpName);
		if (!$csvFile)
		{
?>
		Soubor se nepodařilo otevřít<BR>
<?php
		}
		else
		{
?>
		Parsuji soubor...<BR>
<?php
			$rows = explode("\n", $csvFile);
			if (!$rows)
			{
?>
				Soubor se nepodařilo rozdělit na řádky<BR>
<?php
			}
			else
			{
				$db = mysql_connect($db_host, $db_user, $db_password);
   				mysql_set_charset ("utf8", $db);
				mysql_select_db($db_db, $db);
				mysql_query("delete from shops");
				mysql_query("delete from brands");
				$ok = true;
?>
				Soubor má <?php echo(count($rows) - 1); ?> řádků<BR>
<?php
				$i = 0;
				foreach($rows as $row => $data)
				{
					$i++;
					if (count($rows) <= $row + 1)
						break;
					$fields = explode(",", $data);
					if (count($fields) != 10) {
						echo("Chyba na řádku " . ($row + 1) . ":<BR>" . htmlspecialchars($data));
						echo("<BR>Musí jít o 10 csv oddělených údajů, ale je jich tam " . count($fields));
						$ok = false;
						break;
					} else
					{
//							1, Jan Čarný, 2, Karel Otrok, COOP, 1, Kozolupy, Bubovická 7, 14.25, 50.325
						$person1_id = mysql_escape_string($fields[0]);
						$person1 = mysql_escape_string($fields[1]);
						$person2_id = mysql_escape_string($fields[2]);
						$person2 = mysql_escape_string($fields[3]);
						$brand = mysql_escape_string($fields[4]);
						$shop_id = mysql_escape_string($fields[5]);
						$city = mysql_escape_string($fields[6]);
						$street = mysql_escape_string($fields[7]);
						$lon = mysql_escape_string($fields[8]);
						$lat = mysql_escape_string($fields[9]);

						$res = mysql_query("select count(1) as cnt from people where id = $person1_id and kam = 1");
						$cnt = mysql_fetch_row($res)[0];
						if (!$cnt) {
							echo("V databázi není žádný KAM $person1 s id $person1_id, chyba na řádku $i");
							$ok = false;
							break;
						}
						$res = mysql_query("select count(1) as cnt from people where id = $person2_id and oz = 1");
						$cnt = mysql_fetch_row($res)[0];
						if (!$cnt) {
							echo("V databázi není žádný OZ $person2 s id $person2_id, chyba na řádku $i");
							$ok = false;
							break;
						}
					}
				}
				if ($ok) {
					echo("Kontrola dat: v pořádku<BR>\n");
					echo("Nahrávám obchodní řetězce...<BR>\n");
					$i = 0;
					foreach($rows as $row => $data)
					{
						$i++;
						if (count($rows) <= $row + 1)
							break;
						$fields = explode(",", $data);
						$brand = mysql_escape_string($fields[4]);
						$res = mysql_query("select count(1) as cnt from brands where name = '$brand'");
						if (!$res) {
							$ok = false;
							echo("Chyba na řádku $i: ");
							echo(mysql_error());
							break;
						}
						$cnt = mysql_fetch_row($res)[0];
						if ($cnt < 1) {
							$res = mysql_query("insert into brands (name) values ('$brand')");
							if (!$res) {
								$ok = false;
								echo("Chyba na řádku $i: ");
								echo(mysql_error());
								break;
							}
						} // if ($cnt < 1)
					} // foreach
				} // if ($ok)
				if ($ok) {
					echo("Řetězce nahrány<BR>\n");
					echo("Nahrávám obchody....<BR>\n");
					$i = 0;
					foreach($rows as $row => $data)
					{
						$i++;
						if (count($rows) <= $row + 1)
							break;
						$fields = explode(",", $data);
						$person1_id = mysql_escape_string($fields[0]);
						$person1 = mysql_escape_string($fields[1]);
						$person2_id = mysql_escape_string($fields[2]);
						$person2 = mysql_escape_string($fields[3]);
						$brand = mysql_escape_string($fields[4]);
						$shop_id = mysql_escape_string($fields[5]);
						$city = mysql_escape_string($fields[6]);
						$street = mysql_escape_string($fields[7]);
						$lon = mysql_escape_string($fields[8]);
						$lat = mysql_escape_string($fields[9]);

						$query = "insert into shops(id, kam, oz, brand, city, street, lon, lat) values (" .
							"'$shop_id', " .
							"$person1_id, " .
							"$person2_id, " .
							"0, " .
							"'$city', " .
							"'$street', " .
							"$lon, " .
							"$lat".
						")";

						$res = mysql_query($query);
						if (!$res) {
							$ok = false;
							echo("Chyba na řádku $i v dotazu $query <BR>");
							echo(mysql_error());
							break;
						}
					}
				}
				if ($ok) {
					echo("Hotovo<BR>\n");
				}
			} // else
		}
	}
?>
</P>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="people.php">Editace osob</A><BR>
<A HREF="admin.php">Zpět na administraci</A>
</P>
	<?php
} // if (array_key_exists("csv", $_POST))
else
{
	?>
<H2>Administrace</H2>	
<FORM action="admin.php" method="post" enctype="multipart/form-data">
<input type = "hidden" name = "csv" value = "1">
<TABLE>
<TR><TD>CSV soubor s daty:</TD><TD><INPUT type = "file" name="file"></TD></TR>
</TABLE>
<P>
<INPUT type = "submit" value="Nahrát">
</FORM>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="people.php">Editace osob</A><BR>
</P>

<?php
}
?>

</BODY>
</HTML>
