package network;

import java.io.IOException;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UDPHandler implements NetworkAccess {
	
	public DatagramSocket socket;
	
	public UDPHandler(Integer port) throws IOException {
		this.socket = new DatagramSocket(port);
	}
	
	public Message receive() throws IOException{
		byte[] packet = new byte[1024];
		DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);

		this.socket.receive(receivedPacket);

		Message messageReceived = this.getMessage(receivedPacket.getData());

		return messageReceived;
	}


	private Message getMessage(byte[] binaryMessage){
		String message = new String(binaryMessage);

		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		return gson.fromJson(message, Message.class);
	}

	public void send(Message message) throws IOException {
		//message to bytes
		byte[] packetBytes = this.serialize(message);
		
		DatagramPacket packet = new DatagramPacket(packetBytes,packetBytes.length,message.getAddress(),message.getPort());
		this.socket.send(packet);
	}
	
	private byte[] serialize(Message message){
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		byte[] serializedMessage = new byte[1024];
		String stringMessage = gson.toJson(message);
		serializedMessage = stringMessage.getBytes();

		return serializedMessage;
	}


	public void register() throws IOException{
		
	}

	public int getPort(){
		return this.socket.getPort();
	}

	public InetAddress getInetAddress(){
		return this.socket.getInetAddress();
	}
}
