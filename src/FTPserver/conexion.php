<?php
        $datos=parse_ini_file('conexion.ini'); 
        $conexion=mysqli_connect($datos['Server'],$datos['User'],$datos['Password'],$datos['Database']);
        if(!mysqli_ping($conexion)){
            die('<strong>No pudo conectarse:</strong>'.mysqli_connect_error());
        }
	?>