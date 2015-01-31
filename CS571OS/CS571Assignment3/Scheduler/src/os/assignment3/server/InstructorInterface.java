package os.assignment3.server;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

public interface InstructorInterface extends Remote
{
	boolean lookUp(Date date, String instructorName) throws RemoteException;
	boolean reserve(Date date, String instructorName) throws RemoteException;
}