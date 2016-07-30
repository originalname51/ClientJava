package network_two;

public class MainClass {
	public static void main(String[] args) {
	
		System.out.println("Trying to connect to server");
		Client serverConnection = new Client(args[0], Integer.parseInt(args[1]), args);
		
		serverConnection.sendCommand();
		serverConnection.getMessage();
		serverConnection.shutdown();
		
		System.out.println("Succesful Connect");
		}
}
