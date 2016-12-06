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
    //Función que recibe los datos
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
            System.out.println("El fichero no existe...");
            dout.writeUTF("Error 404: el fichero no existe");
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
        
        //Se escribe hasta final de archivo
        int ch;
        do
        {
        	//Se lee el fichero de 4 en 4 bytes
        	ch=fin.read();
        	//Se envía al servidor
            dout.writeUTF(String.valueOf(ch));
        }
        while(ch!=-1);
        //Se cierra el stream del archivo
        fin.close();
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
    	//Menú simple con las opciones básicas de recibir y enviar archivo
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
    }
}