package pwman;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;

/** The test client. */
public class Test {
  public static void fail(String message) {
    System.err.println("*** FAIL ***");
    System.err.println(message);
    System.exit(1);
  }

  public static void should_equal(String a, String b) throws Exception {
    if (a != b && (a == null || !a.equals(b))) {
      System.err.println("*** FAIL: objects not equal ***");
      System.err.println(a);
      System.err.println(b);
      throw new Exception("Failed"); /* stack trace */
    }
  }

  public static void note(String message) {
    System.out.println(message);
  }

  public static SecureBlobIO connect(final SecureBlobIO.ClientParams cp,
				     final SecureBlobIO.ServerParams sp,
				     final Map<byte[], byte[]> m)
    throws IOException, CorruptMessageException
  {
    final PipedOutputStream outA = new PipedOutputStream();
    final PipedInputStream  inA  = new PipedInputStream(outA);
    final PipedOutputStream outB = new PipedOutputStream();
    final PipedInputStream  inB  = new PipedInputStream(outB);

    new Thread() {
      public void run() {
	try {
	  new NetworkedMapServerThread
	    (new SecureBlobIO(new IOBlobIO(inA, outB), sp), m).run();
	} catch (Exception e) {
	  Debug.warn(e);
	}

      }
    }.start();

    return new SecureBlobIO(new IOBlobIO(inB, outA), cp);
  }

  public static void main(String[] args) throws Exception {
    try {
      Map <byte[], byte[]> m = new FileMap("test");
      m.clear();
      
      note("Setting up the connection");
      SecureBlobIO.ServerParams sp =
	new SecureBlobIO.ServerParams(null, "passw0rd");
      
      SecureBlobIO.ClientParams cp =
	new SecureBlobIO.ClientParams("passw0rd");
      
      SecureBlobIO one = connect(cp, sp, m);
      StringMap m1 = new StringMap(new EncryptedMap(new NetworkedMap(one),
						    cp.getMaster()));
      
      SecureBlobIO.ClientParams cp2 = new SecureBlobIO.ClientParams("passw0rd");
      SecureBlobIO two = connect(cp2, sp, m);
      StringMap m2 = new StringMap(new EncryptedMap(new NetworkedMap(two),
						    cp2.getMaster()));
      
      
      note("Basic testing");
      m1.put("a is for",      "alice");
      m2.put("b is for",      "bob");
      m1.put("c is for",      "carol");
      m2.put("who works for", "the mob");
      
      should_equal(m2.get("b is for"), "bob");
      m1.remove("c is for"); /* busted! */
      should_equal(m1.get("b is for"), "bob");
      should_equal(m1.get("c is for"), null);
      should_equal(m2.get("c is for"), null);
      
      note("Changing the password");
      
      two.setPassword("passw1rd", false);
      
      SecureBlobIO.ClientParams cp3 = new SecureBlobIO.ClientParams("passw1rd");
      SecureBlobIO three = connect(cp3, sp, m);
      StringMap m3 = new StringMap(new EncryptedMap(new NetworkedMap(three),
						    cp3.getMaster()));
      
      note("More tests...");
      m3.put("d is for",          "david");
      m2.put("he doesn't feel",   "fear");
      m1.put("his love notes to", "erin");
      m1.put("are sent in the",   "clear");
      
      should_equal(m1.get("he doesn't feel"), "fear");
      
      m3.put("he doesn't feel",   "pain");
      m3.put("are sent in the",   "plain");
      
      should_equal(m2.get("he doesn't feel"), "pain");
      
      note("Checking wrong password");
      try {
	SecureBlobIO.ClientParams cp4 =
	  new SecureBlobIO.ClientParams("passw0rd");
	SecureBlobIO four = connect(cp4, sp, m);
	fail("You shouldn't be able to log in with the wrong password");
      } catch (CorruptMessageException e) {
	note("Wrong password throws CorruptMessageException");
      } catch (IOException e) {
	note("Wrong password throws IOException");
      }
      
      note("cleaning up");
      m1.clear();
      should_equal(m2.get("he doesn't feel"), null);

      one.close();
      two.close();
      three.close();
      note("*** PASS ***");
    } catch(Exception e) {
      Debug.choke_on(e);
    }
    System.exit(0);
  }
}
