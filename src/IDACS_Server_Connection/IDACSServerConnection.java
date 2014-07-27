   package IDACS_Server_Connection;  
	
   import IDACS_Common.*;
   import IDACS_Server_Connection.*;
	
	
   import java.io.*;
   import java.net.*;
   import java.nio.channels.*;
import java.util.*;

   public class IDACSServerConnection
   {
   
      private IDACSServerListener theListener;
   	
      public IDACSSession[] ActiveSessions = new IDACSSession[IDACSCommon.MAX_CONCURRENT_SESSIONS_FOR_SERVER];
      public int highestActiveSessionIndex;
      public static String CurrentSessionIP;
      private InetAddress remoteComputerIP;
      private int remoteListenerPort;
      private int localListenerPort;
      private InetAddress remoteAddress;
      public long ReTime=0;
      private HashMap<String,Integer> Counter = new HashMap<String,Integer>();
	  public HashMap<String,Long> ST = new HashMap<String,Long>();
      public boolean ListenerKeepRunning = true;	// if false, kill the listener	
   	  public int ceil=0;
      int LastSessionDataReadFrom = 0;
   	   
   
   	//=====================
   	//  Constructor
   	//=====================
      public IDACSServerConnection( InetAddress remoteComp, int remotePt, int localPt )
      {
         remoteComputerIP = remoteComp;
         remoteListenerPort = remotePt;
         localListenerPort = localPt;
      	 
         //SenderSequenceNumber = new Random().nextInt(Integer.MAX_VALUE);
         highestActiveSessionIndex = 0;
      	
      	// start the listener thread
         theListener = new IDACSServerListener(localListenerPort, this);
         theListener.start();
      }
   
      public void close(){
    	  theListener.close();
    	  theListener.interrupt();
    	  System.out.println("Thread is closed : " + theListener.interrupted());
      }
      public boolean isInterrputed(){
    	  return theListener.interrupted();
      }
   
   	//======================
   	//	Functions
   	//======================
   	
   	
   	//===========================================================================
   	//	function sendData()
   	//
   	//	inputs:
   	//		byte[][] data - an array of byte arrays that contain the
   	//							 data payloads for packets to be sent to the listener
   	//
   	//	sends data packets to the listener and waits for them to be ACKed
   	//===========================================================================		
      public boolean sendData(byte[][] data, int remoteIP, int remotePort)
      {
      
         int numberSentQueueSpacesLeft = 0;
         int thisFlightInitialSequenceNumber = 0;
         int thisFlightFirstPacketIndex = 0;
         int numberFullRetransmitAttempts = 0;	//how many times entire flight has been retransmitted after "WaitForACKTimeout" has expired
         boolean TransmitFAIL = false;
      	
         boolean retransmitNonACK_SACKedPacketsTimerACTIVE = false;			
      	
         int currentSessionIndex = -1;
      
      	//=========================================================
      	//	Determine which Session we are using to send the data
      	//=========================================================
      	// use existing Session
         if(remoteIP != 0)
         {
            for(int index = 0; index <= highestActiveSessionIndex; index++)
            {
               if((ActiveSessions[index] != null) && (ActiveSessions[index].remoteComputerIP == remoteIP))
               {
                  currentSessionIndex = index;
                  break;
               }
            }			
         }
      	
      	// new session
         if(currentSessionIndex == -1)
         {
         			// find an empty space in "ActiveSessions"
            for(int index = 0; index < ActiveSessions.length; index++)
            {
               if(ActiveSessions[index] == null)
               {
                  currentSessionIndex = index;
                  break;
               }
            }
         			
         	// if no empty spaces, ERROR
            if(currentSessionIndex == -1)
            {
               System.out.println("Packets cannot be sent because maximum number of sessions open!");
               return false;
            }
            // set up the connection information
            else
            {
            	// update "arrayPositionCurrentHighestActiveSession"
               if(currentSessionIndex > highestActiveSessionIndex)
                  highestActiveSessionIndex = currentSessionIndex;
            		
               ActiveSessions[currentSessionIndex] = new IDACSSession(remoteIP, remotePort, localListenerPort);							
            }															
         }
         IDACSSession currentSession = ActiveSessions[currentSessionIndex];   
			// indicate that session is not idle
			currentSession.KillIdleSessionTimeout.reset();			   
      
      
      	//======================================
      	//	check restrictions on "data" length
      	//======================================
         if(data.length > IDACSCommon.SENT_QUEUE_LENGTH)
         {
            System.out.println("Flight exceeds maximum flight size!");
            return false;
         }
      	// see how many spaces left in "SentQueue"
         if (currentSession.SentQueueLeftEdge <= currentSession.SentQueueRightEdge)
         {numberSentQueueSpacesLeft = currentSession.SentQueueLeftEdge + currentSession.SentQueue.length - currentSession.SentQueueRightEdge - 1;}
         else
         {numberSentQueueSpacesLeft = currentSession.SentQueueLeftEdge - currentSession.SentQueueRightEdge - 1;}
      	
         if(numberSentQueueSpacesLeft < data.length)
         {
            System.out.println("Flight size exceeds available space in IDACS buffer!");
            return false;			
         }
      	
      
         try
         {
         
         	// create socket to send packets   
            DatagramSocket theSocket = new DatagramSocket();
         
            for(int index = 0; index < data.length; index++)
            {
            //============================
            //	Package data into packets
            //============================
               byte[] payload = new byte[IDACSCommon.BASE_DATA_PACKET_PAYLOAD_LENGTH + data[index].length];
            
            // sequence number
               int thisPacketSequenceNumber = currentSession.getSenderSequenceNumber();               
            //------------------------------
               if(index == 0)
               {thisFlightInitialSequenceNumber = thisPacketSequenceNumber;}
            //------------------------------
               System.arraycopy(IDACSCommon.intToByteArray(thisPacketSequenceNumber), 0, payload, 0, 4);
            
            // packet type
               payload[4] = IDACSCommon.DATA_MESSAGE_TYPE;
            
            // flight size
               byte[] flightSize = IDACSCommon.intToByteArray(data.length);
               payload[5] = flightSize[2];
               payload[6] = flightSize[3];
            
            // data offset
               byte[] dataOffset = IDACSCommon.intToByteArray(index);
               payload[7] = dataOffset[2];
               payload[8] = dataOffset[3];
            
            // data
               System.arraycopy(data[index], 0, payload, 9, data[index].length);
            	
            //============================
            //	Send packets
            //============================	
               DatagramPacket theOutPacket = new DatagramPacket( payload, payload.length, InetAddress.getByAddress(IDACSCommon.intToByteArray(currentSession.remoteComputerIP)), currentSession.remoteListenerPort );
               try
               {			            
                  theSocket.send( theOutPacket ); 
               }
                  catch(PortUnreachableException pue){System.out.println(pue);}
                  catch(IOException ioe){System.out.println(ioe);}						       
                  catch(IllegalBlockingModeException ibme){System.out.println(ibme);} 
            
            
            //====================================
            //	Save packets in the "sent" queue
            //====================================
               if(index == 0)
               {thisFlightFirstPacketIndex = currentSession.SentQueueRightEdge;}
			   byte[] FileData = theOutPacket.getData();
			   InetAddress remoteAddress = theOutPacket.getAddress();
               currentSession.SentQueue[currentSession.SentQueueRightEdge] = new IDACSPacketRecord(thisPacketSequenceNumber, data.length, index, thisFlightInitialSequenceNumber, IDACSCommon.ALL_FLAGS_UNSET, theOutPacket,FileData,remoteAddress);
               currentSession.SentQueueRightEdge = (currentSession.SentQueueRightEdge + 1) % currentSession.SentQueue.length;
                     
            }// end "for" loop
         	         
         	// close socket after all packets sent
            theSocket.close();
         	         	
         	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         	//	notify the user how many packets transmitted
           // System.out.println("Transmitted " + data.length + " data packets. Last Sequence Number: ");// + (SenderSequenceNumber - 1));
         	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         	         
         }
            catch(IOException ioe)
            {
               System.out.println(ioe);
               return false;
            }				
      		
      					
      	      				
      	//============================
      	//	Set "WaitForACKTimeout"
      	//============================
         currentSession.WaitForACKTimeout.reset();			
			// indicate that session is not idle
			currentSession.KillIdleSessionTimeout.reset();			   				
      
      	
      	
      	//===============================================================================
      	// enter loop where we wait for
      	//		a) ACK on entire transmission -> return TRUE;
      	//		b) max out the retransmit attempts -> TransmitFAIL == true, return TRUE;
      	//===============================================================================
         while(TransmitFAIL != true)
         {
          	//============================================
         	//	give the network some time to move packets
         	//============================================
            try{Thread.sleep(IDACSCommon.WAIT_FOR_NETWORK_TO_PERCOLATE_TIME);}
               catch(InterruptedException ie){}
         
         
         	//==========================================
         	//	check 'CurrentSendingFlightACKed' flag
         	//==========================================
            if(currentSession.CurrentSendingFlightACKed == true)
            {
               currentSession.CurrentSendingFlightACKed = false;
            	
            	// since the flight has been ACKed, kill these timers
               try
               {
                  currentSession.retransmitNonACK_SACKedPacketsTimer.kill();
                  currentSession.WaitForACKTimeout.kill();					
               }
                  catch(NullPointerException npe){}
            
            	// set Listener priority back to LOW
            	//setListenerPriority_LOW();	  
            	
            	// slide the "sentQueue" window					
               currentSession.SentQueueLeftEdge = (currentSession.SentQueueLeftEdge + data.length) % currentSession.SentQueue.length;
            	
               return true;
            }
         
         	//========================================
         	//	check if 'WaitForACKTimeout' expired
         	//========================================
            if(!currentSession.WaitForACKTimeout.isRunning())
            {				
            	//	if we exceeded the number of allowed retransmit attempts - return FAIL
               if(numberFullRetransmitAttempts >= IDACSCommon.MAX_RETRANSMIT_ATTEMPTS)
               {
                  TransmitFAIL = true;
               	
               	// adjust "SentQueueLeftEdge"
                  currentSession.SentQueueLeftEdge = (currentSession.SentQueueLeftEdge + data.length) % currentSession.SentQueue.length;
               	
                  continue;
               }
            
            	// retransmit all packets in active flight
               try
               {
                  DatagramSocket theSocket = new DatagramSocket();		
               	
               	//============================================================
                	//	Set Listener Priority to LOW while transmitting
               	//============================================================
               	//setListenerPriority_LOW();						
               								
                  for(int index = thisFlightFirstPacketIndex; index != (thisFlightFirstPacketIndex + data.length) % currentSession.SentQueue.length; index = (index + 1) % currentSession.SentQueue.length)
                  {           
                     try
                     { 						            		
                        theSocket.send( currentSession.SentQueue[index].Packet );         	            					
                     }
                        catch(IOException ioe){}
                  }
                  theSocket.close();
               	
               	//============================================================
                	//	Set Listener Priority back to HIGH
               	//============================================================
               	//setListenerPriority_HIGH();						
               	
               }
                  catch(IOException ioe)
                  {}
            	
            	// reset "WaitForACKTimeout"
               currentSession.WaitForACKTimeout.reset();
					// indicate that session is not idle
					currentSession.KillIdleSessionTimeout.reset();			   				
					
            	
               numberFullRetransmitAttempts++;
            	
            	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
               //System.out.println("Re-transmitted ALL packets due to long timer timeout.");
            	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            }
         	
         	
         	
         	//====================================================
         	//	check if 'retransmitNonACK_SACKedPacketsTimer' expired
         	//====================================================
            if((!currentSession.retransmitNonACK_SACKedPacketsTimer.isRunning()) && (currentSession.retransmitNonACK_SACKedPacketsTimerACTIVE == true))
            {								
               currentSession.retransmitNonACK_SACKedPacketsTimerACTIVE = false;
            
            	// retransmit all non-ACK of -SACKed packets in active flight
               try
               {
                  DatagramSocket theSocket = new DatagramSocket();		
               	               								
                  for(int index = thisFlightFirstPacketIndex; index != (thisFlightFirstPacketIndex + data.length) % currentSession.SentQueue.length; index = (index + 1) % currentSession.SentQueue.length)
                  {     
                  	// if packet has not been ACKed or SACKed
                     if(((currentSession.SentQueue[index].Flags & IDACSCommon.ACKED_FLAG) != 0x00) || ((currentSession.SentQueue[index].Flags & IDACSCommon.SACKED_FLAG) != 0x00))
                     {}
                     else
                     {
                        try
                        {
                           theSocket.send( currentSession.SentQueue[index].Packet );         	            					
                        }
                           catch(IOException ioe){}
                     }                  	
                  }               	               	
                  theSocket.close();	
						
						// indicate that session is not idle
						currentSession.KillIdleSessionTimeout.reset();			   										               	
               	
               								
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
               	//	notify the user how many packets transmitted
                 // System.out.println("Re-transmitted data packets.");
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
               				
               }
                  catch(IOException ioe){}
            }    					                  
         
         }// end 'while' loop
      	
      	
      			
      	//=========================================================================================
      	//	if we hit this point, then we have exceeded the maximum number of retransmit attempts
      	//=========================================================================================
      	
      	// slide the "sentQueue" window					
         currentSession.SentQueueLeftEdge = (currentSession.SentQueueLeftEdge + data.length) % currentSession.SentQueue.length;	
      						
         return false;    			  	      
      }
   	
   	//================================================
   	//	function sendACK
   	//
   	//	sends an ACK packet to the specified address
   	//================================================
      public boolean sendACK(int ACKSequenceNumber, InetAddress remoteAddress, int remotePort)
      {
         DatagramSocket theSocket = null;
      	
      	//==================================================
      	//	Find the IDACSSession we need to send ACKs for
      	//==================================================
         int currentSessionIndex = -1;
         int remoteIP = IDACSCommon.byteArrayToInt(remoteAddress.getAddress());
      			
         for(int index = 0; index <= highestActiveSessionIndex; index++)
         {
            if((ActiveSessions[index] != null) && (ActiveSessions[index].remoteComputerIP == remoteIP))
            {
               currentSessionIndex = index;
               break;
            }
         }
         if(currentSessionIndex == -1)
         {
            return false;}
      			
         IDACSSession currentSession = ActiveSessions[currentSessionIndex];
			// indicate that session is not idle
			currentSession.KillIdleSessionTimeout.reset();			   				
			
      	
      	
      
         try
         {
            byte[] Payload = new byte[IDACSCommon.ACK_PACKET_PAYLOAD_LENGTH];
         
         // insert sequence number
            System.arraycopy(IDACSCommon.intToByteArray(currentSession.getSenderSequenceNumber()), 0, Payload, 0, 4);			
         
         // insert message type
            Payload[4] = IDACSCommon.ACK_MESSAGE_TYPE;
         
         // insert ACK sequence number
            System.arraycopy(IDACSCommon.intToByteArray(ACKSequenceNumber), 0, Payload, 5, 4);
         
            theSocket = new DatagramSocket();						
            DatagramPacket theOutPacket = new DatagramPacket( Payload, Payload.length, remoteAddress, remotePort);
         	
         	// add some redundancy to the ACK packet
            for(int index = 0; index < IDACSCommon.ACK_SACK_REDUNDANCY; index++)
            {
               theSocket.send( theOutPacket );
               //System.out.println("ACK packet sent.  ACK Sequence Number: " + ACKSequenceNumber);
            }
         	
            theSocket.close();				
         
            return true;      
         }
            catch (IOException ioe )
            {
               theSocket.close();
               return false;
            }
      }
   	//==============================
   	//	end function sendACK()
   	//==============================
   	
   	
   	
   	//===================================================
   	//	function sendSACKs()
   	//
   	//	send SACK messages to the Data sender for all
   	//	currently received packets in a flight
   	//===================================================		
      public boolean sendSACKs(int firstSequenceNumberInFlight, int remoteAddress)
      {
      
         IDACSSession currentSession = null;
         //DatagramSocket theSocket;
         int firstPacketInFlightIndex = -1;
         int[] currentlyReceivedPackets;
         int FlightSize = 0;
         InetAddress remoteComputerAddress = null;
      	
      	
      	//=======================================================
      	//	Find the correct Session to SACK
      	//=======================================================
         for(int index = 0; index <= highestActiveSessionIndex; index++)
         {
            if((ActiveSessions[index] != null) && (ActiveSessions[index].remoteComputerIP == remoteAddress))
            {
               currentSession = ActiveSessions[index];
               break;
            }
         }
         if(currentSession == null)
         {
            return false;
         }
			// indicate that session is not idle
			currentSession.KillIdleSessionTimeout.reset();			   							
      
      	//=======================================================
      	// find First Flight Sequence Number in 'ReceivedQueue'
      	//=======================================================
         for(int index = 0; index < currentSession.ReceivedQueue.length; index++)
         {
         	// found the first packet in the flight
            if((currentSession.ReceivedQueue[index] != null) && (currentSession.ReceivedQueue[index].SequenceNumber == firstSequenceNumberInFlight))
            {
               firstPacketInFlightIndex = index;
               FlightSize = currentSession.ReceivedQueue[index].FlightSize;
               break;
            }
            // perhaps first packet in the flight is missing, but found A packet in the flight
            else if((currentSession.ReceivedQueue[index] != null) && (currentSession.ReceivedQueue[index].SequenceNumber > firstSequenceNumberInFlight) && (currentSession.ReceivedQueue[index].SequenceNumber < firstSequenceNumberInFlight + currentSession.ReceivedQueue[index].FlightSize))
            {
               int potentialFirstPacketInFlightIndex = (index + currentSession.ReceivedQueue.length - currentSession.ReceivedQueue[index].DataOffset) % currentSession.ReceivedQueue.length;
               if(currentSession.ReceivedQueue[potentialFirstPacketInFlightIndex] == null)
               {
                  firstPacketInFlightIndex = potentialFirstPacketInFlightIndex; 
                  FlightSize = currentSession.ReceivedQueue[index].FlightSize;
               }
            }
         }
      	// if First Packet In Flight not found
         if(firstPacketInFlightIndex == -1)
            return false;
      		
      	// get address of computer we are sending SACKs to
      	//remoteComputerAddress = ReceivedQueue[firstPacketInFlightIndex].Packet.getAddress();
      	
      	
      	//=================================================================================
      	//	step through all of the packet spaces in the buffer to find which are present
      	//=================================================================================
         currentlyReceivedPackets = new int[FlightSize];			// array size can handle worst-case scenario, but is wasteful for most circumstances
         //int FlightSize = currentSession.ReceivedQueue[firstPacketInFlightIndex].FlightSize;
         boolean countingReceivedPackets = false;
         int currentlyReceivedPacketsIndex = 0;
      	
         for(int index = firstPacketInFlightIndex; index != (firstPacketInFlightIndex + FlightSize) % currentSession.ReceivedQueue.length; index = (index + 1) % currentSession.ReceivedQueue.length)
         {
         	// beginning of a received section
            if((currentSession.ReceivedQueue[index] != null) && (countingReceivedPackets == false))
            {
               currentlyReceivedPackets[currentlyReceivedPacketsIndex] = currentSession.ReceivedQueue[index].SequenceNumber;
               currentlyReceivedPacketsIndex++;
               countingReceivedPackets = true;
            }
            // end of a received section
            else if((currentSession.ReceivedQueue[index] == null) && (countingReceivedPackets == true))
            {
               currentlyReceivedPackets[currentlyReceivedPacketsIndex] = currentSession.ReceivedQueue[(index - 1 + currentSession.ReceivedQueue.length) % currentSession.ReceivedQueue.length].SequenceNumber + 1;
               currentlyReceivedPacketsIndex++;	
               countingReceivedPackets = false;		
            	
            	// if fragment is less than a specific size, don't SACK it
               if(currentlyReceivedPackets[currentlyReceivedPacketsIndex - 1] - currentlyReceivedPackets[currentlyReceivedPacketsIndex - 2] <= IDACSCommon.MIN_FRAGMENT_SIZE_TO_SACK)	
               {
                  currentlyReceivedPacketsIndex = currentlyReceivedPacketsIndex - 2;
               }
            }
         }
      	// if we were in the middle of a received section when we hit the end of the flight
         if(countingReceivedPackets == true)
         {
            currentlyReceivedPackets[currentlyReceivedPacketsIndex] = firstSequenceNumberInFlight + FlightSize;
            currentlyReceivedPacketsIndex++;				
         }
      	
      	//========================
      	// send SACK messages
      	//========================
         int numberOfSACKFragments = currentlyReceivedPacketsIndex/2;
         byte[] payload = new byte[1];
         int startReadingSACKFragmentsIndex = 0;
         int ACKSequenceNumber = 0;
      	
      	
      	//----------------------------------------------------------------------------
      	//	determine what will be represented as SACK fragments in the SACK packet   	
      	//---------------------------------------------------------------------------
         // first SACK fragment contains the first Sequence number in the fligt - so ACK the first fragment rather than SACKing
         if((currentlyReceivedPackets[0] == firstSequenceNumberInFlight) && (numberOfSACKFragments > 0))
         {
            ACKSequenceNumber = currentlyReceivedPackets[1];
            numberOfSACKFragments--;
         		
            int[] temp = new int[numberOfSACKFragments*2];
            System.arraycopy(currentlyReceivedPackets, 2, temp, 0, temp.length);
            currentlyReceivedPackets = temp;         		            													
         }
         // first SACK fragment DOES NOT contain the first Sequence Number in the flight - SACK all fragments
         else
         {
            ACKSequenceNumber = firstSequenceNumberInFlight;																	
         }
      	
      	
      	//--------------------------------------------------------------------------------------------------------------
      	// if there are few SACK fragments, can send all fragments in one packet, repeat several times for redundancy
      	//--------------------------------------------------------------------------------------------------------------
         if((numberOfSACKFragments >= 0) && (numberOfSACKFragments <= IDACSCommon.MAXIMUM_SACKS_PER_PACKET))
         {    
         	// set up payload     	
            payload = new byte[IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + ((numberOfSACKFragments) * 2 * 4)];
         	
         	// add packet Sequence Number
            System.arraycopy(IDACSCommon.intToByteArray(currentSession.getSenderSequenceNumber()), 0, payload, 0, 4);
         	
         	// add packet Type
            payload[4] = IDACSCommon.ACK_WITH_SACK_MESSAGE_TYPE;
         	
          	// add ACK Sequence Number
            System.arraycopy(IDACSCommon.intToByteArray(ACKSequenceNumber), 0, payload, 5, 4);
         	
         	// add SACK length
            payload[9] = IDACSCommon.intToByteArray(numberOfSACKFragments)[3];				
         	
         	// add SACK fragments
            for(int index = 0; index < numberOfSACKFragments; index++)
            {
            	// insert SACK fragment Left Edge
               System.arraycopy(IDACSCommon.intToByteArray(currentlyReceivedPackets[index*2]), 0, payload, IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + (index*8), 4);
            	
            	// insert SACK fragment Right Edge
               System.arraycopy(IDACSCommon.intToByteArray(currentlyReceivedPackets[index*2 + 1]), 0, payload, IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + (index*8) + 4, 4);            
            }
         	
         	
         	//-----------------------------------------
         	//	Transmit SACK packet, with redundancy
         	//-----------------------------------------				
            try
            {
               DatagramSocket theSocket = new DatagramSocket();						
               DatagramPacket theOutPacket = new DatagramPacket( payload, payload.length, InetAddress.getByAddress(IDACSCommon.intToByteArray(currentSession.remoteComputerIP)), currentSession.remoteListenerPort );
            
            	// add some redundancy to the ACK packet
               for(int index = 0; index < IDACSCommon.ACK_SACK_REDUNDANCY; index++)
               {
                  theSocket.send( theOutPacket );
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                 // System.out.print("Sent SACK packet A.  ACK Sequence Number: " + ACKSequenceNumber + "  SACK Sequence Numbers: ");
                  for(int index5 = 0; index5 < numberOfSACKFragments*2; index5++)
                  {
                	  //System.out.print(currentlyReceivedPackets[index5] + "  ");
                	  }		
                  System.out.println("");				
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
               }
            
               theSocket.close();													
            }
               catch(IOException ioe)
               {
                  System.out.println(ioe);
                  return false;
               }
         	         	
         }
         
         //---------------------------------------------------------------------------------------------------------
         // there are slightly more SACK fragments, but each SACK fragment is transmitted in multiple SACK packets
         //---------------------------------------------------------------------------------------------------------
         else
         {
         	// set up payload     	
            payload = new byte[IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + (IDACSCommon.MAXIMUM_SACKS_PER_PACKET * 2 * 4)];
         	         	
         	// add packet Type
            payload[4] = IDACSCommon.ACK_WITH_SACK_MESSAGE_TYPE;
         	
          	// add ACK Sequence Number
            System.arraycopy(IDACSCommon.intToByteArray(ACKSequenceNumber), 0, payload, 5, 4);
         	
         	// add SACK length
            payload[9] = IDACSCommon.intToByteArray(IDACSCommon.MAXIMUM_SACKS_PER_PACKET)[3];				
         	
         	
         	// send multiple ACK packets - redundancy
            for(int index = 0; index < numberOfSACKFragments; index++)
            {			
            	// add packet Sequence Number
               System.arraycopy(IDACSCommon.intToByteArray(currentSession.getSenderSequenceNumber()), 0, payload, 0, 4);
            	
            	// add SACK fragments
               for(int index2 = 0; index2 < IDACSCommon.MAXIMUM_SACKS_PER_PACKET; index2++)
               {
               	// insert SACK fragment Left Edge
                  System.arraycopy(IDACSCommon.intToByteArray(currentlyReceivedPackets[(index*2 + index2*2) % (numberOfSACKFragments*2)]), 0, payload, IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + (index2*8), 4);
               
               	// insert SACK fragment Right Edge
                  System.arraycopy(IDACSCommon.intToByteArray(currentlyReceivedPackets[(index*2 + 1 + index2*2) % (numberOfSACKFragments*2)]), 0, payload, IDACSCommon.BASE_SACK_PACKET_PAYLOAD_LENGTH + (index2*8) + 4, 4);            
               }
            	
               try
               {
                  DatagramSocket theSocket = new DatagramSocket();						
                  DatagramPacket theOutPacket = new DatagramPacket( payload, payload.length, InetAddress.getByAddress(IDACSCommon.intToByteArray(currentSession.remoteComputerIP)), currentSession.remoteListenerPort );
                  theSocket.send( theOutPacket );    
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                  //System.out.print("Sent SACK packet B.  ACK Sequence Number: " + ACKSequenceNumber + "  SACK Sequence Numbers: ");
                  for(int index5 = 0; index5 < IDACSCommon.MAXIMUM_SACKS_PER_PACKET; index5++)
                  {
                    // System.out.print(currentlyReceivedPackets[(index*2 + index5*2) % (numberOfSACKFragments*2)] + "  ");
                    // System.out.print(currentlyReceivedPackets[(index*2 + 1 + index5*2) % (numberOfSACKFragments*2)] + "  ");
                  }		
                  System.out.println("");				
               	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                  theSocket.close();
               	
               }
                  catch(IOException ioe){}
            	
            }// end 'for' loop
         
         }// end 'if' statement for if # SACK fragments more ore less than 'MAXIMUM_SACKS_PER_PACKET'
      	
      	
      	//============================
      	// mark ACKED/SACKed packets
      	//============================
      	
      	// ACK packets as necessary
         if(ACKSequenceNumber != firstSequenceNumberInFlight)			
         {
            for(int index = 0; index <= (ACKSequenceNumber - firstSequenceNumberInFlight); index++)
            {
               if((currentSession.ReceivedQueue[(firstPacketInFlightIndex + index) % currentSession.ReceivedQueue.length] != null) &&
               	(currentSession.ReceivedQueue[(firstPacketInFlightIndex + index) % currentSession.ReceivedQueue.length].SequenceNumber == firstSequenceNumberInFlight + index))
               {
                  currentSession.ReceivedQueue[(firstPacketInFlightIndex + index) % currentSession.ReceivedQueue.length].Flags |= IDACSCommon.ACKED_FLAG;
               }
            }			
         }
      	
      	// SACK packets
         for(int index = 0; index < numberOfSACKFragments; index++)
         {
            int beginSackFragmentIndex = (firstPacketInFlightIndex + (currentlyReceivedPackets[index*2] - firstSequenceNumberInFlight)) % currentSession.ReceivedQueue.length;
            int endSackFragmentIndex = (firstPacketInFlightIndex + + (currentlyReceivedPackets[index*2 + 1] - firstSequenceNumberInFlight)) % currentSession.ReceivedQueue.length;
         	
         	// if some mismatch in sequence numbers, abandon this SACK fragment
            if((currentSession.ReceivedQueue[beginSackFragmentIndex].SequenceNumber != currentlyReceivedPackets[index*2]) ||
            	(currentSession.ReceivedQueue[(endSackFragmentIndex - 1 + currentSession.ReceivedQueue.length) % currentSession.ReceivedQueue.length].SequenceNumber != currentlyReceivedPackets[index*2 + 1]))
            {
               continue;
            }
         	
         	// SACK packets
            for(int index2 = beginSackFragmentIndex; index2 != endSackFragmentIndex; index = (index + 1) % currentSession.ReceivedQueue.length)
            {currentSession.ReceivedQueue[index2].Flags |= IDACSCommon.SACKED_FLAG;}
         	
         }
      			
         return true;
      }
   	//==============================
   	//	end function sendSACKs()
   	//==============================
   
   	
   	
      public boolean changeListenerPort()
      {
         return true;
      }
   	
   	
   	
   	//===================================================
   	//	function terminateListener()
   	//
   	//	sets a flag for the listener to terminate itself
   	//===================================================
      public boolean terminateListener()		
      {
         ListenerKeepRunning = false;
      
         return true;
      }
   	//==============================
   	//	end function terminateListener()
   	//==============================
   	
   
   	
      public boolean createListener()
      {
         return true;
      }
   	
   	
   	
   	//======================================================
   	//	function readData()
   	//
   	//	returns complete data flights from "ReceivedQueue"
   	//======================================================		
      public byte[][] readData() throws IOException
      {
         IDACSSession currentSession = null;
      					
      
      	//=============================================
      	//	find a Session to read Data from
      	//=============================================
         if(highestActiveSessionIndex == 0)
         {
            return null;}
      	
         for(int index = (LastSessionDataReadFrom + 1) % highestActiveSessionIndex; ; index = (index + 1) % highestActiveSessionIndex)
         {
         	// second half of this check is if window size is 0
            if((ActiveSessions[index] != null) && !((ActiveSessions[index].ReceivedQueueLeftEdge == ActiveSessions[index].ReceivedQueueRightEdge) || (ActiveSessions[index].ReceivedQueue[ActiveSessions[index].ReceivedQueueLeftEdge] == null)))
            {
               currentSession = ActiveSessions[index];
			    //System.out.println("Current ACTIVESESSIONS : " + index);
			   
               LastSessionDataReadFrom = index;
			   //from this point data packet is not correct
			   
			    //System.out.println("Last Session Read Data From is : " + index);
               break;
            }
		
			
	
			
			//from this points the packet content is not correct anymore
            if(index == LastSessionDataReadFrom)
            {
               break;
            }
         }
		//from this the data packet is not correct
		
         if(currentSession == null)
            return null;
      //from this points the data is not correct
      
      	// window size is 0
         //if((currentSession.ReceivedQueueLeftEdge == currentSession.ReceivedQueueRightEdge) || (currentSession.ReceivedQueue[currentSession.ReceivedQueueLeftEdge] == null))
         //{
         //   return null;
         //}
      			
      			
      			
      	// look at first valid packet in "ReceivedQueue"
         //System.out.println("LEFT EDGE : " + currentSession.ReceivedQueueLeftEdge);
         byte[][] data = new byte[currentSession.ReceivedQueue[currentSession.ReceivedQueueLeftEdge].FlightSize][1441];

      
      	// look through all packets in flight - make sure they are all ACKed
         for(int index = currentSession.ReceivedQueueLeftEdge; index != (currentSession.ReceivedQueueLeftEdge + data.length) % currentSession.ReceivedQueue.length; index = (index + 1) % currentSession.ReceivedQueue.length)
         {
            if((currentSession.ReceivedQueue[index] == null) || ((currentSession.ReceivedQueue[index] != null) && ((currentSession.ReceivedQueue[index].Flags & IDACSCommon.ACKED_FLAG) == 0x00)))
            {
               return null;
            }
         }
      				
      	// package requested messages
			
         int index2 = 0;
         
         String Client = InetAddress.getByAddress((IDACSCommon.intToByteArray(currentSession.remoteComputerIP))).getHostAddress();
         CurrentSessionIP = Client;
         for(int index = currentSession.ReceivedQueueLeftEdge; index != (currentSession.ReceivedQueueLeftEdge + data.length) % currentSession.ReceivedQueue.length; index = (index + 1) % currentSession.ReceivedQueue.length)
         {	
			  
             byte[] temp = currentSession.ReceivedQueue[index].FileData;
				data[index2] = new byte[temp.length];
				System.arraycopy(temp, 0, data[index2], 0, temp.length);
				index2++;
         }						
      	
      	// adjust "ReceivedQueueLeftEdge"
         currentSession.ReceivedQueueLeftEdge = (currentSession.ReceivedQueueLeftEdge + currentSession.ReceivedQueue[currentSession.ReceivedQueueLeftEdge].FlightSize) % currentSession.ReceivedQueue.length;
         this.ST = theListener.ST;
         int value = processData(data).length;
         //System.out.println("start in 2 :" + ST.get(Client));
//         if(Counter.containsKey(Client) == false){
//				Counter.put(Client, value);
//			}
//			else
//				Counter.put(Client, Counter.get(Client).intValue()+value);
//         System.out.println(Counter.get(Client));
//         if(Counter.get(Client) >= ceil){
//        	 ReTime = System.currentTimeMillis() - this.ST.get(Client);
//        	 Counter.remove(Client);
//        	 theListener.ST.remove(Client);
//        	 FileOutputStream fos = new FileOutputStream("Finish.txt");
//        	 System.out.println("Finish".getBytes().length);
//        	 fos.write("Finish".getBytes());
//        	 fos.close();
//				fos = new FileOutputStream(Client + ".txt",true);
//				String Line = "ReceiveTime : " + ReTime+ "\r\n";
// 			fos.write(Line.getBytes());
// 			fos.close();
//        	 
//         }
         return data;
      }
      private void SaveDataToFile(byte[][] data,String Client) throws IOException{
    	  FileOutputStream fos = new FileOutputStream(Client.replace(".", "-") + ".txt",true);
			
			byte[] Filedata = processData(data);
			fos.write(Filedata);
			fos.close();
      }
      private static byte[] processData(byte[][] data){
   		  byte[] filedata = new byte[data.length*1441];
   		  for(int index=0;index<data.length;index++){
   			  System.arraycopy(data[index], 0, filedata, index*1441, data[index].length);
   		  }
   		  byte[] FinalData = findNulls(filedata);
   		  return FinalData;
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
   	//==============================
   	//	end function readData()
   	//==============================
   	
   	
   	//======================================================
   	//	function setListenerPriority_***
   	//
   	//	sets the priority of the IDACSListener "Listener"
   	//======================================================				
      public void setListenerPriority_NORMAL(int normalPriority)
      {
         theListener.setPriority(normalPriority);
         System.out.println("Listener priority updated: " + theListener.getPriority());
         return;
      }
   	
      public void setListenerPriority_LOW(int normalPriority)
      {
         theListener.setPriority(normalPriority);
         System.out.println("Listener priority updated: " + theListener.getPriority());			
         return;		
      }
   	
      public void setListenerPriority_HIGH(int normalPriority)
      {
         theListener.setPriority(normalPriority);
         System.out.println("Listener priority updated: " + theListener.getPriority());			
         return;
      } 	
   	//==============================
   	//	end function setListenerPriority_***()
   	//==============================
   	
   
   }
