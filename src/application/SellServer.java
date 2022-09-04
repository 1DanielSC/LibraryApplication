package application;

import java.io.IOException;

import network.Message;
import network.NetworkAccess;
import network.UDPHandler;


public class SellServer implements Server {

	public NetworkAccess socket;

	public SellServer(String serverPort, String connectionType) {

		System.out.println("Starting SellServer on port " + serverPort + "...");
		try {
			this.connect(serverPort, connectionType);

			System.out.println("SellServer succesfully started on port " + this.socket.getPort() + "...");

			while(true){
				//receive packet
				Message packetMessage = this.socket.receive();
				
				
				//process information
				this.processInformation(packetMessage);

				//reply packet
				Message replyMessage = new Message("action", "accessToken", 0, "name", 
								"author", 0.00, packetMessage.getPort(), packetMessage.getAddress());

				this.socket.send(replyMessage);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


		
	}

	public void connect(String serverPort, String connectionType) {
		System.out.println("Starting SellServer on port " + serverPort + "...");
		try{

			switch (connectionType.toLowerCase()) {

				case "udp":
					this.socket = new UDPHandler(Integer.parseInt(serverPort));
					this.registerLoadBalancer();
					break;

				case "tcp": break; //TODO
				case "http": break; //TODO

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

	public void processInformation(Message packetReceived){
		switch (packetReceived.getAction()) {
			case "CREATE":
				
				break;
		
			default:
				break;
		}
	}
	
	public void registerLoadBalancer(){

	}
	
	public static void main(String[] args) {
		new SellServer(args[0], args[1]);
	}

}
