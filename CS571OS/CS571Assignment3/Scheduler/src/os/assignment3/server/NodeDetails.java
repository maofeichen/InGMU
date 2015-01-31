package os.assignment3.server;

import java.io.Serializable;

public class NodeDetails implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    String rmiBindName;
    String ipAddress;
    int rmiPort;
    int heartbeatPort;

    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getRmiPort() {
        return rmiPort;
    }
    public void setRmiPort(int rmiPort) {
        this.rmiPort = rmiPort;
    }

    public int getHeartbeatPort() {
        return heartbeatPort;
    }
    public void setHeartbeatPort(int heartbeatPort) {
        this.heartbeatPort = heartbeatPort;
    }

    public String getRmiBindName() {
        return rmiBindName;
    }
    public void setRmiBindName(String rmiBindName) {
        this.rmiBindName = rmiBindName;
    }

    public boolean equals(Object obj) {

        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        NodeDetails node = (NodeDetails)obj;
        if(this.rmiPort == node.getRmiPort()
                && this.heartbeatPort == node.getHeartbeatPort()
                && this.ipAddress.equals(node.getIpAddress())
                && this.rmiBindName.equals(node.getRmiBindName())) {
            return true;
        }

        return false;

    }
    
    public int hashCode() {
    
        int result = 1;
        
        result = 31 * result + rmiPort;
        result = 31 * result + heartbeatPort;
        result = 31 * result + ipAddress.hashCode();
        result = 31 * result + rmiBindName.hashCode();
        
        return result;
    
    }
    
    public String toString() {
        
        StringBuilder sb = new StringBuilder(48);
        
        sb.append("[");
        
        sb.append("ipAddress=");
        sb.append(ipAddress);
        sb.append(", ");
        
        sb.append("rmiPort=");
        sb.append(rmiPort);
        sb.append(", ");
        
        sb.append("rmiBindName=");
        sb.append(rmiBindName);
        sb.append(", ");
        
        sb.append("heartbeatPort=");
        sb.append(heartbeatPort);
        
        sb.append("]");
        
        return sb.toString();
    
    }

}//end