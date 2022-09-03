package application;

import java.io.IOException;
import java.net.*;

import network.HTTPHandler;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;


public class BuyServer {
	
	
	public NetworkAccess socket;
	
	
	public void save() { //Save on database
		
	}
	
	public BuyServer(NetworkAccess serverSocket) throws IOException {
		this.setServerSocket(serverSocket);
		System.out.println("BuyServer succesfully started on port " + this.socket.getPort() + ".");

		while(true) {
				
			Message packetReceived = this.socket.receive();
				
			//String message = new String(packetReceived.getData());
			
			this.socket.send(message);
		}
	}
	
	public void setServerSocket(NetworkAccess serverSocket){
		this.socket = serverSocket;
	}
	
	public static void main(String[] args) {

		try{
			System.out.println("Starting SellServer on port " + args[0] + "...");
			if(args[1].toLowerCase() == "udp")
				new BuyServer(new UDPHandler(Integer.parseInt(args[0])));
			else if(args[1].toLowerCase() == "tcp")
				new BuyServer(new TCPHandler(Integer.parseInt(args[0])));
			else
				new BuyServer(new HTTPHandler(Integer.parseInt(args[0])));
		}
		catch(IOException e){
			e.printStackTrace();
		}

		
	}

}
