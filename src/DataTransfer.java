import java.net.*;
import java.io.*;
import java.util.*;

public class DataTransfer extends Thread {

	Socket ClientSoc;

	DataInputStream din;
	DataOutputStream dout;
	
	//Crea los streams de entrada y salida con el socket pasado por parámetro
	public DataTransfer(Socket soc)
	{
		try
		{
			ClientSoc=soc;                        
			din=new DataInputStream(ClientSoc.getInputStream());
			dout=new DataOutputStream(ClientSoc.getOutputStream());
			System.out.println("Cliente FTP Conectado ...");
			start();

		}
		catch(Exception ex)
		{
		}        
	}
	
	//Función enviar archivo que envia el archivo al usuario cliente
	void EnviarData() throws Exception
	{       
		//Lee el directorio del archivo
		String archivo=din.readUTF();
		
		//Crea la ruta abstracta que carga el archivo
		File f=new File(archivo);
		
		//Si no existe, se pasa la señal al usuario y se vuelve atrás
		if(!f.exists())
		{
			dout.writeUTF("Archivo no encontrado");
			return;
		}
		//Si existe, manda la señal de preparado y lee el archivo del servidor
		else
		{
			dout.writeUTF("Preparado");
			
			//Lee el archivo del servidor
			FileInputStream fin=new FileInputStream(f);
			
			//Lo envía de 4 en 4 bytes por el Data Output
			int ch;
			do
			{
				ch=fin.read();
				dout.writeUTF(String.valueOf(ch));
			}
			//Cuando llega a final de documento, finaliza
			while(ch!=-1);    
			fin.close();    
			dout.writeUTF("Archivo recibido correctamente");                            
		}
	}
	
	 //Función que recibe un archivo y lo guarda en servidor
	void RecibirData() throws Exception
	{
		//Lee el mensaje del cliente
		String archivo=din.readUTF();
		
		//Si el archivo no ha sido encontrado por parte del cliente, entonces acabamos la ejecución
		if(archivo.compareTo("Archivo no encontrado")==0)
		{
			return;
		}
		
		//Sino, creamos el archivo con la ruta especificada
		File a=new File(archivo);
		String opcion;
		
		//Si existe el archivo, decírselo al cliente y leer su respuesta
		if(a.exists())
		{
			dout.writeUTF("Archivo existente");
			opcion=din.readUTF();
		}
		else
		{
			//Sino, directamente elige la opción de crear
			dout.writeUTF("Enviar Archivo");
			opcion="S";
		}
		
		//Si la opción recibida es la de almacenar el archivo
		if(opcion.compareTo("S")==0)
		{
			//Se crea el outputstream que guardará el archivo en el servidor
			FileOutputStream fout=new FileOutputStream(a);
			
			//Parámetros locales que leerán el archivo
			int ch;
			String temp;
			do
			{
				//Vamos leyendo el archivo hasta llegar a final de documento
				temp=din.readUTF();
				ch=Integer.parseInt(temp);
				if(ch!=-1)
				{
					fout.write(ch);                    
				}
			}while(ch!=-1);
			
			//Una vez acabamos invocamos close para "cerrar" el archivo
			fout.close();
			
			//Mandamos la señal de que el archivo ha sido enviado correctamente
			dout.writeUTF("Archivo enviado correctamente");
		}
		//Si no se elige la opcion S, se vuelve atras
		else
		{
			return;
		}

	}


	public void run()
	{
		//Sentencia de comandos que debe de leer el servidor, el while(true)
		//permite que se ejecute constantemente hasta recibir la señal de exit.
		while(true)
		{
			try
			{
				System.out.println("Esperando comando ...");
				String comando=din.readUTF();
				if(comando.compareTo("RECOGER")==0){
					System.out.println("\t RECOGER Comando recibido ...");
					EnviarData();
					continue;
				}else{ 
					if(comando.compareTo("ENVIAR")==0){
						System.out.println("\t ENVIAR Comando recibido ...");                
						RecibirData();
						continue;
					}else{ 
						if(comando.compareTo("DESCONECTAR")==0){
							System.out.println("\t DESCONECTAR Comando recibido ...");
							System.exit(1);
						}
					}
				}
			}
			catch(Exception ex)
			{
			}
		}
	}

}
