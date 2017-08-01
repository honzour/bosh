<?php
include("db.php");
include("utils.php");

htmlHeader("Administrace");

if (array_key_exists("csv", $_POST))
{
	?>
<H2>Nahrávání CSV souboru</H2>
<P>
Nahrávám csv soubor...<BR>
<?php
	if (!array_key_exists('file', $_FILES) || !(strlen($tmpName = $_FILES['file']['tmp_name']) > 0)) {
		?>
		Soubor se nepodařilo nahrát na server.<BR>
		<?php
	}
	else
	{
			$csvFile = file_get_contents($tmpName);
			if (!$csvFile)
			{
			?>
				Soubor se nepodařilo otevřít<BR>
			<?php

			}
			else
			{
				?>
					Parsuji soubor...<BR>
				<?php
				$rows = explode("\n", $csvFile);
				if (!$rows)
				{
				?>
					Soubor se nepodařilo rozdělit na řádky<BR>
				<?php
				}
				else
				{
				?>
					Soubor má <?php echo(count($rows)); ?> řádků<BR>
					<?php
					foreach($rows as $row => $data)
					{		
						echo ($row  . " + " . $data . "</BR>");
					}
				}

			}

	}
?>
</P>
<P>
<A HREF="index.php">Zpět na hlavní stránku</A><BR>
<A HREF="admin.php">Zpět na administraci</A>
</P>
	<?php
}
else
{
	?>
<H2>Administrace</H2>	
<FORM action="admin.php" method="post" enctype="multipart/form-data">
<input type = "hidden" name = "csv" value = "1">
<TABLE>
<TR><TD>CSV soubor s daty:</TD><TD><INPUT type = "file" name="file"></TD></TR>
</TABLE>
<P>
<INPUT type = "submit">
</FORM>
	<?php
}
?>

</BODY>
</HTML>
