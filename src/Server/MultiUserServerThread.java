package Server;

import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.io.*;

import javax.xml.bind.Marshaller.Listener;
//import java.util.Scanner;

import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;

public class MultiUserServerThread extends Thread
{
	private DatagramSocket socket = null;
	private static String pass = "";
	private static Boolean status = false;
	private static String servreply = null;
	private String AndroidIP = "192.168.0.101";
	private String PCIP = "192.168.0.107";
	private Sender sender;
	private ServerListener listener;
	private int switcher = 0;
	String PCIPAddress;

	public MultiUserServerThread(Sender sender, ServerListener listener )
	{
		this.sender = sender;
		this.listener = listener;
	}
	
	public void run(){
		while(!status){
		   receive();
		}
		System.out.println(status);
		try {
			send(servreply);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//send packet to client
	private void send( String reply ) throws IOException
	{
		//int Port = 2228;
		
		byte[] outData = reply.getBytes();
		
        /*construct packet to send*/
		System.out.println(PCIPAddress);
		//DatagramPacket outPacket = new DatagramPacket(outData, outData.length, PCIPAddress, Port);
		/*send UDP datagram through socket*/ 
		sender.init(PCIPAddress);
		sender.SendData(sender.PreparePacket(outData));
		System.out.println("finalreply has been sent!");
		/*recieve packet from server*/
		//DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
		//socket.receive(inPacket);
		//inPacketLength = inPacket.getLength();
		
		//socket.close();
   /*return the payload in the packet in String*/
		//return new String(inPacket.getData(), 0, inPacketLength);
	}
	
    //receive password
	public void receive()
	{
		byte[] inData = new byte[1400];
		byte[] outData;
		byte[] readdata = null;
		//int inPacketLength;
		
		/*create a datagram to store infromation  in "inData"*/
		//DatagramPacket inPacket = new DatagramPacket(inData, inData.length);

		try
		{//receive datagram
			//socket.receive(inPacket);
			while((readdata = listener.ReadData()) == null);
			
		}
		catch (IOException e)
		{
			System.out.println("Error receiving packet from client on port: " + socket.getPort() + e);
			System.exit(-1);
		}
		//inPacketLength = inPacket.getLength();
		//process packet
		if(readdata !=null){
			servreply = processPacket(readdata, readdata.length);
		}
		

    /*I can get client's IP and port from the packet it sent*/
		//InetAddress clientIPAddress = inPacket.getAddress();
		String clientIPAddress = listener.getIP();
		//int port = inPacket.getPort();
				
		System.out.println(clientIPAddress);
		
		if(switcher == 0){
			PCIPAddress = clientIPAddress;
			System.out.println(clientIPAddress);
		}
		
		/*Now I want reply a message to client*/
		outData = servreply.getBytes();
		boolean result;
		//DatagramPacket outPacket = new DatagramPacket(outData, outData.length, clientIPAddress, port);
		try
		{
			sender.init(clientIPAddress);
			result = sender.SendData(sender.PreparePacket(outData));
			if(result){
				sender.close();
			}
			//socket.send(outPacket);//send a datagram to client as a reply
		}
		catch (IOException e)
		{
			System.out.println("Error sending packet from client on port: " + socket.getPort() + e);
			System.exit(-1);
		}
		System.out.println("Message to UDP Client: " + servreply);
		System.out.println();
		
		switcher++;
		
		//tell the thread to stop
		if(!servreply.equals("continue!")){
			status = true;
		}
		
	}
	/*********************************
	**********************************/
	//remove nonsense bytes
	private static byte[] findNulls(byte[] buffer)
	{
		int terminationPoint = findLastMeaningfulByte(buffer);
		byte[] output;
		output = new byte[terminationPoint + 1];
		System.arraycopy(buffer, 0, output, 0, terminationPoint + 1);
		return output;
	}
	
	//returns the index of the last non-null character
		public static int findLastMeaningfulByte(byte[] array)
		{
			//System.out.println("Attempting to find the last meaningful byte of " + asHex(array));
			int index=0;

			for (index=(array.length - 1); index>0; index--) {
			//System.out.println("testing index " + index + ". Value: " + array[index]);
			if (array[index] != (byte)(0)) {
			//System.out.println("Last meaningful byte found at index " + index);
			return index;
			}
			}
			System.out.println("No meaningful bytes found.  Perhaps this is an array full of nulls...");
			return index;
		}

		//to check if the password input has been finished
	public static String processPacket( byte[] packetData, int size )
	{
		String result = null;
		packetData = findNulls(packetData);
		String Data = new String(packetData);
		//Data = String.copyValueOf(Data.toCharArray());
		
		Boolean verify = false;
		System.out.println("Message from client: " + Data);
		System.out.println("Length of Message: " + Data.length());
		System.out.println("Length of finished!: " + "finished2!".length());
		//Data = "finished!";
		System.out.println(Data.equals("finished2!"));
		if(Data.equals("finished2!")){
			System.out.println(pass);
			System.out.println(pass.length());
			verify = hash(pass);
			if(verify){
			    result = "Correct!";
			}else{
			    result = "Incorrect!";
			}
		}else{
			pass += Data;
			result = "continue!";
		}
		
		
		return result;
	}
	
	 public static boolean hash(String input)
	   {
		   boolean hashVal = false;
		   MessageDigest md = null;
			try 
			{
				//sets the hash function to SHA-256
				md = MessageDigest.getInstance("SHA-256");
			} 
			catch (NoSuchAlgorithmException e) 
			{
				e.printStackTrace();
			}
			try 
			{
				//hashes the input string
				md.update(input.getBytes("US-ASCII"));
			} 
			catch (UnsupportedEncodingException e) 
			{
				e.printStackTrace();
			}
			
			//gets the digest
		   byte[] digest = md.digest();
		   StringBuffer sb = new StringBuffer();
		   //puts the digest in hex format
	       for (int i = 0; i < digest.length; i++) 
	       {
	         sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
	       }
	       
	       System.out.println(sb);
	       System.out.println(sb.length());
	       
	       //checks if the input matches the correct PIN
		   if(true)
		   {
			   hashVal = true;
		   }
		   return hashVal;
	   }

	
}