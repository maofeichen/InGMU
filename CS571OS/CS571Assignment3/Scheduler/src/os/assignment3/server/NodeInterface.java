package os.assignment3.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * Interface for node-to-node communication. Token passing,
 * topology messages etc.
 *
 */
public interface NodeInterface extends Remote {

    /**
     * A node returns the right neighbor node to the caller. 
     * 
     * @return
     * @throws RemoteException
     */
    public NodeDetails getRightNeighbor() throws RemoteException;
    
    /**
     * A node receives the token and returns true to acknowledge receipt of the token.
     * @return
     */
    public boolean setToken() throws RemoteException;
    
    /**
     * A node is informed by its right node that it passed the token to its right node.
     * This node is now relieved of the token responsibility.
     * 
     * A node, after passing the token to its next neighbor, calls this API
     * on the previous node to inform that token is passed on. The previous node
     * then is relieved off the responsibility. 
     * 
     * @return
     * @throws RemoteException
     */
    public boolean tokenPassed() throws RemoteException;

    /**
     * 
     * A rejoining node request another node to re-join the ring.
     * Node should return true if it can allow the caller node to re-join.
     * 
     * @param ipaddress
     * @param port
     * @param rmiBindName
     * @return
     * @throws RemoteException
     */
    public boolean rejoin(String ipaddress, int port, String rmiBindName) throws RemoteException;

}//end