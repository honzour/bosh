<?php

$db_host = "localhost";
$db_user = "root";
$db_password = "korinek";
$db_db = "bosh";


function errorHeader($code, $desc) {
		$protocol = (isset($_SERVER['SERVER_PROTOCOL']) ? $_SERVER['SERVER_PROTOCOL'] : 'HTTP/1.0');
		header($protocol . ' ' . $code . ' ' . $desc);
		die("Error $code, $desc");
}

?>
