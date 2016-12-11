import java.net.*;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.X509EncodedKeySpec;
import java.io.*;
import java.util.*;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.sql.*;

public class DataTransfer extends Thread {

	Socket ClientSoc;
	private String nusuario;
	private String DB_URL = "jdbc:mysql://localhost/FTPServer";
	DataInputStream din;
	DataOutputStream dout;
	
	//Variables de seguridad
	private Key publicRSAKey;
	private Key privateRSAKey;
	private Key AESKey;
	
	//Crea los streams de entrada y salida con el socket pasado por par�metro
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
			
			//Se crea el stream que leer� el dato
	        DataInputStream data= new DataInputStream(fin);
	        
	        //Si hay stream nos dar� su tama�o
	        int tamanyo = data.available();
	        
	        //Inicializamos la variable
	        byte[] b=new byte[tamanyo];
	        byte[] enc;
	        data.readFully(b);
	        
	        Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
	        enc=cipher.doFinal(b);
	        //Se env�a por el dataoutput
	        dout.write(enc);
			/*
			//Lo env�a de 4 en 4 bytes por el Data Output
			int ch;
			do
			{
				ch=fin.read();
				dout.writeUTF(String.valueOf(ch));
			}
			//Cuando llega a final de documento, finaliza
			while(ch!=-1);   */
			
			data.close();
			fin.close();    
			dout.writeUTF("Archivo enviado correctamente");                            
		}
	}
	
	 //Funci�n que recibe un archivo y lo guarda en servidor
	void RecibirData() throws Exception
	{
		//Lee el mensaje del cliente
		Cipher cipher=null;
		String leer=din.readUTF();
		String[] rutaString=leer.split("\\\\");
		String ruta=rutaString[rutaString.length-1];
		String archivo="C:\\xampp\\htdocs\\FTPServer\\"+ruta;
		
		//Si el archivo no ha sido encontrado por parte del cliente, entonces acabamos la ejecuci�n
		if(leer.compareTo("Archivo no encontrado")==0)
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
			
	        //Se crea el stream que leer� el dato
	        DataOutputStream data= new DataOutputStream(fout);
	        
	        //Inicializamos la variable leyendo lo que nos pasan
	        byte[] b=null;
	        int blength = din.read();
	        b=new byte[blength];
	        din.readFully(b);
	        cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, AESKey);
	        b=cipher.doFinal(b);
	        //Lo escribimos y cerramos
	        data.write(b);
	        
	        /*
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
			*/
	        
			//Una vez acabamos invocamos close para "cerrar" el archivo
			fout.close();
			data.close();
			
			//Mandamos la se�al de que el archivo ha sido enviado correctamente
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
    	RSACliente=KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(RSAClientebyte));
    	cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
    	cipher.init(Cipher.ENCRYPT_MODE, RSACliente);
    	key=cipher.doFinal(AESKey.getEncoded());
    	dout.write(key.length);
    	dout.write(key);    	
    }
    
	public boolean login() throws Exception{
		
		//leemos las variables por parte del usuario
		String user=din.readUTF();
		int passl=din.read();
		byte[] pass=new byte[passl];
		din.readFully(pass);
		Cipher cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, AESKey);
		pass=cipher.doFinal(pass);
		String password=String.format("%064x", new java.math.BigInteger(1, pass));
		System.out.println(password);
		boolean logueo=false;
		
		Connection conn=null;
		Statement envio=null;
		try{
		//REgistra el driver JDBC
		Class.forName("com.mysql.jdbc.Driver");
		
		//Creamos la conexi�n
		conn = DriverManager.getConnection(DB_URL, "usuarioprueba","prueba");
		envio = conn.createStatement();
		
		//Hacemos la petici�n
		ResultSet rs = envio.executeQuery("SELECT * from usuarios WHERE usuarios.nombre='"+user+"'");
		while(rs.next()){
			if(user.compareTo(rs.getString("nombre"))==0 && password.compareTo(rs.getString("contra"))==0){
				logueo=true;
			}
		}
		
		rs.close();
		envio.close();
		conn.close();
	   }catch(SQLException se){
		      //Miramos errores del JDBC
		      se.printStackTrace();
	   }catch(Exception e){
		   //Posibles errores del Class.name
		   e.printStackTrace();
	   }finally{
		   	//�ltimas comprobaciones
		   try{
			   if(envio!=null)
				   envio.close();
		   }catch(SQLException se2){
		   }
		   try{
			   if(conn!=null)
				   conn.close();
		   }catch(SQLException se){
			   se.printStackTrace();
		   }
	   }
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
					System.out.println("\t LOGIN Comando recibido ...");
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
