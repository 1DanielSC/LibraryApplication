package application;

import java.io.IOException;

import network.Message;
import network.NetworkAccess;
import network.UDPHandler;


public class BuyServer implements Server{
	
	
	public NetworkAccess socket;
	
	
	public void registerLoadBalancer(){

	}
	
	public BuyServer(String serverPort, String connectionType) {

		try{
			this.connect(serverPort, connectionType);
			System.out.println("BuyServer succesfully started on port " + this.socket.getPort() + ".");

			while(true) {
					
				Message packetReceived = this.socket.receive();

				System.out.println(packetReceived.toString());
						
				Message replyMessage = new Message("action", "", "accessToken", 0, "name", 
				"author", 0.00, packetReceived.getPort(), packetReceived.getAddress());
				this.socket.send(replyMessage);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public void connect(String serverPort, String connectionType) throws IOException{
		System.out.println("Starting BuyServer on port " + serverPort + "...");
		try{

			switch (connectionType.toLowerCase()) {
				case "udp":
					this.socket = new UDPHandler(Integer.parseInt(serverPort));
					this.registerLoadBalancer();
					break;
				case "tcp" : break;
				case "http": break;
				default:
					System.out.println("Unknown connection type. Aborting server...");
					System.exit(0);
					break;
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new BuyServer(args[0], args[1]);
	}

}
