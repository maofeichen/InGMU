package pwman;

public class CorruptMessageException extends Exception {
  public CorruptMessageException () { }
  public CorruptMessageException (String s) { super(s); }
}