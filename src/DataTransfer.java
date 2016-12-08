import java.net.*;
import java.io.*;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;

public class DataTransfer extends Thread {

	Socket ClientSoc;

	DataInputStream din;
	DataOutputStream dout;
	
	//Crea los streams de entrada y salida con el socket pasado por par�metro
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
	
	//Funci�n enviar archivo que envia el archivo al usuario cliente
	void EnviarData() throws Exception
	{       
		//Lee el directorio del archivo
		String archivo=din.readUTF();
		
		//Crea la ruta abstracta que carga el archivo
		File f=new File(archivo);
		
		//Si no existe, se pasa la se�al al usuario y se vuelve atr�s
		if(!f.exists())
		{
			dout.writeUTF("Archivo no encontrado");
			return;
		}
		//Si existe, manda la se�al de preparado y lee el archivo del servidor
		else
		{
			dout.writeUTF("Preparado");
			
			//Lee el archivo del servidor
			FileInputStream fin=new FileInputStream(f);
			
			//Lo env�a de 4 en 4 bytes por el Data Output
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
	
	 //Funci�n que recibe un archivo y lo guarda en servidor
	void RecibirData() throws Exception
	{
		//Lee el mensaje del cliente
		String archivo=din.readUTF();
		
		//Si el archivo no ha sido encontrado por parte del cliente, entonces acabamos la ejecuci�n
		if(archivo.compareTo("Archivo no encontrado")==0)
		{
			return;
		}
		
		//Sino, creamos el archivo con la ruta especificada
		File a=new File(archivo);
		String opcion;
		
		//Si existe el archivo, dec�rselo al cliente y leer su respuesta
		if(a.exists())
		{
			dout.writeUTF("Archivo existente");
			opcion=din.readUTF();
		}
		else
		{
			//Sino, directamente elige la opci�n de crear
			dout.writeUTF("Enviar Archivo");
			opcion="S";
		}
		
		//Si la opci�n recibida es la de almacenar el archivo
		if(opcion.compareTo("S")==0)
		{
			//Se crea el outputstream que guardar� el archivo en el servidor
			FileOutputStream fout=new FileOutputStream(a);
			
			//Par�metros locales que leer�n el archivo
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
			
			//Mandamos la se�al de que el archivo ha sido enviado correctamente
			dout.writeUTF("Archivo enviado correctamente");
		}
		//Si no se elige la opcion S, se vuelve atras
		else
		{
			return;
		}

	}

	public boolean login() throws Exception{
		//leemos las variables por parte del usuario
		String user=din.readUTF();
		String pass=din.readUTF();
        System.out.println("Se han recibido los par�metros");
		
		//Creamos la url que nos llevar� a nuestro servidor local con la base de datos
		String url = "http://localhost/FTPserver/index.php";
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();

		//a�ade el header al Post, que es la informaci�n meta de la petici�n
		con.setRequestProperty("Method","POST");
		con.setRequestProperty("User-Agent", "Mozilla\5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		//aqu� a�adimos los valores del string que pasaremos por el post
		String urlParametros = "user="+user+"&pass="+pass;

		// Enviar la petici�n del post
		//Primero confirma que se puede realizar dicho envio
		con.setDoOutput(true);
		
		//Genera el output stream para poder escribir en la direcci�n del post
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		
		//Manda los par�metros
		wr.writeBytes(urlParametros);
		wr.flush();
		wr.close();

		//Respuesta por parte de la base de datos/servidor local en valor de int
		//Supongo que se referir� a error 404 y tal
		//int respuestaCod = con.getResponseCode();
		//System.out.println("Se�al del servidor : " + respuestaCod);

		//Lee la respuesta generada por el PHP como si fuera una p�gina web
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String linea;
		StringBuffer respuesta = new StringBuffer();

		while ((linea = in.readLine()) != null) {
			respuesta.append(linea);
		}
		in.close();
		String login=respuesta.toString();
		
		//Ahora tratamos la respuesta por parte del codigo, en este caso,
		//Si la respuesta es correcta, devolvemos que el login ha sido correcto
		boolean logueo=false;
		if(login.contains("Login correcto")){
			logueo=true;
		}
		
		//Devolvemos el resultado de la comparaci�n
		return logueo;
	}

	public void run()
	{
		//Sentencia de comandos que debe de leer el servidor, el while(true)
		//permite que se ejecute constantemente hasta recibir la se�al de exit.
		while(true)
		{
			try
			{
				System.out.println("Esperando comando ...");
				String comando=din.readUTF();
				if(comando.compareTo("LOGIN")==0){
					System.out.println("\t RECOGER Comando recibido ...");
					if(login()){
						dout.writeUTF("LOGIN CORRECTO");
					}else{
						dout.writeUTF("LOGIN FALLIDO");
					}
					continue;
				}else{
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
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

}
