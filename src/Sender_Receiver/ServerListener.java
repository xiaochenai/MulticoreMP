package Sender_Receiver;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import javax.crypto.*;
import javax.crypto.spec.*;	

import IDACS_Server_Connection.*;
import IDACS_Common.*;
import IDACS_Connection.IDACSConnection;
import biz.source_code.base64Coder.Base64Coder;
  
  
  
   public class ServerListener
   {
   	   	
      static final int NUM_GARBAGE_OPERATIONS = 5;
      static final int DEFAULT_SENDER_IN_PORT = 9031;
	  static final int DEFAULT_LISTENER_IN_PORT = 9032;
	  private String RmtIP;
	  private int RmtPort;
	  private int LocalPort;
	  private InetAddress RmtC;
	  private IDACSServerConnection theConnection;

   	  public void init(String RmtIP,int RmtPort,int LocalPort) throws UnknownHostException{
   		  this.RmtIP = RmtIP;
		  this.LocalPort = LocalPort;
		  this.RmtPort = RmtPort;
		  this.RmtC = InetAddress.getByName("127.0.0.1");
		  this.theConnection = new IDACSServerConnection( null, IDACSCommon.DEFAULT_SENDER_IN_PORT, IDACSCommon.DEFAULT_LISTENER_IN_PORT );
   	  }
	  public void init() throws UnknownHostException{
		  this.RmtIP = RmtIP;
		  this.LocalPort = DEFAULT_SENDER_IN_PORT;
		  this.RmtPort = DEFAULT_LISTENER_IN_PORT;
		  this.RmtC = InetAddress.getByName("127.0.0.1");
		  this.theConnection = new IDACSServerConnection( null, IDACSCommon.DEFAULT_SENDER_IN_PORT, IDACSCommon.DEFAULT_LISTENER_IN_PORT );
		  System.out.println("Finish Initialization");
		  this.theConnection.ceil = 20000000;
	  }
	  public void init(int RmtPort,int LocalPort) throws UnknownHostException{
		  this.LocalPort = LocalPort;
		  this.RmtPort = RmtPort;
		  this.RmtC = null;
		  this.theConnection = new IDACSServerConnection( null, IDACSCommon.DEFAULT_SENDER_IN_PORT, IDACSCommon.DEFAULT_LISTENER_IN_PORT );
		  System.out.println("Finish Initialization");
	  }
	  public byte[] ReadData() throws IOException{
		  byte[][] aa = null;
		  int i=0;
		  while(( aa = this.theConnection.readData())==null) {
			  i++;
			  
		  }
		  if(aa != null){
			  //System.out.println("RECEIVED DATA " + new String(Base64Coder.decodeLines(new String(processData(aa)))));
			  return processData(aa);
		  }
		  else {
			return null;
		}
	  }
	  public void close(){
		  theConnection.close();
	  }
	  public boolean isInterrupted(){
		  if(theConnection != null)
		  return theConnection.isInterrputed();
		  else {
			  System.out.println("theConnection is null");
			return true;
		}
	  }
	  public String getIP(){
		  return theConnection.CurrentSessionIP;
	  }
//      public static void main( String[] args) throws IOException, NoSuchAlgorithmException 
//      {
//               		
//         // create and instance of IDACSConnection (which starts the listener)
//         //IDACSServerConnection theConnection = new IDACSServerConnection( null, IDACSCommon.DEFAULT_SENDER_IN_PORT, IDACSCommon.DEFAULT_LISTENER_IN_PORT );
//      	 long start=0,end=0,accumulator=0;
//      	 //theConnection.ceil = Integer.parseInt(args[0]);
//         System.out.println("Server Listener running.");
//         ServerListener sL = new ServerListener();
//         sL.init();
//      			         		
//         // run the main program  
//         do
//         {
//            // read data received from 
//        	start = System.currentTimeMillis();
//            byte[] data = sL.ReadData();
//           
//    
//            
//         		      	
//            // garbage operations
//            if(data != null)
//            {	
//            	//String RMTIP = theConnection.CurrentSessionIP;
//            	//long ReTime = theConnection.ReTime;
//            	start = System.currentTimeMillis();
//				//byte[] Filedata = processData(data);
//				System.out.println("data : " + new String(Base64Coder.decodeLines(new String(data))));
////				MessageDigest hash =  MessageDigest.getInstance("SHA-1");
////				hash.update(Filedata);
////				byte[] HashValue = hash.digest();
////				String HashInBase64 = Base64Coder.encodeLines(HashValue);
////				FileOutputStream fos = new FileOutputStream("Hash of Received Data From" + RMTIP + ".txt",true);
////				fos.write((HashInBase64 ).getBytes());
////				fos.close();
////				end = System.currentTimeMillis();
//
//
//    			
//    			
//            	
//    			sL.close();
//            		
//            }// end garbage operations
//         		
//         		                        
//         }
//            while(true);         
//      	
//      }
   	
   	  private static byte[] processData(byte[][] data){
   		  byte[] filedata = new byte[data.length*1441];
   		  for(int index=0;index<data.length;index++){
   			  System.arraycopy(data[index], 0, filedata, index*1441, data[index].length);
   		  }
   		  byte[] FinalData = findNulls(filedata);
   		  return FinalData;
   	  }
      public static void AESencrypt(byte[] input) throws Exception
      {
            
       	// Get the KeyGenerator
         KeyGenerator kgen = KeyGenerator.getInstance("AES");
         kgen.init(128); // 192 and 256 bits may not be available
      
       	// Generate the secret key specs.
         SecretKey skey = kgen.generateKey();
         byte[] raw = skey.getEncoded();
      
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
      
      
       	// Instantiate the cipher      
         Cipher cipher = Cipher.getInstance("AES");
      
      	// encrypt
         cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
         byte[] encrypted = cipher.doFinal(input);
      
      	// decrypt
         cipher.init(Cipher.DECRYPT_MODE, skeySpec);
         byte[] original = cipher.doFinal(encrypted);
      
      
         return;				
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

	//remove non meaningful bytes from byte[] buffer	
	private static byte[] findNulls(byte[] buffer)
		{
			int terminationPoint = findLastMeaningfulByte(buffer);
			byte[] output;
			output = new byte[terminationPoint + 1];
			System.arraycopy(buffer, 0, output, 0, terminationPoint + 1);
			return output;
		}
   	
   	
      public static void SHA1Hash(byte[] input) throws Exception
      {      
         MessageDigest digest = MessageDigest.getInstance("SHA-1");
         digest.reset();
         byte[] output = digest.digest(input);
       
         return;
      }
   	
   		
   }