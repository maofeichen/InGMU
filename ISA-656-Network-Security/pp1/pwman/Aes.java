package pwman;

import java.util.Arrays;
import java.nio.charset.Charset;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

/**
 * Utility class; provides easy-to-use, statically-safe wrapper around
 * authenticated AES/CTR in Java crypto library.
 */
public final class Aes {
  public final Hmac hmac;
  
  /**
   * Constructs a new instance of AES with a random key.
   */
  public Aes() {
    this(random_block());
  }

  /**
   * Construct an AES key from an array of bytes.  TODO: throw a
   * useful exception if keybytes are broken.
   */
  public Aes(byte[] key) {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    hmac = null; 
  }

  /**
   * Construct an AES key from a password.
   */
  public Aes(String password) {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    hmac = null;
  }

  /** Creates a new random IV */
  public static byte[] random_block() {
    byte[] out = new byte[16];
    new SecureRandom().nextBytes(out);
    return out;
  }

  /**
   * Encrypt a message with AES.
   *
   * @param message the message to encrypt
   * @return the ciphertext
   */
  public byte[] encrypt(byte[] message) {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  /**
   * Decrypt a message with AES.
   *
   * @param ciphertext the ciphertext to decrypt
   * @return the plaintext
   * @throws CorruptMessageException if the ciphertext is too short to
   * have an IV
   */
  public byte[] decrypt(byte[] message_)
    throws CorruptMessageException
  {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  /**
   * Decrypt a message with AES, returning null if an error occurs.
   *
   * @param ciphertext the ciphertext to decrypt
   * @return the plaintext, or null if it doesn't decrypt cleanly.
   */
  public byte[] tryDecrypt(byte[] ciphertext) {
    if (ciphertext == null) return null;
    try {
      return decrypt(ciphertext);
    } catch (CorruptMessageException e) {
      Debug.warn(e);
      return null;
    }
  }

  /**
   * Encrypt an AES key with AES.
   *
   * @param wrappee the AES instance to wrap
   * @return the wrapped key
   */
  public byte[] wrapAes(Aes wrappee) {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  /**
   * Decrypt an AES key which has been encrypted with wrapAes().
   *
   * @param wrapped the AES instance to unwrap
   * @return an AES instance initialized with the unwrapped key
   */
  public Aes unwrapAes(byte[] wrapped) throws CorruptMessageException {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  /**
   * Encrypt a MAC key with AES.
   *
   * @param wrappee the MAC instance to wrap
   * @return the wrapped key
   */
  public byte[] wrapMac(Hmac wrappee) {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }

  /**
   * Decrypt a MAC key which has been encrypted with wrapMac().
   *
   * @param wrapped the MAC instance to unwrap
   * @return a MAC instance initialized with the unwrapped key
   */
  public Hmac unwrapMac(byte[] wrapped) throws CorruptMessageException {
    /* !!! write me */

    /* !!! dummy code, so compiler won't complain     */
    /* !!! remove for PP1 Milestone #2 implementation */
    return null;
  }
}
