<?php
include("db.php");
include("utils.php");

	setcookie($var_login, "", time() + 3600, $cookie_path);
	setcookie($var_password, "", time() + 3600, $cookie_path);


	htmlHeader("Odhlášení");
?>
<H3>Odhlášení</H3>
<P>
Jste úspěšně odhlášeni.
</P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="people.php">Editace osob</A><BR>
<A HREF="admin.php">Administrace</A><BR>
<A HREF="csv.php">CSV export</A>
<P>
</P>
</BODY>
</HTML>

