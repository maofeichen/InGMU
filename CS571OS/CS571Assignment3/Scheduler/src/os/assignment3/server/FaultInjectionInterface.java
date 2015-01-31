package os.assignment3.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A client program can request DepartmentNode to go into
 * fault fault state. A client can also resume server from fault state
 * into regular state.
 * 
 * @author ychandol
 *
 */
public interface FaultInjectionInterface extends Remote {

    /**
     * Un-conditionally sets department node into fault state.
     * 
     * @return
     * @throws RemoteException
     */
    public boolean injectFaultStatus() throws RemoteException;
    
    /**
     * Resumes node from fault state into regular state. 
     *  
     * @return
     * @throws RemoteException
     */
    public boolean resumeOperation() throws RemoteException;

}//end