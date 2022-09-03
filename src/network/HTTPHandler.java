package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class HTTPHandler implements NetworkAccess{
	
	public Socket socket;
	
    public HTTPHandler(Integer port){
        
    }
    
    public void send(Message message) throws IOException{
    	
    }
	public Message receive() throws IOException{
		
	}
	
	public void register() throws IOException{
		
	}

	public int getPort() throws IOException{
		return this.socket.getPort();
	}
	
	public InetAddress getInetAddress() {
		return this.socket.getInetAddress();
	}
    
   
}
