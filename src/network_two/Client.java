package network_two;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
//http://www.rgagnon.com/javadetails/java-0542.html	
private	Socket connectionSocket;
private Socket 	DataServerSocket;
private	ServerSocket dataServer;
private InputStream is;
private FileOutputStream file;
private BufferedOutputStream bos;

private	PrintWriter out;
private	BufferedReader in;
private BufferedReader consoleMessage;
String [] args;
private final int  	   MESSAGE_LENGTH = 500;
private final int	   CHAR_OFFSET    = 100;

public	Client(String hostName, int portnumber, String [] args){
	try {
		this.connectionSocket = new Socket(hostName, portnumber);
		this.out			  = new PrintWriter(this.connectionSocket.getOutputStream(),true);
		this.in				  = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
		this.consoleMessage   = new BufferedReader(new InputStreamReader(System.in));
		this.args 			  = args;

	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
}

//http://www.rgagnon.com/javadetails/java-0542.html
private void _make_client_socket()
{
	try {
		if(args[2].equals("-l")){
		this.dataServer = new ServerSocket(Integer.parseInt(args[3]),50,InetAddress.getLocalHost());
		}
		else if(args[2].equals("-g"))
		{
		this.dataServer = new ServerSocket(Integer.parseInt(args[4]),50,InetAddress.getLocalHost());
		}
	} catch (NumberFormatException e) {
		System.out.println("Server has invalid port number.\n");
		e.printStackTrace();
	} catch (UnknownHostException e) {
		System.out.println("Server has invalid HostName.\n");
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	try {
		this.DataServerSocket = this.dataServer.accept();
		this.is 			  = this.DataServerSocket.getInputStream();
	} catch (IOException e) {
		System.out.println("Failure to accept or make Server Socket Input Stream");
		e.printStackTrace();
	}
}


public void shutdown()
{
	try {
		this.connectionSocket.close();
		this.out.close();
		this.in.close();
		this.consoleMessage.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

public void getMessage()
{
	String outputMessage;

	try {
		outputMessage = in.readLine();
		if(outputMessage.equals("ERRO"))
		{
			System.out.println("Error Parsing Arguments");
			return;
		}
		_make_client_socket();
		
		if(args[2].equals("-l"))
		{
			_lprotocol();
		}
		else if(args[2] == "-g")
		{
			int messageLength = _messageSize();
			if(messageLength == 0)
			{
				System.out.println("Error. File does not exist or is not availible.");
			}
		}		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

private int _messageSize()
{
	int size = 0;
	String howLongMessageIs;
	
    try {
    	howLongMessageIs = in.readLine();
    	  size = Integer.parseInt(howLongMessageIs.trim());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return size;
}

public boolean _validateParameters(String [] arguments)
{
	
return true;
}
//http://stackoverflow.com/questions/4644415/java-how-to-get-input-from-system-console
//http://stackoverflow.com/questions/2340106/what-is-the-purpose-of-flush-in-java-streams
public void sendCommand()
{
	String sendme = "";
	
	for(int i = 0; i < args.length; i++)
	{
		sendme = sendme + args[i] + " ";
	}
	out.println(Integer.toString(sendme.length() + CHAR_OFFSET+1));
	out.flush();
	out.println(sendme);
	return;
}


public void _lprotocol()
{
	try {
		BufferedReader fileList	 = new BufferedReader(new InputStreamReader(this.DataServerSocket.getInputStream()));
	
		int nameAmount = 0;
		nameAmount = _messageSize();		
		String period = ".";
		String twoP   = "..";
		String File;
		System.out.println("File List Is Following:");
		for(int i =0; i < nameAmount; i++)
	{
		char[] arr = new char[256];
		fileList.read(arr, 0, 256);
		File = String.valueOf(arr);
		File = File.trim();
		if(!(File.equals(period)) && !(File.equals(twoP)))
		{
			System.out.println(File);
		}			
	}
		fileList.close();	
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	

	

}


}
