package application;

import java.io.IOException;

public interface Server {

	public void connect(String serverPort, String connectionType, String hbPort) throws IOException;

	public void registerIntoLoadBalancer(String serverPort, String hbPort);

}
