package os.assignment3.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HeartbeatServer extends Thread {
    
    public static final String NAME = "HeartbeatServer";

    private int port;
    private DepartmentNode departmentNode;

    public HeartbeatServer(int port, DepartmentNode departmentNode) {
        this.port = port;
        this.departmentNode = departmentNode;
        this.setName(NAME);
    }

    public void run() {

        ServerSocket serverSocket = null;
        try {

            serverSocket = new ServerSocket(port);
            System.out.println("Heartbeat server Started and listening to the port "+ port);

            try { 
                while (true)
                {
                    if(departmentNode.getStatus() == NodeStatus.faulty) {
                        System.out.println("Not handling heartbeats as node is in injected faulty state");
                    } else {
                        System.out.println ("Waiting for Connection...");
                        Socket clientSocket = serverSocket.accept();
                        (new HeartbeatHandler(clientSocket, departmentNode)).start();
                    }

                }
            } 
            catch (IOException e) 
            { 
                System.out.println("Accept failed: "+e.getMessage());
                e.printStackTrace(System.out);
            } 

        } catch (IOException e) {
            e.printStackTrace(System.out);
            System.err.println("Could not listen on port: "+port); 
            System.exit(1); 
        } 
        finally
        {
            try {
                System.out.println("Server socket closed.");
                serverSocket.close(); 
            }
            catch (IOException e)
            { 
                System.err.println("Could not close port: "+ port); 
                System.exit(1); 
            } 
        }

    }
    
    public static void main(String[] args) throws Exception {
        DepartmentNode node = new DepartmentNode(null, 3, 2);
        HeartbeatServer hs = new HeartbeatServer(3002, node);
        hs.run();
    }

}//end