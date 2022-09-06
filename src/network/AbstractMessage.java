package network;

import java.io.Serializable;
import java.net.InetAddress;

public class AbstractMessage implements Serializable{
    protected String action;
    protected String error;
    protected Integer port;
    protected InetAddress address;

    public AbstractMessage(){

    }

    public AbstractMessage(String action, String error, Integer port, InetAddress address) {
        this.action = action;
        this.error = error;
        this.port = port;
        this.address = address;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    public InetAddress getAddress() {
        return address;
    }
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    
}
