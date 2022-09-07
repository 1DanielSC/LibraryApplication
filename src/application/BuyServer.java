package application;

import java.io.IOException;

import model.Book;
import network.DatabaseMessage;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;


public class BuyServer implements Server{
	
	
	public NetworkAccess socket;
	
	
	public void registerLoadBalancer(String serverPort){

	}
	
	public BuyServer(String serverPort, String connectionType) {

		try{
			this.connect(serverPort, connectionType);
			System.out.println("BuyServer succesfully started on port " + this.socket.getPort() + ".");

			while(true) {
					
				Message packetReceived = this.socket.receive();

				DatabaseMessage responseFromDatabase = this.sendToDatabase(packetReceived);
						
				Message replyMessage = new Message();
				replyMessage.setPort(packetReceived.getPort());
				replyMessage.setAddress(packetReceived.getAddress());
				
				System.out.println("Response from database: " + replyMessage.getError());
				
				if(!responseFromDatabase.getError().toUpperCase().equals("OK"))
					replyMessage.setError("Error: it was not possible to save the book");
				else
					replyMessage.setError("OK");
				
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
					this.registerLoadBalancer(serverPort);
					break;

				case "tcp" : 
					this.socket = new TCPHandler(Integer.parseInt(serverPort));
					this.registerLoadBalancer(serverPort);
				break;
				
				case "http": break;
				default:
					System.out.println("Unknown connection type. Aborting server...");
					System.exit(1);
					break;
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}
	}


	public DatabaseMessage sendToDatabase(Message message) throws IOException{
		
		Book bookToSend = new Book(message.getName(), message.getAuthor(), message.getPrice());

		DatabaseMessage databaseMessage = new DatabaseMessage(message.getAction(), bookToSend, 9000);
		databaseMessage.setAddress(message.getAddress());

		this.socket.send(databaseMessage); //send to database
		DatabaseMessage response = this.socket.receiveDatabaseMessage(); //receive response from database

		return response;
	}


	
	public static void main(String[] args) {
		new BuyServer(args[0], args[1]);
	}

}
