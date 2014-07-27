package PC;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tpm.TcTpmPubkey;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcTssContextFactory;
import iaik.tc.tss.impl.csp.TcCrypto;
//import iaik.tc.tss.test.tsp.java.TestDefines;
import iaik.tc.utils.logging.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.swing.JOptionPane;




import biz.source_code.base64Coder.Base64Coder;
import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;

public class TpmIDVerifyENC {
	
	private static DatagramSocket socketClient = null;
	static final int MAX_PACKET_SIZE = 32;
	private static String servIP = "192.168.0.106";
    private static int servPort = 2228;
    private static InetAddress serverIPAddress;

	public static void CreateSigningKey(Sender Sender, ServerListener ServerListener) throws UnknownHostException, BindException {
		Sender sender = Sender;
		ServerListener serverListener = ServerListener;
		if(sender.isInterrupted()){
			System.out.println("sender is not binded");
			sender.init(servIP);
			System.out.println("finish initilization");
		}
		else {
			System.out.println("sender is  binded");
			sender.close();
			sender.init(servIP);
		}
		if(serverListener.isInterrupted()){
			System.out.println("listener is not binded");
			serverListener.init();
		}else{
			serverListener.close();
			serverListener.init();
		}
		try {
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

			
			// create AIK key container
			TcIRsaKey aikKey = context.createRsaKeyObject( //
					TcTssConstants.TSS_KEY_SIZE_2048 | //
							TcTssConstants.TSS_KEY_TYPE_SIGNING | //
							TcTssConstants.TSS_KEY_MIGRATABLE |
							TcTssConstants.TSS_KEY_AUTHORIZATION);
			
			aikKey.setAttribUint32(TcTssConstants.TSS_TSPATTRIB_KEY_INFO, TcTssConstants.TSS_TSPATTRIB_KEYINFO_SIGSCHEME, TcTssConstants.TSS_SS_RSASSAPKCS1V15_SHA1);
			
			
			// set secret for AIK key
			//input authentication secret for AIK
			String TPMSignsecret = JOptionPane.showInputDialog("Please create TPM Signing Key Authentication Secret Here!");
			
			//create usage and migration secret policy object and assigned to AIK key object
			TcBlobData keyUsageSecret = TcBlobData
					.newString(TPMSignsecret);
			TcBlobData keyMigrationSecret = TcBlobData
					.newByteArray(TcTssConstants.TSS_WELL_KNOWN_SECRET);
			TcIPolicy keyUsgPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			TcIPolicy keyMigPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
			keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyUsageSecret);
			keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyMigrationSecret);
			keyUsgPolicy.assignToObject(aikKey);
			keyMigPolicy.assignToObject(aikKey);
			
			// create singing key and load it
			aikKey.createKey(srk, null);
			
			// Store the created key on the HDD for later use
			TcTssUuid keyUUID = TcUuidFactory.getInstance()
					.getUuidU1SK2();
			
			/*context.unregisterKey(TcTssConstants.TSS_PS_TYPE_SYSTEM,
								TcUuidFactory.getInstance()
								.getUuidU1SK2());*/
			
			//register the binding key with keyUUID
			context.registerKey(aikKey, TcTssConstants.TSS_PS_TYPE_SYSTEM,
					keyUUID, TcTssConstants.TSS_PS_TYPE_SYSTEM, TcUuidFactory
							.getInstance().getUuidSRK());
						
			

			Log.info("key1 registered in persistent system storage with "
					+ keyUUID.toString());

			// Load the key into a key slot of the TPM
			aikKey.loadKey(srk);
			
			

			// get public part of signKey
			TcTpmPubkey pubSignKey = new TcTpmPubkey(aikKey.getAttribData(TcTssConstants.TSS_TSPATTRIB_KEY_BLOB,
					TcTssConstants.TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY));
			
			// convert public TPM key into java key
			PublicKey pubSignKeyJava = TcCrypto.pubTpmKeyToJava(pubSignKey);
			
			context.closeContext();
			
			//Send the public key to the server
			byte[] pubKey = pubSignKeyJava.getEncoded();
			
			try {
//				serverIPAddress =  InetAddress.getByName(servIP);
//				socketClient = new DatagramSocket(servPort);
//				//set a timer for the socket to remain open
//				//socketServer.setSoTimeout(15000);
//				byte[] buffer = new byte[MAX_PACKET_SIZE];
//				buffer = "ENC".getBytes();
//						
//				DatagramPacket Connection = new DatagramPacket(buffer,buffer.length, serverIPAddress, servPort);
//				socketClient.send(Connection);
				//send ENC to server
				sender.SendData(sender.PreparePacket(Base64Coder.encodeLines("ENC:1".getBytes()).getBytes()));
				System.out.println("ENC Packet sent!");
				
//				byte[] inData = new byte[100];
//				DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
//				socketClient.receive(inPacket);
				//receive response
				byte[] receivedBytes = serverListener.ReadData();
				String receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
				System.out.println("received response : " + receivedStrings);
				//int inPacketLength = inPacket.getLength();
				//send pub key to server
//				DatagramPacket pubKeyConnection = new DatagramPacket(pubKey, pubKey.length, serverIPAddress, servPort);
//				socketClient.send(pubKeyConnection);
				sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(("pubkey:" + new String(pubKey)).getBytes()).getBytes()));
				System.out.println("Packet sent!");
				
				//receive response
				byte[] rep = new byte[1400];
//				DatagramPacket repPacket = new DatagramPacket(rep, rep.length);
//				socketClient.receive(repPacket);
				//int inPacketLength = inPacket.getLength();
				
				
				System.out.println("Public Key has been sent and saved!");
				
				//send the index.piece to server
				talk("Index.piece", sender, serverListener);
				
//				socketClient.close();
				
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
		} catch (TcTssException e) {
			
		}
	}
	
	//sends the input file to server
		public static void talk(String fileName,Sender Sender,ServerListener ServerListener)
		{
			Sender sender = Sender;
			ServerListener serverListener = ServerListener;
			File oldFile = new File(fileName);
			String echoFileName = oldFile.getName();
			byte[] echoBytes = null;
			int numPackets = 0;

				System.out.println("filename: "+echoFileName);
				//gets the contents of the file
				echoBytes = read(fileName);
				System.out.println("contents(bytes): " + echoBytes);
			
			//determines the number of packets needed to send
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
//					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			        final DataOutputStream dos = new DataOutputStream(baos);
//			        dos.writeInt(numPackets);
//			        dos.close();
//					byte[] buff = baos.toByteArray();
//					
//					//saves the filename to a byte array
//					final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//					final DataOutputStream dos2 = new DataOutputStream(baos2);
//					dos2.writeBytes(echoFileName);
//			        dos2.close();
//					byte[] fileName2 = baos2.toByteArray();
					if (numPackets >= 1) 
					{
						int c=0;
						//sends the number of packets being sent
//						DatagramPacket numPacks = new DatagramPacket(buff,buff.length,serverIPAddress,servPort);
//						socketClient.send(numPacks);
//						DatagramPacket expectedPacket = new DatagramPacket(buffer, buffer.length);
//						socketClient.receive(expectedPacket);
						sender.SendData(sender.PreparePacket(Base64Coder.encodeLines((numPackets+"").getBytes()).getBytes()));
						byte[] receivedBytes = ServerListener.ReadData();
						String receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
						
						//sends the name of the file
//						DatagramPacket nameFile = new DatagramPacket(fileName2,fileName2.length,serverIPAddress,servPort);
//						socketClient.send(nameFile);
//						expectedPacket = new DatagramPacket(buffer, buffer.length);
//						socketClient.receive(expectedPacket);
						
						sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(echoFileName.getBytes()).getBytes()));
						receivedBytes = serverListener.ReadData();
						receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
						System.out.println("received replies : " + receivedStrings);
						
						//sends the contents of the file
						for(int i=0;i<echoBytes.length;i++){
							buffer[c] = echoBytes[i];
							c++;
							if(c==MAX_PACKET_SIZE){
//								DatagramPacket requestPacket = new DatagramPacket(buffer,buffer.length,serverIPAddress,servPort);
//								socketClient.send(requestPacket);
								sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(buffer).getBytes()));
								buffer = new byte[MAX_PACKET_SIZE];
								System.out.println("send to server : " + new String(buffer));
								c=0;
//								expectedPacket = new DatagramPacket(buffer, buffer.length);
//								socketClient.receive(expectedPacket);
								receivedBytes = serverListener.ReadData();
								receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
								System.out.println("reply from server " + receivedStrings);
								buffer = new byte[MAX_PACKET_SIZE];
							}
						}
						
						//sends the last bit of contents if there are any
//						DatagramPacket requestPacket = new DatagramPacket(buffer,buffer.length,serverIPAddress,servPort);
//						socketClient.send(requestPacket);
						sender.SendData(sender.PreparePacket(Base64Coder.encodeLines(buffer).getBytes()));
						System.out.println("send to server : " + new String(buffer));
//						expectedPacket = new DatagramPacket(buffer, buffer.length);
//						socketClient.receive(expectedPacket);
						receivedBytes = serverListener.ReadData();
						receivedStrings = new String(Base64Coder.decodeLines(new String(receivedBytes)));
						System.out.println("received reply from server" + receivedStrings);
						
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
				 private static void deleteFile(String file)
				 {
					  File f1 = new File(file);
					  f1.delete();
				 }

}

