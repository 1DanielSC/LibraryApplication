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
		BufferedWriter out = null;
		try {
			connection = new Socket ("localhost", message.getPort());	//FIX - LoadBalancer - JMeter port is closed
			System.out.println("Handler: sending to port " + message.getPort());
			message.setPort(this.serverPort);		
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));


			byte[] msg = this.serializeMessage(message);

			out.write(new String(msg));
       		out.flush();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
			
		}finally{
			try {
				if(out != null) out.close();
				System.out.println("Handler send: closing " + connection.getLocalSocketAddress());
				if(connection != null) connection.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	 

	public void send(Message message, Integer port) throws IOException{ 
		Socket connection = null;
		BufferedWriter out = null;
		try {
			connection = new Socket ("localhost", port);//message.getPort() = JMeter port

			 out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

			byte[] msg = this.serializeMessage(message);


			out.write(new String(msg));
       		out.flush();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(out != null) out.close();
				if(connection != null) connection.close();
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
		BufferedWriter out = null;
		try {
			connection = new Socket ("localhost", databaseMessage.getPort()); //before: databaseMessage.getPort()
			
			
			out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
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
				if(out != null) out.close();
				if(connection != null) connection.close();
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
			nextClient.setSoTimeout(10000);
			System.out.println("Handler receive:  this.socket.getLocalPort: " + this.socket.getLocalPort());
			System.out.println("Handler receive: getLocalPort: " + nextClient.getLocalPort());
			System.out.println("Handler receive: getPort: " + nextClient.getPort());
			System.out.println("Handler receive: getRemoteSocketAddress: " + nextClient.getRemoteSocketAddress().toString()); 
			
			InetSocketAddress add = (InetSocketAddress) nextClient.getRemoteSocketAddress();
			System.out.println("Handler receive: InetSocketAddress port: " + add.getPort());
			

			BufferedReader input = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
			Message msg = this.deserializeMessage(input.readLine().getBytes());


			System.out.println("Handler receive(): pacote recebido: " + msg.toString());	

			if(msg.getAction().equals("/sell") || msg.getAction().equals("/buy") || msg.getAction().equals("/login"))
				msg.setPort(nextClient.getPort());// obter porta do JMeter - FIX (nao esta sendo possivel)
				

			System.out.println("Handler receive(): apos setPort(.getPort()): " + msg.toString()); 
			
			
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
