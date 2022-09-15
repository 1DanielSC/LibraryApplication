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

	public Socket jmeterSocket;

	public int serverPort;


	public TCPHandler(Integer port) throws IOException{
		this.socket = new ServerSocket(port);
		this.serverPort = port;
	}
	

	public void sendJMeter(Message message){
		BufferedWriter out = null;
		try {
			
			if(this.jmeterSocket.isClosed()){
				System.out.println("-----SOCKET FECHADO----");
			}

			message.setPort(this.serverPort);
			out = new BufferedWriter(new OutputStreamWriter(this.jmeterSocket.getOutputStream()));
			byte[] msg = this.serializeMessage(message);

			out.write(new String(msg));
       		out.flush();

			System.out.println("-----SENT TO JMETER--------");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
			
		}finally{
			try {
				if(out != null) out.close();
				if(this.jmeterSocket != null) this.jmeterSocket.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
	}
	

	public void send(Message message) throws IOException{ 

		Socket connection = null;
		BufferedWriter out = null;
		try {

			if(message.getAction().equals("send back to JMeter") && this.serverPort == 9050){
				this.sendJMeter(message);
				return;
			}


			connection = new Socket("localhost", message.getPort());	//FIX - LoadBalancer - JMeter port is closed
			//System.out.println("Handler: sending to port " + message.getPort());
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
			connection = new Socket ("localhost", port);

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

	



	public Message receive() throws IOException{
		try {
			
			Socket nextClient = this.socket.accept(); // nextClient eh o socket do JMeter
															//quando o metodo terminar, ele "deixara de existir"

			BufferedReader input = new BufferedReader(new InputStreamReader(nextClient.getInputStream()));
			Message msg = this.deserializeMessage(input.readLine().getBytes());

			//Paliative solution
			if(msg.getAction().equals("/buy") ||
				msg.getAction().equals("/sell") ||
				msg.getAction().equals("/login") ||
				msg.getAction().equals("/login/create user")){
				this.jmeterSocket = nextClient;
			}

			if(msg.getAction().equals("/sell") || msg.getAction().equals("/buy") 
			|| msg.getAction().equals("/login") || msg.getAction().equals("/login/create user"))
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




	private DatabaseMessage deserializeDatabaseMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), DatabaseMessage.class);
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


	private byte[] serializeMessage(DatabaseMessage databaseMessage){

		Gson gson = new GsonBuilder()
		        .setLenient().serializeNulls()
		        .create();
		
		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(databaseMessage);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}

	private Message deserializeMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), Message.class);
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
