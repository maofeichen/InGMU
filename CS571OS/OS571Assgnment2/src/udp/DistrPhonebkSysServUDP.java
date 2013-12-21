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
public class DistrPhonebkSysServUDP {
	private final static String HOSTNMAE = "localhost";
	private final static int MAXNUMPhnbkRcrd = 32;
	private final static int TIMEOUT = 10000;

	static String str_phonebkFileName;
	static // System.out.println(str_phonebkFileName);
	int int_clientPort;
	
	static int int_chldrnPort;
	static String str_chldrnPort = ""; // for NULL input

	static int int_parentPort;
	static String str_parentPort = ""; // for NULL input

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length != 4 ){
			System.err.println("Server command arguments error");
			System.exit(1);
		}
	
		str_phonebkFileName = args[0].trim();
		// System.out.println(str_phonebkFileName);
		int_clientPort = Integer.parseInt(args[1]);
		
		if (args[2].toUpperCase().equals("NULL") ){
			str_chldrnPort = "NULL";
			int_chldrnPort = 0;
		}
		else
			int_chldrnPort = Integer.parseInt(args[2]);
		
		if ( args[3].toUpperCase().equals("NULL")){
			str_parentPort = "NULL";
			int_parentPort = 0;
		}
		else
			int_parentPort = Integer.parseInt(args[3]);
	
		Runnable dk_rServToClnt = new ThreadHndlrCrtDgmSckt(int_clientPort, true);
		Thread dk_tServToClnt = new Thread(dk_rServToClnt);
		dk_tServToClnt.start();
		
		Runnable dk_rServToServ = new ThreadHndlrCrtDgmSckt(int_chldrnPort, false);
		Thread dk_tServToServ = new Thread(dk_rServToServ);
		dk_tServToServ.start();
			
	}
	
	
	static class ThreadHndlrCrtDgmSckt implements Runnable {
		int portNumber;
		String srchRslt = null;
		String inputUser;
		boolean isListenClient;
		int dk_clntIdx;
		
		public ThreadHndlrCrtDgmSckt (int portNumber, boolean isListenClient) {
			this.portNumber = portNumber;
			dk_clntIdx = 0;
			this.isListenClient = isListenClient;
		}
		
		public void run() {
			try{
				Runnable rCrtServDatagram;
				
				DatagramSocket dk_servSocket = new DatagramSocket(portNumber );
				System.out.println("The server starts "	+ "at localhost "
						+ "on " + portNumber );
				
				// System.out.println("The " + dk_clntIdx + "th client send request packet");
				
				if (isListenClient)
					rCrtServDatagram = new ThreadHndlrServToClnt(dk_servSocket);
				else
					rCrtServDatagram = new ThreadHndlrServToServ(dk_servSocket);
				Thread tCrtServDtgrmSckt = new Thread(rCrtServDatagram);
				tCrtServDtgrmSckt.start();
				
				dk_clntIdx++;
			}
			
			catch (IOException e) {
				System.out.println(e.getMessage() );
			}
		}
	}
	
	static class ThreadHndlrServToClnt implements Runnable {
		DatagramSocket dk_servToClint;
		
		String srchRslt = null;
		String inputUser;
		
		public ThreadHndlrServToClnt (DatagramSocket dk_servToClint){
			this.dk_servToClint = dk_servToClint ;
		}
		
		public void run() {
			try{
				DatagramPacket dp_rqst = new DatagramPacket( new byte[1024], 1024);
				dk_servToClint.receive(dp_rqst);
				inputUser = new String( dp_rqst.getData(), 0, dp_rqst.getLength() );
				
				Phonebook phonebk = new Phonebook(str_phonebkFileName);
				// phonebk.srchPhonebook(inputUser);
				
				String[] arry_inputLine = inputUser.split(" ");
				for (String input:arry_inputLine){
					System.out.println("User input is: " + input );
				}
				phonebk.srchPhonebkByStrArry(arry_inputLine);
				if ( phonebk.isRcrdFound == false) {
					System.out.println("Not found in local server");
					if (str_parentPort.equals("NULL"))
						srchRslt = "Not found record; but reach root server; return anyway";
					else
						srchRslt = rqstPrntServ(inputUser);
				}
				else {  // found in direct phone book
					System.out.println("Found in local server");
					srchRslt = phonebk.getSrchRcrdRsltStr();
				}
				
				byte[] byte_srchRslt = srchRslt.getBytes();
				DatagramPacket dp_rpns = new DatagramPacket(byte_srchRslt, 
						byte_srchRslt.length, 
						dp_rqst.getAddress(), 
						dp_rqst.getPort());
				dk_servToClint.send(dp_rpns);
			}
			catch (IOException e ) { System.out.println( e.getMessage() ); }
		}
		
		public static String rqstPrntServ(String srchKeyInput ){

			// List<String> list_srchRslt = new ArrayList<String>();
			String str_srchResult = null;
			
			try {
				DatagramSocket dk_toPrntServ = new DatagramSocket();
				System.out.println("Request to parent server");		
				dk_toPrntServ.setSoTimeout(TIMEOUT);
				
				byte[] byte_srchKeyInput = srchKeyInput.getBytes();
				InetAddress host = InetAddress.getByName(HOSTNMAE);
				DatagramPacket dp_rqstPrntServ = new DatagramPacket(byte_srchKeyInput, 
						byte_srchKeyInput.length, host, int_parentPort);
				dk_toPrntServ.send(dp_rqstPrntServ);
				
				DatagramPacket dp_rspnsPrntServ = new DatagramPacket( new byte[1024], 1024);
				dk_toPrntServ.receive(dp_rspnsPrntServ);
				
				str_srchResult = new String( dp_rspnsPrntServ.getData(), 0, dp_rspnsPrntServ.getLength() );
				// System.out.println(str_srchRslt);
			}
			catch ( IOException e ) {
					System.out.println(e.getMessage() );
			}
			return str_srchResult;
		}
	}
	
	static class ThreadHndlrServToServ implements Runnable {
		DatagramSocket dk_servToServ;
		
		String srchRslt = null;
		String inputUser;
		
		public ThreadHndlrServToServ(DatagramSocket dk_servToServ) { this.dk_servToServ = dk_servToServ; }
		public void run() {
			try{
				// receive packet from children servers
				DatagramPacket dp_rqstChldServ = new DatagramPacket( new byte[1024], 1024);
				dk_servToServ.receive(dp_rqstChldServ);
				inputUser = new String( dp_rqstChldServ.getData(), 0, dp_rqstChldServ.getLength());
				
				Phonebook phonebk = new Phonebook(str_phonebkFileName);
				phonebk.srchPhonebook(inputUser);
				if ( phonebk.isRcrdFound == false) {
					System.out.println("Not found in local server");
					if (str_parentPort.equals("NULL"))
						srchRslt = "Not found record; but reach root server; return anyway";
					else
						srchRslt = rqstPrntServ(inputUser);
				}
				else {  // found in direct phone book
					System.out.println("Found in local server");
					srchRslt = phonebk.getSrchRcrdRsltStr();
				}
				
				byte[] byte_srchRslt = srchRslt.getBytes();
				DatagramPacket dp_rpnsToChldServ = new DatagramPacket(byte_srchRslt, 
						byte_srchRslt.length, 
						dp_rqstChldServ.getAddress(), 
						dp_rqstChldServ.getPort());
				dk_servToServ.send(dp_rpnsToChldServ);
			}
			catch (IOException e) {
				System.out.println(e.getMessage() );
			}
		}
		
		public static String rqstPrntServ(String srchKeyInput ){

			// List<String> list_srchRslt = new ArrayList<String>();
			String str_srchResult = null;
			
			try {
				DatagramSocket dk_toPrntServ = new DatagramSocket();
				System.out.println("Request to parent server");		
				dk_toPrntServ.setSoTimeout(TIMEOUT);
				
				byte[] byte_srchKeyInput = srchKeyInput.getBytes();
				InetAddress host = InetAddress.getByName(HOSTNMAE);
				DatagramPacket dp_rqstPrntServ = new DatagramPacket(byte_srchKeyInput, 
						byte_srchKeyInput.length, host, int_parentPort);
				dk_toPrntServ.send(dp_rqstPrntServ);
				
				DatagramPacket dp_rspnsPrntServ = new DatagramPacket( new byte[1024], 1024);
				dk_toPrntServ.receive(dp_rspnsPrntServ);
				
				str_srchResult = new String( dp_rspnsPrntServ.getData(), 0, dp_rspnsPrntServ.getLength() );
				// System.out.println(str_srchRslt);
			}
			catch ( IOException e ) {
					System.out.println(e.getMessage() );
			}
			return str_srchResult;
		}
	}
	/**
	 * Phonebook class
	 * mainly implement search
	 */
	static class Phonebook {
		List<PhonebkRecord> list_phonebook;
		String str_phnbkFileName;
		private boolean isRcrdFound; // search hit?
		private List<String> list_srchRcrdRsult = new ArrayList<String>();
		
		String str_srchRcrdRslt;
		
		public Phonebook(String str_phnbkFileName) {
			isRcrdFound = false; // initial false, if search hit, flip it
			this.str_phnbkFileName = str_phnbkFileName;
			list_phonebook = new ArrayList<PhonebkRecord>(); 
			readPhonebk();
	
			/*
			for( PhonebkRecord phnbkRcod:list_phonebook) {
				System.out.println(phnbkRcod.getLastName() );
				System.out.println(phnbkRcod.getFirstName() );
				System.out.println(phnbkRcod.getPhoneNum());
			}
			*/
		}

		public boolean getIsRcrdFound() { return isRcrdFound; }
		public List<String> getSrchRcrdRsultList() { return list_srchRcrdRsult; }
		
		public String getSrchRcrdRsltStr() {
			String srchRcrdRsltTempt = null;
			for (String aRcrd:list_srchRcrdRsult) {
				srchRcrdRsltTempt += aRcrd + "\n";
			}
			str_srchRcrdRslt = srchRcrdRsltTempt;
			return str_srchRcrdRslt;
		}
		
		private void readPhonebk() {
			try{
				BufferedReader readPhonebk = new BufferedReader( new FileReader(str_phnbkFileName ) );
				
				String str_line;
				int int_countLine = 0;
				int int_RcrdIdx; 
				PhonebkRecord[] arry_phnbkRcod = new PhonebkRecord[MAXNUMPhnbkRcrd];
				for( int i = 0; i < MAXNUMPhnbkRcrd; i++){ 
					arry_phnbkRcod[i] =	new PhonebkRecord(); 
				}
				while ( ( str_line = readPhonebk.readLine() ) != null ) {
					int_RcrdIdx = int_countLine / 3;
					// System.out.println(int_RcrdIdx);
					if (str_line.length() > 0 ){
						str_line = str_line.trim();
						if ( int_countLine % 3 == 0) { // set last name
							arry_phnbkRcod[int_RcrdIdx].setLastName(str_line);
						}
						else if( int_countLine % 3 == 1) // set first name
							arry_phnbkRcod[int_RcrdIdx].setFirstName(str_line);
						else if( int_countLine % 3 == 2) { // set phone number
							arry_phnbkRcod[int_RcrdIdx].setPhoneNum(Integer.parseInt(str_line) );
							list_phonebook.add(arry_phnbkRcod[int_RcrdIdx]);
						}
						// System.out.println(str_line);
						int_countLine++;
					}
				}
				readPhonebk.close();
			}
			catch (IOException e) {
				System.out.println("Exception caught when tring to read the phonebook");
				System.out.println(e.getMessage());
			}
		}
		
		void srchPhonebook(String inputSrchKey){
			String srchRcrdResult;
			for( PhonebkRecord phnbkRcod:list_phonebook) {
				if ( inputSrchKey.equals(phnbkRcod.getFirstName() ) 
						|| inputSrchKey.equals(phnbkRcod.getLastName() ) ) {
					srchRcrdResult = "Record found: "
							+ " " + phnbkRcod.getFirstName()
							+ " " + phnbkRcod.getLastName()
							+ " " + phnbkRcod.getPhoneNum()
							+ ",";
					list_srchRcrdRsult.add(srchRcrdResult);
					// System.out.println(rcrdFound);
					isRcrdFound = true;
				}
			}
			if ( isRcrdFound == false ){
				srchRcrdResult = "The record has not found at this local server";
				list_srchRcrdRsult.add(srchRcrdResult);
			}
			// else
				// System.out.println(rcrdFound);
		}
		
		void srchPhonebkByStrArry(String[] inputSrchKey){
			String srchRcrdResult;

			if (inputSrchKey.length == 1) {
				for( PhonebkRecord phnbkRcod:list_phonebook) {
					if ( inputSrchKey[0].equals(phnbkRcod.getFirstName() ) 
							|| inputSrchKey[0].equals(phnbkRcod.getLastName() ) ) {
						srchRcrdResult = "Record found: "
								+ " " + phnbkRcod.getFirstName()
								+ " " + phnbkRcod.getLastName()
								+ " " + phnbkRcod.getPhoneNum()
								+ ",";
						list_srchRcrdRsult.add(srchRcrdResult);
						// System.out.println(rcrdFound);
						isRcrdFound = true;
					}
				}
			}
			else {
			
				if ( inputSrchKey[0].equals("*") 
						&& inputSrchKey[1].equals("*" )) { // return all local records
					for( PhonebkRecord phnbkRcod:list_phonebook) {
						srchRcrdResult = "Record found: "
								+ " " + phnbkRcod.getFirstName()
								+ " " + phnbkRcod.getLastName()
								+ " " + phnbkRcod.getPhoneNum()
								+ ",";
						list_srchRcrdRsult.add(srchRcrdResult);
						// System.out.println(rcrdFound);
						isRcrdFound = true;
					}
				}
	
				else if ( inputSrchKey[0].equals("*") && 
						!inputSrchKey[1].equals("*") ){ // * name
					for( PhonebkRecord phnbkRcod:list_phonebook) {
						if ( inputSrchKey[1].equals(phnbkRcod.getFirstName() ) 
								|| inputSrchKey[1].equals(phnbkRcod.getLastName() ) ) {
							srchRcrdResult = "Record found: "
									+ " " + phnbkRcod.getFirstName()
									+ " " + phnbkRcod.getLastName()
									+ " " + phnbkRcod.getPhoneNum()
									+ ",";
							list_srchRcrdRsult.add(srchRcrdResult);
							// System.out.println(rcrdFound);
							isRcrdFound = true;
						}
					}			
				} 
	
				else if ( !inputSrchKey[0].equals("*") && 
						inputSrchKey[1].equals("*") ){ // First name and *
					for( PhonebkRecord phnbkRcod:list_phonebook) {
						if ( inputSrchKey[0].equals(phnbkRcod.getFirstName() ) 
								|| inputSrchKey[0].equals(phnbkRcod.getLastName() ) ) {
							srchRcrdResult = "Record found: "
									+ " " + phnbkRcod.getFirstName()
									+ " " + phnbkRcod.getLastName()
									+ " " + phnbkRcod.getPhoneNum()
									+ ",";
							list_srchRcrdRsult.add(srchRcrdResult);
							// System.out.println(rcrdFound);
							isRcrdFound = true;
						}
					}			
				} 	
				
				else if ( !inputSrchKey[0].equals("*") && 
						!inputSrchKey[1].equals("*") ){ // First name and Last Name	
					for( PhonebkRecord phnbkRcod:list_phonebook) {
						if ( ( inputSrchKey[0].equals(phnbkRcod.getFirstName() ) 
								&& inputSrchKey[1].equals(phnbkRcod.getLastName() ) )
								|| 
							( inputSrchKey[1].equals(phnbkRcod.getFirstName() ) 
								&& inputSrchKey[0].equals(phnbkRcod.getLastName() ) ) ) {
							srchRcrdResult = "Record found: "
									+ " " + phnbkRcod.getFirstName()
									+ " " + phnbkRcod.getLastName()
									+ " " + phnbkRcod.getPhoneNum()
									+ ",";
							list_srchRcrdRsult.add(srchRcrdResult);
							// System.out.println(rcrdFound);
							isRcrdFound = true;
						}
					}					
				}
			}
		
			if ( isRcrdFound == false ){
				srchRcrdResult = "The record has not found at this local server";
				list_srchRcrdRsult.add(srchRcrdResult);
			}
		}	
	}
	
	/** 
	 * Record of phonebook
	 */
	static class PhonebkRecord{
		String lastName;
		String firstName;
		int int_phoneNum;
		
		public PhonebkRecord() {}

		void setLastName(String lastName) { this.lastName = lastName; }
		void setFirstName(String firstName) { this.firstName = firstName; }
		void setPhoneNum(int int_phoneNum) { this.int_phoneNum = int_phoneNum; }
		String getLastName() {return lastName;}
		String getFirstName() { return firstName;}
		int getPhoneNum() { return int_phoneNum;}
	}

}
