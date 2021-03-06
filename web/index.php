<?php
include("db.php");
include("utils.php");

function htmlSelectChooseBoss() {
	global $db;
	global $_GET;
	global $person_id;
?>
<BR><BR>
Šéf:
	<select name="filter_boss">
		<option value="-1" <?php 
		if (!isset($_GET["filter_boss"]) || $_GET["filter_boss"] == -1) echo("selected"); ?> >Kdokoliv</option>
<?php
		$q = "SELECT id, name FROM people p WHERE p.boss = 1 and exists (select 1 from shops s where s.boss = p.id) ORDER BY name";

		$result = mysql_query($q, $db);
			if (!$result) {
				echo(mysql_error());
			}
			while ($row = mysql_fetch_row($result)) {
				echo("<option value=\"" .$row[0]. "\"");
		if (isset($_GET["filter_boss"]) && $_GET["filter_boss"] == $row[0]) echo(" selected ");
				echo(">" .$row[1]. "</option>\n");

			}
?>
	</select>
<?php

}


function htmlSelectChooseChannel() {
	global $db;
	global $_GET;
	global $person_id;
?>
<BR><BR>
Kanál:
	<select name="filter_channel">
		<option value="-1" <?php
		if (!isset($_GET["filter_channel"]) || $_GET["filter_channel"] == -1) echo("selected"); ?> >Všechny</option>
<?php
		$q = "SELECT DISTINCT channel FROM shops ORDER BY channel";

		$result = mysql_query($q, $db);
			if (!$result) {
				echo(mysql_error());
			}
			while ($row = mysql_fetch_row($result)) {
				echo("<option value=\"" .$row[0]. "\"");
		if (isset($_GET["filter_channel"]) && $_GET["filter_channel"] == $row[0]) echo(" selected ");
				echo(">" .$row[0]. "</option>\n");

			}
?>
	</select>
<?php

}


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

		
			if (isset($_GET["shop"])) {
				$shop = $_GET["shop"];
			}

			if (isset($_GET["datefrom"])) {
				$datefrom = $_GET["datefrom"];
			}
			if (isset($_GET["dateto"])) {
				$dateto = $_GET["dateto"];
			}

		?>
		<BR>
		<FORM method="get" action = "index.php">
		Vypisuji až <INPUT type="text" name = "limit" value="<?php echo($limit); ?>"> záznamů od pozice <INPUT type="text" name = "offset" value="<?php echo($offset); ?>">.<BR><BR>
		Nahrál: 
<?php
		if ($admin) {
			htmlSelectAdminBoss(true);
			htmlSelectChooseBoss();
                        htmlSelectChooseChannel();
			$where = "1 = 1";
		} else if ($boss) {
			htmlSelectAdminBoss(false);
			htmlSelectChooseBoss();
                        htmlSelectChooseChannel();
			$where = "1 = 1" /*"s.boss = $person_id"*/;
		} else {
			echo($name);
			$where = "p.worker = $person_id";
		}

		if (isset($_GET["filter_worker"]) && $_GET["filter_worker"] != -1) {
			$where .= "  and p.worker = " . mysql_escape_string($_GET["filter_worker"]);
		}

		if (isset($_GET["filter_boss"]) && $_GET["filter_boss"] != -1) {
			$where .= "  and s.boss = " . mysql_escape_string($_GET["filter_boss"]);
		}

                if (isset($_GET["filter_channel"]) && $_GET["filter_channel"] != -1) {
			$where .= "  and s.channel ='" . mysql_escape_string($_GET["filter_channel"]) . "'";
		}

		if (isset($shop)) {
			$where .= " and concat_ws(', ', b.name, s.street, s.city) LIKE '%" . mysql_escape_string($shop) . "%' ";
 		}

		if (isset($datefrom) && strlen($datefrom) == 10) {
			$where .= " and date(savedtime) >= '" . mysql_escape_string($datefrom) . "'";
		}

		if (isset($dateto) && strlen($dateto) == 10) {
			$where .= " and date(savedtime) <= '" . mysql_escape_string($dateto) . "'";
		}
?>
		<BR><BR>
Obchod obsahuje: <INPUT type = "text" name = "shop" value="<?php if (isset($shop)) {	echo(htmlspecialchars( $shop ) ); } ?>">
		<BR><BR>
Nahráno od <INPUT type = "text" name = "datefrom" value="<?php if (isset($datefrom)) {	echo(htmlspecialchars( $datefrom ) ); } ?>" placeholder="2017-01-01"> do <INPUT type = "text" name = "dateto" value="<?php if (isset($dateto)) {	echo(htmlspecialchars( $dateto ) ); } ?>" placeholder="2017-12-31">
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
		        echo("<B>" . htmlspecialchars($row[6]) . "</B>\n");
?>
<TABLE>
	<TR>
		<TD>
<?php
				$raw = $row[1];
				echo("<A href=\"detail.php?id=" . $row[0] ."\"><IMG width=\"100\" src=\"data:image/png;base64,". base64_encode($raw) ."\" alt=\"fotka\"></A><BR>\n");
?>
		</TD>
		<TD style="padding-left: 10px;">
<?php
		        echo("poznámka Bosch: " . htmlspecialchars($row[2]) . "<BR>\n");
		        echo("poznámka konkurence: " . htmlspecialchars($row[3]) . "<BR>\n");
		        echo("tourplan: " . ($row[4] ? "ano" : "ne") . ", ");
		        echo("objednávka: " . ($row[5] ? "ano" : "ne") . ", ");
		        echo("čas: " . htmlspecialchars($row[7]) . ", ");
		        echo("z webu: " . ($row[8] == NULL ? "nevíme" : ($row[8] ? "ano" : "ne")) . ", ");
		        echo("nahrál: " . ($row[9] == NULL ? "nevíme" : $row[9]) . "<BR>\n");
?>

		</TD>
	</TR>			
</TABLE>

<?php
				echo("<A href=\"index.php?action=delete&amp;id=" . $row[0] ."\">smazat</A>\n<BR>");
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

