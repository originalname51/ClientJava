package network_two;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	
private	Socket connectionSocket;
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
	long fileSize = _messageSize();
	
	
}

private long _messageSize()
{
	long size = 0;
	String howLongMessageIs;
	
    try {
    	howLongMessageIs = in.readLine();
    	  size = Long.parseLong(howLongMessageIs.trim());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    return size;
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


}
