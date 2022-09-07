package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoadBalancer {
    public NetworkAccess socket;
    public HashMap<String, ArrayList<Integer>> microServices;

    public int lastBuyServer;
    public int lastSellServer;

    public LoadBalancer(String port, String connectionProtocol){

        this.microServices = new HashMap<>();
        this.lastBuyServer = 0;
        this.lastSellServer = 0;

        try {
            this.connect(port, connectionProtocol);

            while(true){

                //receive route + port (from server instances)
                //receive route + data (from JMeter)

                //create or update route and add port
                //redirect request

                // send back OK message
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void connect(String port, String connectionProtocol) throws IOException{
        System.out.println("Starting Load Balancer on port " + port);
        switch (connectionProtocol.toLowerCase()) {
            case "udp":
                this.socket = new UDPHandler(Integer.parseInt(port));
                break;

            case "tcp":
            this.socket = new TCPHandler(Integer.parseInt(port));
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
            this.microServices.put(route, new ArrayList<>(port));
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

    //check aliveness of instance

    public static void main(String[] args) {
		new LoadBalancer(args[0], args[1]);
	}
}
