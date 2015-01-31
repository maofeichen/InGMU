package pwman;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.crypto.*;
import java.security.*;

public class SecureBlobIO extends BlobIO {
  private final BlobIO io;

  /** parameters for the server side of a connection */
  public static class ServerParams {
    /** read from a file */
    public ServerParams(File f)
      throws IOException,
	     FileNotFoundException,
	     CorruptMessageException
    {
      /* !!! write me */
    }
    
    /** generate anew and write to a file */
    public ServerParams(File f, String password)
      throws IOException,
	     FileNotFoundException
    {
      /* !!! write me */
      write();
    }

    /** write out to a file */
    public void write() throws IOException, FileNotFoundException {
      /* !!! write me */
    }
  }

  /** parameters for the client side of a connection */
  public static class ClientParams {
    /* initialize with a new password */
    public ClientParams(String password) {
      /* !!! write me */
    }

    /* get the master key; only available after handshake */
    public Aes getMaster() {
      /* !!! write me */

      /* !!! dummy code, so compiler won't complain     */
      /* !!! remove for PP1 Milestone #2 implementation */
      return null;
    }
  }

  /** Create a new secure socket on the client side. */
  public SecureBlobIO(BlobIO io,
		      ClientParams params)
    throws IOException,
	   CorruptMessageException
  {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    this.io = io;
  }

  /** Create a new secure socket on the server side. */
  public SecureBlobIO(BlobIO io,
		      ServerParams params)
    throws IOException,
	   CorruptMessageException
  {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    this.io = io;
  }

  /** Marshal, mac and send a packet of data */
  public synchronized void write(byte[] data)
    throws IOException
  {
    /* !!! write me */
  }

  public synchronized void setPassword(String password, boolean clean)
    throws IOException
  {
    /* 
     * If the clean parameter is set, change the master key as well.
     * Currently, nothing calls this with clean, so you can ignore
     * this.
     */

    /* !!! write me */
  }

  /** Receive, check and unmarshal a packet of data */
  public synchronized byte[] read()
    throws IOException, CorruptMessageException
  {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  public void close() {
    io.close();
  }
}
