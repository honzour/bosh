<?php

function htmlHeader($title) {
?><!DOCTYPE HTML>
<HTML>
<HEAD>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<TITLE><?php echo($title);?></TITLE>
<STYLE>
html,body {
-webkit-text-size-adjust:none;
-moz-text-size-adjust: none;
-ms-text-size-adjust: none;
 }
</STYLE>
</HEAD>
<BODY>
<?php

}

function errorHeader($code, $desc) {
		$protocol = (isset($_SERVER['SERVER_PROTOCOL']) ? $_SERVER['SERVER_PROTOCOL'] : 'HTTP/1.0');
		header($protocol . ' ' . $code . ' ' . $desc);
		die("Error $code, $desc");
}


$cookie_expire = time() + 86400 * 30;
$cookie_path = "/";

$var_login = "login";
$var_password = "password";



function loginUser() {
    global $person_id;
    global $name;
    global $admin;
    global $boss;
    global $worker;
	global $db;
	global $error;

	global $db_host;
	global $db_user;
	global $db_password;
	global $db_db;

	global $cookie_expire;
	global $cookie_path;

	global $var_login;
	global $var_password;

	$person_id = false;
	$name = false;
	$admin = false;
	$boss = false;
	$worker = false;
	$db = false;
	$error = false;
	

	if (isset($_POST[$var_login]) || isset($_POST[$var_password])) {
		$login = $_POST[$var_login];
		$password = $_POST[$var_password];
		setcookie($var_login, $login, $cookie_expire, $cookie_path);
		setcookie($var_password, $password, $cookie_expire, $cookie_path);

	} else {
		if (isset($_COOKIE[$var_login]) || isset($_COOKIE[$var_password])) {
			$login = $_COOKIE[$var_login];
			$password = $_COOKIE[$var_password];
		} else {
			$login = "";
			$password = "";
		}
	}

//echo($login . "' '". $password);

	$db = mysql_connect($db_host, $db_user, $db_password);
	if (!$db) {
		errorHeader(500, "Cannot connect to the database " . mysql_error());
	}
    mysql_set_charset ("utf8", $db);
	if (!mysql_select_db($db_db, $db)) {
		errorHeader(500, "Cannot select the database " . mysql_error($db));
	}

	$loginok = true;
	if ($login == "" || $password == "")
	{
		$loginok = false;
	} else {
		$result = mysql_query("SELECT id, name, admin, boss, worker FROM people where login = '" . mysql_escape_string($login) . "' and password = '" . mysql_escape_string($password) . "'",$db);
		if (!$result) {
			echo(mysql_error());
		}
		$row = mysql_fetch_row($result);
		if (!$row) {
			$loginok = false;

			$error = "Chybný login nebo heslo. Přihlaste se znovu.";

		}
		else
		{
			$person_id = $row[0];
			$name = $row[1];
			$admin = $row[2];
			$boss = $row[3];
			$worker = $row[4];
		}
	}
}

?>
