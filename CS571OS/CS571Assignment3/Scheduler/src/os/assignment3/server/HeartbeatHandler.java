package os.assignment3.server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import os.assignment3.util.Util;


public class HeartbeatHandler extends Thread {
	
	private Socket clientSocket;
	private DepartmentNode departmentNode;

	public HeartbeatHandler(Socket clientSocket, DepartmentNode departmentNode) {
		this.clientSocket = clientSocket;
        this.departmentNode = departmentNode;
	}
	
	public void run()
	{
		System.out.println ("New Communication Thread Started");
		
		BufferedReader in = null;
		BufferedWriter out = null;

		try { 

			out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			String inputLine = in.readLine();
			System.out.println("Message received from heartbeat client: "+ inputLine);

			NodeStatus status = departmentNode.getStatus();
			out.write(status.toString());
			out.flush();
            System.out.println("Message sent to the heartbeat client:\n"+status.toString());

		} catch (Throwable e) 
		{ 
			System.out.println("Problem communication with heartbeat client: "+e.getMessage());
			e.printStackTrace(System.out);
		} finally {
			System.out.println("HeartbeatHandler finally");
			Util.closeReader(in);
			Util.closeWriter(out);
			Util.closeSocket(clientSocket);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
