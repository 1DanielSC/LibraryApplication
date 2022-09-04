package application;

import java.io.IOException;

public interface Server {

	public void connect(String serverPort, String connectionType) throws IOException;

	public void registerLoadBalancer();

}
