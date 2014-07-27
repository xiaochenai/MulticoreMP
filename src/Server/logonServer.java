package Server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;


public class logonServer {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 */
	//start of the server programs
	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
		String AndroidIP = "192.168.0.101";
		String PCIP = "192.168.0.107";
		Sender sender = new Sender();
		ServerListener listener = new ServerListener();
		listener.init();
		DatagramSocket serverSocket = null;
		boolean listening = true;
		boolean request = false;
		try
		{/*Server Listen on Port 8888*/
			serverSocket = new DatagramSocket(2228);
		}
		catch (IOException e)
		{
			System.out.println("Could not listen on port: 2228");
			System.exit(-1);
		}
		try
		{//stry to display IP address of server
			InetAddress addr = InetAddress.getLocalHost();
			System.out.println("\nServer address: "+String.valueOf(addr));
		}
		catch(Exception e)
		{
			System.out.println("Bad IP Address!"+e);
		}
		System.out.println("Listen on port: 2228\n");
		System.out.println("Waiting for packets...\n");
		/*Server wait for packets*/
		while (listening)
		{
			//serverSocket.receive(input);
			request = serverSocket.isBound();
			if (request)
			{/*a thread is for a user to process his packets*/
				new MultiUserServerThread(sender, listener).run();
				listening = false;
			}

		}
		serverSocket.close();
		while(true){		
			IDVerification.IDService();
		}
	}
	
	

}
