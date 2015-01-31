package pwman;

import java.io.*;
import java.net.*;

/**
 * General class for input and output with length-encoded arrays of
 * bytes, using an InputStream and an OutputStream.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class IOBlobIO extends BlobIO {
  private final InputStream  is;
  private final OutputStream os;

  /** Tuple constructor */
  public IOBlobIO(InputStream is, OutputStream os) {
    this.is = is;
    this.os = os;
  }

  /** Socket constructor */
  public IOBlobIO(Socket sock)
    throws IOException
  {
    this(sock.getInputStream(), sock.getOutputStream());
  }

  /**
   * Write a single blob.
   */
  public void write(byte[] x)
    throws IOException
  {
    os.write(lengthEncode(x));
  }

  /**
   * Read a single blob.
   */
  public byte[] read()
    throws IOException,
	   CorruptMessageException
  { 
    return lengthDecode(is);
  }

  public void close() {
    try {
      is.close();
      os.close();
    } catch (IOException e) {
      Debug.warn(e);
    }
  }
}
