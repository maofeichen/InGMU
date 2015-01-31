package os.assignment3.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.DatagramSocket;
import java.net.Socket;

public class Util {
	
	public static void closeReader(BufferedReader reader) {
		try {
			if(reader != null) {
				reader.close();
			}
		} catch(Exception exc) {
			
		}
	}

	public static void closeWriter(BufferedWriter writer) {
		try {
			if(writer != null) {
				writer.close();
			}
		} catch(Exception exc) {
			
		}
	}

	public static void closeSocket(Socket socket) {
		try {
			if(socket != null) {
				socket.close();
			}
		} catch(Exception exc) {
			
		}
	}

	public static void closeDGSocket(DatagramSocket socket) {
		try {
			if(socket != null) {
				socket.close();
			}
		} catch(Exception exc) {
			
		}
	}
	
	public static void sleep(long sleepTime) {
	    try {
	        Thread.sleep(sleepTime);
	    } catch(InterruptedException exc) {
	        System.out.println("Interrupted exception for thread "+Thread.currentThread().getName());
	    }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
