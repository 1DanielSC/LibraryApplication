package database;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import model.Book;

import network.Message;

public class Database {

	public List<Book> database;
	
	
	public void add(Book book) {
		this.database.add(book);
	}
	
	public void remove(Integer id) {
		this.database.remove(id);
	}
	
	public void update(Integer id, Book book) {
		this.remove(id);
		this.add(book);
	}
	
	
	public void operate(Message message) {
		switch(message.getAction()) {
			case "CREATE":
				Book newBook = new Book(message.getName(), message.getAuthor(), message.getPrice());
				this.add(newBook);
				break;
				
			case "DELETE":
				this.remove(message.getId());
				break;
				
				
			case "UPDATE":
				this.update(message.getId(), new Book(message.getName(), message.getAuthor(), message.getPrice()));
				break;	
		}
	}
	
	public Database(String port) {
		System.out.println("Starting Database server at port" + port + "...");
		this.database = new ArrayList<Book>();
		try {
			
			DatagramSocket serverSocket = new DatagramSocket(Integer.getInteger(port));
			System.out.println("Database server succesfully started at port " + port + ".");
			
			
			byte[] packet = new byte[1024];
			DatagramPacket receivedPacket = new DatagramPacket(packet, packet.length);
			serverSocket.receive(receivedPacket);
			
			String message = new String(receivedPacket.getData());
			System.out.println("Message received: " + message);
			
			//receber em string e converter para Gson
			//usar os metodos get do Gson e criar uma instancia Message
			
			//Message mes = new Message(receivedPacket.getData());    --- protocolo binario (byte para classe java)
			
			
			byte[] reply = new byte[1024];
			DatagramPacket replyPacket = new DatagramPacket(reply, reply.length, 
					receivedPacket.getAddress(), receivedPacket.getPort());
			serverSocket.send(replyPacket);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void save(String item) {
		
	}
	
	public void deleter(String item) {
		
	}
	
	public static void main(String[] args) {
		new Database(args[0]);

	}

}
