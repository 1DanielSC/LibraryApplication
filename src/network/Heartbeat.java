package network;

import java.net.SocketTimeoutException;

public interface Heartbeat {
    public void send(int instanceHbPort) throws SocketTimeoutException;
    public boolean receive();
    public void setSoTimeout(int timeout);
}
