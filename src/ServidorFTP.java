import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorFTP {


	public static void main(String args[]) throws Exception
	{
		ServerSocket soc=new ServerSocket(5218);
		System.out.println("Servidor FTP comenzo en el puerto numero 5217");
		while(true)
		{
			System.out.println("Esperando para conectar ...");
			DataTransfer t=new DataTransfer(soc.accept());

		}
	}

}
