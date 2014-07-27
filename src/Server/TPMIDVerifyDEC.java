package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.swing.JOptionPane;


public class TPMIDVerifyDEC {
	
	static final int MAX_PACKET_SIZE = 32;
	private static DatagramSocket socketServer = null;
	private static String clientIP = "172.17.48.174";
    private static int clientPort = 2228;
    private static InetAddress clientIPAddress;
	
    //TPM Identity Attestation
	public static void sigVerify(){
		
		
    try {
    	    RND genkey = new RND();
    	    byte[] servNonce = new byte[128];
    	    for(int i=0; i<4; i++){
    	    	System.arraycopy(genkey.getRandom(), 0, servNonce, 32*i, 32);    	    	
    	    }
			socketServer = new DatagramSocket(clientPort);
			clientIPAddress = InetAddress.getByName(clientIP);
			//send the server nonce to client
			DatagramPacket serverNonce = new DatagramPacket(servNonce, servNonce.length, clientIPAddress, clientPort);
			socketServer.send(serverNonce);
			//set a timer for the socket to remain open
			//socketServer.setSoTimeout(15000);
			byte[] buffer = new byte[65507];
			
			//receive the TPM Nonce		
			DatagramPacket TPMNonce = new DatagramPacket(buffer,buffer.length);
			socketServer.receive(TPMNonce);
			byte[] data = findNulls(TPMNonce.getData());
			byte[] TPMnonce = data;
			DatagramPacket reply = new DatagramPacket(TPMNonce.getData(), TPMNonce.getLength(), TPMNonce.getAddress(), TPMNonce.getPort());
			socketServer.send(reply);
			
			byte[] Nonce = new byte[256];
			
			//combine two nonces
			System.arraycopy(servNonce, 0, Nonce, 0, servNonce.length);
			System.arraycopy(TPMnonce, 0, Nonce, servNonce.length, TPMnonce.length);
			
			//receive the signature from TPM
			byte[] sign = new byte[65507];
			DatagramPacket recsign = new DatagramPacket(sign,sign.length);
			socketServer.receive(recsign);
			byte[] signature = findNulls(recsign.getData());
			
			// try to verify signature (in Java) with the public key
			Signature sig = Signature.getInstance("SHA1withRSA");
			
			
			//read the public key from pub.txt
			byte[] pKey = FromHex("pub.txt");
			
			//make the public key
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pKey);
			PublicKey pubKey = keyFactory.generatePublic(publicKeySpec);
			
			// assign public key
			sig.initVerify(pubKey);
									
			byte[] digest = Hash.hash(Nonce);
			// assign hash as data value
			sig.update(digest);
									
			boolean verificationOk = sig.verify(signature);
									
			if(verificationOk){
				DatagramPacket sigreply = new DatagramPacket("verified!".getBytes(), "verified!".length(), clientIPAddress, clientPort);
				socketServer.send(sigreply);
				byte[] cor = new byte[100];
				DatagramPacket correply = new DatagramPacket(cor, cor.length);
				socketServer.receive(correply);
			  talk("Index.piece");
			}else{
				JOptionPane.showMessageDialog(null, "Signature is not verified!");
				System.exit(0);
			}
			socketServer.close();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	//sends the input file to server
		public static void talk(String fileName)
		{
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
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    final DataOutputStream dos = new DataOutputStream(baos);
				dos.writeInt(numPackets);
				dos.close();
				byte[] buff = baos.toByteArray();
							
				//saves the filename to a byte array
				final ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
				final DataOutputStream dos2 = new DataOutputStream(baos2);
				dos2.writeBytes(echoFileName);
			    dos2.close();
				byte[] fileName2 = baos2.toByteArray();
				if (numPackets >= 1) 
				{
					int c=0;
					//sends the number of packets being sent
					DatagramPacket numPacks = new DatagramPacket(buff,buff.length,clientIPAddress,clientPort);
					socketServer.send(numPacks);
					DatagramPacket expectedPacket = new DatagramPacket(buffer, buffer.length);
					socketServer.receive(expectedPacket);
								
					//sends the name of the file
					DatagramPacket nameFile = new DatagramPacket(fileName2,fileName2.length,clientIPAddress,clientPort);
					socketServer.send(nameFile);
					expectedPacket = new DatagramPacket(buffer, buffer.length);
					socketServer.receive(expectedPacket);
					System.out.println(new String(expectedPacket.getData(),"US-ASCII"));
								
					//sends the contents of the file
					for(int i=0;i<echoBytes.length;i++){
					        buffer[c] = echoBytes[i];
					        c++;
					    if(c==MAX_PACKET_SIZE){
							DatagramPacket requestPacket = new DatagramPacket(buffer,buffer.length,clientIPAddress,clientPort);
							socketServer.send(requestPacket);
							buffer = new byte[MAX_PACKET_SIZE];
							c=0;
							expectedPacket = new DatagramPacket(buffer, buffer.length);
							socketServer.receive(expectedPacket);
							System.out.println(new String(expectedPacket.getData(),"US-ASCII"));
							buffer = new byte[MAX_PACKET_SIZE];
						}
					}
								
					//sends the last bit of contents if there are any
					/*DatagramPacket requestPacket = new DatagramPacket(buffer,buffer.length,clientIPAddress,clientPort);
					socketServer.send(requestPacket);
					expectedPacket = new DatagramPacket(buffer, buffer.length);
					socketServer.receive(expectedPacket);
					System.out.println(new String(expectedPacket.getData(),"US-ASCII"));*/
								
					//deletes the piece that was sent
					deleteFile(fileName);
					deleteFile("pub.txt");
				}
			}catch (SocketException e)
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
						        
				input.close();
			     } catch (FileNotFoundException ex) {
				 } catch (IOException ex) {}
			return result;
		   }
						
		//deletes a file
	    private static void deleteFile(String file)
	    {
			File f1 = new File(file);
			f1.delete();
		}
	
	
	/**  
     * Convert char to byte  
     * @param c char  
     * @return byte  
     */  
     private static byte charToByte(char c) {    
        return (byte) "0123456789ABCDEF".indexOf(c);    
   }
	
	/**  
     * Convert hex string to byte[]  
     * @param hexString the hex string  
     * @return byte[]  
     */  
     public static byte[] hexStringToBytes(String hexString) {    
        if (hexString == null || hexString.equals("")) {    
            return null;    
       }   
        hexString = hexString.toUpperCase();   
         int length = hexString.length() / 2;    
        char[] hexChars = hexString.toCharArray();    
        byte[] d = new byte[length];    
        for (int i = 0; i < length; i++) {    
            int pos = i * 2;    
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
       }   
         return d;    
   }
	
	private static byte[] FromHex(String filename) throws IOException {
	    BufferedReader reader = new BufferedReader( new FileReader (filename));
	    String line  = null;
	    StringBuilder stringBuilder = new StringBuilder();
//		    String ls = System.getProperty("line.separator");
	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
//		        stringBuilder.append( ls );
	    }
	    String sIV = stringBuilder.toString();
	    byte[] bIV = hexStringToBytes(sIV);
	    reader.close();
	    return bIV;
	}
}
