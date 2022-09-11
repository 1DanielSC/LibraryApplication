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
	public int serverPort;
	
	public TCPHandler(Integer port) throws IOException{
		this.socket = new ServerSocket(port);
		this.serverPort = port;
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
	
	public void send(Message message) throws IOException{ 
		Socket connection = null;
		try {
			connection = new Socket ("localhost", message.getPort());
			message.setPort(this.serverPort);		

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
	 

	public void send(Message message, Integer port) throws IOException{ 
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
			connection = new Socket ("localhost", databaseMessage.getPort()); //before: databaseMessage.getPort()
			
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			databaseMessage.setPort(this.serverPort);
			

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
			System.out.println("Handler receive:  this.socket.getLocalPort: " + this.socket.getLocalPort());
			System.out.println("Handler receive: getLocalPort: " + nextClient.getLocalPort());
			System.out.println("Handler receive: getPort: " + nextClient.getPort());
			
			
			BufferedReader input = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
			Message msg = this.deserializeMessage(input.readLine().getBytes());


			System.out.println("Handler receive(): pacote recebido: " + msg.toString());	//BuyServer printou 9050

			if(msg.getAction().equals("/sell") || msg.getAction().equals("/buy") || msg.getAction().equals("/login"))
				msg.setPort(nextClient.getPort());// problema aqui, o pacote recebido pelo BuyServer esta com a porta do socket TCP
				

			System.out.println("Handler receive(): apos setPort(.getPort()): " + msg.toString()); //BuyServer printou 54588
			
			
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
