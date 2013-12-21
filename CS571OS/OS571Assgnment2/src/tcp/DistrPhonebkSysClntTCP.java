/**
 * CS571 OS Assignment 2
 * Maofei Chen
 * G00709508
 */
package tcp;
import java.net.*;
import java.util.Scanner;
import java.io.*;

/**
 * @author mchen
 * Distributed Phonebook System Client TCP version
 */
public class DistrPhonebkSysClntTCP {
	static final String HOST_NAME = "localhost";
	static int int_clntPort;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		if (args.length != 1 ){
			System.err.print("Client command arguments error");
			System.exit(1);
		}
		
		int_clntPort = Integer.parseInt(args[0]);
		
		try{
			Socket sk_clnt = new Socket(HOST_NAME, int_clntPort);
			System.out.println("Client connects to server "
					+ "at " + HOST_NAME 
					+ " on " + int_clntPort);
			try{
				PrintWriter outToServ = new PrintWriter( sk_clnt.getOutputStream(), true);
				BufferedReader inFromServ = new BufferedReader(
					new InputStreamReader( sk_clnt.getInputStream() ) );
				// Scanner inFromServ = new Scanner( 
				//		new InputStreamReader( sk_clnt.getInputStream() ) );
				BufferedReader stdIn = new BufferedReader(
						new InputStreamReader( System.in ) );
				
				String str_userInput = "";
				String str_srchResult = "";
				while ( ( str_userInput = stdIn.readLine() ) != null ) {
					// System.out.println("The user input is: " + str_userInput);
					outToServ.println(str_userInput.toUpperCase() );
					System.out.println("Result from direct server on port "	+ int_clntPort + " are:");
					str_srchResult = inFromServ.readLine();
					// str_srchResult = inFromServ.nextLine();
					String[] array_srchResult = str_srchResult.split(",");
					for ( String aRcrd:array_srchResult)
						System.out.println( aRcrd );
					// while ( (str_srchResult = inFromServ.readLine() ) != null) 
					//	System.out.println( str_srchResult);
				}
			}
			finally { sk_clnt.close(); }
		}
		catch ( IOException e) {
			System.err.println( "Couldn't get I/O for the connection to "
					+ HOST_NAME );
			System.exit(1);
		}
	}
}
