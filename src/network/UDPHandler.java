package network;

import java.io.IOException;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UDPHandler implements NetworkAccess {
	
	public DatagramSocket socket;
	
	public UDPHandler(Integer port) throws IOException {
		System.out.println("Port: " + port);
		this.socket = new DatagramSocket(port);
	}
	
	public Message receive() throws IOException{
		byte[] packet = new byte[1024];
		DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);
		
		this.socket.receive(receivedPacket);

		Message messageReceived = this.deserializeMessage(receivedPacket.getData());
		
		//fixed !!! Now refactor it!
		messageReceived.setPort(receivedPacket.getPort());
		messageReceived.setAddress(receivedPacket.getAddress());
		
		System.out.println(messageReceived);

		return messageReceived;
	}


	public DatabaseMessage receiveDatabaseMessage() throws IOException{
		byte[] packet = new byte[1024];
		DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);
		
		this.socket.receive(receivedPacket);

		DatabaseMessage messageReceived = this.deserializeDatabaseMessage(receivedPacket.getData());
		messageReceived.setPort(receivedPacket.getPort());
		messageReceived.setAddress(receivedPacket.getAddress());

		return messageReceived;
	}

	private DatabaseMessage deserializeDatabaseMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), DatabaseMessage.class);
	}


	private Message deserializeMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		Gson gson = new GsonBuilder()
        .setLenient().serializeNulls()
        .create();

		return gson.fromJson(message.trim(), Message.class);
	}

	public void send(Message message) throws IOException {

		byte[] packetBytes = this.serializeMessage(message);
		
		DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, message.getAddress(), message.getPort());
		
		this.socket.send(packet);
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

	public void send(DatabaseMessage databaseMessage) throws IOException{
		byte[] packetBytes = this.serializeMessage(databaseMessage);
		
		DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length, databaseMessage.getAddress(), databaseMessage.getPort());
		System.out.println("Adddrs: " + packet.getAddress());
		this.socket.send(packet);
	}


	public void register() throws IOException{
		
	}

	public int getPort(){
		return this.socket.getLocalPort();
	}

	public InetAddress getInetAddress(){
		return this.socket.getLocalAddress();
	}
}
