package pwman;

public final class Debug {
  static void choke_on(Exception e) {
    e.printStackTrace();
    System.exit(1);
  }

  static void warn(Exception e) {
    e.printStackTrace();
  }

  static void warn(String s, Exception e) {
    System.err.println("Debug: Warning: ".concat(s));
    e.printStackTrace();
  }

  static void warn(String s) {
    System.err.println(s);
  }

  static void choke(String s) {
    System.err.println(s);
    System.exit(1);
  }  
}