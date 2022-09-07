package network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;

import java.net.*;

public class TCPHandler implements NetworkAccess{
	
	public final ServerSocket socket;
	
	
	public TCPHandler(Integer port) throws IOException{
		this.socket = new ServerSocket(port);
		
	}
	
	
	public void send(Message message) throws IOException{ //send back to JMeter
		Socket connection = null;
		try {
			connection = new Socket ("localhost", message.getPort());//message.getPort() = JMeter port
			ObjectOutputStream output = new
			ObjectOutputStream(connection.getOutputStream());
				
			output.writeObject(message);
			output.flush();
			
			try {
				connection.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	

	public void send(Message message, Integer port) throws IOException{ //send back to JMeter
		Socket connection = null;
		try {
			connection = new Socket ("localhost", port);//message.getPort() = JMeter port
			ObjectOutputStream output = new
			ObjectOutputStream(connection.getOutputStream());
				
			output.writeObject(message);
			output.flush();
			
			try {
				connection.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public void send(DatabaseMessage databaseMessage) throws IOException{ //send to database
		Socket connection = null;
		try {
			connection = new Socket ("localhost", databaseMessage.getPort());//message.getPort() = JMeter port
			ObjectOutputStream output = new
			ObjectOutputStream(connection.getOutputStream());
				
			output.writeObject(databaseMessage);
			output.flush();
			
			try {
				connection.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}


	public Message receive() throws IOException{ //receive from JMeter
		try {
			Socket nextClient = this.socket.accept();
			
			ObjectInputStream input =new ObjectInputStream(nextClient.getInputStream());
			
			Message msg = (Message) input.readObject();
			
			try {
				this.socket.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			return msg;
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public DatabaseMessage receiveDatabaseMessage() throws IOException{ //receive from database
		try {
			Socket nextClient = this.socket.accept();
			
			ObjectInputStream input =new ObjectInputStream(nextClient.getInputStream());
			
			DatabaseMessage msg = (DatabaseMessage) input.readObject();
			
			try {
				this.socket.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			return msg;
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
			
		}
		
		return null;
	}

	
	public void register() throws IOException{
		
	}

	public int getPort() throws IOException{
		return this.socket.getLocalPort();
	}
	public InetAddress getInetAddress() {
		return this.socket.getInetAddress();
	}
}
