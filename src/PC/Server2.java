package PC;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

import biz.source_code.base64Coder.Base64Coder;
import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;

public class Server2
{
	// Constant specifying the packet size to send/receive
	static final int MAX_PACKET_SIZE = 256;
	private int echoClientPort = 2228; /* Echo client port */
	private String clientIP = "192.168.0.109"; /* IP address of Android */
	DatagramSocket socketServer = null;
	byte[] key = new byte[MAX_PACKET_SIZE];
	byte[] dbuffer = new byte[MAX_PACKET_SIZE];
	DatagramPacket reply;
	public Sender sender;
	public ServerListener serverListener;
	public boolean fileTransferFinished = false;
	private ArrayList<ArrayList<String>> receivedStrings;
	public long keyTransmit;
	
	/** Creates a new instance of Server 
	 * @param receivedStrings2 
	 * @param androidIP 
	 * @throws UnknownHostException 
	 * @throws BindException */
	public Server2(Sender sender,ServerListener serverListener, ArrayList<ArrayList<String>> receivedStrings2, String androidIP) throws UnknownHostException, BindException
	{
		System.out.println("Server 2");
		this.clientIP = androidIP;
		System.out.println("ANdroid IP: " + this.clientIP);
		this.receivedStrings = receivedStrings2;
		this.sender = sender;
		System.out.println("server2 finish initialization");
	}
	
	public boolean getAndroidKey() throws IOException
	{
		System.out.println("deleting old ArrayList");
		for(ArrayList<String> AL:receivedStrings){
			AL.clear();
		}
		System.out.println("Finish Deleting");
		boolean output = true;
	  System.out.println("waiting for Android Key");
	  while(receivedStrings.get(0).size() == 0);
	  System.out.println("key received");
	  key = receivedStrings.get(0).get(0).split(":")[1].getBytes();
	  //get the key from the Android device
	  System.out.println("key from Android " + Base64Coder.encodeLines(key));
	  
	  return output;
	}
	
	//makes the connection for decryption
	public boolean makeDConnection(String[] fileNames) throws IOException
	{	
		
		boolean output = true;
		String message="";
//		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(("key:"+new String(key)).getBytes()).getBytes()));
		//receive packets until told that the process is finished
		
			dbuffer = new byte[MAX_PACKET_SIZE];

			listen(2223, fileNames);
		//closes the socket
		
		return output;
	}
	
	//creates the connection for encryption, connect with android
	public boolean makeEConnection(String[] filenames) throws UnknownHostException, BindException
	{
		long start = System.currentTimeMillis();
		boolean output = true;
		if(sender.isInterrupted()){
			System.out.println("sender is not binded");
			sender.init(clientIP);
			System.out.println("finish initilization");
		}
		else {
			System.out.println("sender is  binded");
			sender.close();
			sender.init(clientIP);
		}
		byte[] buffer = new byte[MAX_PACKET_SIZE];
		
		

		System.out.println("waiting for Android Key");
		while(receivedStrings.get(0).size() == 0);
		System.out.println("key received");
		key = receivedStrings.get(0).get(0).split(":")[1].getBytes();
		System.out.println("key from android " + Base64Coder.encodeLines(key));
		
		this.keyTransmit = System.currentTimeMillis() - start;
		//sends all pieces to the Android device
		System.out.println("start to send file content");
		System.out.println("filename length : " + filenames.length);
		for(int i=0;i<filenames.length;i++){
			byte[] buff= new byte[MAX_PACKET_SIZE];
			buffer = new byte[MAX_PACKET_SIZE];
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();
//				DataOutputStream dos = new DataOutputStream(baos);
//				dos.writeBytes("encrypt");
//				dos.close();
//				buff = baos.toByteArray();
//				DatagramPacket encPack=new DatagramPacket(buff,buff.length,InetAddress.getByName(clientIP),echoClientPort);
//				socketServer.send(encPack);
			String file = filenames[i].replace(".txt", ".piece");
		    System.out.println(file);
		    System.out.println("Go to TALK");
			talk(file);
		}
		
		//sends a message letting the Android know the process is finished
		byte[] buff= new byte[MAX_PACKET_SIZE];

		System.out.println("print out received relpys");
		for(ArrayList<String> AL:receivedStrings){
			for(String s:AL){
				System.out.println("rely strings : " + s);
			}
		}
		
		//socketServer.close();
		System.out.println("Socket Closed");
		return output;
	}

	/**
	* Listens for incoming packets and echos them back to clients.
	*
	* @param port The port on which to listen for incoming packets.
	 * @param serverListener2 
	 * @param sender2 
	*/
	public void listen(int port, String[] fileNames) 
	{	
		fileTransferFinished = false;
		try
		{
byte[] buffer = new byte[MAX_PACKET_SIZE];
			
			//get the number of packets to be sent
			Socket toAndroid = new Socket(clientIP,4444);
			PrintStream out = new PrintStream(toAndroid.getOutputStream());
			out.println("key received");
			String receivedString;
			while(receivedStrings.get(4).size()==0);
			while(receivedStrings.get(1).size() == 0);
			
			receivedString = receivedStrings.get(1).get(0);
			
			System.out.println("FileName and File Content Received : " + receivedString);



			//get the filename of the packets being sent
			String fileName = "";
			buffer = new byte[MAX_PACKET_SIZE];
			String[] fileName_fileContent =  receivedString.split(":split:");
			fileName = fileName_fileContent[1];
			String fileContentinBase64 = fileName_fileContent[2];
			
			
			
				

			System.out.println("finish receing file content");
			System.out.println("Filename:"+fileName);
			//display message saying file received
			fileName = fileName.replaceAll("\\..*","");
			fileName+=".piece";
			System.out.println("FileName changed is:"+fileName);
		    Hash hash = new Hash();
		    byte[] temp_hash = hash.GetHash(findNulls(fileContentinBase64.getBytes()), 1);
		    System.out.println("Hash value of received file is " + Base64Coder.encodeLines(temp_hash));
			File folder=new File("C:\\Users\\xiao\\Documents\\" + fileName);
			folder.createNewFile();
			FileOutputStream fos = null;
			fos = new FileOutputStream(folder);
			fos.write(Base64Coder.decodeLines(fileContentinBase64));
			fos.flush();
			fos.close();
			
			out.println("allReceived");
			out.close();
			fileTransferFinished = true;
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

	//sends the input file to the Android device
	public void talk(String fileName)
	{
		File oldFile = new File(fileName);
		String echoFileName = oldFile.getName();
		byte[] echoBytes = null;
		int numPackets = 0;

			System.out.println("filename: "+echoFileName);
			//gets the contents of the file
			echoBytes = read(fileName);
			System.out.println("contents(bytes): " + echoBytes);
		
		//determines the number of packets needed to send to Android
		if(echoBytes.length%MAX_PACKET_SIZE==0)
		{
			numPackets = echoBytes.length/MAX_PACKET_SIZE;
		}
		else
		{
			numPackets = (echoBytes.length/MAX_PACKET_SIZE)+1;
		}
		System.out.println("#packs: " + numPackets);
			try
			{
				//saves number of packets to a byte array
				byte[] buffer = new byte[MAX_PACKET_SIZE];

				if (numPackets >= 1) 
				{
					int c=0;
					System.out.println("IP :" + clientIP + " Port : " + echoClientPort);

					//sends the name of the file
					System.out.println("send out file name : " + echoFileName);
					//sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(("FN:"+echoFileName).getBytes()).getBytes()));
					//do base64 to file content
					String fileContent = Base64Coder.encodeLines(echoBytes);
					String SendString = "FileName" + ":split:" + echoFileName + ":split:" + fileContent;
					//System.out.println("send out string : " + SendString);
					//sends the contents of the file
					Hash hash = new Hash();
					byte[] temp_hash = hash.GetHash(fileContent.getBytes(),1);
					System.out.println("content hash value : " + Base64Coder.encodeLines(temp_hash));
					int n=0;
					//send file name and file Content to Android
					sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(SendString.getBytes()).getBytes()));
					Socket toAndroid = new Socket(clientIP,4444);
					PrintStream out = new PrintStream(toAndroid.getOutputStream());
					out.println("File Send out");
					System.out.println("File send out");
					toAndroid.close();
					//deletes the piece that was sent
					deleteFile(fileName);
				}
			}
			catch (SocketException e)
			{
				System.err.println("Error creating the client socket: " + e.getMessage());
			}
			catch (IOException e)
			{
				System.err.println("Client socket Input/Output error: " + e.getMessage());
			}
			System.out.println("file sent");
	}
			
			//read the contents of a file and stores them in a byte array
			public static byte[] read(String aInputFileName)
			{
			    File file = new File(aInputFileName);
			    byte[] result = new byte[(int)file.length()];
			    try {
			      InputStream input = null;
			        int totalBytesRead = 0;
			        input = new BufferedInputStream(new FileInputStream(file));
			        while(totalBytesRead < result.length){
			          int bytesRemaining = result.length - totalBytesRead;
			          //input.read() returns -1, 0, or more :
			          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
			          if (bytesRead > 0){
			            totalBytesRead = totalBytesRead + bytesRead;
			          }
			        }
			        /*
			         the above style is a bit tricky: it places bytes into the 'result' array; 
			         'result' is an output parameter;
			         the while loop usually has a single iteration only.
			        */
			        input.close();
			    } catch (FileNotFoundException ex) {
			    } catch (IOException ex) {
			    }
			    return result;
			  }
			
			//deletes a file
			private static byte[] findNulls(byte[] buffer)
			{
				int terminationPoint = findLastMeaningfulByte(buffer);
				byte[] output;
				output = new byte[terminationPoint + 1];
				System.arraycopy(buffer, 0, output, 0, terminationPoint + 1);
				return output;
			}
			private static int findLastMeaningfulByte(byte[] array)
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
			 private static void deleteFile(String file)
			 {
				  File f1 = new File(file);
				  f1.delete();
			 }
}