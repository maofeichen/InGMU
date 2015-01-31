package os.assignment3.server;

//import org.json.simple.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import os.assignment3.util.Util;

public class HeartbeatClient
{

    private static final String UTHERE = "uthere";

    private Socket clientSocket;

    private String ipAddress;
    private int port;
    private String rmiBindName;
    private int timeoutSec;
    
    private NodeStatus status;

    public HeartbeatClient(String serverIpAddress, int serverPort, int timeoutSec, String serverName) {
        this.ipAddress = serverIpAddress;
        this.port = serverPort;
        this.timeoutSec = timeoutSec;
        this.rmiBindName = serverName;
    }

    public NodeStatus verifyNodeStatus()
    {

        BufferedWriter out = null;
        BufferedReader in = null;

        try {

            status = null;
            if(!connectToServer()) {
                return null;
            }

            //Initialization to allow transfer of data between server and client
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //Send message to the server
            out.write(UTHERE);
            out.newLine();
            out.flush();
            System.out.println("Heartbeat sent to node "+rmiBindName+": "+UTHERE);

            //Get return message from the server
            StringBuilder response = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            System.out.println("Message received from node :" + response);
            if(response.indexOf(NodeStatus.started.toString()) >= 0) {
                System.out.println("Node "+rmiBindName+" is looking fine with status: "+response);
                status = NodeStatus.valueOf(response.toString());
                return NodeStatus.started;
            } else if(response.indexOf(NodeStatus.faulty.toString()) >= 0) {
                System.out.println("Node "+rmiBindName+" is faulty (injected) fine with status: "+response);
                status = NodeStatus.valueOf(response.toString());
                return NodeStatus.faulty;
            }

        }
        catch (Exception exception)
        {
            System.out.println("Exception is "+exception.getMessage());
            exception.printStackTrace(System.out);
        } finally {

            Util.closeReader(in);
            Util.closeWriter(out);
            Util.closeSocket(clientSocket);

        }

        //there may be timeout or other socket exception
        //return null
        System.out.println("Node "+rmiBindName+" \"may be\" faulty with status: "+NodeStatus.faulty);

        return null;

    }

    private boolean connectToServer() {

        try {

            //Connect to the server
            InetAddress address = InetAddress.getLocalHost();
            clientSocket = new Socket(address, port);
            if(clientSocket != null) {
                clientSocket.setSoTimeout(timeoutSec * 1000);
                
                return true;
            }
            return true;
            
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ipAddress);
        } catch (IOException e) {
            //System.err.println("Couldn't get I/O for the connection");
        }

        return false;

    }
    
    public static void main(String[] args) {
        HeartbeatClient hc = new HeartbeatClient("127.0.0.1", 3002, 5, "department2");
        hc.verifyNodeStatus();
    }

}//end