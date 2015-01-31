package pwman;

import java.util.Arrays;
import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class; provides easy-to-use, statically-safe wrapper around
 * HmacSHA1 in Java crypto library.
 */
public final class Hmac {
  private static final Object mutex = new Object();
  private static KeyGenerator kg = null;
  final byte[] key;

  /**
   * Constructs a new Hmac instance with a random key.
   */
  public Hmac() {
    this(Aes.random_block());
  }
  
  /**
   * Construct an HMAC key from an array of bytes.  TODO: throw a
   * useful exception if keybytes are broken.
   */
  public Hmac(byte[] key) {
    this.key = key;
  }

  /**
   * Computes the mac of the given message, appends it, and returns in
   * a new buffer.
   *
   * @param  message the mesage to be mac'd
   * @return a buffer with the message and its mac
   */
  public byte[] mac(byte message[]) {
    return mac(message, new byte[0][]);
  }

  /**
   * Computes the mac of the given message and returns in a new
   * buffer.
   *
   * @param  message the mesage to be mac'd
   * @return a buffer with the message and its mac
   */
  public byte[] getmac(byte message[]) {
    return getmac(message, new byte[0][]);
  }

  /**
   * Computes the mac of the given message and associated data,
   * appends it, and returns in a new buffer.
   *
   * @param  message the mesage to be mac'd
   * @param  assoc some associated data
   * @return a buffer with the message and its mac
   */
  public byte[] mac(byte message[], byte[][] assoc) {
    byte vmac[] = getmac(message, assoc);
    byte out[] = Arrays.copyOf(message,
			       message.length + vmac.length);
    for (int i=0; i<vmac.length; i++) {
      out[i+message.length] = vmac[i];
    }
    return out;
  }

  /**
   * Computes the mac of the given message and associated data and
   * returns in a new buffer.
   *
   * @param  message the mesage to be mac'd
   * @param  assoc some associated data
   * @return a buffer with the message and its mac
   */
  public byte[] getmac(byte message[], byte[][] assoc) {
    try {

      Mac hmac = Mac.getInstance("HmacSHA1");
      hmac.init(new SecretKeySpec(key, "HmacSHA1"));
      for (byte [] as : assoc) {
	hmac.update(as);
      }
      hmac.update(message);
      return hmac.doFinal();

    } catch (Exception e) {
      Debug.choke_on(e);
      return null;
    }
  }

  /**
   * Checks and strips the mac off a message created with mac().
   *
   * @param message the message to be unmac'd
   * @param assoc some associated data
   * @return the message with its mac stripped off
   * @throws CorruptMessageException if the message is too short, or
   * if the macs don't match.
   */
  public byte[] unmac(byte message[], byte[][] assoc)
    throws CorruptMessageException
  {
    if (message.length < 20)
      throw new CorruptMessageException("message too short");

    byte vmessage[] = Arrays.copyOf(message,
				    message.length - 20);
    byte vmac[]     = getmac(vmessage, assoc);

    for (int i=0; i<vmac.length; i++) {
      if (message[i+vmessage.length] != vmac[i])
	throw new CorruptMessageException("mac failed!");
    }
    return vmessage;
  }

  /**
   * Checks and strips the mac off a message created with mac().
   *
   * @param message the message to be unmac'd
   * @return the message with its mac stripped off
   * @throws CorruptMessageException if the message is too short, or
   * if the macs don't match.
   */
  public byte[] unmac(byte message[])
    throws CorruptMessageException
  {
    return unmac(message, new byte[0][]);
  }
}

