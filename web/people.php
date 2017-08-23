<?php
include("db.php");
include("utils.php");

htmlHeader("Uživatelé");
	

?>
<H2>Správa uživatelů</H2>	
<TABLE border="3">
<TR>
	<TD>id</TD>
	<TD>jméno</TD>
	<TD>admin</TD>
	<TD>kam</TD>
	<TD>oz</TD>
	<TD>login</TD>
	<TD>heslo</TD>
	<TD colspan="2">akce</TD>
</TR>
<?php
	$db = mysql_connect($db_host, $db_user, $db_password);
	mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);
	if ((array_key_exists("change", $_POST))) {
		$query = "update people set " . 
			"name = '" . mysql_escape_string($_POST["name"]) . "', " .
			"login = '" . mysql_escape_string($_POST["login"]) . "', " .
			"password = '" . mysql_escape_string($_POST["password"]) . "', " .
			"kam = " . (isset($_POST["kam"]) ? 1 : 0) . ", " .
			"oz = " . (isset($_POST["oz"]) ? 1 : 0) . ", " .
			"admin = " . (isset($_POST["admin"]) ? 1 : 0) . 
			" where id = '" . mysql_escape_string($_POST["id"]) . "'";
		mysql_query($query);
	}
	if ((array_key_exists("new", $_POST))) {
		$query = "insert into people (name, login, password, kam, oz, admin) values (" . 
			"'" . mysql_escape_string($_POST["name"]) . "', " .
			"'" . mysql_escape_string($_POST["login"]) . "', " .
			"'" . mysql_escape_string($_POST["password"]) . "', " .
			(isset($_POST["kam"]) ? 1 : 0) . ", " .
			(isset($_POST["oz"]) ? 1 : 0) . ", " .
			(isset($_POST["admin"]) ? 1 : 0) .
			")";
		mysql_query($query);
	}
	$res = mysql_query("select id, name, admin, kam, oz, login, password from people");
	while ($row = mysql_fetch_row($res)) {
?>


<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0]); ?><input type = "hidden" name = "id" value = "<?php echo($row[0]); ?>"></TD>
		<TD><input type="text" name="name" value="<?php echo(htmlspecialchars($row[1])); ?>"></TD>
		<TD><input type="CHECKBOX" name="admin" <?php echo(($row[2] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" name="kam" <?php echo(($row[3] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" name="oz" <?php echo(($row[4] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="text" name="login" value="<?php echo(htmlspecialchars($row[5])); ?>"></TD>
		<TD><input type="text" name="password" value="<?php echo(htmlspecialchars($row[6])); ?>"></TD>
		<TD><INPUT type = "submit" name= "change" value="Upravit"></TD>
		<TD><INPUT type = "submit" name= "delete" value="Smazat"></TD>
	</TR>
</FORM>
<?php
}
	$res = mysql_query("select coalesce(max(id),0) from people");
	$row = mysql_fetch_row($res);
?>

<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0] + 1); ?></TD>
		<TD><input type="text" name="name" value=""></TD>
		<TD><input type="CHECKBOX" name="admin"></TD>
		<TD><input type="CHECKBOX" name="kam"></TD>
		<TD><input type="CHECKBOX" name="oz" checked></TD>
		<TD><input type="text" name="login" value=""></TD>
		<TD><input type="text" name="password" value=""></TD>
		<TD colspan="2"><INPUT type = "submit" name = "new" value="Nový"></TD>

	</TR>
</FORM>
</TABLE>
<P>
<?php
if (isset($query)) {
	echo("Provedena změna v databázi: " . $query);
}
?>
</P>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="admin.php">Zpět na administraci</A>
</P>

</BODY>
</HTML>
