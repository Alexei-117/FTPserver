import java.net.*;
import java.io.*;
import java.util.*;


class ClienteFTP
{
    public static void main(String args[]) throws Exception
    {
    	//Nombre del ordenador del usuario
    	java.net.InetAddress localmachine=java.net.InetAddress.getLocalHost();
    	
    	
    	//Conexi�n en s�
        Socket soc=new Socket(localmachine.getHostName(),5218);
        ClienteTransferData t=new ClienteTransferData(soc);
        t.displayMenu();
        
    }
}