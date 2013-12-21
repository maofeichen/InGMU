/**
 * CS571 OS Assignment 2
 * Maofei Chen
 * G00709508
 */
package tcp;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

/**
 * @author mchen
 * Distributed Phonebook System Server TCP version
 */
public class DistrPhonebkSysServTCP {
	private final static int MAXNUMPhnbkRcrd = 32;
	
	static String str_phonebkFileName;
	static int int_clientPort;

	static int int_chldrnPort;
	static String str_chldrnPort = ""; // for NULL input
	
	static int int_parentPort;
	static String str_parentPort = ""; // for NULL input
	
	final static String HOST_NAME = "localhost";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
		if (args.length != 4 ){
			System.err.println("Server command arguments error");
			System.exit(1);
		}
	
		str_phonebkFileName = args[0].trim();
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
		/*
		ServerSocket sk_servToClnt = new ServerSocket(int_clientPort);
		System.out.println("server starts at localhost on port " + int_clientPort );
		
		ServerSocket sk_servToServ = new ServerSocket(int_chldrnPort);
		System.out.println("server starts at localhost on port " + int_chldrnPort);
		
		while (true){
			Socket sk_clientToServ = sk_servToClnt.accept();
			Runnable sckt_rServToClnt = new ThreadHndlrCrtServSckt(sk_clientToServ, true);
			Thread sckt_tServToClnt = new Thread(sckt_rServToClnt);
			sckt_tServToClnt.start();
			
			Socket sk_chldrnServToPrntServ = sk_servToServ.accept();
			Runnable sckt_rServToChldServ = new ThreadHndlrCrtServSckt(sk_chldrnServToPrntServ, false);
			Thread sckt_tServToChldServ = new Thread(sckt_rServToChldServ );
			sckt_tServToChldServ.start();
		}
		*/
	
		// while true?
		// create server socket for client
		Runnable sckt_rServToClnt = new ThreadHndlrCrtServSckt(int_clientPort, true);
		Thread sckt_tServToClnt = new Thread(sckt_rServToClnt);
		sckt_tServToClnt.start();
	
		// create server socket for children server
		Runnable sckt_rServToChldServ = new ThreadHndlrCrtServSckt(int_chldrnPort, false);
		Thread sckt_tServToChldServ = new Thread(sckt_rServToChldServ );
		sckt_tServToChldServ.start();
	}

	static class ThreadHndlrCrtServSckt implements Runnable {
		int portNumber;
		boolean isListenClient;
    	int sk_clntIdx;
    	ServerSocket serverSocket;
    	Socket clientSocket;
    
    	public ThreadHndlrCrtServSckt (int portNumber, boolean isListenClient){
    		this.portNumber = portNumber;
    		this.isListenClient = isListenClient;
    		sk_clntIdx = 0;
    	}
    	
    	public ThreadHndlrCrtServSckt (Socket clientSocket, boolean isListenClient){
    		this.clientSocket = clientSocket;
    		sk_clntIdx = 0;
    	}
		public void run () {
			try {
				Runnable rCrtServSckt;
				ServerSocket serverSocket = new ServerSocket( portNumber );
				System.out.println("server starts at localhost on port " + portNumber );
				try {
					Socket clientSocket = serverSocket.accept();     
		            
					System.out.println("The " + sk_clntIdx + "th client request " );
					if (isListenClient) 
						rCrtServSckt = new ThreadHndlrServToClient(clientSocket);
					else
						rCrtServSckt = new ThreadHndlrServToServer(clientSocket);
		            // Runnable rThreadHandler = new ThreadHandler(serverSocket, clientSocket);
		            Thread tCrtServSckt = new Thread(rCrtServSckt);
		            tCrtServSckt.start();
		            
		            sk_clntIdx++;
				}
				finally { serverSocket.close(); }
	            
			} catch (IOException e) {
				System.err.println("Fail to create new thread to hanlde new client request");
				System.out.println(e.getMessage() );
			}
		}
	}
	/**
	 * Handle the client request 
	 * @author mchen
	 *
	 */
	static class ThreadHndlrServToClient implements Runnable {
		private Socket sk_clnt;	
		
		public ThreadHndlrServToClient(Socket sk_clnt ) { this.sk_clnt = sk_clnt; }
	
		// process request from client on server side
		public void run() {
			String inputUser;
			List<String> list_rcrdFound; 
			String str_rcrdFound = "";
			Phonebook phonebook = new Phonebook(str_phonebkFileName); // only one instance?

			try {
				PrintWriter outToClnt = new PrintWriter(sk_clnt.getOutputStream(), true);
				BufferedReader inFromClnt = new BufferedReader(
						new InputStreamReader( sk_clnt.getInputStream() ) );
				
				while ( ( inputUser = inFromClnt.readLine() ) != null ) {
					// System.out.println("The line user input is: " + str_inputLine );
					String[] arry_inputLine = inputUser.split(" "); 
					for (String aInput:arry_inputLine){
						System.out.println("User input is: " + aInput );
					}
					
					phonebook.srchPhonebkByStrArry(arry_inputLine);
					if ( phonebook.isRcrdFound == false) {
						System.out.println("Not found in local server");
						if (str_parentPort.equals("NULL"))
							str_rcrdFound = "Not found record; but reach root server; return anyway";
						else
							str_rcrdFound = rqstParentServ(inputUser);
					}
					else {  // found in direct phone book
						System.out.println("Found in local server");
						str_rcrdFound = phonebook.getSrchRcrdRsltStr();
					}
					outToClnt.println(str_rcrdFound);
				}
			}
			catch (IOException e) {
				System.out.println("Exception caught when trying to listen on port "
						+ int_clientPort 
						+ " or listening for a connection");
				System.out.println(e.getMessage());
			}
		}
	
		/**
		 * if a server does not hit in its own phone book, 
		 * it request its parent, behave like a client,
		 *  until hit the root server
		 */
		public String rqstParentServ(String inputSrchKey) {
			List<String> list_srchRslt = new ArrayList<String>();
			String str_srchResult = "";
			
			try {
				// request its parent
				Socket sk_rqstPrntServ = new Socket(HOST_NAME, int_parentPort);
				System.out.println("Server on port " + int_clientPort 
						+ " request to its parent server on port " 
						+ int_parentPort);
			
				PrintWriter outToPrntServ = new PrintWriter( sk_rqstPrntServ.getOutputStream(), true);
				BufferedReader inFromPrntServ = new BufferedReader(
						new InputStreamReader( sk_rqstPrntServ.getInputStream() ) );
			
				outToPrntServ.println(inputSrchKey );
				
				System.out.println("Result from parent server on port "	+ int_parentPort + " are:");
				str_srchResult = inFromPrntServ.readLine();
				
			}
			catch ( IOException e ) {
				System.err.println( "Server couldn't get I/O for the connection to its parent "
					+ HOST_NAME);
				System.exit(1);
			}
			return str_srchResult;
		}
		
	}
	
	static class ThreadHndlrServToServer implements Runnable {

		private Socket sk_chldServ;	
		
		public ThreadHndlrServToServer(Socket sk_chldServ ) {
			this.sk_chldServ = sk_chldServ;

		}
	
		// process request from children server on server side
		public void run() {
			String inputUser;
			String str_rcrdFound = "";
			List<String> list_rcrdFound;
			Phonebook phonebook = new Phonebook(str_phonebkFileName);

			try {
				PrintWriter outToChldServ = new PrintWriter(sk_chldServ.getOutputStream(), true);
				BufferedReader inFromChldServ = new BufferedReader(
						new InputStreamReader( sk_chldServ.getInputStream() ) );
			
				inputUser = inFromChldServ.readLine();
				// System.out.println("The line user input is: " + str_inputLine );
				String[] arry_inputLine = inputUser.split(" "); 
				for (String input:arry_inputLine)
					System.out.println("User input is: " + input );
					
				phonebook.srchPhonebkByStrArry(arry_inputLine);
				
				// str_srchKey = inFromChldServ.readLine().trim();
				// phonebook.srchPhonebook( str_srchKey.trim() ); // search direct server
				if ( phonebook.isRcrdFound == false) {
					System.out.println("Not found in local server");
					if (str_parentPort.equals("NULL"))
						str_rcrdFound = "Not found record; but reach root server; return anyway";
					else
						str_rcrdFound = rqstParentServ(inputUser);
				}
				else {  // found in direct phone book
					System.out.println("Found in local server");
					// list_rcrdFound = phonebook.getSrchRcrdRsultList();
					str_rcrdFound = phonebook.getSrchRcrdRsltStr();
				}
				outToChldServ.println(str_rcrdFound );
			}
			catch (IOException e) {
				System.out.println("Exception caught when trying to listen on port "
						+ int_clientPort 
						+ " or listening for a connection");
				System.out.println(e.getMessage());
			}
		}
		
		/**
		 * if a server does not hit in its own phone book, 
		 * it request its parent, behave like a client,
		 *  until hit the root server
		 */
		public String rqstParentServ(String inputSrchKey) {
			List<String> list_srchRslt = new ArrayList<String>();
			String str_srchResult = "";
			
			try {
				// request its parent
				Socket sk_rqstPrntServ = new Socket(HOST_NAME, int_parentPort);
				System.out.println("Server on port " + int_clientPort 
						+ " request to its parent server on port " 
						+ int_parentPort);
			
				PrintWriter outToPrntServ = new PrintWriter( sk_rqstPrntServ.getOutputStream(), true);
				BufferedReader inFromPrntServ = new BufferedReader(
						new InputStreamReader( sk_rqstPrntServ.getInputStream() ) );
			
				outToPrntServ.println(inputSrchKey );
				
				System.out.println("Result from parent server on port "	+ int_parentPort + " are:");
				str_srchResult = inFromPrntServ.readLine();
				
			}
			catch ( IOException e ) {
				System.err.println( "Server couldn't get I/O for the connection to its parent "
					+ HOST_NAME);
				System.exit(1);
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
		private String str_srchRcrdRslt = "";
		
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
			for (String aRcrd:list_srchRcrdRsult )
				str_srchRcrdRslt += aRcrd;
			
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
