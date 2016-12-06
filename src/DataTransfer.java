import java.net.*;
import java.io.*;
import java.util.*;

public class DataTransfer extends Thread {

	Socket ClientSoc;

	DataInputStream din;
	DataOutputStream dout;

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
	void EnviarFile() throws Exception
	{        
		String archivo=din.readUTF();
		File f=new File(archivo);
		if(!f.exists())
		{
			dout.writeUTF("Archivo no encontrado");
			return;
		}
		else
		{
			dout.writeUTF("Preparado");
			FileInputStream fin=new FileInputStream(f);
			int ch;
			do
			{
				ch=fin.read();
				dout.writeUTF(String.valueOf(ch));
			}
			while(ch!=-1);    
			fin.close();    
			dout.writeUTF("Archivo recibido correctamente");                            
		}
	}

	void RecibirFile() throws Exception
	{
		String archivo=din.readUTF();
		if(archivo.compareTo("Archivo no encontrado")==0)
		{
			return;
		}
		File a=new File(archivo);
		String option;

		if(a.exists())
		{
			dout.writeUTF("Archivo existente");
			option=din.readUTF();
		}
		else
		{
			dout.writeUTF("Enviar Archivo");
			option="S";
		}

		if(option.compareTo("S")==0)
		{
			FileOutputStream fout=new FileOutputStream(a);
			int ch;
			String temp;
			do
			{
				temp=din.readUTF();
				ch=Integer.parseInt(temp);
				if(ch!=-1)
				{
					fout.write(ch);                    
				}
			}while(ch!=-1);
			fout.close();
			dout.writeUTF("Archivo enviado correctamente");
		}
		else
		{
			return;
		}

	}


	public void run()
	{
		while(true)
		{
			try
			{
				System.out.println("Esperando comando ...");
				String comando=din.readUTF();
				if(comando.compareTo("RECOGER")==0)
				{
					System.out.println("\tRECOGER Comando recibido ...");
					EnviarFile();
					continue;
				}
				else if(comando.compareTo("ENVIAR")==0)
				{
					System.out.println("\tENVIAR Comando recibido ...");                
					RecibirFile();
					continue;
				}
				else if(comando.compareTo("DESCONECTAR")==0)
				{
					System.out.println("\tDESCONECTAR Comando recibido ...");
					System.exit(1);
				}
			}
			catch(Exception ex)
			{
			}
		}
	}

}
