package network;

import java.io.IOException;
import java.net.InetAddress;

public interface NetworkAccess {
	public void send(Message message) throws IOException;
	public void send(DatabaseMessage databaseMessage) throws IOException;


	public Message receive() throws IOException;
	public DatabaseMessage receiveDatabaseMessage() throws IOException;

	
	public void register() throws IOException; //Load Balancer

	public int getPort() throws IOException;
	public InetAddress getInetAddress();
}
