import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorFTP {


	public static void main(String args[]) throws Exception
	{
		
		//Abre el puerto 5218 /deberiamos de especificarlo nosotros por parámetro
		ServerSocket soc=new ServerSocket(5218);
		System.out.println("Servidor FTP comenzo en el puerto numero 5218");
		while(true)
		{
			System.out.println("Esperando para conectar ...");
			DataTransfer t=new DataTransfer(soc.accept());

		}
	}

}
