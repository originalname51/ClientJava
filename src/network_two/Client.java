package network_two;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
	// http://www.rgagnon.com/javadetails/java-0542.html
	private Socket connectionSocket;
	private Socket DataServerSocket;
	private ServerSocket dataServer;
	private InputStream is;
	private FileOutputStream file;
	private BufferedOutputStream bos;
	private PrintWriter out;
	private BufferedReader in;
	String[] args;
	private final int CHAR_OFFSET = 100;

	
	/*
	 * Client constructor. Will make a socket using the hostname and portnumber
	 * as well as set up input and output streams.
	 * 
	 * */
	public Client(String hostName, int portnumber, String[] args) {
		try {
			this.connectionSocket = new Socket(hostName, portnumber);
			this.out = new PrintWriter(this.connectionSocket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
			this.args = args;

		} catch (IOException e) {
			try {
				this.connectionSocket.close();
				this.out.close();
				this.in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

/*
 * http://www.rgagnon.com/javadetails/java-0542.html
 * This will make a server socket for the FTP data connection using the arguments.
 * It will either use the 3rd or 4th argument.
 * 
 * 
 * 
 * */
	private void _make_client_socket() {
		
		//Make the socket, catch errors.
		try {
			if (args[2].equals("-l")) {
				this.dataServer = new ServerSocket(Integer.parseInt(args[3]), 2, InetAddress.getLocalHost());
			} else if (args[2].equals("-g")) {
				this.dataServer = new ServerSocket(Integer.parseInt(args[4]), 2, InetAddress.getLocalHost());
			}
		} catch (NumberFormatException e) {
			System.out.println("Server has invalid port number.\n");
			shutdown();
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Server has invalid HostName.\n");
			shutdown();
			e.printStackTrace();
		} catch (IOException e) {
			shutdown();
			e.printStackTrace();
		}

		//Make the data socket, get input stream. Output stream not needed. Catch errors.
		try {
			this.DataServerSocket = this.dataServer.accept();
			this.is = this.DataServerSocket.getInputStream();
		} catch (IOException e) {
			System.out.println("Failure to accept or make Server Socket Input Stream");
			try {
				this.dataServer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			shutdown();
			e.printStackTrace();
		}
	}

	//Helper function to shutdown connections set up in constructor.
	public void shutdown() {
		try {
			this.connectionSocket.close();
			this.out.close();
			this.in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 *  1) Recieve message from the server to see if command is valid.
	 *  2) If command is valid make a ftp socket and have the server connect
	 *  3) if the argument was -l go to l protocol. Else, goto G protocol.
	 *  
	 *  Note: Connections are shut down in sub functions and in main.
	 * */
	public void getMessage() {
		String outputMessage;

		//See if server has validated argument.
		try {
			outputMessage = in.readLine();
			if (!outputMessage.equals("PASS")) {
				System.out.println("Error Parsing Arguments");
				return;
			}
			
			_make_client_socket(); //set up ftp connection

			if (args[2].equals("-l")) {
				_lprotocol();	//Execute L commands.
			} else if (args[2].equals("-g")) {
				_gprotocol();	//Execute G commands.
			}
		} catch (IOException e) {
			shutdown();
			e.printStackTrace();
		}
	}

	/*
	 * Parse an integer sent by server. This is a utility function used throughout the program.
	 * Message will return any integer the server sends over.
	 * */
	private int _messageSize() {
		int size = 0;
		String howLongMessageIs;
		char [] buff = new char[256];
		try {
			in.read(buff,0,256);
			howLongMessageIs = String.valueOf(buff);
			size = Integer.parseInt(howLongMessageIs.trim());
		} catch (IOException e) {
			shutdown();
			e.printStackTrace();
		}
		return size;
	}

	/*
	 * L protocol will list files.
	 * 1) Make input stream as a buffered reader.
	 * 2) Read how many files there are
	 * 3) Read into the buffer the file name.
	 * 4) Print the file if it isn't "." or ".."
	 * */
	// http://stackoverflow.com/questions/4644415/java-how-to-get-input-from-system-console
	// http://stackoverflow.com/questions/2340106/what-is-the-purpose-of-flush-in-java-streams
	public void sendCommand() {
		String sendme = "";

		for (int i = 0; i < args.length; i++) {
			sendme = sendme + args[i] + " ";
		}
		out.println(Integer.toString(sendme.length() + CHAR_OFFSET + 1));
		out.flush();
		out.println(sendme);
		return;
	}

	public void _lprotocol() {
		try {
			//Set the inputstream to a buffered reader.
			BufferedReader fileList = new BufferedReader(new InputStreamReader(this.DataServerSocket.getInputStream()));

			int nameAmount = 0;
			nameAmount = _messageSize(); //get how many files there are.
			String period = ".";
			String twoP = "..";
			String File;
			System.out.println("File List Is Following:");
			for (int i = 0; i < nameAmount; i++) {
				char[] arr = new char[256];
				fileList.read(arr, 0, 256);
				File = String.valueOf(arr);	//set file string to the input buffer.
				File = File.trim();	//trim out white space.
				if (!(File.equals(period)) && !(File.equals(twoP))) {
					System.out.println(File);	//print file if not . or ..
				}
			}
			fileList.close();	// close the buffered reader (which will close the input stream)
			this.dataServer.close(); //close the ftp server.
		} catch (IOException e) {
			try {
				//if it doesnt work close it all.
				this.dataServer.close();
				this.DataServerSocket.close();
			} catch (IOException e1) {
				return;
			}
			e.printStackTrace();
		}

	}
/*
 * G protocol will get a file or return an error.
 * 
 * 1) Set a byte array to ~500,000 bytes.
 * 2) Check to see if file already exist, if it does add an integer value to the end up to 100 or throw an error.
 * 3) 
 * */
	public void _gprotocol() {
		int bytesRead;
		int current = 0;
		byte[] myByteArray = new byte[50000000];


		File f = new File(args[3]);
		if(f.exists())
		{
			boolean works = true;
			for(int i = 0; i < 100; i++)
			{
				f = new File(args[3] + i);
				if(!f.exists())
				{	
					args[3] = args[3] + i;
					works = true;
					break;
				}
			}
			if(!works)
			{
				System.out.println("File cannot be made. Exiting.");
				  try {
					DataServerSocket.close();
					  dataServer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				return;
			}
		}
		
		try {
			this.file = new FileOutputStream(args[3]);
			this.bos = new BufferedOutputStream(this.file);
		} catch (FileNotFoundException e1) {
			  try {
				dataServer.close();
				DataServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			e1.printStackTrace();
		}
		
		
		int messageLength = _messageSize();
		if (messageLength == -1) {
			System.out.println("Error. File does not exist or is not availible.");
			return;
		}
		while (messageLength != 0) {
			try {
				do {
					bytesRead = this.is.read(myByteArray, current, messageLength);
					if (bytesRead >= 0)
						current += bytesRead;
				} while (bytesRead > -1);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			messageLength = _messageSize();
		}

		try {
			this.bos.write(myByteArray, 0, current);
			this.bos.flush();
			System.out.println("File " + args[3] + " downloaded (" + current + " bytes read). Transfer Complete.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
