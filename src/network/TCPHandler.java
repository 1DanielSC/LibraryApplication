package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.*;

public class TCPHandler implements NetworkAccess{
	
	public final ServerSocket socket;
	
	
	public TCPHandler(Integer port) throws IOException{
		this.socket = new ServerSocket(port);
		
	}
	

	private byte[] serializeMessage(Message message){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(message);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}
	
	public void send(Message message) throws IOException{ //send back to JMeter
		Socket connection = null;
		try {
			connection = new Socket ("localhost", message.getPort());//message.getPort() = JMeter port

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));


			byte[] msg = this.serializeMessage(message);

			out.write(new String(msg));
       		out.flush();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
			
		}finally{
			try {
				connection.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	 

	public void send(Message message, Integer port) throws IOException{ //send back to JMeter
		Socket connection = null;
		try {
			connection = new Socket ("localhost", port);//message.getPort() = JMeter port

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

			byte[] msg = this.serializeMessage(message);


			out.write(new String(msg));
       		out.flush();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}finally{
			try {
				connection.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	

	private byte[] serializeMessage(DatabaseMessage databaseMessage){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(databaseMessage);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}

	public void send(DatabaseMessage databaseMessage) throws IOException{ //send to database
		Socket connection = null;
		try {
			connection = new Socket ("localhost", databaseMessage.getPort());
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

			byte[] msg = this.serializeMessage(databaseMessage);

			out.write(new String(msg));
       		out.flush();
			
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally{
			try {
				connection.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}

	private Message deserializeMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), Message.class);
	}

	public Message receive() throws IOException{ //receive from JMeter
		try {
			Socket nextClient = this.socket.accept();
			System.out.println("TCP local port: " + nextClient.getLocalPort());
			System.out.println("TCP port: " + nextClient.getPort());
			

			
			BufferedReader input = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));

			Message msg = this.deserializeMessage(input.readLine().getBytes());
			msg.setPort(nextClient.getPort());
			
			return msg;
						
		}catch(IOException e) {
			e.printStackTrace();
			try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		
		
		return null;
	}

	private DatabaseMessage deserializeDatabaseMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), DatabaseMessage.class);
	}
	
	public DatabaseMessage receiveDatabaseMessage() throws IOException{ //receive from database
		try {
			Socket nextClient = this.socket.accept();
			
			BufferedReader input = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
			
			DatabaseMessage msg = this.deserializeDatabaseMessage(input.readLine().getBytes());
			msg.setPort(nextClient.getPort());
			System.out.println("TCP receiveDB: " + nextClient.getPort());
			
			return msg;

		}catch(IOException e) {
			e.printStackTrace();
			try {
				this.socket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
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
