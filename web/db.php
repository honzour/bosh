<?php

$db_host = "localhost";
$db_user = "root";
$db_password = "korinek";
$db_db = "bosh";

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

	$db = mysql_connect($db_host, $db_user, $db_password);
	if (!$db) {
		echo(mysql_error());
	}
    mysql_set_charset ("utf8", $db);
	mysql_select_db($db_db, $db);

	$loginok = true;
	if ($login == "" || $password == "")
	{
		$loginok = false;
	} else {
		$result = mysql_query("SELECT id, name, admin, kam, oz FROM people where login = '" . mysql_escape_string($login) . "' and password = '" . mysql_escape_string($password) . "'",$db);
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
