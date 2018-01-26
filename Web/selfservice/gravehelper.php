<?php

$servername = "localhost";
$username = "user";
$password = "pass";
$dbname = "db";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
 die("Connection failed: " . $conn->connect_error);
} 

function print_PageHead()
{
	echo '<html>';
	echo '<head>';
	echo "<style>";
	echo "body { font-family: 'Arial'; }";
	echo "td { font-family: 'Arial'; font-size: 10pt; }";
	echo "tr { font-family: 'Arial'; font-size: 10pt; }";
	echo "</style>";
	echo '</head>';
	echo '<body><h1>Grave-Helper</h1>';
}

function print_PageFooter()
{
	echo "</body></html>";
}

function print_DBResult($code, $conn)
{


    $theCode = mysqli_real_escape_string ( $conn , $code );
	$sql = "CALL show_graves_for_player_enjin('" . $theCode . "')";

	echo '<hr>';
	echo '<p>The following Graves have been found. Before contacting a Staff member, please visit the location and check if you can find the grave yourself.<br>';
	echo '<b>Graves are purged from the Server after 14 Days</b>; So keep an eye on the "Grave expires" column. It will be impossible for you and for us to recover items from your grave after that time.</p>';
	echo '<br>';
	
	$result = $conn->query($sql);
	$fields_num = mysqli_num_fields($result);

	echo "<table border='1' cellpadding='1' cellspacing='1' width=100%><tr>";
	echo '<th width=20%>Dimension</th>';
	echo '<th width=15%>X</th>';
	echo '<th width=15%>Y</th>';
	echo '<th width=15%>Z</th>';
	echo '<th width=15%>Grave placed</th>';
	echo '<th width=20%>Grave expires</th>';
	echo "</tr>\n";
	// printing table rows
	$colored = false;

	while($row = mysqli_fetch_row($result))
	{
	  $colored = !$colored;
	  if($colored)
		echo '<tr BGCOLOR="#e5efff">';
	  else
		echo '<tr>';

	  echo '<td>' . $row[0] . '</td>';
	  echo '<td>' . $row[1] . '</td>';
	  echo '<td>' . $row[2] . '</td>';
	  echo '<td>' . $row[3] . '</td>';
	  echo '<td>' . $row[4] . '</td>';
	  echo '<td>' . $row[5] . '</td>';
	  echo "</tr>\n";
	}
	mysqli_free_result($result);
	echo '</table>';
	echo '<br><a href="gravehelper.php">Back to Main-Page</a>';
}

function printForm()
{
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="view.css" media="all">
<script type="text/javascript" src="view.js"></script>

</head>
<body id="main_body" >
	
	<img id="top" src="top.png" alt="">
	<div id="form_container">
	
		<h1><a>Untitled Form</a></h1>
		<form id="form_69015" class="appnitro"  method="post" action="gravehelper.php">
					<div class="form_description">
			<h2>GTNH Self-Service Portal :: Server Delta</h2>
		</div>						
			<ul >
			
					<li id="li_1" >
		<label class="description" for="codebox">Enter your lookup code </label>
		<div>
			<input id="codebox" name="code" class="element text medium" type="text" maxlength="255" value=""/> 
		</div><p class="guidelines" id="guide_1"><small>Login on Delta, and type in /gravehelper request to generate your personal lookup code</small></p> 
		</li>
		<li class="buttons">
				<input id="saveForm" class="button_text" type="submit" name="submit" value="Submit" />
		</li>
			</ul>
		</form>	
	</div>
	<img id="bottom" src="bottom.png" alt="">
	</body>
</html>
<?php
}

if (isset($_POST['code']))
{
	print_PageHead();
	print_DBResult($_POST['code'], $conn);
	print_PageFooter();
} else 
{
	printForm();
}

?>
