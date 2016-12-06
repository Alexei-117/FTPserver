import java.net.*;
import java.io.*;
import java.util.*;


class ClienteFTP
{
    public static void main(String args[]) throws Exception
    {
        Socket soc=new Socket("127.0.0.1",5217);
        ClienteTransferData t=new ClienteTransferData(soc);
        t.displayMenu();
        
    }
}