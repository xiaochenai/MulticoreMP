package PC;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import javax.swing.JOptionPane;

import biz.source_code.base64Coder.Base64Coder;

import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIHash;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcTssContextFactory;

public class TpmIDVerifyDEC {

	private static DatagramSocket socketClient = null;
	static final int MAX_PACKET_SIZE = 32;
	private static String servIP = "192.168.0.103";
    private static int servPort = 2228;
    private static InetAddress serverIPAddress;
	
    //TPM Identity Attestation and index.piece file retrieve
	public static void signID(Sender Sender, ServerListener ServerListener){
	try {
		//Tell server it is signature verify process
		Sender sender = Sender;
		ServerListener serverListener = ServerListener;
		serverIPAddress =  InetAddress.getByName(servIP);
		socketClient = new DatagramSocket(servPort);
		byte[] buffer = new byte[MAX_PACKET_SIZE];
		buffer = "DEC".getBytes();
		
		if(!sender.isInterrupted()){
			sender.close();
			sender.init(servIP);
		}else {
			sender.init(servIP);
		}
				
//		DatagramPacket Connection = new DatagramPacket(buffer,buffer.length, serverIPAddress, servPort);
//		socketClient.send(Connection);
		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines("DEC".getBytes()).getBytes()));
		System.out.println("DEC Packet sent!");
		
		//receive the nonce from server
		byte[] buf = new byte[1400];
//		DatagramPacket noncePacket = new DatagramPacket(buf, buf.length);
//		socketClient.receive(noncePacket);
		
//		byte[] servNonce = findNulls(noncePacket.getData());
		byte[] receivedBytes = serverListener.ReadData();
		String receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
		System.out.println("received : " + receivedStrings);
		//create context and connect to tpm
		TcIContext context = new TcTssContextFactory().newContextObject();
		context.connect();

		// ..first configure, create and load key..
		// Set up the Storage Root KEY (SRK)
		TcIRsaKey srk = context
				.createRsaKeyObject(TcTssConstants.TSS_KEY_TSP_SRK);
        //System.out.println(srk.toString());
		// set SRK policy
		TcIPolicy srkPolicy = context
				.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
		srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_SHA1, TcBlobData
				.newByteArray(TcTssConstants.TSS_WELL_KNOWN_SECRET));
		srkPolicy.assignToObject(srk);

		TcIRsaKey keyPKRetrieved = context.getKeyByUuid(
				TcTssConstants.TSS_PS_TYPE_SYSTEM, TcUuidFactory.getInstance()
				.getUuidU1SK2());
		

		//input the sign key authentication secret here
		String TPMSignsecret = JOptionPane.showInputDialog("Please input TPM signing Key Authentication Secret Here!");		
		
		//create usage secret policy object and assign to AIK object
		TcBlobData keyPKLoadedSecret = TcBlobData.newString(TPMSignsecret);
		TcIPolicy keyPKLoadedPolicy = context
				.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
		keyPKLoadedPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
				keyPKLoadedSecret);
		keyPKLoadedPolicy.assignToObject(keyPKRetrieved);
       
		//retrieve the AIK
		keyPKRetrieved.loadKey(srk);
				
		//Create the Nonce by combining server nonce and tpm nonce
		// generate random data using TPM
		TcBlobData random = context.getTpmObject().getRandom(128);
		byte[] TPMnonce = random.asByteArray();
		
		byte[] Nonce = new byte[256];
		
		//combine two nonces
		System.arraycopy(findNulls(receivedStrings.getBytes()), 0, Nonce, 0, findNulls(receivedStrings.getBytes()).length);
		System.arraycopy(TPMnonce, 0, Nonce, findNulls(receivedStrings.getBytes()).length, TPMnonce.length);
		
		//send the tpm nonce to server
//		DatagramPacket tpmnonce = new DatagramPacket(TPMnonce,TPMnonce.length, serverIPAddress, servPort);
//		socketClient.send(tpmnonce);
		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(TPMnonce).getBytes()));
		System.out.println("TPM Nonce sent! : " + new String(TPMnonce));
		
//		DatagramPacket rePacket = new DatagramPacket(buf, buf.length);
//		socketClient.receive(rePacket);
		receivedBytes = serverListener.ReadData();
		receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
		System.out.println("receive reply from server : " + receivedStrings);
		byte[] digest = Hash.hash(Nonce);
		//create hash object
		TcBlobData nonce = TcBlobData.newByteArray(digest);
		TcIHash hash = context.createHashObject(TcTssConstants.TSS_HASH_SHA1);
		hash.updateHashValue(nonce);

		// sign the hash object with the private signing key (take place inside the TPM)
		TcBlobData signature = hash.sign(keyPKRetrieved);
		byte[] sig = signature.asByteArray();
		
		//send the signature to server for identity verification
//		DatagramPacket sign = new DatagramPacket(sig,sig.length, serverIPAddress, servPort);
//		socketClient.send(sign);
		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(sig).getBytes()));
		System.out.println("signature has been sent to server!");
		byte[] buff = new byte[100];
//		DatagramPacket sigreply = new DatagramPacket(buff, buff.length);
//		socketClient.receive(sigreply);
		receivedBytes = serverListener.ReadData();
		receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
		System.out.println("received Strings for Verification : " + receivedStrings);
		buff = findNulls(buff);
		String result = new String(buff);
		
		//system exit if signature is not verified
		if(!receivedStrings.equals("verified!")){ 
			JOptionPane.showMessageDialog(null, "Signature is not verified!");
			System.exit(-1); 
			}
		
//		DatagramPacket reply = new DatagramPacket(sigreply.getData(), sigreply.getLength(), serverIPAddress, servPort);
//		socketClient.send(reply);
		sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(receivedStrings.getBytes()).getBytes()));

		//receive the index file if the signature is verified
		listen("Index.piece",sender,serverListener);
		
		context.unregisterKey(TcTssConstants.TSS_PS_TYPE_SYSTEM,
				TcUuidFactory.getInstance()
				.getUuidU1SK2());
		
//		socketClient.close();
	} catch (Exception e) {
		e.printStackTrace();
	}
					
		
	} 
	
	
	/**
	* Listens for incoming packets and echos them back to clients.
	 * @param ServerListener 
	 * @param Sender 
	*
	* @param port The port on which to listen for incoming packets.
	*/
	public static void listen(String fileNames, Sender Sender, ServerListener ServerListener) 
	{
		try
		{
			byte[] buffer = new byte[MAX_PACKET_SIZE];
			Sender sender = Sender;
			ServerListener serverListener = ServerListener;
			//get the number for packets to be sent
//			DatagramPacket numPackets = new DatagramPacket(buffer,buffer.length);
//			socketClient.receive(numPackets);
//			DatagramPacket reply = new DatagramPacket(numPackets.getData(), numPackets.getLength(), numPackets.getAddress(), numPackets.getPort());
//			socketClient.send(reply);
			byte[] receivedBytes = serverListener.ReadData();
			String receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
			sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(receivedStrings.getBytes()).getBytes()));
			System.out.println("receive Strings :" + receivedStrings);
//			ByteArrayInputStream bais = new ByteArrayInputStream(numPackets.getData());
//			DataInputStream dis = new DataInputStream(bais);
			int packNum = Integer.parseInt(receivedStrings);
			System.out.println("Number of packs:"+packNum);
			
			//get the filename of the packets being sent
			String fileName = "";
			buffer = new byte[MAX_PACKET_SIZE];
//			DatagramPacket arrivalPacket = new DatagramPacket(buffer,buffer.length);
//			socketClient.receive(arrivalPacket);
//			fileName = new String(arrivalPacket.getData(),"US-ASCII");
			receivedBytes = serverListener.ReadData();
			receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
			System.out.println("received FileName : " + receivedStrings);
			fileName = receivedStrings;
			
//			reply = new DatagramPacket(arrivalPacket.getData(), arrivalPacket.getLength(), arrivalPacket.getAddress(), arrivalPacket.getPort());
//			socketClient.send(reply);
			sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(receivedStrings.getBytes()).getBytes()));
			//this part may has some problems
			//get the contents of the file being sent
			byte[] data = new byte[MAX_PACKET_SIZE*packNum];
			int dataIndex = 0;
			int lastindex = 0;
			for(int i = 0; i < packNum; i++)
			{
				receivedBytes = serverListener.ReadData();
				receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
				
				System.arraycopy(receivedStrings.getBytes(), 0, data, lastindex, receivedStrings.getBytes().length);
				lastindex = receivedStrings.getBytes().length;
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
		int index=0;

		for (index=(array.length - 1); index>0; index--) {
			
		if (array[index] != (byte)(0)) {

		return index;
		}
		}
		System.out.println("No meaningful bytes found.  Perhaps this is an array full of nulls...");
		return index;
	}
	
}
