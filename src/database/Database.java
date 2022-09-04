package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.Server;
import model.Book;

import network.Message;
import network.NetworkAccess;
import network.UDPHandler;

public class Database implements Server{

	public List<Book> database;

	public NetworkAccess socket;
	
	
	public Database(String serverPort, String connectionType) {
		System.out.println("Starting Database server on port "+ "serverPort" +"..." );
		this.database = new ArrayList<Book>();

		try {
			this.connect(serverPort, connectionType);
			System.out.println("Database server succesfully started at port " + serverPort + ".");
			
			Message packetReceived = this.socket.receive();


			this.socket.send(packetReceived);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}


	public void connect(String serverPort, String connectionType){
		try {
			switch (connectionType.toLowerCase()) {
				case "udp":
					this.socket = new UDPHandler(Integer.parseInt(serverPort));
					this.registerLoadBalancer();
					break;

				case "tcp": break;
				case "http": break;

				default:
					System.out.println("Unknown connection type. Aborting server...");
					System.exit(0);
					break;
			}
		} catch (IOException e) {
			
		}
	}

	public void registerLoadBalancer(){

	}
	
	
	public void operate(Message message) {
		switch(message.getAction().toUpperCase()) {
			case "CREATE":
				Book newBook = new Book(message.getName(), message.getAuthor(), message.getPrice());
				this.save(newBook);
				break;
				
			case "DELETE":
				this.delete(message.getId());
				break;
				
				
			case "UPDATE":
				this.update(message.getId(), new Book(message.getName(), message.getAuthor(), message.getPrice()));
				break;	
		}
	}
	
	
	public void save(Book book) {
		this.database.add(book);
	}

	public void update(Integer id, Book book) {
		this.delete(id);
		this.save(book);
	}
	
	public void delete(Integer id) {
		this.database.remove(id);
	}

	
	public static void main(String[] args) {
		new Database(args[0], args[1]);

	}

}
