<?php
include("db.php");
include("utils.php");

	loginUser();
	htmlHeader("Detail obrázku");


	if ($error) {
		die($error);
	}
	$id = mysql_escape_string($_GET["id"]);

	$q = "SELECT p.id, p.photo, p.note, p.note2, p.istourplan, p.isorder, concat_ws(', ', b.name, s.street, s.city), p.savedtime, p.web, e.name FROM photos p left join shops s on s.id = p.shop left join brands b on b.id = s.brand left join people e on e.id = p.worker where p.id = $id";

	$result = mysql_query($q, $db);
	if (!$result) {
		echo(mysql_error());
	}

	if ($row = mysql_fetch_row($result)) {
				echo("<H2> obrázek " . $row[0] . "</H2>\n");
		        echo("obchod: " . htmlspecialchars($row[6]) . "<BR><BR>\n");
		        echo("poznámka Bosch: " . htmlspecialchars($row[2]) . "<BR><BR>\n");
		        echo("poznámka konkurence: " . htmlspecialchars($row[3]) . "<BR><BR>\n");
		        echo("tourplan: " . ($row[4] ? "ano" : "ne") . "<BR><BR>\n");
		        echo("objednávka: " . ($row[5] ? "ano" : "ne") . "<BR><BR>\n");
		        echo("čas: " . htmlspecialchars($row[7]) . "<BR><BR>\n");
		        echo("z webu: " . ($row[8] == NULL ? "nevíme" : ($row[8] ? "ano" : "ne")) . "<BR><BR>\n");
		        echo("nahrál: " . ($row[9] == NULL ? "nevíme" : $row[9]) . "<BR><BR>\n");
				echo("<IMG src=\"data:image/png;base64,". base64_encode($row[1]) ."\" alt=\"fotka\"><BR>\n");

	} else {
		?>
Obrázek nenalezen.
		<?php
	}
	mysql_free_result($result);
	mysql_close($db);


 ?>

</BODY>
</HTML>

