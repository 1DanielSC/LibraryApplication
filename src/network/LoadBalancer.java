package network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class LoadBalancer {
    public NetworkAccess socket;    
    
    public HashMap<String, ArrayList<Tuple<Integer, Integer>>> microServices;

    public final ScheduledThreadPoolExecutor heartBeatExecutor = new ScheduledThreadPoolExecutor(1);

    public Heartbeat hb;

    public int lastBuyServer;
    public int lastSellServer;
    public int lastAuthServer;
    public int jmeterPort;

    public LoadBalancer(String connectionProtocol){
        heartBeatExecutor.scheduleWithFixedDelay(this::heartbeat, 2, 5, TimeUnit.SECONDS);
 
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

    
    public void heartbeat(){
        System.out.println("HeartBear: checking servers liveness");

        this.hb.setSoTimeout(6000);

        for(String microService : this.microServices.keySet()){
            for(Tuple<Integer, Integer> ports : this.microServices.get(microService)){

                int hbPort = ports.second;

                try {
                    System.out.println("HeartBeat: sending request to " + microService + "; on hbPort="+hbPort);
                    this.hb.send(hbPort); 

                    if(this.hb.receive()){
                        System.out.println(microService + " on port " + ports.first + " is alive!");
                    }else{
                        System.out.println(microService + " on port " + ports.first + " is NOT alive!");
                        this.microServices.get(microService).remove(ports);
                    }

                }catch (IOException e){
                    System.out.println(microService + " on port " + ports.first + " is dead!");
                    this.microServices.get(microService).remove(ports);
                }

                System.out.println("Available bhPorts: " + this.microServices.get(microService));
                if(this.microServices.get(microService).size() == 0){
                    this.microServices.remove(microService);
                    break;
                }
            }
            
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
	            this.registerServerInstance("/buy", message.getId(), Integer.parseInt(message.getName())); //"name" will hold hbPort
	            break;

            case "create sell instance":
	            System.out.println("Load Balancer: Vou registrar instancia /sell");
	            this.registerServerInstance("/sell", message.getId(), Integer.parseInt(message.getName())); 
	            break;

            case "create authentication instance":
	            System.out.println("Load Balancer: Vou registrar instancia - authentication");
	            this.registerServerInstance("/login", message.getId(), Integer.parseInt(message.getName())); 
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
                this.hb = new UDPLoadBalancerHB(7000);
                break;

            case "tcp":
                this.socket = new TCPHandler(9050);
                this.hb = new TCPLoadBalancerHB(7000);
                break;

            case "http": 
                this.socket = new HTTPHandler(9050);
                this.hb = new TCPLoadBalancerHB(7000);
                break;
            default:
                System.out.println("Unknown connection type. Aborting server...");
                System.exit(1);
                break;
        }
    }


    public void registerServerInstance(String route, Integer port, Integer heartbeatPort){

        Tuple<Integer, Integer> instancePorts = new Tuple<Integer,Integer>(port, heartbeatPort);

        if(!this.microServices.containsKey(route)){
            ArrayList<Tuple<Integer, Integer>> instancesPort = new ArrayList<>();
            instancesPort.add(instancePorts);
            this.microServices.put(route, instancesPort);
            System.out.println("Route: " + route + " Ports: " + this.microServices.get(route));
            return;
        }

        System.out.println("LB: ja tenho uma instancia para a rota: " + route);
        ArrayList<Tuple<Integer, Integer>> instances2 = this.microServices.get(route);

        if(!instances2.contains(instancePorts)){
            this.microServices.get(route).add(instancePorts);
            System.out.println("LB: Services registered after new " + route +": " + this.microServices.get(route));
        }else
            System.out.println("LB: service " + route + " on port " + port + " already exists");

    }

    public int roundRobinAlgorithm(String route){
        ArrayList<Tuple<Integer, Integer>> instances2 = this.microServices.get(route);

        int portSelected = -1;
        int length = instances2.size();

        if(route.equals("/buy")){
            this.lastBuyServer++;
            this.lastBuyServer = this.lastBuyServer%length;
            portSelected = instances2.get(this.lastBuyServer).first;
        }
        else if(route.equals("/sell")){
            this.lastSellServer++;
            this.lastSellServer = this.lastSellServer%length;
            portSelected = instances2.get(this.lastSellServer).first;
        }
        else if(route.equals("/login") || route.equals("/login/create user")){
            this.lastAuthServer++;
            this.lastAuthServer = this.lastAuthServer%length;
            portSelected = instances2.get(this.lastAuthServer).first;
        }

        System.out.println("Load Balancer: Port selected: " + portSelected);
        return portSelected;
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
