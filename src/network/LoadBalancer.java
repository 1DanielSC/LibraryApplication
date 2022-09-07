package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoadBalancer {
    public NetworkAccess socket;
    public HashMap<String, ArrayList<Integer>> microServices;

    public int lastBuyServer;
    public int lastSellServer;

    public LoadBalancer(String connectionProtocol){

        this.microServices = new HashMap<>();
        this.lastBuyServer = 0;
        this.lastSellServer = 0;

        try {
            this.connect(connectionProtocol);
            System.out.println("Load Balancer successfully started on port 9050");
            while(true){
                //receive route + port (from server instances)
                //receive route + data (from JMeter)
                Message packetReceived = this.socket.receive();

                System.out.println("Load Balancer: " + packetReceived.toString());

                //create or update route and add port
                //redirect request
                this.operate(packetReceived);
                

                // send back OK message
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void operate(Message packetReceived){
        int mappedPort = -1;
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
                //redirect request to a BuyServer instance
                break;

            case "/sell":
                mappedPort = this.roundRobinAlgorithm("/sell");
                //redirect request to a SellServer instance
                break;

            case "/login":
                //redirect request to a LoginServer instance
                break;

            default:
                System.out.println("Load Balancer: Action not recognized");
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
            port = this.lastBuyServer;
        }
        else if(route.equals("/sell")){
            this.lastSellServer++;
            this.lastSellServer = this.lastSellServer%size;
            port = this.lastSellServer;
        }

        return port;
    }

    //check liveness of instance

    public static void main(String[] args) {
		new LoadBalancer(args[0]);
	}
}
