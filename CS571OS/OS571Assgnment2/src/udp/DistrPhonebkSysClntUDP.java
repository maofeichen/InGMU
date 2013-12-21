/**
 * CS571 OS Assignment 2
 * Maofei Chen
 * G00709508 
 */
package udp;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * @author mchen
 *
 */
public class DistrPhonebkSysClntUDP {
	private final static String HOST_NMAE = "localhost";
	private final static int TIMEOUT = 10000;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 1 ){
			System.err.print("Client command arguments error");
			System.exit(1);
		}
		
		int int_clntPort = Integer.parseInt(args[0]);
	
		try {
			DatagramSocket sk_clnt = new DatagramSocket();
			System.out.println("Client connects to server "
				+ "at " + HOST_NMAE 
				+ " on " + int_clntPort);		
			sk_clnt.setSoTimeout(TIMEOUT);
			
			BufferedReader stdIn = new BufferedReader(
					new InputStreamReader( System.in ) );
			
			String str_userInput;
			while ( ( str_userInput = stdIn.readLine() ) != null ) {
				byte[] byte_inputUser = str_userInput.toUpperCase().getBytes();
				InetAddress host = InetAddress.getByName(HOST_NMAE);
				DatagramPacket dp_rqstClnt = new DatagramPacket(byte_inputUser, byte_inputUser.length, host, int_clntPort);
				sk_clnt.send(dp_rqstClnt);
				
				DatagramPacket dp_rspnsClnt = new DatagramPacket( new byte[1024], 1024);
				sk_clnt.receive(dp_rspnsClnt);
				
				String str_srchRslt = new String( dp_rspnsClnt.getData(), 0, dp_rspnsClnt.getLength() );
				System.out.println(str_srchRslt);
			}
		}
		catch (IOException e){
			
		}
	}

}
