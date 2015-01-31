package pwman;

import java.util.*;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;

import java.io.*;
import java.net.*;

/**
 * Networked map server.  This class implements the back-end for the
 * networked map object.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class NetworkedMapServer extends Thread {
  public static int DEFAULT_PORT = 2550;
  private final Map<byte[],byte[]> backer;
  private final ServerSocket socket;
  private final SecureBlobIO.ServerParams ssParams;

  /**
   * Create a new server, backed by the given blob map, which listens
   * on the given port number.
   */
  public NetworkedMapServer(int port,
			    Map<byte[],byte[]> backer,
			    SecureBlobIO.ServerParams ssParams)
    throws IOException,
	   SecurityException
  {
    this.socket   = new ServerSocket(port);
    this.backer   = backer;
    this.ssParams = ssParams;
  }

  /**
   * Run the server in the current thread.  To run it in a new thread,
   * call start().
   */
  public void run() {
    try {
      while(true) {
	Socket clientSock = socket.accept();
	try {
	  SecureBlobIO ss = new SecureBlobIO(new IOBlobIO(clientSock),
					     ssParams);
	  new NetworkedMapServerThread(ss,backer).start();
	} catch (Exception e) {
	  Debug.warn(e);
	}
      }
    } catch (IOException e) {
      Debug.warn(e);
    }
  }

  public static void main(String[] args) throws Exception {
    SecureBlobIO.ServerParams sp = null;
    FileMap fm = new FileMap("net_test");
    File f = new File("net_test/ssparams");
    try {
      sp = new SecureBlobIO.ServerParams(new File("net_test/ssparams"));
    } catch (FileNotFoundException e) {
      Debug.warn("Setting password to 'passw0rd'");
      sp = new SecureBlobIO.ServerParams(new File("net_test/ssparams"),
					 "passw0rd");
    }

    new NetworkedMapServer(DEFAULT_PORT, fm, sp).run();
  }
}
