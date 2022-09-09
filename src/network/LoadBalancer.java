package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoadBalancer {
    public NetworkAccess socket;
    public HashMap<String, ArrayList<Integer>> microServices;

    public int lastBuyServer;
    public int lastSellServer;
    public int jmeterPort;

    public LoadBalancer(String connectionProtocol){

        this.microServices = new HashMap<>();
        this.lastBuyServer = 0;
        this.lastSellServer = 0;

        try {
            this.connect(connectionProtocol);
            System.out.println("Load Balancer successfully started on port 9050");
            while(true){
                this.teste();
                /* 

                //Receiving packet: Two possible scenarios
                    //receive from server instances
                    //receive from JMeter (route + data)
                Message packetReceived = this.socket.receive();
                this.socket.send(packetReceived, packetReceived.getPort()); //adicionei para testar direto no jmeter
                	//o JMeter recebeu - OK!! pROBLEMA ESTA AQUI EM BAIXO
                
                
                
                System.out.println("Load Balancer: " + packetReceived.toString());
                System.out.println("Load Balancer (ACTION): " + packetReceived.getAction().toLowerCase());
                //request sent from JMeter - we need to save its port to send back message to it
                if(packetReceived.getAction().toLowerCase().equals("/buy") ||
                packetReceived.getAction().toLowerCase().equals("/sell") ||
                packetReceived.getAction().toLowerCase().equals("/login")){
                    System.out.println("Load Balancer: JMeter port: " + packetReceived.getPort());
                    this.jmeterPort = packetReceived.getPort();
                }

                // send back OK message to JMeter - we need to store its port
                if(!packetReceived.getError().equals("")){
                    packetReceived.setPort(this.jmeterPort);
                    System.out.println("Load Balancer: sending to JMeter on port " + packetReceived.getPort());
                    
                    System.out.println("Load Balancer: Sending to JMeter: " + packetReceived.toString());
                    this.socket.send(packetReceived, packetReceived.getPort());
                    continue;
                }

                

                //create or update route and add port
                //redirect request
                
                Message packetToServer = this.operate(packetReceived);


                //redirect request to proper server
                if(packetToServer != null){
                    this.socket.send(packetToServer);
                }
                */
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public void teste() throws IOException{
        Message packetReceived = this.socket.receive();

        //check action
            //whether it's a JMeter request (API consumption) or Server request (LB register)

        Message packet = this.operate2(packetReceived);
        
        if(packet != null) this.socket.send(packet);
    }


    public Message operate2(Message message){
        Message replyMessage = new Message();
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
	            System.out.println("Load Balancer: JMeter port: " + message.getPort());
	            this.jmeterPort = message.getPort();
	
	            mappedPort = this.roundRobinAlgorithm("/buy");
	            replyMessage.setPort(mappedPort);
	            replyMessage.setAction("create");
	            return replyMessage;
            
            case "/sell":
	            System.out.println("Load Balancer: JMeter port: " + message.getPort());
	            this.jmeterPort = message.getPort();
	
	            mappedPort = this.roundRobinAlgorithm("/sell");
	            replyMessage.setPort(mappedPort);
	            replyMessage.setAction("remove");
	            return replyMessage;

            case "/login":
	            System.out.println("Load Balancer: JMeter port: " + message.getPort());
	            this.jmeterPort = message.getPort();
	            return replyMessage;

            case "create buy instance":
	            System.out.println("Load Balancer: Vou registrar /buy");
	            this.registerServerInstance("/buy", message.getId()); 
	            break;

            case "create sell instance":
	            System.out.println("Load Balancer: Vou registrar /sell");
	            this.registerServerInstance("/sell", message.getId()); 
	            break;

            case "send back to JMeter":
	            replyMessage.setPort(this.jmeterPort);
	            System.out.println("Load Balancer: Sending to JMeter: " + replyMessage.toString());
	            System.out.println("Load Balancer: sending to JMeter on port " + replyMessage.getPort());
	            return replyMessage;
            

            default:
            	System.out.println("Load Balancer: Action not recognized");
                break;
        }

        return null;
    }


    public void connect(String connectionProtocol) throws IOException{
        System.out.println("Starting Load Balancer on port 9050...");
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

    public Message operate(Message packetReceived) throws UnknownHostException {
        int mappedPort = -1;

        Message packet = new Message();
        packet.setAddress(InetAddress.getLocalHost());
        
        switch (packetReceived.getAction().toLowerCase()) {
            case "create buy instance":
                System.out.println("Load Balancer: Vou registrar /buy");
                this.registerServerInstance("/buy", packetReceived.getId()); 
                break;
        
            case "create sell instance":
                System.out.println("Load Balancer: Vou registrar /sell");
                this.registerServerInstance("/sell", packetReceived.getId());
                break;

            case "/buy":
                mappedPort = this.roundRobinAlgorithm("/buy");
                packet.setPort(mappedPort);
                packet.setAction("create");
                return packet; //packet for redirecting request to a BuyServer instance
                
            case "/sell":
                mappedPort = this.roundRobinAlgorithm("/sell");
                packet.setPort(mappedPort);
                packet.setAction("remove");
                return packet; //packet for redirecting request to a SellServer instance

            case "/login":
                //redirect request to a LoginServer instance
                break;

            default:
                System.out.println("Load Balancer: Action not recognized");
                break;
        }
        return null;
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

        System.out.println("Load Balancer: Port selected: " + port);
        return port;
    }

    //check liveness of instance

    public static void main(String[] args) {
		new LoadBalancer(args[0]);
	}
}
