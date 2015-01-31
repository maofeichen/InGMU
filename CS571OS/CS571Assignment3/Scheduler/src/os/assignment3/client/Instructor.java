package os.assignment3.client;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;
import java.util.Scanner;

import os.assignment3.server.InstructorInterface;

public class Instructor
{

    static InstructorInterface rmiServer;
    static Registry registry;

    static String serverName;
    static String serverAddress;
    static int serverPort;
    
    static String instructorName = "Instructor Bob";

    static public void main(String args[])
    {
      //ask user for server's address and port
        Scanner stdIn = new Scanner(System.in);
        System.out.print("Enter Server's Name: ");
        serverName = stdIn.nextLine();  

        getServerDetails();
        
        try {
            // get the registry
            registry = LocateRegistry.getRegistry(serverAddress, serverPort);

            // look up the remote object
            rmiServer = (InstructorInterface)(registry.lookup(serverName));

            processRequests();
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    private static void processRequests() throws RemoteException {
        Scanner stdIn = new Scanner(System.in);
        String next = "y";
        while (next.equalsIgnoreCase("y")){
            System.out.println("");
            
            System.out.print("Enter Request Type Number (1-Lookup, 2-Reserve): ");
            String requestType = stdIn.nextLine();

            if (requestType.equals("1")){
                System.out.print("Enter Date (12/dd/13): ");
                String date = stdIn.nextLine();
                
                boolean l = rmiServer.lookUp(new Date(), instructorName); //message back from server
                System.out.println("Lookup Response from Server: "+l);
            } else if (requestType.equals("2")){
                System.out.print("Enter Date (12/dd/13): ");
                String date = stdIn.nextLine();
                
                if (rmiServer.lookUp(new Date(), instructorName)){
                    System.out.println("Date Available. Making reservation.");
                    boolean r = rmiServer.reserve(new Date(), instructorName); //message back from server
                    System.out.println("Reserve Response from Server: "+r);
                } else {
                    System.out.println("Date not Available");
                }
            } else{
                System.out.println("Incorrect Request Type Number!");
                System.out.print("Enter Request Type Number (1-Lookup, 2-Reserve): ");
                requestType = stdIn.nextLine();
            }

            System.out.println("Another Request? (y/n): ");
            next = stdIn.nextLine();
        }
    }

    private static void getServerDetails() {
        serverAddress = "127.0.0.1";
        if (serverName.equals("department1")){
            serverPort = 2001;
        } else if (serverName.equals("department2")){
            serverPort = 2002;
        } else if (serverName.equals("department3")){
            serverPort = 2003;
        } else if (serverName.equals("department4")){
            serverPort = 2004;
        } else {
            System.err.println("Incorrect server name!");
        }
    }
}