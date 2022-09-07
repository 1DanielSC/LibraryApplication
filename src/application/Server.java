package application;

import java.io.IOException;

public interface Server {

	public void connect(String serverPort, String connectionType) throws IOException; //stablish UDP/TCP/HTTP connection

	public void registerIntoLoadBalancer(String serverPort);

}
