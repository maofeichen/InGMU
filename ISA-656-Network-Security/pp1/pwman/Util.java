package pwman;

import pwman.Debug;
import pwman.CorruptMessageException;
import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Arrays;

public final class Util {
  public static final int MAX_MESSAGE_LENGTH = 10000000; // ten million bytes

  public static byte[] cat(byte[][] bss) {
    int len = 0;
    for (byte[] bs : bss)
      len += bs.length;

    byte[] out = new byte[len];
    int off = 0;
    for (byte[] bs : bss) {
      for (byte b : bs) {
	out[off] = b;
	off++;
      }
    }
    return out;
  }

  public static byte[] cat(byte[] a, byte[] b) {
    byte[][] ab = {a,b};
    return cat(ab);
  }

  public static byte[] cat(byte[] a, byte[] b, byte[] c) {
    byte[][] abc = {a,b,c};
    return cat(abc);
  }

  public static String hex(byte[] bytes) {
    char out[] = new char[bytes.length * 2];

    for (int i=0; i<bytes.length; i++) {
      out[2*i  ] = Character.forDigit((bytes[i] >> 4) & 15, 16);
      out[2*i+1] = Character.forDigit(bytes[i] & 15, 16);
    }

    return new String(out);
  }

  public static byte[] unhex(String hexels) {
    int len = hexels.length();
    if (len%2 != 0) return null;
    byte out[] = new byte[len/2];

    for (int i=0; i<len/2; i++) {
      int bhi = Character.digit(hexels.charAt(2*i  ), 16);
      int blo = Character.digit(hexels.charAt(2*i+1), 16);
      if (bhi < 0 || blo < 0) return null;
      out[i] = (byte) (bhi << 4 | blo);
    }
    return out;
  }
}
