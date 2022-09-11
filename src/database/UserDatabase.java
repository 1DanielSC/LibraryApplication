package database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import application.Server;
import model.User;
import network.Message;
import network.NetworkAccess;
import network.TCPHandler;
import network.UDPHandler;

public class UserDatabase implements Server{
    private List<User> database;

    public NetworkAccess socket;

    public UserDatabase(String databasePort, String connectionType) throws IOException{
        this.database = new ArrayList<>();
        this.connect(databasePort, connectionType);
        System.out.println("User Database server succesfully started on port " + this.socket.getPort() + ".");
        while(true){
            Message packetReceived = this.socket.receive();

            //this.processPacket(packetReceived);


            this.socket.send(packetReceived);
        }
    }

    public void connect(String databasePort, String connectionType){
        System.out.println("Starting UserDatabase server on port " + databasePort + "...");
        try {
            switch(connectionType.toLowerCase()){
                case "udp":
                    this.socket = new UDPHandler(Integer.parseInt(databasePort));
                    this.registerIntoLoadBalancer(databasePort);
                    break;

                case "tcp":
                    this.socket = new TCPHandler(Integer.parseInt(databasePort));
                    break; 
                case "http": break; //TODO

                default:
                    System.out.println("Unknown connection type. Aborting server...");
                    System.exit(1);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add(User user){
        this.database.add(user);
    }

    public void removeById(Integer id){
        this.database.remove(id);
    }

    public boolean isPresent(User user){
        return this.database.contains(user);
    }

    public void registerIntoLoadBalancer(String databasePort){

    }
}
