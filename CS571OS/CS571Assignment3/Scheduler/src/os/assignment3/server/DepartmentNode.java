package os.assignment3.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import os.assignment3.util.Util;

@SuppressWarnings("unused")
public class DepartmentNode extends java.rmi.server.UnicastRemoteObject
    implements InstructorInterface, NodeInterface, FaultInjectionInterface
{

    private static final long serialVersionUID = 1L;
    
    /**
     * Specifies how frequently should the node check for right node availability
     */
    public static final int NEIGHBOR_CONN_INTERVAL_SEC = 3;

    private String configFileName;
    private Properties configuration;
    private NodeStatus status = NodeStatus.initprogress;
    
    /**
     * Represents if the node has token with it or not.
     */
    private AtomicBoolean hasToken = new AtomicBoolean(false);
    
    /**
     * Specifies no of instructor threads that are requesting either
     * lookup / reserve
     */
    private AtomicInteger numOfClients = new AtomicInteger(0);
    
    private Object lock = new Object();
    private Object tokenTimerLock = new Object();

    private TokenTimer tokenTimer;

    /**
     * Time node can spend in critical section - d1
     */
    private int csExecTimeSec;
    /**
     * time node should wait before passing token to right node - d2
     */
    private int processDelayTimeSec;

    /**
     * No of times heartbeat sent to declare node as possible
     * faulty state.
     */
    private int noHeartbeatTries=2;

    /**
     * Server listening for Heartbeat packets
     */
    private HeartbeatServer hbServer;
    
    /**
     * RMI handle to the nighbor node
     */
    private NodeInterface nodeClient;
    
    private NodeDetails thisNode;
    private NodeDetails leftNode;
    private NodeDetails rightNode;
    private NodeDetails rightOfRightNode;

    private ArrayList<String> schedule = new ArrayList<String>();
    public static final String scheduleFileName = "schedule.txt";

    public DepartmentNode(String configFileName, int csExecTimeSec, int processDelayTimeSec) throws RemoteException {

        this.configFileName = configFileName;
        this.csExecTimeSec = csExecTimeSec;
        this.processDelayTimeSec = processDelayTimeSec;

        try {

            System.out.println("Loading configuration...");
            configuration = new Properties();
            configuration.load(new FileInputStream(configFileName));
            System.out.println("Department node started with config...");
            configuration.list(System.out);

        } catch(Exception exc) {
            System.out.println("Error loading configuration from "+configFileName+" "+exc.getMessage());
            exc.printStackTrace();
            System.exit(1);
        }
        
        loadNodeDetails();
        
        //create token if configured for
        createToken();

        bindToRegistry();
        
        //start heartbeat server
        hbServer = new HeartbeatServer(thisNode.getHeartbeatPort(), this);
        hbServer.start();

        //set status to started, neighbors can connect now
        setStatus(NodeStatus.started);
        
        NodeConnector connector = new NodeConnector(rightNode);
        connector.start();
        
        try {
            connector.join();
        } catch(InterruptedException exc) {
            System.out.println("InterruptedException: "+exc.getMessage());
        }
        
        //set the token acquired time
        if(hasToken.get()) {
            System.out.println("Node configured to create token, created token");
            tokenTimer = new TokenTimer(csExecTimeSec);
            
         /* After all departments nodes setup, begin to pass token from first department
          * via input s in the console at first department node */
			Thread tCirTokenByConsole = new Thread() {
				public void run() {
					BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
					String input;
					try {
						while ( (input = stdIn.readLine() ) != null){
							if (input.equals("s"))
								tokenTimer.start();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			tCirTokenByConsole.start();
        }
    }
    
    private void createToken() {
        
        if(configuration.getProperty("create_token") != null
                && configuration.getProperty("create_token").trim().length() > 0) {
            boolean flag = Boolean.parseBoolean(configuration.getProperty("create_token"));
            hasToken.set(flag);
        }
        
    }

    /**
     * Continuously checks for the specified node availability and connects to it
     * when it becomes available.
     *
     */
    private class NodeConnector extends Thread {
        
        public static final String NAME = "NodeConnector";
        
        private NodeDetails node;
        
        public NodeConnector(NodeDetails node) {
            this.node = node;
            this.setName(NAME);
        }
    
        public void run() {

            while(true) {
                
                try {
                    
                    //try to send heartbeat to right node
                    NodeStatus status = verifyNodeStatus(node);
                    //System.out.println("---------status is "+status);
                    if(status == NodeStatus.started) {
                        System.out.println("---------status is "+status);
                        //start node client and connect to right node
                        System.out.println("Connecting to neighbir node "+node+"...");

                        connectToNeighbor(rightNode);
                        System.out.println("Connected fine to neighbor "+node);
                        
                        rightOfRightNode = nodeClient.getRightNeighbor();
                        System.out.println("Right of right node is: "+rightOfRightNode);
                        
                        break;

                    } else {
                        //System.out.println("Will try to connecto to node after "+NEIGHBOR_CONN_INTERVAL_SEC+" sec again");
                    }
                    
                    Thread.sleep(NEIGHBOR_CONN_INTERVAL_SEC * 1000);
                
                } catch(Exception exc) {
                    System.out.println("Error checking / connecting to node "+node+": "+exc.getMessage());
                }
            
            }
            
            System.out.println("Connector exiting after successfull connection to node "+node);

        }

        private void connectToNeighbor(NodeDetails node) throws RemoteException, NotBoundException {
            
            try {

                //System.out.println("Connecting to neighbor node "+node+"...");
                // get the registry
                Registry registry = LocateRegistry.getRegistry(node.getIpAddress(), node.getRmiPort());
                // look up the remote object
                nodeClient = (NodeInterface)(registry.lookup(node.getRmiBindName()));
                System.out.println("Connected to neighbor node "+node+" successfully: "+nodeClient);

            } catch (RemoteException | NotBoundException e) {
                System.out.println("Error connecting to node "+node);
                throw e;
            }
            
        }
    
    }
    
    /**
     * Thread that releases the token after a specified delay
     */
    private class TokenTimer extends Thread {
        
        private long tokenPossessionTime; 
        
        public TokenTimer(long tokenPossessionTime) {
            this.tokenPossessionTime = tokenPossessionTime;
            this.setName("TokenTimer");
        }
        
        public void run() {
            
            synchronized(tokenTimerLock) {
                try {
                    tokenTimerLock.wait(tokenPossessionTime * 1000);
                } catch(InterruptedException exc) {
                    System.out.println("Token timer interrupted!");
                }
            }

            try {
            
                System.out.println("-----------node client="+nodeClient);
                nodeClient.setToken();
                System.out.println("Releasied token and passed it to the neighbor");
            
            } catch(RemoteException exc) {
                System.out.println("Remote exception while sending token to neighbor: "+exc.getMessage());
            }
            
        }
        
    }
    
    private void loadNodeDetails() {
        
        thisNode = new NodeDetails();
        thisNode.setRmiPort(Integer.parseInt(configuration.getProperty("rmi_port")));
        thisNode.setHeartbeatPort(Integer.parseInt(configuration.getProperty("heartbeat_port")));
        thisNode.setRmiBindName(configuration.getProperty("rmi_bind_name"));

        try {
            thisNode.setIpAddress(new String(InetAddress.getLocalHost().getAddress()));
        } catch(UnknownHostException exc) {
            System.out.println("Error getting local host addr!, wierd!!: "+exc.getMessage());
            exc.printStackTrace(System.out);
            System.exit(1);
        }

        rightNode = new NodeDetails();
        rightNode.setIpAddress(configuration.getProperty("rightnode.ip_address"));
        rightNode.setRmiPort(Integer.parseInt(configuration.getProperty("rightnode.rmi_port")));
        rightNode.setHeartbeatPort(Integer.parseInt(configuration.getProperty("rightnode.heartbeat_port")));
        rightNode.setRmiBindName(configuration.getProperty("rightnode.rmi_bind_name"));

        leftNode = new NodeDetails();
        leftNode.setIpAddress(configuration.getProperty("leftnode.ip_address"));
        leftNode.setRmiPort(Integer.parseInt(configuration.getProperty("leftnode.rmi_port")));
        leftNode.setHeartbeatPort(Integer.parseInt(configuration.getProperty("leftnode.heartbeat_port")));
        leftNode.setRmiBindName(configuration.getProperty("leftnode.rmi_bind_name"));

    }

    public synchronized NodeStatus getStatus() {
        return status;
    }
    public synchronized void setStatus(NodeStatus status) {
        this.status = status;
    }

    // This method is called from the remote client by the RMI.
    // ReceiveMessageInterface implementation
    @Override
    public boolean lookUp(Date date, String instructorName) throws RemoteException
    {
        //TODO - Question? Should we have this line here instead?
        //increment no of clients
        numOfClients.getAndIncrement();
        
        while(!hasToken.get()) {
            
            System.out.println("Node does not have token, instructor "+instructorName+" request to lookup date "+date+" is waiting...");
            synchronized(lock) {
                try {
                    //wait unconditional until notified
                    lock.wait();
                } catch(InterruptedException exc) {
                    System.out.println("Thread "+Thread.currentThread().getName()+" in lookup is interrupted!");
                }
            }
        }
        
        boolean available = false;
        //critical section, let only one client go thru it
        synchronized(lock) {

            
            System.out.println("Lookup will be done for instructor "+instructorName+" reaquest for date "+date);

            readSchedule();

            if (schedule.contains(date.toString())) {
                System.out.println("Instructor "+instructorName+", requested lookup date "+date+" is not available");
                available = false;
            } else {
                System.out.println("Instructor "+instructorName+", requested lookup date "+date+" is available");
                available = true;
            }
        
        }
        
        numOfClients.getAndDecrement();
        
        //done with critical section, if no more requests, release the token
        if(numOfClients.get() <= 0) {
            System.out.println("No client requests waiting, notifying TokenTimer to release token");
            synchronized(tokenTimerLock) {
                tokenTimerLock.notify();
            }
        }
        
        return available;

    }

    // This method is called from the remote client by the RMI.
    // ReceiveMessageInterface implementation
    @Override
    public boolean reserve(Date date, String instructorName) throws RemoteException
    {

        while(!hasToken.get()) {
            
            System.out.println("Node does not have token, instructor "+instructorName+" request to reserve date "+date+" is waiting...");
            synchronized(lock) {
                try {
                    lock.wait();
                } catch(InterruptedException exc) {
                    System.out.println("Thread "+Thread.currentThread().getName()+" in reserve is interrupted!");
                }
            }
        }
        
        boolean available = false;
        //critical section, let only one client go thru it
        synchronized(lock) {

            //increment no of clients
            numOfClients.getAndIncrement();
            
            System.out.println("reserve will be done for instructor "+instructorName+" reaquest for date "+date);

            if (lookUp(date, instructorName)) {
                writeSchedule(date);
                System.out.println("Instructor "+instructorName+", requested date "+date+" is reserved");
                available = true;
            } else {
                System.out.println("Instructor "+instructorName+", requested date "+date+" is not reserved");
                available = false;
            }
        
        }
        
        numOfClients.getAndDecrement();

        //done with critical section, if no more requests, release the token
        if(numOfClients.get() <= 0) {
            System.out.println("No client requests waiting, notifying TokenTimer to release token");
            synchronized(tokenTimerLock) {
                tokenTimerLock.notify();
            }
        }
        
        return available;

    }

    public void bindToRegistry()
    {		

        try {
            // create the registry and bind the name and object.
            Registry registry = LocateRegistry.createRegistry(thisNode.getRmiPort());
            System.out.println("Creatied rmi registry on the port "+thisNode.getRmiPort());
            registry.rebind(thisNode.getRmiBindName(), this);
            System.out.println("Bound to rmi registry with name "+thisNode.getRmiBindName());

        } catch(RemoteException e) {
            System.out.println("Error binding to registry: "+e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        }	

    }

    private void readSchedule() {
        try {
            File file = new File(scheduleFileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String sCurrentLine;


            while ((sCurrentLine = br.readLine()) != null) {
                schedule.add(sCurrentLine.trim());
            }

            br.close();

        } catch (IOException e) {
            System.out.println("Could not read the Schedule file!");
            e.printStackTrace();
        }
    }

    private void writeSchedule(Date date){
        try{
            String content = "";
            for(int i = 0; i < schedule.size(); i++){
                content += schedule.get(i) + "\n";
            }
            content += date;

            File file = new File(scheduleFileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //send heart beat to the specified node
    private NodeStatus verifyNodeStatus(NodeDetails node) {
        
        //call heartbeat client, verify status after timer expires
        HeartbeatClient client = null;
        NodeStatus status = null;

        for(int count = 1; count <= noHeartbeatTries; ) {

            //System.out.println("Try "+count+": sending heartbeat to node "+node.getRmiBindName());
            client = new HeartbeatClient(node.getIpAddress(), node.getHeartbeatPort(),
                    csExecTimeSec+processDelayTimeSec, node.getRmiBindName());
            status = client.verifyNodeStatus();
            if(status == NodeStatus.faulty || status == null) {
                ++count;
                //System.out.println("Node "+node.getRmiBindName()+" \"possibly\" faulty or not started yet, trying (try"+count+") again to confirm");
                continue;
            } else {
                break;
            }

        }

        return status;

    }

    //NodeInterface implementation
    @Override
    public boolean rejoin(String ipaddress, int port, String rmiBindName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public NodeDetails getRightNeighbor() throws RemoteException {
        return rightNode;
    }
    
    @Override
    public boolean setToken() throws RemoteException {
        
        hasToken.set(true);
        System.out.println("------------Received token from neighbor");
        
        if(numOfClients.get() <= 0) {
            
            System.out.println("No waiting client requests, passing the token to neighbor");
            //set token to false so no client requests will be honored
            //while we send token to the neighbor
            hasToken.set(false);
            
            //no clients waiting, sleep for d2
            Util.sleep(processDelayTimeSec * 1000);
            System.out.println("-------------nodeClient="+nodeClient);
            boolean received = nodeClient.setToken();
            if(received) {
                System.out.println("Successfully passed token to neighbor");
            } else {
                //TODO
                System.out.println("Error passing token to neigbor, will kick off fault detection");
                //retry logic, token creation if needed
            }
        
        }
        
        //TODO start a timer for bounded waiting
        tokenTimer = new TokenTimer(csExecTimeSec);
        tokenTimer.start();
        System.out.println("TokenTimer started");

        synchronized (lock) {
            lock.notifyAll();
        }
        
        return true;
    }
    
    @Override
    public boolean tokenPassed() throws RemoteException {
        return true;
    }

    @Override
    public boolean injectFaultStatus() throws RemoteException {
        setStatus(NodeStatus.faulty);
        // get the registry
        Registry registry = LocateRegistry.getRegistry(thisNode.getIpAddress(), thisNode.getRmiPort());

        return UnicastRemoteObject.unexportObject(registry,true);
    }

    @Override
    public boolean resumeOperation() throws RemoteException {
    	 // get the registry
        Registry registry = LocateRegistry.getRegistry(thisNode.getIpAddress(), thisNode.getRmiPort());

        UnicastRemoteObject.exportObject(registry);  
        return false;
    }
    
    private static void printUsage() {
        System.out.println("Usage: DepartmentNode <configuration file name> <critical sec exc time in sec> <processing delay in sec>");
    }

    public static void main(String args[])
    {
        
        int cs_exec_time_sec = 0;
        int process_delay_sec = 0;

        if (args.length != 3) {
            System.out.println("Configration file not specified!");
            printUsage();
            System.exit(1);
        } else {
            try {
                cs_exec_time_sec = Integer.parseInt(args[1]);
            } catch(NumberFormatException exc) {
                System.out.println("Error in parsing critical section exec time: "+exc.getMessage()+", please specify valid value");
                printUsage();
                System.exit(1);
            }
            try {
                process_delay_sec = Integer.parseInt(args[2]);
            } catch(NumberFormatException exc) {
                System.out.println("Error in parsing processing delay time: "+exc.getMessage()+", please specify valid value");
                printUsage();
                System.exit(1);
            }
        }
        
        if(cs_exec_time_sec == 0 || process_delay_sec == 0) {
            System.out.println("Invalid values (zero) for d1 and/or d2, please specify valid values");
            printUsage();
            System.exit(1);
        }

        try {
        
            String config_file_name = args[0];
            
            File file = new File(config_file_name);
            if(!file.exists()) {
                System.out.println("File "+config_file_name+" not found, please specify correct file name");
                printUsage();
                System.exit(1);
            }

            DepartmentNode dept = new DepartmentNode(config_file_name, cs_exec_time_sec, process_delay_sec);
        
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}//end