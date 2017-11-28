<?php
include("db.php");
include("utils.php");

function htmlSelectAdminBoss($admin) {
	global $db;
	global $_GET;
	global $person_id;
?>
	<select name="filter_worker">
		<option value="-1" <?php 
		if (!isset($_GET["filter_worker"]) || $_GET["filter_worker"] == -1) echo("selected"); ?> >Kdokoliv</option>
<?php
		if ($admin) {
			$q = "SELECT id, name FROM people WHERE worker = 1 ORDER BY name";
		} else {
			$q = "SELECT p.id, p.name FROM people p WHERE p.worker = 1 and exists (select 1 from shops s where s.worker = p.id and s.boss = $person_id) ORDER BY p.name";
		}
		$result = mysql_query($q, $db);
			if (!$result) {
				echo(mysql_error());
			}
			while ($row = mysql_fetch_row($result)) {
				echo("<option value=\"" .$row[0]. "\"");
		if (isset($_GET["filter_worker"]) && $_GET["filter_worker"] == $row[0]) echo(" selected ");
				echo(">" .$row[1]. "</option>\n");

			}
?>
	</select>
<?php
}

	loginUser();

	htmlHeader("Seznam obrázků");

?>
	<H2>Seznam obrázků</H2>
<?php

	if ($name) {
		echo("<P>Přihlášen " . $name . "</P>");	
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

		if ($worker || $boss || $admin) {
		
			if (isset($_GET["limit"])) {
				$limit = (int)$_GET["limit"];
			} else {
				$limit = 5;
			}
			if (isset($_GET["offset"])) {
				$offset = (int)$_GET["offset"];
			} else {
				$offset = 0;
			}
			if ($limit < 0)
				$limit = 0;
			if ($offset < 0)
				$offset = 0;

		?>
		<BR>
		<FORM method="get" action = "index.php">
		Vypisuji až <INPUT type="text" name = "limit" value="<?php echo($limit); ?>"> záznamů od pozice <INPUT type="text" name = "offset" value="<?php echo($offset); ?>">.<BR><BR>
		Nahrál: 
<?php
		if ($admin) {
			htmlSelectAdminBoss(true);
			$where = "1 = 1";
		} else if ($boss) {
			htmlSelectAdminBoss(false);
			$where = "s.boss = $person_id";
		} else {
			echo($name);
			$where = "p.worker = $person_id";
		}

		if (isset($_GET["filter_worker"]) && $_GET["filter_worker"] != -1) {
			$where .= "  and p.worker = " . mysql_escape_string($_GET["filter_worker"]);
		}
?>
		<BR><BR>
		<INPUT type="submit" value="Odeslat">
		</FORM>

<?php

			if (array_key_exists("action", $_GET) && $_GET["action"] == "delete") {
				$id = mysql_escape_string($_GET["id"]);
				mysql_query("DELETE FROM photos WHERE id = '" . $id . "'", $db);
			}

			


			$q = "SELECT p.id, p.photo, p.note, p.note2, p.istourplan, p.isorder, concat_ws(', ', b.name, s.street, s.city), p.savedtime, p.web, e.name FROM photos p left join shops s on s.id = p.shop left join brands b on b.id = s.brand left join people e on e.id = p.worker where "
			. $where .
			" order by p.savedtime desc limit " . $limit . " offset " . $offset;
			$result = mysql_query($q, $db);
			if (!$result) {
				echo(mysql_error());
			}

			while ($row = mysql_fetch_row($result)) {
?>
<P>
<?php
				echo("<H3> obrázek " . $row[0] . "</H3>\n");
		        echo("obchod: " . htmlspecialchars($row[6]) . "<BR><BR>\n");
		        echo("poznámka: " . htmlspecialchars($row[2]) . "<BR><BR>\n");
		        echo("koment k promotérovi: " . htmlspecialchars($row[3]) . "<BR><BR>\n");
		        echo("tourplan: " . ($row[4] ? "ano" : "ne") . "<BR><BR>\n");
		        echo("objednávka: " . ($row[5] ? "ano" : "ne") . "<BR><BR>\n");
		        echo("čas: " . htmlspecialchars($row[7]) . "<BR><BR>\n");
		        echo("z webu: " . ($row[8] == NULL ? "nevíme" : ($row[8] ? "ano" : "ne")) . "<BR><BR>\n");
		        echo("nahrál: " . ($row[9] == NULL ? "nevíme" : $row[9]) . "<BR><BR>\n");
				echo("<IMG src=\"data:image/png;base64,". base64_encode($row[1]) ."\" alt=\"fotka\"><BR>\n");
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
</P>
<?php
	}
?>
<P>
<A HREF="logout.php">Odhlášení</A>
</P>

<?php
		}

 ?>
</BODY>
</HTML>

