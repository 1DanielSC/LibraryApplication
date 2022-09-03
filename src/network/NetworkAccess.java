package network;

import java.io.IOException;
import java.net.InetAddress;

public interface NetworkAccess {
	public void send(Message message) throws IOException;
	public Message receive() throws IOException;
	
	public void register() throws IOException; //Load Balancer

	public int getPort() throws IOException;
	public InetAddress getInetAddress();
}
