<?php
include("db.php");
include("utils.php");

htmlHeader("Uživatelé");
	

?>

<?php

	loginUser();
	
	if ($admin) {
		$loginok = true;
	} else
	{
		if ($person_id) {
?>
Nejste přihlášen jako administrátor.
<P>
<A HREF="logout.php">Odhlášení</A><BR>
</P>
<?php
		} else
	{
?>
Nejste vůbec přihlášen.
<P>
<A HREF="index.php">Přihlášení na hlavni stránce</A><BR>
</P>
<?php
	}
			
	} 
	
	if ($loginok)
	{
?>
<H2>Správa uživatelů</H2>	
<TABLE border="3">
<TR>
	<TD>id</TD>
	<TD>jméno</TD>
	<TD>admin</TD>
	<TD>boss</TD>
	<TD>worker</TD>
	<TD>login</TD>
	<TD>heslo</TD>
	<TD colspan="2">akce</TD>
</TR>
<?php

		if ((array_key_exists("change", $_POST))) {
			$query = "update people set " . 
				"name = '" . mysql_escape_string($_POST["name"]) . "', " .
				"login = '" . mysql_escape_string($_POST["flogin"]) . "', " .
				"password = '" . mysql_escape_string($_POST["fpassword"]) . "', " .
				"boss = " . (isset($_POST["boss"]) ? 1 : 0) . ", " .
				"worker = " . (isset($_POST["worker"]) ? 1 : 0) . ", " .
				"admin = " . (isset($_POST["admin"]) ? 1 : 0) . 
				" where id = '" . mysql_escape_string($_POST["id"]) . "'";
			mysql_query($query);
		}
		if ((array_key_exists("delete", $_POST))) {
			$query = "delete from people where id = '" . mysql_escape_string($_POST["id"]) . "'";
			mysql_query($query);
		}

		if ((array_key_exists("new", $_POST))) {
			$query = "insert into people (id, name, login, password, boss, worker, admin) values (" . 
				mysql_escape_string($_POST["id"]) . ", " .
				"'" . mysql_escape_string($_POST["name"]) . "', " .
				"'" . mysql_escape_string($_POST["flogin"]) . "', " .
				"'" . mysql_escape_string($_POST["fpassword"]) . "', " .
				(isset($_POST["boss"]) ? 1 : 0) . ", " .
				(isset($_POST["worker"]) ? 1 : 0) . ", " .
				(isset($_POST["admin"]) ? 1 : 0) .
				")";
			mysql_query($query);
		}
		$res = mysql_query("select id, name, admin, boss, worker, login, password from people");
		while ($row = mysql_fetch_row($res)) {
?>


<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0]); ?><input type = "hidden" name = "id" value = "<?php echo($row[0]); ?>"></TD>
		<TD><input type="text" name="name" value="<?php echo(htmlspecialchars($row[1])); ?>"></TD>
		<TD><input type="CHECKBOX" name="admin" <?php echo(($row[2] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" name="boss" <?php echo(($row[3] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="CHECKBOX" name="worker" <?php echo(($row[4] == 1) ? "checked" : ""); ?>></TD>
		<TD><input type="text" name="flogin" value="<?php echo(htmlspecialchars($row[5])); ?>"></TD>
		<TD><input type="text" name="fpassword" value="<?php echo(htmlspecialchars($row[6])); ?>"></TD>
		<TD><INPUT type = "submit" name= "change" value="Upravit"></TD>
		<TD><INPUT type = "submit" name= "delete" value="Smazat"></TD>
	</TR>
</FORM>
<?php
	}
	$res = mysql_query("select coalesce(max(id),0) from people order by id");
	$row = mysql_fetch_row($res);
?>

<FORM action="people.php" method="post">
	<TR>
		<TD><?php echo($row[0] + 1); ?><input type = "hidden" name = "id" value = "<?php echo($row[0] + 1); ?>"></TD>
		<TD><input type="text" name="name" value=""></TD>
		<TD><input type="CHECKBOX" name="admin"></TD>
		<TD><input type="CHECKBOX" name="boss"></TD>
		<TD><input type="CHECKBOX" name="worker" checked></TD>
		<TD><input type="text" name="flogin" value=""></TD>
		<TD><input type="text" name="fpassword" value=""></TD>
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
<?php
}
?>
</BODY>
</HTML>
