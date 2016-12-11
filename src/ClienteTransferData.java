import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
//Cositas de Nacho
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ClienteTransferData
{
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;
    
    //Cosas Nacho
	private JFrame frame;

	//Variables de seguridad
	private Key publicRSAKey;
	private Key privateRSAKey;
	private Key AESKey;
	
	
    //Constructor que recibe el socket de la conexion e iniciliza los datos
    public ClienteTransferData(Socket soc)
    {
        try
        {
        	
            ClientSoc=soc;
            din=new DataInputStream(ClientSoc.getInputStream());
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            br=new BufferedReader(new InputStreamReader(System.in));
            publicRSAKey=null;
            privateRSAKey=null;
            AESKey=null;
            generarRSA();
        }
        catch(Exception ex)
        {
        }        
    }
    //Función que recibe los datos
    void EnviarData() throws Exception
    {   
    	//Se pide la ruta del fichero
    	Cipher cipher=null;
        String archivo;
        System.out.print("Escriba el nombre del fichero :");
        archivo=br.readLine();
        
        //Se crea el objeto fichero y se verifica su existencia
        File f=new File(archivo);
        if(!f.exists())
        {
            System.out.println("Error 404: El fichero no existe...");
            dout.writeUTF("Archivo no encontrado");
            return;
        }
        
        //Se envía el nombre del archivo
        dout.writeUTF(archivo);
        
        //Espera mensaje del servidor para ver si existe ya el archivo
        String msgDeServer=din.readUTF();
        
        //Verificamos que el mensaje recibido es el de archivo existente
        if(msgDeServer.compareTo("Archivo existente")==0){
            String Opcion;
            System.out.println("El archivo ya existe. ¿Desea sobreescribirlo (S/N) ?");
            Opcion=br.readLine();            
            if(Opcion=="S"){
            	
            	//si la opcion es de sobreeesribir, continuamos la ejecución
                dout.writeUTF("S");
            }else{
            	
            	//Sino, la terminamos
                dout.writeUTF("N");
                return;
            }
        }
        //Compienza el envío del archivo
        System.out.println("Enviando archivo ...");
        
        //Se crea el stream que leerá el archivo del ordenador a la aplicación
        FileInputStream fin=new FileInputStream(f);
        
        //Se crea el stream que leerá el dato
        DataInputStream data= new DataInputStream(fin);
        
        //Si hay stream nos dará su tamaño
        int tamanyo = data.available();
        
        //Inicializamos la variable
        byte[] b=new byte[tamanyo];
        byte[] enc=new byte[tamanyo];
        data.readFully(b, 0, tamanyo);
        
        //Encriptamos
        cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, AESKey);
        enc=cipher.doFinal(b);
        
        System.out.println(b);
        System.out.println(enc);
        //Se envía por el dataoutput
        dout.write(b);
        /*
        int ch;
        do
        {
        	//Se lee el fichero de 4 en 4 bytes
        	ch=fin.read();
        	
        	//Se envía al servidor
            dout.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        */
        
        //Se cierra el stream del archivo
        fin.close();
        data.close();
        
        //Se recibe la última confirmación por parte del servidor
        System.out.println(din.readUTF());
        
    }
    
    void RecibirData() throws Exception
    {
        String archivo;
        System.out.print("Escriba el nombre del fichero :");
        archivo=br.readLine();
        //Lee el nombre del fichero y la envía al servidor
        dout.writeUTF(archivo);
        
        //Recibe el mensaje del servidor con el estado del envío
        String msgDeServer=din.readUTF();
        
        //Si no está encontrado, termina la ejecución
        if(msgDeServer.compareTo("Archivo no encontrado")==0){
            System.out.println("Error 404: Archivo no encontrado en el servidor ...");
            return;
        }else{
        	//Espera a la confirmación del servidor de que el archivo está listo
        	if(msgDeServer.compareTo("Preparado")==0){
	            System.out.println("Recibiendo Archivo ...");
	            File f=new File(archivo);
	            
	            //Si el archivo ya existe
	            if(f.exists())
	            {
	                String opcion;
	                System.out.println("El archivo ya existe. ¿Desea sobreescribirlo (S/N) ?");
	                opcion=br.readLine(); 
	                
	                //Si no desea sobreescribirlo, se borra el flujo de datos y se acaba la ejecución
	                if(opcion=="N")    
	                {
	                    dout.flush();
	                    return;    
	                }                
	            }
	            //Se crea un File Output para escribir el archivo recibido en el ordenador
	            FileOutputStream fout=new FileOutputStream(f);
				
		        //Se crea el stream que leerá el dato
		        DataOutputStream data= new DataOutputStream(fout);
		        
		        //Inicializamos la variable leyendo lo que nos pasan
		        byte[] b=null;
		        din.read(b);
		        
		        //Lo escribimos y cerramos
		        data.write(b);
		        
	            
	            /*int ch;
	            //string que almacena temporalmente el archivo
	            String temp;
	            do
	            {
	            	//Lee el archivo por el DataInputStream
	                temp=din.readUTF();
	                ch=Integer.parseInt(temp);
	                
	               //Escribe hasta que no es el final de documento
	                if(ch!=-1)
	                {
	                    fout.write(ch);                    
	                }
	                //Va escribiendo hasta llegar al final
	            }while(ch!=-1);
	            */
	            
	            //Cierra el stream que conecta con el ordenador
	            fout.close();
	            data.close();
	            
	            System.out.println(din.readUTF());
	                
	        }
        }
        
    }
    
    private void generarRSA() throws Exception{
    	
    	KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");
    	kpg.initialize(1024);
    	KeyPair kp=kpg.genKeyPair();
    	publicRSAKey = kp.getPublic();
    	privateRSAKey = kp.getPrivate();
    	
    }

    private void recibirAES() throws Exception{
    	Cipher cipher=null;
        byte[] encAESKey;
        int encAESlength;
        
        dout.writeUTF("SOLICITARCLAVE");
        dout.writeUTF(Base64.getEncoder().encodeToString(publicRSAKey.getEncoded()));
        encAESlength=din.read();
        encAESKey=new byte[encAESlength];
        din.readFully(encAESKey);
        cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateRSAKey);
        AESKey=new SecretKeySpec(cipher.doFinal(encAESKey), "AES");
    }
    
    public void displayMenu() throws Exception
    {
    	//Menú simple con las opciones básicas de recibir y enviar archivo
    	
    	/*Primero debe de hacer el login aquí, y si IF es certero, devuelve a este menú
    	 * Rudimentario que se modificará posteriormente
    	 * */
    	boolean login=false;
    	
    	//Cosas Nacho
    	frame=new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//Fuente del texto//
		JTextField textField = new JTextField();
		textField.setBounds(125,55,200,25);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JPasswordField textField2 = new JPasswordField();
		textField2.setBounds(125,125,200,25);
		frame.getContentPane().add(textField2);
		textField2.setColumns(10);
		
		//Label
		JLabel lblNewLabel=new JLabel("Usuario");
		lblNewLabel.setFont(new Font("Arial", 20, 20));
		lblNewLabel.setBounds(125,25,100,20);
		frame.getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel2=new JLabel("Contraseña");
		lblNewLabel2.setFont(new Font("Arial", 20, 20));
		lblNewLabel2.setBounds(125,100,150,20);
		frame.getContentPane().add(lblNewLabel2);
    	
		// Boton
		JButton btnNewButton=new JButton("Loguearse");
		btnNewButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
		        //Envío de la información al servidor
				try{
					dout.writeUTF("LOGIN");
					
					String user=textField.getText();
					char[] passChar=textField2.getPassword();
					String pass=new String(passChar);
					
					System.out.println("pass:"+pass);
					
		        	dout.writeUTF(user);
		        	dout.writeUTF(pass);
		        	
				}catch(Exception error){
					JOptionPane.showMessageDialog(null, "No se ha podido realizar el envio");
				}
		        //Esperamos la respuesta del servidor y realizamos el login
				try{
					String respuesta=din.readUTF();
					boolean login=false;
					if(respuesta.compareTo("LOGIN CORRECTO")==0){
			        	login=true;
			        }
					
					//Si la respuesta es correcta creamos el menú

					if(login){
						recibirAES();
						crearMenu();
					}else{
						//Sino mostrmaos el mensaje de alerta
						JOptionPane.showMessageDialog(null, "Usuario y/o contraseña incorrectos");
					}
				}catch(Exception error2){
					JOptionPane.showMessageDialog(null, "Error del servidor, inténtelo más tarde");
				}
			}
			
			public void crearMenu(){
		        //Crear el nuevo menú
				frame.dispose();
				
				//Iniciar las variables del menú
		    	frame=new JFrame();
				frame.setBounds(100, 100, 450, 300);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(null);
				
				//Fuente del texto//
				JTextField textField = new JTextField();
				textField.setBounds(40,45,350,40);
				frame.getContentPane().add(textField);
				textField.setColumns(10);
				
				//Label
				JLabel lblNewLabel=new JLabel("Inserte la ruta del archivo");
				lblNewLabel.setFont(new Font("Arial", 20, 20));
				lblNewLabel.setBounds(40,20,350,20);
				frame.getContentPane().add(lblNewLabel);
				
				// Boton
				JButton buttonEnviar=new JButton("Enviar");
				btnNewButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try{
							 dout.writeUTF("ENVIAR");
				             EnviarData();
						}catch(Exception error){
							JOptionPane.showMessageDialog(null, "Hubo un error con el envío");
						}
					}
				});
				
				JButton buttonRecibir=new JButton("Recibir");
				btnNewButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try{
							dout.writeUTF("RECOGER");
							RecibirData();
						}catch(Exception error2){
							JOptionPane.showMessageDialog(null, "Hubo un error con la descarga");
						}
					}
				});
				
				JButton buttonSalir=new JButton("Salir");
				btnNewButton.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try{
							dout.writeUTF("DESCONECTAR");
							System.exit(1);
						}catch(Exception error3){
							JOptionPane.showMessageDialog(null, "Error en la salida del sistema");
						}
					}
				});
				
				buttonEnviar.setFont(new Font("Arial", Font.BOLD, 20));
				buttonEnviar.setBounds(60, 100, 123, 52);
				frame.getContentPane().add(buttonEnviar);
				
				buttonRecibir.setFont(new Font("Arial", Font.BOLD, 20));
				buttonRecibir.setBounds(240, 100, 123, 52);
				frame.getContentPane().add(buttonRecibir);
				
				buttonSalir.setFont(new Font("Arial", Font.BOLD, 20));
				buttonSalir.setBounds(155, 180, 123, 52);
				frame.getContentPane().add(buttonSalir);
				
				frame.setVisible(true);
			}
		});
		
		//Fuente del boton//
		
		btnNewButton.setFont(new Font("Arial", Font.BOLD, 20));
		btnNewButton.setBounds(150, 170, 150, 52);
		frame.getContentPane().add(btnNewButton);
		
		//Activar el frame
		frame.setVisible(true);
		
    	//Código antiguo por si acaso
    	/*System.out.println("[ Login ]");
    	dout.writeUTF("LOGIN");
        System.out.println("Inserte el usuario:");
        String user=br.readLine();
        System.out.println("Inserte la contraseña");
        String pass=br.readLine();
        */
      
        //Obtenemos la clave AES usando RSA

        
    	//El if de comprobación que lleva a los diferentes menús
    	/*if(login){
	        while(true)
	        {    
	            System.out.println("[ MENÚ ]");
	            System.out.println("1. Enviar archivo");
	            System.out.println("2. Recibir archivo");
	            System.out.println("3. Salir");
	            System.out.print("\n Elija una opción :");
	            int eleccion;
	            eleccion=Integer.parseInt(br.readLine());
	            if(eleccion==1){
	                dout.writeUTF("ENVIAR");
	                EnviarData();
	            }
	            else{
	            	if(eleccion==2){
		                dout.writeUTF("RECOGER");
		                RecibirData();
		            }else{
		                dout.writeUTF("DESCONECTAR");
		                System.exit(1);
		            }
	            }
	        }
    	}else{
    		//Aquí muestra el mensaje de login fallido, vuélvalo a intentar
    		System.exit(1);
    	}*/
    }
}