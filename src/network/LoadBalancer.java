package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoadBalancer {
    public NetworkAccess socket;
    public HashMap<String, ArrayList<Integer>> microServices;

    public int lastBuyServer;
    public int lastSellServer;
    public int lastAuthServer;
    public int jmeterPort;

    public LoadBalancer(String connectionProtocol){

        this.microServices = new HashMap<>();
        this.lastBuyServer = 0;
        this.lastSellServer = 0;
        this.lastAuthServer = 0;

        try {
            this.connect(connectionProtocol);
            System.out.println("Load Balancer successfully started on port 9050");
            while(true){

            	Message packetReceived = this.socket.receive();

                System.out.println("Load Balancer (receiving): " + packetReceived.toString());
                Message packet = this.processPacket(packetReceived);
                
                if(packet != null) {
                    System.out.println("Load Balancer (sending): " + packet.toString());
                    this.socket.send(packet);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public Message processPacket(Message message){
        //Message replyMessage = new Message(message);
        Message replyMessage = new Message();
        replyMessage.setUsername(message.getUsername());
        replyMessage.setPassword(message.getPassword());
        replyMessage.setError(message.getError());
        replyMessage.setAddress(message.getAddress());
        replyMessage.setId(message.getId());
        replyMessage.setName(message.getName());
        replyMessage.setPrice(message.getPrice());
        replyMessage.setAccessToken(message.getAccessToken());
        replyMessage.setAuthor(message.getAuthor());

        int mappedPort = -1;


        switch (message.getAction()) {
            case "/buy":
	            System.out.println("Load Balancer: Received from JMeter port: " + message.getPort());
	            this.jmeterPort = message.getPort();

                if(!this.checkAccessToken(message)){
                    replyMessage.setPort(this.jmeterPort);
                    replyMessage.setError("Error: invalid token");
                    replyMessage.setAction("send back to JMeter");
                    return replyMessage;
                }
                System.out.println("LB: token is valid!");

	            mappedPort = this.roundRobinAlgorithm("/buy");
	            replyMessage.setPort(mappedPort);
	            replyMessage.setAction("create");
	            return replyMessage;
            
            case "/sell":
	            System.out.println("Load Balancer: Received from JMeter port: " + message.getPort());
	            this.jmeterPort = message.getPort();
                
                if(!this.checkAccessToken(message)){
                    replyMessage.setPort(this.jmeterPort);
                    replyMessage.setError("Error: invalid token");
                    replyMessage.setAction("send back to JMeter");
                    return replyMessage;
                }
                System.out.println("LB: token is valid!");

	            mappedPort = this.roundRobinAlgorithm("/sell");
	            replyMessage.setPort(mappedPort);
	            replyMessage.setAction("remove");
	            return replyMessage;

            case "/login":
	            System.out.println("Load Balancer: Received from JMeter port: " + message.getPort());

                mappedPort = this.roundRobinAlgorithm("/login");
	            replyMessage.setPort(mappedPort);
                replyMessage.setAction("login");
	            this.jmeterPort = message.getPort();
	            return replyMessage;

            case "/login/create user":
	            System.out.println("Load Balancer: Received from JMeter port: " + message.getPort());

                mappedPort = this.roundRobinAlgorithm("/login");
	            replyMessage.setPort(mappedPort);
                replyMessage.setAction("create user");
	            this.jmeterPort = message.getPort();
	            return replyMessage;


            case "create buy instance":
	            System.out.println("Load Balancer: Vou registrar instancia /buy");
	            this.registerServerInstance("/buy", message.getId()); 
	            break;

            case "create sell instance":
	            System.out.println("Load Balancer: Vou registrar instancia /sell");
	            this.registerServerInstance("/sell", message.getId()); 
	            break;

            case "create authentication instance":
	            System.out.println("Load Balancer: Vou registrar instancia - authentication");
	            this.registerServerInstance("/login", message.getId()); 
	            break;

            case "send back to JMeter":
	            replyMessage.setPort(this.jmeterPort);
                replyMessage.setAction(message.getAction());
	            System.out.println("Load Balancer: Sending to JMeter: " + replyMessage.toString());
	            System.out.println("Load Balancer: sending to JMeter on port " + replyMessage.getPort());
	            return replyMessage;
            

            default:
            	System.out.println("Load Balancer: Action not recognized");
                replyMessage.setError("Error: Action not recognized");
                replyMessage.setPort(message.getPort());
                return replyMessage;
        }

        return null;
    }


    public void connect(String connectionProtocol) throws IOException{
        System.out.println("Starting Load Balancer on port 9050...");
        System.out.println("Network connection: " + connectionProtocol);
        switch (connectionProtocol.toLowerCase()) {
            case "udp":
                this.socket = new UDPHandler(9050);
                break;

            case "tcp":
                this.socket = new TCPHandler(9050);
                break;

            case "http": break;
            default:
                System.out.println("Unknown connection type. Aborting server...");
                System.exit(1);
                break;
        }
    }


    public void registerServerInstance(String route, Integer port){
        if(!this.microServices.containsKey(route)){
            ArrayList<Integer> instancesPort = new ArrayList<>();
            instancesPort.add(port);
            this.microServices.put(route, instancesPort);
            System.out.println("Route: " + route + " Ports: " + this.microServices.get(route));
            return;
        }
        ArrayList<Integer> instances = this.microServices.get(route);
        this.microServices.remove(route);
        if(!instances.contains(port)){
            instances.add(port);
            this.microServices.put(route, instances);
        }
    }

    public int roundRobinAlgorithm(String route){
        ArrayList<Integer> instances = this.microServices.get(route);

        int port = -1;
        int size = instances.size();

        if(route.equals("/buy")){
            this.lastBuyServer++;
            this.lastBuyServer = this.lastBuyServer%size;
            port = instances.get(this.lastBuyServer);
        }
        else if(route.equals("/sell")){
            this.lastSellServer++;
            this.lastSellServer = this.lastSellServer%size;
            port = instances.get(this.lastSellServer);
        }
        else if(route.equals("/login") || route.equals("/login/create user")){
            this.lastAuthServer++;
            this.lastAuthServer = this.lastAuthServer%size;
            port = instances.get(this.lastAuthServer);
        }

        System.out.println("Load Balancer: Port selected: " + port);
        return port;
    }

    public boolean checkAccessToken(Message message){
        Message messageToAuth = new Message();
        messageToAuth.setUsername(message.getUsername());
        messageToAuth.setAction("check token");
        messageToAuth.setAccessToken(message.getAccessToken());
        messageToAuth.setAddress(message.getAddress());
        
        try {
            messageToAuth.setPort(this.socket.getPort());
            int mappedPort = this.roundRobinAlgorithm("/login");

            this.socket.send(messageToAuth, mappedPort);

            Message replyFromAuth = this.socket.receive();

            if(replyFromAuth.getError().equals("OK"))
                return true;
            else 
                return false;

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("LB: it was not possible to communicate with Authentication service");
        return false;
    }

    public static void main(String[] args) {
		new LoadBalancer(args[0]);
	}
}
