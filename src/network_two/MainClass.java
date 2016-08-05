package network_two;

public class MainClass {
	public static void main(String[] args) {
	
		if(ValidateParameters(args))
		{
		Client serverConnection = new Client(args[0], Integer.parseInt(args[1]), args);
		serverConnection.sendCommand();
		serverConnection.getMessage();
		serverConnection.shutdown();
		System.out.println("****End Transmission****");
		}
		else
			System.out.println("Parameters Incorrect. -l or -g are commands. Valid port number is required.\n"
					+ "Output is servername portnumber [arg] [file or return port] [returnport if -g selected");
		}

public static boolean ValidateParameters(String [] arguments)
{

//  validate to see if port numbers are correct. Server and file not validated. 
	String line = "(\\d{1,5})";
	if(arguments[2].equals("-l") && arguments.length < 5)
		if(arguments[1].matches(line) && arguments[3].matches(line))
		{
			int port1 = Integer.parseInt(arguments[1]);
			int port2 = Integer.parseInt(arguments[3]);
			return port1 < 65536 && port2 > 1024 && port2 < 65536 && port2 > 1024 ? true : false;
		}

	if(arguments[2].equals("-g") && arguments.length < 6)
		if(arguments[1].matches(line) && arguments[4].matches(line))
		{
			int port1 = Integer.parseInt(arguments[1]);
			int port2 = Integer.parseInt(arguments[4]);
			return port1 < 65536 && port2 > 1024 && port2 < 65536 && port2 > 1024 ? true : false;
		}
return false;
}
}