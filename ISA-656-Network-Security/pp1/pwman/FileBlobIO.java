package pwman;

import java.io.*;


/**
 * General class for input and output with length-encoded arrays of
 * bytes, using the filesystem.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class FileBlobIO extends BlobIO {
  private final File f;

  /** Singleton constructor */
  public FileBlobIO(File f) {
    this.f = f;
  }

  /**
   * Write a single blob, by clobbering the file.
   */
  public void write(byte[] x)
    throws IOException
  {
    try {

      File tmp = File.createTempFile("fbio-",".tmp",f.getParentFile());
      FileOutputStream os = new FileOutputStream(tmp);
      
      os.write(lengthEncode(x));
      os.flush();
      os.close();

      if (!tmp.renameTo(f)) {
	tmp.delete();
	throw new IOException("can't rename");
      }

    } catch (SecurityException e) {
      Debug.choke_on(e);
    } catch (FileNotFoundException e) {
      throw new IOException ("file not found");
    }
  }

  /**
   * Read a single blob.
   */
  public byte[] read()
    throws IOException,
	   CorruptMessageException
  {
    FileInputStream fis = new FileInputStream(f);
    byte[] x = null;
    try {
      x = lengthDecode(fis);
    } catch (SecurityException e) {
      Debug.choke_on(e);
    } catch (FileNotFoundException e) {
      throw new IOException ("file not found");
    } finally {
      fis.close();
    }
    return x;
  }

}