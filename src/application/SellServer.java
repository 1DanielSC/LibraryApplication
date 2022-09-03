package application;

import java.io.IOException;
import java.net.*;

import network.Message;
import network.NetworkAccess;
import network.UDPHandler;


public class SellServer implements Server {

	public NetworkAccess socket;

	public SellServer(NetworkAccess serverSocket) throws IOException {

		this.setNetworkAccess(serverSocket);
		System.out.println("SellServer succesfully started on port " + this.socket.getPort() + "...");

		while(true){
			//receive packet
			Message packetMessage = this.socket.receive();
			
			
			//process information


			//reply packet
			Message replyMessage = new Message("action", "accessToken", 0, "name", 
							"author", 0.00, packetMessage.getPort(), packetMessage.getAddress());

			this.socket.send(replyMessage);
		}
	}

	public void setNetworkAccess(NetworkAccess serverSocket){
		this.socket = serverSocket;
	}
	
	public void registerLoadBalance(){

	}
	
	public static void main(String[] args) {
		try{
			System.out.println("Starting SellServer on port " + args[0] + "...");
			NetworkAccess socket = new UDPHandler(Integer.parseInt(args[0]));
			new SellServer(socket);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}

}
