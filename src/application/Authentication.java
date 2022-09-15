package application;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;

public class Authentication implements Server{

    public NetworkAccess socket;
    public HashMap<String, String> tokens;


    public Authentication(String serverPort, String connectionType){
        this.tokens = new HashMap<>();
        
        try{
            this.connect(serverPort, connectionType);
			System.out.println("Authentication server succesfully started on port " + this.socket.getPort() + ".");

            while(true){
                Message packetReceived = this.socket.receive();
                int port = packetReceived.getPort(); 
                System.out.println("Authentication (receiving): " + packetReceived.toString());


                Message replyMessage = this.processPacket(packetReceived); //TODO investigar mudança de porta
                replyMessage.setPort(packetReceived.getPort());
				replyMessage.setAddress(packetReceived.getAddress());

                System.out.println("Authentication: sending to LB: "+ replyMessage.toString());

                
                replyMessage.setPort(port);
                replyMessage.setAction("send back to JMeter");
                this.socket.send(replyMessage);
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void connect(String serverPort, String connectionProtocol) throws IOException{
        System.out.println("Starting Authentication on port " + serverPort + "...");
        System.out.println("Network connection: " + connectionProtocol);
		try{

			switch (connectionProtocol.toLowerCase()) {
				case "udp":
					this.socket = new UDPHandler(Integer.parseInt(serverPort));
					this.registerIntoLoadBalancer(serverPort);
					break;

				case "tcp" : 
					this.socket = new TCPHandler(Integer.parseInt(serverPort));
					this.registerIntoLoadBalancer(serverPort);
				break;
				
				case "http": break;
				default:
					System.out.println("Unknown connection type. Aborting server...");
					System.exit(1);
					break;
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}
    }

    public void registerIntoLoadBalancer(String serverPort){
        try {
			Message messageToLoadBalancer = new Message();
			messageToLoadBalancer.setAction("create authentication instance");

			messageToLoadBalancer.setAddress(InetAddress.getLocalHost());
			messageToLoadBalancer.setId(this.socket.getPort());


			System.out.println("Auth Server: minha porta: " + this.socket.getPort());
			System.out.println("Auth Server: Vou me registrar no Load Balancer");

			this.socket.send(messageToLoadBalancer, 9050);

		} catch (IOException e) {
			e.printStackTrace();
		}
    }


    public Message processPacket(Message message){
        Message replyMessage = new Message();

        switch (message.getAction().toLowerCase()) {
            case "check token":
                if(this.checkToken(message.getUsername(), message.getAccessToken()))
                    replyMessage.setError("OK");
                else
                    replyMessage.setError("Error: invalid token");
                break;
            
            case "login": //login
                replyMessage = this.login(message);
                break;
            
            case "create user": 
                replyMessage = this.createUser(message);
                break;

            default:
                System.out.println("Authentication: Action not recognized");
                break;
        }

        
        replyMessage.setPort(message.getPort()); 
        replyMessage.setAddress(message.getAddress()); 
        return replyMessage;
    }

    public boolean checkToken(String username, String token){
        return this.tokens.get(username).equals(token);
    }

    public Message login(Message message){
        try {
            this.socket.send(message,9001); 
            
            Message response = this.socket.receive();

            if(response.getError().equals("OK")){
                this.tokens.put(message.getUsername(), message.getUsername() + message.getPassword());
                response.setAccessToken(message.getUsername() + message.getPassword());
            }

            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Message createUser(Message message){
        try {

            message.setPort(this.socket.getPort()); 
            System.out.println("Auth: Sending to UserDatabase: " + message.toString());
            this.socket.send(message,9001); 
            
            return this.socket.receive();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        new Authentication(args[0], args[1]);
    }
}
