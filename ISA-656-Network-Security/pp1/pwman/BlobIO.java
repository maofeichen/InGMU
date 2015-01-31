package pwman;

import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.util.LinkedList;
import java.util.Arrays;

/**
 * General class for input and output with length-encoded arrays of
 * bytes.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public abstract class BlobIO {
  public static final int MAX_MESSAGE_LENGTH = 10000000; // ten million bytes

  /**
   * Write an array of blobs.
   */
  public void writes(byte[][] xs)
    throws IOException
  {
    write(polyLengthEncode(xs));
  }

  /**
   * Write a single blob.
   */
  public abstract void write(byte[] x)
    throws IOException;

  /**
   * Read an array of blobs.
   */
  public byte[][] reads()
    throws IOException,
	   CorruptMessageException
  {
    byte[] x = read();
    return polyLengthDecode(x);
  }

  /**
   * Read a single blob.
   */
  public abstract byte[] read()
    throws IOException,
	   CorruptMessageException;

  /**
   * Encode an int into binary data at a given offset.
   */
  protected static void intEncode(byte[] data, int offset, int x) {
    for (int i=offset; i<offset+4; i++) {
      data[i] = (byte) (x & 0xFF);
      x = x >> 8;
    }
  }

  /**
   * Encode an int from binary data at a given offset.
   */
  protected static int intDecode(byte[] data, int offset) 
    throws CorruptMessageException
  {

    if (data == null || offset + 4 > data.length)
      throw new CorruptMessageException("buffer too short");

    /* decode the length */
    int len = 0;
    for (int i=offset + 3; i>=offset; i--) {
      len = (len << 8) | (((int) data[i]) & 0xFF);
    }

    if (len < 0 || len > MAX_MESSAGE_LENGTH)
      throw new CorruptMessageException("message length out of bounds");

    return len;
  }

  /**
   * Encode a byte array with its length.
   */
  protected static byte[] lengthEncode(byte[] message) {
    int len = message.length, x=len;

    byte[] out = new byte[4+len];
    intEncode(out, 0, x);
    for (int i=0; i<len; i++) {
      out[i+4] = message[i];
    }
    return out;
  }

  /**
   * An implementation for read[].
   */
  protected static byte[] lengthDecode(InputStream data)
    throws IOException, CorruptMessageException
  {
    byte len_buffer[] = reallyRead(data, 4);
    if (len_buffer == null) return null;
    return reallyRead(data, intDecode(len_buffer, 0));
  }

  /**
   * Encode an array of blobs by their lengths.
   */
  protected static byte[] polyLengthEncode(byte[][] data) {
    int len = 0;
    for (byte[] datum : data) {
      len += 4+datum.length;
    }

    byte[] out = new byte[len];
    int offset = 0;
    for (int i=0; i<data.length; i++) {
      intEncode(out, offset, data[i].length);
      offset += 4;
      for (int j=0; j<data[i].length; j++, offset++) {
	out[offset] = data[i][j];
      }
    }

    return out;
  }

  /**
   * Decode an array of bytes by their lengths.
   */
  protected static byte[][] polyLengthDecode(byte[] data)
    throws CorruptMessageException
  {
    LinkedList<byte[]> list = new LinkedList<byte[]>();
    int offset = 0;

    while (offset < data.length) {

      /* grab the length */
      int sublen = intDecode(data, offset);
      offset += 4;

      /* sanity check */
      if (offset + sublen > data.length)
	throw new CorruptMessageException("buffer overrun");
	
      /* copy it over */
      list.add(Arrays.copyOfRange(data, offset, offset + sublen));
      offset += sublen;
    }
    
    byte[][] out = new byte[list.size()][];
    int i = 0;
    for (byte[] bs : list) {
      out[i] = bs;
      i++;
    }
      
    return out;
  }

  /**
   * Read from an input stream until the specified number of
   * characters have been read, or an exception occurs.
   */
  protected static byte[] reallyRead(InputStream data, int len)
    throws IOException
  {
    int read = 0;
    byte out[] = new byte[len];
    while (read < len) {
      int v = data.read(out, read, len-read);
      if (v <= 0)
	throw new EOFException();

      read += v;
    }
    return out;
  }

  /**
   * Close this BlobIo.
   */
  public void close() {
  }
}
