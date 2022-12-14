package application;

import java.io.IOException;
import java.net.InetAddress;

import model.Book;
import network.DatabaseMessage;
import network.HTTPHandler;
import network.Heartbeat;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.TCPHeartbeat;
import network.UDPHandler;
import network.UDPHeartbeat;


public class BuyServer implements Server{
	
	
	public NetworkAccess socket;
	
	public Heartbeat hb;

	
	public void registerIntoLoadBalancer(String serverPort, String hbPort){
		try {
			Message messageToLoadBalancer = new Message();
			messageToLoadBalancer.setAction("create buy instance");

			messageToLoadBalancer.setAddress(InetAddress.getLocalHost());
			messageToLoadBalancer.setId(this.socket.getPort());
			messageToLoadBalancer.setName(hbPort);

			System.out.println("Buy Server: minha porta: " + this.socket.getPort());
			System.out.println("Buy Server: Vou me registrar no Load Balancer");

			this.socket.send(messageToLoadBalancer, 9050);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public BuyServer(String serverPort, String connectionType, String hbPort) {

		try{
			this.connect(serverPort, connectionType, hbPort);
			System.out.println("BuyServer succesfully started on port " + this.socket.getPort() + ".");

			while(true) {
					
				Message packetReceived = this.socket.receive();

				System.out.println("BuyServer (receiving): " + packetReceived.toString());

				DatabaseMessage responseFromDatabase = this.sendToDatabase(packetReceived);
						
				Message replyMessage = new Message();
				replyMessage.setPort(packetReceived.getPort());
				replyMessage.setAddress(packetReceived.getAddress());
				
				System.out.println("Response from database: " + responseFromDatabase.getError());
				
				if(!responseFromDatabase.getError().toUpperCase().equals("OK"))
					replyMessage.setError("Error: it was not possible to save the book");
				else
					replyMessage.setError("OK");

				
				System.out.println("BuyServer: sending to LB: "+replyMessage.toString());

				replyMessage.setAction("send back to JMeter");
				this.socket.send(replyMessage);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
	}
	
	public void connect(String serverPort, String connectionType, String hbPort) throws IOException{
		System.out.println("Starting BuyServer on port " + serverPort + "...");
		System.out.println("Network connection: " + connectionType);
		try{

			switch (connectionType.toLowerCase()) {
				case "udp":
					this.socket = new UDPHandler(Integer.parseInt(serverPort));
					this.hb = new UDPHeartbeat(Integer.parseInt(hbPort));
					this.registerIntoLoadBalancer(serverPort, hbPort);
					break;

				case "tcp" : 
					this.socket = new TCPHandler(Integer.parseInt(serverPort));
					this.hb = new TCPHeartbeat(Integer.parseInt(hbPort));
					this.registerIntoLoadBalancer(serverPort, hbPort);
				break;
				
				case "http": 
					this.socket = new HTTPHandler(Integer.parseInt(serverPort));
					this.hb = new TCPHeartbeat(Integer.parseInt(hbPort));
					this.registerIntoLoadBalancer(serverPort, hbPort);
					break;
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
		new BuyServer(args[0], args[1], args[2]); //port - tcp/udp/http - heartbeat port
	}

}
