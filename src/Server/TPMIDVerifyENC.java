package Server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


public class TPMIDVerifyENC {

	static final int MAX_PACKET_SIZE = 32;
	private static DatagramSocket socketServer = null;
	private static String clientIP = "172.17.48.174";
    private static int clientPort = 2228;
    private static InetAddress clientIPAddress;
		
	public static void receivePubKey(){
		
		//byte[] pubKey = null;
		System.out.println("**************receive pubkey******************");
		try {		
			//receive the public key file
			socketServer = new DatagramSocket(2228);
			clientIPAddress = InetAddress.getByName(clientIP);
			//send a reply to client
			byte[] lastreply = new byte[100];
			DatagramPacket lastrep = new DatagramPacket(lastreply, lastreply.length, clientIPAddress, clientPort);
			socketServer.send(lastrep);
			//set a timer for the socket to remain open
			//socketServer.setSoTimeout(15000);
			byte[] buffer = new byte[65507];
					
			DatagramPacket Connection = new DatagramPacket(buffer,buffer.length);
			socketServer.receive(Connection);
			byte[] data = findNulls(Connection.getData());
			byte[] pubKey = data;
			//save the public key in file pub.txt
			FileOutputStream pub = new FileOutputStream("pub.txt");
		    pub.write(asHex(pubKey).getBytes());
			pub.close();
			System.out.println("pub key received!");
			DatagramPacket reply = new DatagramPacket(Connection.getData(), Connection.getLength(), Connection.getAddress(), Connection.getPort());
			
			socketServer.send(reply);
			
			
			listen("Index.piece");
			
			socketServer.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	* Listens for incoming packets and echos them back to clients.
	*
	* @param port The port on which to listen for incoming packets.
	*/
	public static void listen(String fileNames) 
	{
		try
		{
			byte[] buffer = new byte[MAX_PACKET_SIZE];
			
			//get the number for packets to be sent
			DatagramPacket numPackets = new DatagramPacket(buffer,buffer.length);
			socketServer.receive(numPackets);
			DatagramPacket reply = new DatagramPacket(numPackets.getData(), numPackets.getLength(), numPackets.getAddress(), numPackets.getPort());
			socketServer.send(reply);
			ByteArrayInputStream bais = new ByteArrayInputStream(numPackets.getData());
			DataInputStream dis = new DataInputStream(bais);
			int packNum = dis.readInt();
			System.out.println("Number of packs:"+packNum);
			
			//get the filename of the packets being sent
			String fileName = "";
			buffer = new byte[MAX_PACKET_SIZE];
			DatagramPacket arrivalPacket = new DatagramPacket(buffer,buffer.length);
			socketServer.receive(arrivalPacket);
			fileName = new String(arrivalPacket.getData(),"US-ASCII");
			
			reply = new DatagramPacket(arrivalPacket.getData(), arrivalPacket.getLength(), arrivalPacket.getAddress(), arrivalPacket.getPort());
			socketServer.send(reply);
			System.out.println("Filename:"+fileName);
			//get the contents of the file being sent
			byte[] data = new byte[MAX_PACKET_SIZE*packNum];
			int dataIndex = 0;
			for(int i = 0; i < packNum; i++)
			{
				buffer = new byte[MAX_PACKET_SIZE];
				arrivalPacket = new DatagramPacket(buffer,buffer.length);
				socketServer.receive(arrivalPacket);
				for(int j = 0; j < MAX_PACKET_SIZE; j++)
				{
					data[dataIndex] = buffer[j];
					dataIndex++;
				}
				
				// create and send response packet
				reply = new DatagramPacket(arrivalPacket.getData(), arrivalPacket.getLength(), arrivalPacket.getAddress(), arrivalPacket.getPort());
				socketServer.send(arrivalPacket);
			}

			System.out.println("Data received: " + data);
			
			//save the file that was sent
			File newFile = new File(fileName);
			FileOutputStream fos = null;
			fos = new FileOutputStream(newFile);
			fos.write(data);
			fos.close();
		}
		catch (SocketException e)
		{
			System.err.println("Error creating the server socket: " + e.getMessage());
		}
		catch (IOException e)
		{
			System.err.println("Server socket Input/Output error: " + e.getMessage());
		}
	}

	
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
	
		
	/**
	* Turns array of bytes into string
	*
	* @param buf	Array of bytes to convert to hex string
	* @return	Generated hex string
	*/
	public static String asHex (byte[] buf)
	{
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
		if (((int) buf[i] & 0xff) < 0x10)
		strbuf.append("0");

		strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}

		return strbuf.toString();
	}
}
