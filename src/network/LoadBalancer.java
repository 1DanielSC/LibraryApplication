package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class LoadBalancer {
    public NetworkAccess socket;
    public HashMap<String, ArrayList<Integer>> microServices;


    public LoadBalancer(String port, String connectionProtocol){


        this.microServices = new HashMap<>();
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


    public void addInstance(String route, Integer port){
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

    public void roundRobinAlgorithm(String route){
        ArrayList<Integer> instances = this.microServices.get(route);
    }

    //check aliveness of instance

    public static void main(String[] args) {
		new LoadBalancer(args[0], args[1]);
	}
}
