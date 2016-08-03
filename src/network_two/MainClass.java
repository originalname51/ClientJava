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
			System.out.println("Parameters Incorrect");
		}

public static boolean ValidateParameters(String [] arguments)
{
	if (arguments[2].equals("-l") || arguments[2].equals("-g") && arguments.length  < 6)
		return true;
	else
		return false;

}
}