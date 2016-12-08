import java.net.*;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.io.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

public class DataTransfer extends Thread {

	Socket ClientSoc;
	private String nusuario;
	DataInputStream din;
	DataOutputStream dout;
	
	//Variables de seguridad
	private Key publicRSAKey;
	private Key privateRSAKey;
	private Key AESKey;
	
	//Crea los streams de entrada y salida con el socket pasado por parámetro
	public DataTransfer(Socket soc)
	{
		try
		{
            publicRSAKey=null;
            privateRSAKey=null;
            AESKey=null;
            generarRSA();
            generarAES();
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
		String leer=din.readUTF();
		String[] rutaString=leer.split("\\\\");
		String ruta=rutaString[rutaString.length-1];
		String archivo="C:\\xampp\\htdocs\\FTPServer\\"+ruta;
		
		//Si el archivo no ha sido encontrado por parte del cliente, entonces acabamos la ejecución
		if(leer.compareTo("Archivo no encontrado")==0)
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

    public void generarRSA() throws Exception{
    	
    	KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(1024);
    	KeyPair kp=kpg.genKeyPair();
    	publicRSAKey = kp.getPublic();
    	privateRSAKey = kp.getPrivate();
    	
    }
    
    public void generarAES() throws Exception{
    	
    	KeyGenerator keygen =KeyGenerator.getInstance("AES");
    	keygen.init(128);
    	AESKey=keygen.generateKey();
    	
    }
    
    public void solicitarClave() throws Exception{
    	Cipher cipher=null;
    	byte[] key=null;
    	Key RSACliente=null;
    	byte[] RSAClientebyte=null;
    	
    	RSAClientebyte=Base64.getDecoder().decode(din.readUTF());
    	RSACliente=new SecretKeySpec(RSAClientebyte, 0, RSAClientebyte.length, "RSA");
    	cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
    	cipher.init(Cipher.ENCRYPT_MODE, RSACliente);
    	key=cipher.doFinal(AESKey.getEncoded());
    	dout.write(key);
    	
    }
    
	public boolean login() throws Exception{
		//leemos las variables por parte del usuario
		String user=din.readUTF();
		String pass=din.readUTF();
		
		//Creamos la url que nos llevará a nuestro servidor local con la base de datos
		String url = "http://localhost/FTPserver/index.php";
		URL obj = new URL(url);
		URLConnection con = obj.openConnection();

		//añade el header al Post, que es la información meta de la petición
		con.setRequestProperty("Method","POST");
		con.setRequestProperty("User-Agent", "Mozilla\5.0");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		//aquí añadimos los valores del string que pasaremos por el post
		String urlParametros = "user="+user+"&pass="+pass;

		// Enviar la petición del post
		//Primero confirma que se puede realizar dicho envio
		con.setDoOutput(true);
		
		//Genera el output stream para poder escribir en la dirección del post
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		
		//Manda los parámetros
		wr.writeBytes(urlParametros);
		wr.flush();
		wr.close();

		//Respuesta por parte de la base de datos/servidor local en valor de int
		//Supongo que se referirá a error 404 y tal
		//int respuestaCod = con.getResponseCode();
		//System.out.println("Señal del servidor : " + respuestaCod);

		//Lee la respuesta generada por el PHP como si fuera una página web
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
		
		//Devolvemos el resultado de la comparación
		return logueo;
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
				if(comando.compareTo("LOGIN")==0){
					System.out.println("\t RECOGER Comando recibido ...");
					if(login()){
						dout.writeUTF("LOGIN CORRECTO");
					}else{
						dout.writeUTF("LOGIN FALLIDO");
						System.exit(1);
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
							else{
								if(comando.compareTo("SOLICITARCLAVE")==0){
									solicitarClave();
									continue;
								}
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
