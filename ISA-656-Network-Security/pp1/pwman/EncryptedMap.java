package pwman;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;

import javax.crypto.*;
import java.security.*;

/**
 * Encrypted map from blobs to blobs, backed by another such map.
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class EncryptedMap extends AbstractMap<byte[],byte[]> {
  private final Map<byte[],byte[]> backer;
  private final Aes cipher;

  /**
   * Create a new encrypted map.
   *
   * @param backer a plaintext map backing this one
   * @param cipher the cipher with which to encrypt the map
   */
  public EncryptedMap(Map<byte[],byte[]> backer, Aes cipher) {
    this.backer = backer;
    this.cipher = cipher;
  }

  /**
   * Get all the entries of the encrypted map.  You probably don't
   * really want to call this, but it's required by the interface.
   */
  public HashSet<Entry<byte[],byte[]>> entrySet() {
    HashSet<Entry<byte[],byte[]>> out = new HashSet<Entry<byte[],byte[]>>();
    
    Set<Entry<byte[],byte[]>> back = backer.entrySet();
    if (back == null) return null;

    try {
      for (Entry<byte[],byte[]> e : back) {
	out.add(new SimpleImmutableEntry<byte[],byte[]>(
			 cipher.decrypt(e.getKey()),
			 cipher.decrypt(e.getValue())));
      }

      return out;
    } catch (CorruptMessageException e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Get all the keys of the map.
   * @return a set of keys, or null if a decryption error occurs
   */
  public HashSet<byte[]> keySet() {
    HashSet<byte[]> out = new HashSet<byte[]>();
    
    Set<byte[]> back = backer.keySet();
    if (back == null) return null;

    for (byte[] e : back) {
      // out.add(cipher.decrypt(e));
      out.add(e);
    }
    
    return out;
  }

  /**
   * Get the value for a particular key.
   *
   * @param key the map key
   * @return the value for that key, or null if the key isn't in the
   * map, or if an error occurs
   */
  public byte[] get(Object key_) {
    byte[] key = (byte[]) key_;
    return cipher.tryDecrypt(backer.get(key));
  }

  /**
   * Remove all entries in the map.
   */
  public void clear() {
    backer.clear();
  }

  /**
   * Set the value for a particular key.
   *
   * @param key the map key to set
   * @param value the new value
   * @return the value for that key, or null if the key wasn't in the
   * map, or if an error occurs
   */
  public byte[] put(byte[] key, byte[] value) {
    return cipher.tryDecrypt(backer.put(key,
					cipher.encrypt(value)));
  }

  /**
   * Remove the given key.
   *
   * @param key the map key to remove
   * @return the value for that key, or null if the key wasn't in the
   * map, or if an error occurs
   */
  public byte[] remove(Object key_) {
    byte[] key = (byte[]) key_; /* whiskey tango foxtrot */
    return cipher.tryDecrypt(backer.remove(key));
  }
}
