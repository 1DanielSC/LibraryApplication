package network;

import java.io.IOException;
import java.net.*;

public class UDPHandler implements NetworkAccess {
	
	public DatagramSocket socket;
	
	public UDPHandler(Integer port) throws IOException {
		this.socket = new DatagramSocket(port);
	}
	
	public Message receive() throws IOException{
		byte[] packet = new byte[1024];
		DatagramPacket receivedPacket = new DatagramPacket(packet,packet.length);

		this.socket.receive(receivedPacket);

		String message = new String(receivedPacket.getData());
		//receivedPacket.getData() retorna um vetor de bytes
		//A classe String possui um construtor que transforma um vetor de bytes em string

		//Precisamos agora pegar essa string e transformá-la num objeto JSON


		System.out.println(message);
		
		

		Message packetMessage = new Message("action", "accessToken", 0, "name", "author", 0.00, 0, message.get);

		return packetMessage;
	}

	public void send(Message message) throws IOException {
		
		byte[] packetBytes = new byte[1024];
		//message to bytes
		
		DatagramPacket packet = new DatagramPacket(packetBytes,packetBytes.length,message.getAddress(),message.getPort());
		this.socket.send(packet);
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
