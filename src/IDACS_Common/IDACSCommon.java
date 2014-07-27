   package IDACS_Common;
 
   import java.io.*;
   import java.net.*;
   import java.util.*;

   public class IDACSCommon   
	{
	
		public static final int MAX_UDP_PACKET_LENGTH = 1450;
		public static final int RECEIVE_QUEUE_LENGTH = 10000;
		public static final int SENT_QUEUE_LENGTH = 10000;
		public static final int MIN_FRAGMENT_SIZE_TO_SACK = 5;
		
	   public static final int DEFAULT_SENDER_IN_PORT = 9031;
      public static final int DEFAULT_LISTENER_IN_PORT = 9032;
		
		public static final int MAX_UDP_BUFFER_SIZE = 1000000; // about 1 MB
		
		public static final int LISTENER_TIMEOUT_INTERVAL = 1;		//on UDP read, wait 1 ms for packet to be available to read before giving up
		public static final int NO_PACKET_RECEIVED_SLEEP_TIME = 1;	// if no packet received, sleep 2 milliseconds before trying again
		
		public static final int MAXIMUM_SACKS_PER_PACKET = 3;
		public static final int ACK_SACK_REDUNDANCY = 3;				// if number of SACK fragments <= "MAXIMUM_SACKS_PER_PACKET", this is how many redundant packets with SACKS will be sent
			
		public static final byte ALL_FLAGS_UNSET = (byte) 0x00;
      public static final byte ACKED_FLAG = (byte) 0x80;
      public static final byte SACKED_FLAG = (byte) 0x40;
   	
   	
      public static final int ACK_PACKET_PAYLOAD_LENGTH = 9;  
		public static final int BASE_SACK_PACKET_PAYLOAD_LENGTH = 10;
		public static final int BASE_DATA_PACKET_PAYLOAD_LENGTH = 9;
   	
      public static final byte DATA_MESSAGE_TYPE = (byte) 0x01;
      public static final byte ACK_MESSAGE_TYPE = (byte) 0x02;
      public static final byte ACK_WITH_SACK_MESSAGE_TYPE = (byte) 0x03;
   	
		public static final int WAIT_FOR_NETWORK_TO_PERCOLATE_TIME = 20;
      public static final int SEND_SACK_BACK_TO_SENDER_TIMER_VALUE = 20;	// 2 milliseconds after last packet from a flicght is received, transmit SACK messages
      public static final int RETRNASMIT_NON_ACKED_SACKED_PACKETS_TIMER_DURATION = 2000;	// retransmit non-ACK or -SACKed packets 5 milliseconds after last ACK/SACK packet is received
   	public static final int WAIT_FOR_FINAL_ACK_TIMER_DURATION = 20000;	// wait for final ACK on a packet or flight of packets for 20 seconds for each packet sent before retransmitting the entire flight
		public static final int MAX_RETRANSMIT_ATTEMPTS = 4;
		public static final int WAIT_FOR_MORE_PACKETS_IN_FLIGHT_BEFORE_CANCEL = (3*WAIT_FOR_FINAL_ACK_TIMER_DURATION) / 2;	// how long the IDACSListener waits for another packet in the current flight before cancelling it
   	public static final int KEEP_IDLE_SESSION_DURATION = 180000;		// keep a session for 180 seconds before discarding it - MUST BE LARGER THAN ALL THE OTHER TIMERS	
		
		public static final int SERVER_UDP_BUFFER_SIZE = 2000000;		// use 100 MB for UDP buffer space for server
	
		//===========================
		// Server variables
		//============================
		public static final int MAX_CONCURRENT_SESSIONS_FOR_SERVER = 100;
   
    //********************************************************
    //  int byteArrayToInt()
    //
    //  inputs:
    //      byte[] b - the byte array to be converted to an integer
    //
    //  This function converts a byte array to an integer
    //********************************************************                
      public static int byteArrayToInt(byte[] b) 
      {
         int result = 0;
        
         try
         {   
            if(b.length >= 1)
            {result += (b[b.length - 1] & 0xFF);}                
            if(b.length >= 2)
            {result += ((b[b.length - 2] & 0xFF) << 8);}
            if(b.length >= 3)
            {result += ((b[b.length - 3] & 0xFF) << 16);}
            if(b.length >= 4)
            {result += (b[b.length - 4] << 24);}
            
            return result;
         }
            catch(NullPointerException npe){
               return 0;}
      }
    //****************************************
    //  end byteArrayToInt() function
    //****************************************    
    
    
    //********************************************************
    //  int intToByteArray()
    //
    //  inputs:
    //      int b - the integer to be converted to a byte array
    //
    //  This function converts an integer into a byte array
    //********************************************************                
      public static byte[] intToByteArray(int value) 
      {
         try
         {   
            return new byte[] {(byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)(value)};     }
            catch(NullPointerException npe){
               return null;}                
      }
    //****************************************
    //  end intToByteArray() function
    //****************************************    
    
    
      public static byte[] getSubArray(byte[] array, int offset, int length) 
      {
         byte[] result = new byte[length];
         System.arraycopy(array, offset, result, 0, length);
         return result;
      }	 
    
    
    
    
   }