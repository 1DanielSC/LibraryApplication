package application;

import network.NetworkAccess;

public interface Server {
	public void registerLoadBalance();
	public void setNetworkAccess(NetworkAccess serverSocket);

}
