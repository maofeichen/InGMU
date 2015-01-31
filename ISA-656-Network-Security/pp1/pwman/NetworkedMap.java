package pwman;

import java.util.*;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleImmutableEntry;

import java.io.*;

import pwman.Util;
import pwman.SecureBlobIO;
import pwman.Debug;

/**
 * Networked map from blobs to blobs.  This class implements a map by
 * sending and receiving commands (as arrays of blobs) through a
 * SecureBlobIO connection.  It is locked while any operation is in
 * progress, so it is thread-safe but does not support multiple
 * actions at a given time (clearly, there is room for improvement
 * here).
 * <p>
 * You shouldn't need to change anything in this class for the cs255
 * programming project.
 */
public class NetworkedMap extends AbstractMap<byte[],byte[]> {
  static final byte TAG_ENTRY_SET = 0;
  static final byte TAG_KEY_SET   = 1;
  static final byte TAG_GET       = 2;
  static final byte TAG_PUT       = 3;
  static final byte TAG_REMOVE    = 4;
  static final byte TAG_CLEAR     = 5;

  private final SecureBlobIO socket;

  /**
   * Create a new networked map.
   *
   * @param socket the secure socket to send the commands.
   */
  public NetworkedMap(SecureBlobIO socket) {
    this.socket = socket;
  }

  /**
   * Get all the entries of the map.
   * You probably don't really want to call this, but it's required by the interface.
   */
  public synchronized HashSet<Entry<byte[],byte[]>> entrySet() {
    HashSet<Entry<byte[],byte[]>> out = new HashSet<Entry<byte[],byte[]>>();
    
    try {

      byte[][] mess = {{TAG_ENTRY_SET}};
      socket.writes(mess);

      byte[][] answer = socket.reads();
      if (answer.length % 2 != 0)
	throw new CorruptMessageException("Odd length answer in entrySet()");

      for (int i=0; i<answer.length/2; i++) {
	out.add(new SimpleImmutableEntry<byte[],byte[]>(answer[2*i],answer[2*i+1]));
      }
      return out;

    } catch (Exception e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Get all the keys of the map.
   * @return a set of keys, or null if a network error occurs
   */
  public synchronized HashSet<byte[]> keySet() {
    HashSet<byte[]> out = new HashSet<byte[]>();

    try {

      byte[][] mess = {{TAG_KEY_SET}};
      socket.writes(mess);

      byte[][] answer = socket.reads();
      for (byte[] ans : answer) {
	out.add(ans);
      }
      return out;

    } catch (Exception e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Slurp an answer off the network and return it.
   */
  private byte[] answer()
    throws CorruptMessageException, IOException
  {
    byte[][] answer = socket.reads();
    if (answer.length == 0)
      return null;
    else if (answer.length == 1)
      return answer[0];
    else
      throw new CorruptMessageException("Multipart answer");
  }

  /**
   * Remove all the entries in the map.
   */
  public synchronized void clear() {
    try {
      byte[][] mess = {{TAG_CLEAR}};
      socket.writes(mess);
      answer(); /* for synchronization */
    } catch (Exception e) {
      Debug.warn(e);
    }
  }


  /**
   * Get the value for a particular key.
   *
   * @param key the map key
   * @return the value for that key, or null if the key isn't in the
   * map, or if an error occurs
   */
  public synchronized byte[] get(Object key_) {
    byte[] key = (byte[]) key_;
    try {
      
      byte[][] mess = {{TAG_GET}, key};
      socket.writes(mess);
      return answer();

    } catch (Exception e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Set the value for a particular key.
   *
   * @param key the map key to set
   * @param value the new value
   * @return the value for that key, or null if the key wasn't in the
   * map, or if an error occurs
   */
  public synchronized byte[] put(byte[] key, byte[] value) {
    try {

      byte[][] mess = {{TAG_PUT}, key, value};
      socket.writes(mess);
      return answer();

    } catch (Exception e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Remove the given key.
   *
   * @param key the map key to remove
   * @return the value for that key, or null if the key wasn't in the
   * map, or if an error occurs
   */
  public synchronized byte[] remove(Object key_) {
    byte[] key = (byte[]) key_;
    try {

      byte[][] mess = {{TAG_REMOVE}, key};
      socket.writes(mess);
      return answer();

    } catch (Exception e) {
      /* this method is not allowed to throw */
      Debug.warn(e);
      return null;
    }
  }  
}
