import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

class ClienteTransferData
{
    Socket ClientSoc;

    DataInputStream din;
    DataOutputStream dout;
    BufferedReader br;
    
    //Constructor que recibe el socket de la conexion e iniciliza los datos
    public ClienteTransferData(Socket soc)
    {
        try
        {
            ClientSoc=soc;
            din=new DataInputStream(ClientSoc.getInputStream());
            dout=new DataOutputStream(ClientSoc.getOutputStream());
            br=new BufferedReader(new InputStreamReader(System.in));
        }
        catch(Exception ex)
        {
        }        
    }
    //Funci�n que recibe los datos
    void EnviarData() throws Exception
    {   
    	//Se pide la ruta del fichero
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
        
        //Se env�a el nombre del archivo
        dout.writeUTF(archivo);
        
        //Espera mensaje del servidor para ver si existe ya el archivo
        String msgDeServer=din.readUTF();
        
        //Verificamos que el mensaje recibido es el de archivo existente
        if(msgDeServer.compareTo("Archivo existente")==0){
            String Opcion;
            System.out.println("El archivo ya existe. �Desea sobreescribirlo (S/N) ?");
            Opcion=br.readLine();            
            if(Opcion=="S"){
            	
            	//si la opcion es de sobreeesribir, continuamos la ejecuci�n
                dout.writeUTF("S");
            }else{
            	
            	//Sino, la terminamos
                dout.writeUTF("N");
                return;
            }
        }
        //Compienza el env�o del archivo
        System.out.println("Enviando archivo ...");
        
        //Se crea el stream que leer� el archivo del ordenador a la aplicaci�n
        FileInputStream fin=new FileInputStream(f);
        
        //Se escribe hasta final de archivo
        int ch;
        do
        {
        	//Se lee el fichero de 4 en 4 bytes
        	ch=fin.read();
        	
        	//Se env�a al servidor
            dout.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        
        //Se cierra el stream del archivo
        fin.close();
        
        //Se recibe la �ltima confirmaci�n por parte del servidor
        System.out.println(din.readUTF());
        
    }
    
    void RecibirData() throws Exception
    {
        String archivo;
        System.out.print("Escriba el nombre del fichero :");
        archivo=br.readLine();
        //Lee el nombre del fichero y la env�a al servidor
        dout.writeUTF(archivo);
        
        //Recibe el mensaje del servidor con el estado del env�o
        String msgDeServer=din.readUTF();
        
        //Si no est� encontrado, termina la ejecuci�n
        if(msgDeServer.compareTo("Archivo no encontrado")==0){
            System.out.println("Error 404: Archivo no encontrado en el servidor ...");
            return;
        }else{
        	//Espera a la confirmaci�n del servidor de que el archivo est� listo
        	if(msgDeServer.compareTo("Preparado")==0){
	            System.out.println("Recibiendo Archivo ...");
	            File f=new File(archivo);
	            
	            //Si el archivo ya existe
	            if(f.exists())
	            {
	                String opcion;
	                System.out.println("El archivo ya existe. �Desea sobreescribirlo (S/N) ?");
	                opcion=br.readLine(); 
	                
	                //Si no desea sobreescribirlo, se borra el flujo de datos y se acaba la ejecuci�n
	                if(opcion=="N")    
	                {
	                    dout.flush();
	                    return;    
	                }                
	            }
	            //Se crea un File Output para escribir el archivo recibido en el ordenador
	            FileOutputStream fout=new FileOutputStream(f);
	            int ch;
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
	            
	            //Cierra el stream que conecta con el ordenador
	            fout.close();
	            System.out.println(din.readUTF());
	                
	        }
        }
        
    }

    public void displayMenu() throws Exception
    {
    	//Men� simple con las opciones b�sicas de recibir y enviar archivo
    	
    	/*Primero debe de hacer el login aqu�, y si IF es certero, devuelve a este men�
    	 * Rudimentario que se modificar� posteriormente
    	 * */
    	boolean login=true;
    	
    	//Las entradas de datos
    	System.out.println("[ Login ]");
    	dout.writeUTF("LOGIN");
        System.out.println("Inserte el usuario:");
        String user=br.readLine();
        System.out.println("Inserte la contrase�a");
        String pass=br.readLine();
        
        //Env�o de la informaci�n al servidor
        dout.writeUTF(user);
        dout.writeUTF(pass);
        
    	//El if de comprobaci�n que lleva a los diferentes men�s
    	if(login){
	        while(true)
	        {    
	            System.out.println("[ MEN� ]");
	            System.out.println("1. Enviar archivo");
	            System.out.println("2. Recibir archivo");
	            System.out.println("3. Salir");
	            System.out.print("\n Elija una opci�n :");
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
    		//Aqu� muestra el mensaje de login fallido, vu�lvalo a intentar
    		System.exit(1);
    	}
    }
}