import java.net.*;
import java.io.*;
import java.util.*;


class ClienteFTP
{
    public static void main(String args[]) throws Exception
    {
        Socket soc=new Socket("DESKTOP-QPH0IB2",5218);
        ClienteTransferData t=new ClienteTransferData(soc);
        t.displayMenu();
        
    }
}