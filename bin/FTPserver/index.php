<?php
	session_start();
	include("conexion.php");
?>
<!DOCTYPE html>
<html lang="es">

<head>
   <title>Servidor FTP</title>
	<meta charset="UTF-8"/>
</head>

<body>
	<?php 
		$login=false;
		if(isset($_POST["user"])){
			$user=$_POST["user"];
			$pass=$_POST["pass"];
			$sentencia="";
			$resultado=mysqli_query($conexion,$sentencia);
			$fila=mysqli_fetch_assoc($resultado);
			if($fila["nombre"]==$user && $fila["contra"]==$pass){
				$login=true;
			}
		}
		if($login){
			echo "Login correcto";
		}else{
			echo "Login fallido";
		}
		
		session_destroy();
		mysqli_close($conexion);
	?>

</body>
</html>