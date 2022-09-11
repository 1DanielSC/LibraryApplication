package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.Server;
import model.Book;
import network.DatabaseMessage;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;

public class Database implements Server{

	public List<Book> database;

	public NetworkAccess socket;
	
	
	public Database(String serverPort, String connectionType) {
		System.out.println("Starting Database server on port "+ serverPort + "..." );
		this.database = new ArrayList<Book>();

		try {
			this.connect(serverPort, connectionType);
			
			System.out.println("Database server succesfully started on port " + this.socket.getPort() + ".");
			while(true){
				DatabaseMessage packetReceived = this.socket.receiveDatabaseMessage();

				System.out.println("Pacote recebido (BD): " + packetReceived.toString());
				System.out.println("BD: porta do pacote recebido: " + packetReceived.getPort());
				
				DatabaseMessage replyMessage = this.operate(packetReceived);
				replyMessage.setPort(packetReceived.getPort());
				replyMessage.setAddress(packetReceived.getAddress());

				this.socket.send(replyMessage);
			}


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
					this.registerIntoLoadBalancer(serverPort);
					break;

				case "tcp": 
					this.socket = new TCPHandler(Integer.parseInt(serverPort));
					break;
				case "http": break;

				default:
					System.out.println("Unknown connection type. Aborting server...");
					System.exit(1);
					break;
			}
		} catch (IOException e) {
			
		}
	}

	public void registerIntoLoadBalancer(String serverPort){

	}
	
	
	public DatabaseMessage operate(DatabaseMessage message) {
		DatabaseMessage response = new DatabaseMessage();
		switch(message.getAction().toUpperCase()) {
			case "CREATE":
				this.save(message.getBook());
				response.setError("OK");
				return response;
				
				
			case "DELETE":
				if(this.delete(message.getBook().getId()))
					response.setError("OK");
				else response.setError("Error: Book not found");
				return response;
				
				
			case "UPDATE":
				if(this.update(message.getBook().getId(), new Book(message.getBook().getName(), message.getBook().getAuthor(), message.getBook().getPrice())))
					response.setError("OK");
				else response.setError("Error: Book not found");
				return response;

			case "SELECTBYID":
				Book queryResult = this.selectById(message.getBook().getId());
				if(queryResult == null)
					response.setError("Error: No book found");
				else
					response.setBook(queryResult);response.setError("OK");
				return response;

			default:
				response.setError("FAIL");
				return response;
		}
	}
	
	
	public void save(Book book) {
		book.setId(this.database.size());
		this.database.add(book);
		System.out.println(this.database.toString());
	}

	public boolean update(Integer id, Book book) {
		if(this.delete(id)){
			this.save(book);
			return true;
		}
		return false;
	}
	
	public boolean delete(Integer id) {
		return this.database.remove(id);
	}

	public Book selectById(Integer id){
		return this.database.get(id);
	}
	
	public static void main(String[] args) {
		new Database(args[0], args[1]);

	}

}
