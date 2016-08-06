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
import java.util.Arrays;

public class Client {
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
	 * as well as set up input and output streams. NOTE: This does not set up or have anything
	 * to do with FTP data ports.
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
 * This will make a server socket for the FTP data connection using the arguments.
 * It will either use the 3rd or 4th argument depending on the listing (arg[2] of l means 3rd argument is port, 
 * arg[2] of g means 4th argument is port number.
 * 
 * Number format exception should never happen as it is tested for in main.
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
	 *  1) Receive message from the server to see if command is valid.
	 *  2) If command is valid make a ftp socket and have the server connect
	 *  3) if the argument was -l go to l protocol. Else, goto G protocol.
	 *  
	 *  Note: Connections are shut down in sub functions and in main.
	 * */
	public void getMessage() {

		//See if server has validated argument.
			int serverResponse = _messageSize();
			if (serverResponse != 0){
				System.out.println("Server had error Parsing Arguments. Please check arguments and try again.");
				return;
			}
			_make_client_socket(); //set up ftp connection
			if (args[2].equals("-l")) {
				_lprotocol();	//Execute L commands.
			} else if (args[2].equals("-g")) {
				_gprotocol();	//Execute G commands.
			}
	}

	/*
	 * Parse an integer sent by server. This is a utility function used throughout the program.
	 * Message will return any integer the server sends over.
	 * */
	private int _messageSize() {
		int size = 0;
		String howLongMessageIs;
		char [] buff = new char[17];
		Arrays.fill(buff, '\0');
		try {
			in.read(buff,0,17);
			howLongMessageIs = String.valueOf(buff);
			size = Integer.parseInt(howLongMessageIs.trim());
		} catch (IOException e) {
			System.out.println("Failure in _messageSize");
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
		out.println(Integer.toString(sendme.length() + CHAR_OFFSET));
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
 * 3) Make a file with the filename attempting to get from server
 * 4) Obtain initial message size from server. If value is -1 there has been an error. Handle error and return to main.
 * 5) While the message Length is not 0 read the file into the pre-existing byte array. Keep track of how many bytes are read.
 * 6) Write the byte array to the file.
 * 
 * This is primarily based on the following code: 
 * http://www.rgagnon.com/javadetails/java-0542.html
 * https://www.caveofprogramming.com/java/java-file-reading-and-writing-files-in-java.html
 * https://docs.oracle.com/javase/tutorial/essential/io/bytestreams.html
 * */
	public void _gprotocol() {
		int bytesRead;
		int current = 0;

		//Check to see if file exist. If it does, try and create a new file with a slightly different name.
		File f = new File(args[3]);
		if(f.exists())
		{
			boolean works = true;
			//Works for files up to .
			for(int i = 1; i < 1000; i++)
			{
				f = new File(args[3] + "(" + i + ")");
				if(!f.exists())
				{	
					args[3] = args[3] + "(" + i + ")";
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
		int fileSize = _messageSize();
		System.out.println(fileSize);
		//Make a byte array from the filesize. add a buffer of 30000 bytes. Exit if filesize if -1.(error from server)
		byte[] myByteArray = new byte[fileSize + 30000];	
		if(fileSize == -1)
		{
			System.out.println("Server does not have file. Exiting...");
			  try {
				DataServerSocket.close();
				  dataServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
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
		
		
//		int messageLength = _messageSize();
		
		/*
		 * Code adapted from tutorial java guide. This will use message length to control 
		 * the file transfer.
		 * */
//		while (messageLength != 0) {
		int howManyRead = 0;
			try {
				do {
					//Read the amount sent to a byte array
					bytesRead = this.is.read(myByteArray, 0, fileSize);
					howManyRead += bytesRead;
				} while (bytesRead > -1);
			} catch (FileNotFoundException e) {
				System.out.println("Error. File does not exist or is not availible.");
				e.printStackTrace();
				return;
			} catch (IOException e) {
				System.out.println("Error in IO getting file bytes");
				e.printStackTrace();
				return;
			}
//			messageLength = _messageSize();	//see if anymore data was sent.
//		}
		
		System.out.println("File Succesfully Recived. Writing data to file....");
		/*
		 * This code is from the tutorial code cited in the header. It writes the byte array to the file.
		 * */
		try {
			this.bos.write(myByteArray, 0, current);
			this.bos.flush();
			System.out.println("File " + args[3] + " downloaded (" + current + " bytes read). Transfer Complete.");
		} catch (IOException e) {
			System.out.println("Error writing file");
			e.printStackTrace();
			return;
		}
		try {
			this.bos.close();
			this.is.close();
			DataServerSocket.close();
			dataServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
