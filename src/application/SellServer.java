package application;

import java.io.IOException;
import java.net.InetAddress;

import model.Book;
import network.DatabaseMessage;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;


public class SellServer implements Server {

	public NetworkAccess socket;

	public SellServer(String serverPort, String connectionType) {

		try {
			this.connect(serverPort, connectionType);

			System.out.println("SellServer succesfully started on port " + this.socket.getPort() + "...");

			while(true){
				//receive packet
				Message packetMessage = this.socket.receive();
				
				System.out.println("Vou enviar um pacote");
				//process the packet received
				DatabaseMessage responseFromDatabase = this.sendToDatabase(packetMessage);
				

				Message replyMessage = new Message();
				replyMessage.setPort(packetMessage.getPort());
				replyMessage.setAddress(packetMessage.getAddress());

				System.out.println("Response from database: " + responseFromDatabase.getError());

				if(!responseFromDatabase.getError().toUpperCase().equals("OK"))
					replyMessage.setError("Error: it was not possible to save the book");
				else
					replyMessage.setError("OK");

				
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
					this.registerIntoLoadBalancer(serverPort);
					break;

				case "tcp": 
					this.socket = new TCPHandler(Integer.parseInt(serverPort));
					this.registerIntoLoadBalancer(serverPort);
					break; //TODO
					
				case "http": break; //TODO

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
		System.out.println(message.getName() + message.getAuthor() + message.getPrice());
		System.out.println("hi");
		Book bookToSend = new Book(message.getName(), message.getAuthor(), message.getPrice());

		DatabaseMessage databaseMessage = new DatabaseMessage("CREATE", bookToSend, 9000);
		databaseMessage.setAddress(message.getAddress());

		this.socket.send(databaseMessage); //send to database
		DatabaseMessage response = this.socket.receiveDatabaseMessage(); //receive response from database

		return response;
	}
	
	public void registerIntoLoadBalancer(String serverPort){
		try {
			
			Message messageToLoadBalancer = new Message();
			messageToLoadBalancer.setAction("create sell instance");
			messageToLoadBalancer.setAddress(InetAddress. getLocalHost());
			messageToLoadBalancer.setId(this.socket.getPort());

			System.out.println("Sell Server: Vou me registrar no Load Balancer");
			this.socket.send(messageToLoadBalancer, 9050);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new SellServer(args[0], args[1]);
	}

}
