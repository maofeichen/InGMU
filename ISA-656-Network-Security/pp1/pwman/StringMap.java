package pwman;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import pwman.Debug;

/**
 * Map from Strings to Strings, based on a given map from blobs to
 * blobs.  Uses UTF-8 encoding, though this is easily changeable.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class StringMap extends AbstractMap<String,String> {
  private final Map<byte[], byte[]> backer;
  private final Charset charset;

  /** Convenience: encode with the map's charset, which is currently UTF-8 */
  private byte[] encode(String s) {
    if (s == null) return null;
    return charset.encode(s).array();
  }

  /** Convenience: decode with the map's charset, which is currently UTF-8 */
  private String decode(byte[] s) {
    if (s == null) return null;
    return charset.decode(ByteBuffer.wrap(s)).toString();
  }

  /** Create a new StringMap, backed by the given ByteMap, using a
   * charset of UTF-8
   */
  public StringMap(Map<byte[],byte[]> backer) {
    this.backer = backer;

    Charset charset_ = null;
    try {
      charset_ = Charset.forName("UTF-8");
    } catch (Exception e) {
      /* choke on:
       * IllegalCharsetNameException (can't happen)
       * IllegalArgumentException    (can't happen)
       * UnsupportedCharsetException (really, really shouldn't happen)
       */
      Debug.choke_on(e);
    }
    charset = charset_;
  }

  /**
   * Return the map's entries.  Required, but in this application,
   * seldom used.
   */
  public Set<Entry<String,String>> entrySet() {
    Set<Entry<String,String>> out =
      new HashSet<Entry<String,String>>();

    Set<Entry<byte[],byte[]>> back = backer.entrySet();
    for (Entry<byte[],byte[]> e : back) {
      out.add(new SimpleImmutableEntry<String,String>
	      (decode(e.getKey()), decode(e.getValue())));
    }

    return out;
  }

  /**
   * Return the map's keys.
   */
  public TreeSet<String> keySet() {
    TreeSet<String> out = new TreeSet<String>();
    Set<byte[]> back = backer.keySet();
    for (byte[] e : back) {
      out.add(decode(e));
    }
    return out;
  }

  /**
   * Clear the set.
   */
  public void clear() {
    backer.clear();
  }


  /**
   * Remove a value from the map.  Return the previous value, if there
   * was any.
   */
  public String remove(Object key_) {
    String key = (String) key_; /* worse than failure */
    return decode(backer.remove(encode(key)));
  }

  /**
   * Retrieve a value from the map.  Return null if the given key is
   * not in the map.
   */
  public String get(Object key_) {
    String key = (String) key_;
    return decode(backer.get(encode(key)));
  }

  /**
   * Add or change a value in the map.  Return the previous value, if
   * there was any.
   */
  public String put(String key, String value) {
    return decode(backer.put(encode(key), encode(value)));
  }
}
