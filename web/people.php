<?php
include("db.php");
include("utils.php");

htmlHeader("Uživatelé");
	

if (array_key_exists("change", $_POST))
{

	?>
<H2>Změna uživatele</H2>
<P>
</P>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="people.php">Zpět na uživatele</A><BR>
<A HREF="admin.php">Zpět na administraci</A>
</P>
	<?php
}
else
{
	?>
<H2>Správa uživatelů</H2>	
<TABLE>
<TH>
	<TD>id</TD>
	<TD>jméno</TD>
	<TD>admin</TD>
	<TD>kam</TD>
	<TD>oz</TD>
	<TD>login</TD>
	<TD>heslo</TD>
	<TD colspan="2">akce</TD>
</TH>
<?php
	$db = mysql_connect($db_host, $db_user, $db_password);
	mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);
	$res = mysql_query("select id, name, admin, kam, oz, login, password from people");
	while ($row = mysql_fetch_row($res)) {
?>


<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0]); ?><input type = "hidden" name = "change" value = "1"></TD>
		<TD><input type="text" value="<?php echo(htmlspecialchars($row[1])); ?>"></TD>
		<TD><input type="text" value="<?php echo(htmlspecialchars($row[5])); ?>"></TD>
		<TD><input type="CHECKBOX" <?php echo(($row[2] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" <?php echo(($row[3] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" <?php echo(($row[4] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="text" value="<?php echo(htmlspecialchars($row[5])); ?>"></TD>
		<TD><input type="text" value="<?php echo(htmlspecialchars($row[6])); ?>"></TD>
		<TD><INPUT type = "submit" value="Upravit"></TD>
		<TD><INPUT type = "submit" value="Smazat"></TD>
	</TR>
</FORM>
<?php
}
	$res = mysql_query("select coalesce(max(id),0) from people");
	$row = mysql_fetch_row($res);
?>

<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0] + 1); ?><input type = "hidden" name = "change" value = "1"></TD>
		<TD><input type="text" value=""></TD>
		<TD><input type="text" value=""></TD>
		<TD><input type="CHECKBOX"></TD>
		<TD><input type="CHECKBOX"</TD>
		<TD><input type="CHECKBOX" checked></TD>
		<TD><input type="text" value=""></TD>
		<TD><input type="text" value=""></TD>
		<TD colspan="2"><INPUT type = "submit" value="Nový"></TD>

	</TR>
</FORM>
</TABLE>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="admin.php">Zpět na administraci</A>
</P>

	<?php
}
?>

</BODY>
</HTML>
