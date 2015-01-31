package pwman;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.security.SecureRandom;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.math.BigInteger;

import pwman.CorruptMessageException;
import pwman.Debug;
import pwman.Util;
import pwman.FileBlobIO;

/**
 * Local filesystem implementation of a map from blobs to blobs.
 * We implement it this way for simplicity of testing, for
 * interoperability with other tools (such as version-control and
 * backup systems) and so that if the password manager ever breaks
 * badly, passwords can be restored.
 * <p>
 * This storage manager is thread-safe, but is NOT transactionally
 * consistent with respect to other FileMap objects using the same
 * directory.  This would require a locking protocol, which I don't
 * really feel like implementing.
 * <p>
 * Furthermore, it silently returns wrong answers on hash collisions.
 * Since it uses a hash function which is 2-universal, these should be
 * extremely rare, but this is still a HACK.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class FileMap extends AbstractMap<byte[],byte[]> {
  private final File containingDirectory;
  private final BigInteger modulus;
  private static final String fileNameRegex
    = "[0-9a-zA-Z]+.pair";
  
  /**
   * Opens a new FileMap directory, creating it if it
   * doesn't exist.
   *
   * @param containingDirectory the directory to open or create
   * @throws IOException        if an IO error occurs
   */
  public FileMap(String containingDirectory)
    throws IOException {
    this(new File(containingDirectory));
  }

  /**
   * Opens a new FileMap directory, creating it if it
   * doesn't exist.
   *
   * @param containingDirectory the directory to open or create
   * @throws IOException        if an IO error occurs
   */
  public FileMap(File containingDirectory)
    throws IOException {
    this.containingDirectory = containingDirectory;
    
    if (!containingDirectory.exists()) {
      if (!containingDirectory.mkdir())
	throw new IOException("Can't create containing directory");
    } else if (!containingDirectory.isDirectory()) {
      throw new IOException("Not a directory");
    }

    File modulusFile = new File(containingDirectory, "modulus");
    byte[] bytesMod = null;

    BigInteger modulus_ = null;

    try {

      bytesMod = new FileBlobIO(modulusFile).read();
      modulus_ = new BigInteger(bytesMod);

    } catch (IOException e) {

      // roll up a new prime
      modulus_ = new BigInteger(160, 10, new SecureRandom());
      bytesMod = modulus_.toByteArray();
      new FileBlobIO(modulusFile).write(bytesMod);

    } catch (CorruptMessageException e) {
      throw new IOException("Can't decode modulus file");
    }

    modulus = modulus_;
  }

  /**
   * Open a file, read out the first two lines from it, close it
   * again.
   *
   * @return A key,value pair, or null if this fails for any reason
   */
  private SimpleImmutableEntry<byte[],byte[]> grab(File f) {
    try {

      FileBlobIO io = new FileBlobIO(f);

      byte[][] kv = io.reads();
      if (kv.length != 2)
	throw new CorruptMessageException("grab length != 2");
      
      return new SimpleImmutableEntry<byte[],byte[]>(kv[0], kv[1]);

    } catch (FileNotFoundException e) {
      // normal; fall through
    } catch (IOException e) {
      Debug.warn("grab",e);
    } catch (CorruptMessageException e) {
      Debug.warn("grab",e);
    }

    return null;
  }
  
  /**
   * Hash a key string, by returning its value modulo Modulus.  This
   * is not a cryptographically secure hash function, but it is
   * 2-universal, so collisions should be exceedingly rare.  In fact,
   * because this library is a HACK, we assume that collisions don't
   * ever happen at all.
   * <p>
   * Encodes the string using UTF-8.
   *
   * @param key the string to be hashed
   * @return a 160-bit hex-coded string (i.e. at most 40 characters)
   */
  private String hash(byte[] key) {
    try {
      // set the top bit so that leading zeros are figured in
      BigInteger bi_key = new BigInteger(key).setBit(key.length * 8);
      return bi_key.mod(modulus).toString(36);
    } catch (ArithmeticException e) {
      Debug.choke_on(e);
      /* don't */ return null;
    }
  }

  /**
   * Determine the name of the file which should be used to store the
   * given key.
   *
   * @param key the key in the dictionary
   * @return the name of the file to store
   */
  private File fileFor(byte[] key) {
    return new File(containingDirectory, hash(key).concat(".pair"));
  }

  /**
   * AbstractMap interface.
   *
   * @return The set of all entries in the map.
   */
  public synchronized HashSet<Entry<byte[],byte[]>> entrySet() {
    HashSet<Entry<byte[],byte[]>> out
      = new HashSet<Entry<byte[],byte[]>>();
    
    File[] contents = containingDirectory.listFiles();
    if (out == null) return null;
    for (int i=0; i < contents.length; i++) {
      if (!Pattern.matches(fileNameRegex, contents[i].getName()))
	continue;
      
      Entry<byte[],byte[]> en = grab(contents[i]);
      if (en != null)
	out.add(en);
    }
    
    return out;
  }
  
  /**
   * AbstractMap interface: look up a key.
   *
   * @param key the key to look up
   * @return the associated value, or null if it isn't in the map
   */
  public synchronized byte[] get(Object key_) {
    byte[] key = (byte[]) key_;
    Entry<byte[],byte[]> ent = grab(fileFor(key));

    if (ent != null && Arrays.equals(ent.getKey(), key))
      return ent.getValue();
    else
      return null;
  }
  
  /**
   * AbstractMap interface: delete a key from the map.
   *
   * @param key the key to delete.
   */
  public synchronized byte[] remove(Object key_) {
    byte[] key = (byte[]) key_;
    byte[] out = get(key);
    try {
      fileFor(key).delete();
    } catch (SecurityException e) {
      Debug.choke_on(e);
    }
    return out;
  }

  /**
   * Clear the map.
   */
  public synchronized void clear() {
    for (byte[] k : keySet()) {
      remove(k);
    }
  }
  
  /**
   * AbstractMap interface: put a key in the map.
   *
   * @param key the key to put in the map
   * @param value the value to put in the map
   */
  public synchronized byte[] put(byte[] key, byte[] value) {
    byte[] out = get(key);
    byte[][] wr = { key, value };
    try {
      new FileBlobIO(fileFor(key)).writes(wr);
    } catch (IOException e) {
      Debug.warn(e);
    }
    return out;
  }
}
