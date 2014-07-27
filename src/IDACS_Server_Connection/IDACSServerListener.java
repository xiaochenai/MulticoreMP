   package IDACS_Server_Connection;
 
   import IDACS_Common.*;
   import IDACS_Server_Connection.*;

 
   import java.io.*;
   import java.net.*;
import java.util.*;

   public class IDACSServerListener extends Thread
   {
   
      private int ListenerPort;
      private IDACSServerConnection ParentConnection;
      private int initialThreadPriority = 0;
      private HashMap<String,Integer> Counter = new HashMap<String,Integer>();
	  public HashMap<String,Long> ST = new HashMap<String,Long>();
	  private boolean Reset=false;
	  private boolean Closed = false;
	  private DatagramSocket ListenerSocket = null;
   
   	//========================
   	//	Constructor
   	//========================
      public IDACSServerListener( int port, IDACSServerConnection theConnection)
      {
         ListenerPort = port;
         ParentConnection = theConnection;
         initialThreadPriority = 5;
      }
   
   
      public void close(){
    	  Closed = true;
    	  ListenerSocket.close();
    	  Thread.currentThread().interrupt();
    	  
      }
      
      public void run() 
      {      				
    	  Thread.currentThread().setPriority(NORM_PRIORITY);
    	 System.out.println("Listener Priority : " + Thread.currentThread().getPriority());
         int flightSize = 0;
         int dataOffset = 0;
         int flightInitialSequenceNumber = 0;
         int ACKSequenceNumber = 0;
         int SACKLength = 0;
         int[] SACKEdges = null;
         InetAddress remoteComputerAddress = null;              	
      	
      	
         boolean SystemError = false;
         boolean receiveTimeoutExpired = false;		
      	
         byte[] buffer = null;           
         DatagramPacket theInPacket = null;
 
      	//=========================
      	//	Set up the listener socket
      	//=========================	    
         try
         {     		
            buffer = new byte[IDACSCommon.MAX_UDP_PACKET_LENGTH];           
            theInPacket = new DatagramPacket( buffer, buffer.length );		
         
            ListenerSocket = new DatagramSocket( ListenerPort );
            ListenerSocket.setSoTimeout( IDACSCommon.LISTENER_TIMEOUT_INTERVAL );	
         	
         	// set initial buffer size
            ListenerSocket.setReceiveBufferSize(IDACSCommon.SERVER_UDP_BUFFER_SIZE);
         }
            catch(SocketException se)
            {
               System.out.println(se);
               SystemError = true;
            }  
      
      
      
      	//====================================
      	//	Loop for the life of the thread
      	//====================================
         while((ParentConnection.ListenerKeepRunning == true) && (SystemError == false))
         {
         
         	//=============================================================
         	//	service 'sendSACKTimer' if it has expired
         	//	- this timer expiring indicates that we are in the middle
         	//	of receiving a flight of packets and it is time to send
         	//	SACK messages to the sender
         	//=============================================================
            for(int index = 0; index < ParentConnection.highestActiveSessionIndex; index++)
            {
				//System.out.println("highestActivitySessionIndex : " + ParentConnection.highestActiveSessionIndex);
               if((ParentConnection.ActiveSessions[index] != null) && (ParentConnection.ActiveSessions[index].readyToSendSACKS == true) && (!ParentConnection.ActiveSessions[index].sendSACKTimer.isRunning()) && (ParentConnection.ActiveSessions[index].flightActive == true))
               {
                  ParentConnection.sendSACKs(ParentConnection.ActiveSessions[index].ActiveFlightInitialSequenceNumber, ParentConnection.ActiveSessions[index].remoteComputerIP);
                  ParentConnection.ActiveSessions[index].readyToSendSACKS = false;
               }
            }
         
         
         	//=============================================================
         	//	service 'cancelCurrentFlightTimer' if it has expired
         	//	- this timer expiring indicates that a flight is currently
         	//	active but we have not received any packets from this flight
         	//	for a while, so abandon this flight
         	//=============================================================
            try
            {
               for(int index = 0; index < ParentConnection.highestActiveSessionIndex; index++)
               {
                  if((ParentConnection.ActiveSessions[index] != null) && (ParentConnection.ActiveSessions[index].flightActive == true) && (ParentConnection.ActiveSessions[index].flightComplete == false) && (!ParentConnection.ActiveSessions[index].cancelCurrentFlightTimer.isRunning()))
                  {
					 System.out.println("cancelCurrentFlightTimer");
                     ParentConnection.ActiveSessions[index].flightActive = false;
                     ParentConnection.ActiveSessions[index].flightComplete = false;
                  
                  // adjust "ReceivedQueueLeftEdge"
                     ParentConnection.ActiveSessions[index].ReceivedQueueLeftEdge = (ParentConnection.ActiveSessions[index].ReceivedQueueLeftEdge + ParentConnection.ActiveSessions[index].ReceivedQueue[ParentConnection.ActiveSessions[index].ReceivedQueueLeftEdge].FlightSize) % ParentConnection.ActiveSessions[index].ReceivedQueue.length;  					
                  
                  // restore thread priority - make sure there are not any other active Sessions that require high priority at the moment
                     boolean restorePriority = true;
                     for(int innerindex = 0; innerindex < ParentConnection.highestActiveSessionIndex; index++)
                     {
                        if((ParentConnection.ActiveSessions[innerindex] != null) && (ParentConnection.ActiveSessions[innerindex].flightActive == true))
                        {
                           restorePriority = false;
                           break;
                        }
                     }
                     if(restorePriority == true)						
                        ParentConnection.setListenerPriority_NORMAL(initialThreadPriority);
                  
                  
                     System.out.println("Cancelled flight due to Timeout.");
                  }
               }
            }
               catch(NullPointerException npe){System.out.println("ERROR!!!  " + npe);}
         
         
         	//=============================================================
         	//	service 'KillIdleSessionTimeout' if it has expired
         	//	- this timer expiring indicates that a Session has been idle
         	//	for the allotted period, so we destroy it to free up buffer space
         	//=============================================================
				int lastActiveSessionIndex = -1;
            for(int index = 0; index < ParentConnection.highestActiveSessionIndex; index++)
            {
               if((ParentConnection.ActiveSessions[index] != null) && (!ParentConnection.ActiveSessions[index].KillIdleSessionTimeout.isRunning()))
               {
                  ParentConnection.ActiveSessions[index] = null;
						System.out.println("KillIdleSessionTimeout");
						// see if we need to adjust "highestActiveSessionIndex"
						if(index + 1 == ParentConnection.highestActiveSessionIndex)
						{
							if(lastActiveSessionIndex == -1)
								ParentConnection.highestActiveSessionIndex = 0;
							else
								ParentConnection.highestActiveSessionIndex = lastActiveSessionIndex + 1;
						}
						
                  System.out.println("Destroyed session due to inactivity.");
               }
					
					// record presence of last active session
					if(ParentConnection.ActiveSessions[index] != null)					
						lastActiveSessionIndex = index;					
            }
         
         
         
         
            //=====================
            //	Receive a Packet
            //=====================
            try
            {
               receiveTimeoutExpired = false;
               ListenerSocket.setSoTimeout( IDACSCommon.LISTENER_TIMEOUT_INTERVAL );

               ListenerSocket.receive( theInPacket );
			   
            }                  
               catch (SocketTimeoutException ste)
               {                     
                  receiveTimeoutExpired = true;
               }  			
               catch (IOException ioe){}
         
         
            //=============================================
            //	If no packet received and no active flights
            //=============================================
            if(receiveTimeoutExpired == true)
            {
               boolean takeANap = true;
				//System.out.println("receiveTimeoutExpired");
            	// determine if we should sleep for a while
               for(int index = 0; index < ParentConnection.highestActiveSessionIndex; index++) 
               {
                  if((ParentConnection.ActiveSessions[index] != null) && (ParentConnection.ActiveSessions[index].flightActive == true))
                  {
                     takeANap = false;
                     break;
                  }
               }
            
            	// sleep for a while if we should
               if(takeANap == true)
               {
                  try
                  {Thread.sleep(IDACSCommon.NO_PACKET_RECEIVED_SLEEP_TIME);}
                     catch(InterruptedException ie){}		
               }            
            }
            
            
            //===========================================
            //	if packet received, Process the packet
            //===========================================
            else if(receiveTimeoutExpired == false && Closed == false)
            {				
            	//=======================
            	// Extract packet info
            	//=======================
               byte[] packetData = theInPacket.getData();
			   //no problem here, so we do have received the data correctly
			
               int sequenceNumber = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 0, 4));
               byte type = packetData[4];
               remoteComputerAddress = theInPacket.getAddress();
               String Client = remoteComputerAddress.getHostAddress();
            	
               switch(type)
               {
               	// DATA packet
                  case IDACSCommon.DATA_MESSAGE_TYPE:
				
                     flightSize = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 5, 2));
                     dataOffset = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 7, 2));
                     flightInitialSequenceNumber = sequenceNumber - dataOffset; 
            
                     break;
               
               	// ACK packet
                  case IDACSCommon.ACK_MESSAGE_TYPE:
                  
                     ACKSequenceNumber = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 5, 4));
                  	
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                     //System.out.println("ACK packet received.  ACK Sequence number: " + ACKSequenceNumber);
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!							
                  	
                     break;
               
               	// ACK_PLUS_SACK packet
                  case IDACSCommon.ACK_WITH_SACK_MESSAGE_TYPE:
                  
                     ACKSequenceNumber = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 5, 4));
                  
                     SACKLength = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 9, 1));
                     SACKEdges = new int[SACKLength*2];
                  
                     for(int index = 0; index < SACKLength; index++)
                     {
                     // left edge
                        SACKEdges[index*2] = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, (index*8)+10, 4));
                     // right edge
                        SACKEdges[index*2 + 1] = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, (index*8)+14, 4));
                     }
                  	
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                     //System.out.println("ACK/SACK packet received.  ACK Sequence number: " + ACKSequenceNumber);
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!							
                  	
                     break;
               }
            
            
            
            	//============================================
            	//	Processing and saving the packet
            	//============================================
            
            	//================================================================================
            	//	Determine which session in "ActiveSessions[]" this packet is associated with
            	//================================================================================
               int currentSessionIndex = -1;
               int remoteAddress = IDACSCommon.byteArrayToInt(remoteComputerAddress.getAddress());
               
               for(int index = 0; index < ParentConnection.highestActiveSessionIndex; index++)
               {
                  if((ParentConnection.ActiveSessions[index] != null) && (ParentConnection.ActiveSessions[index].remoteComputerIP == remoteAddress))
                  {
                     currentSessionIndex = index;
                     break;
                  }
               }
            	
            	// if this is a new session, create space in the buffer arrays
               if(currentSessionIndex == -1)
               {
               	// find an empty space in "ActiveSessions"
                  for(int index = 0; index < ParentConnection.ActiveSessions.length; index++)
                  {
                     if(ParentConnection.ActiveSessions[index] == null)
                     {
                        currentSessionIndex = index;
                        break;
                     }
                  }
               	
               	// if no empty spaces, ERROR
                  if(currentSessionIndex == -1)
                  {
                     System.out.println("Incoming session dropped because maximum number of sessions open.");
                     continue;
                  }
                  // set up the connection information
                  else
                  {
                  	// update "ParentConnection.highestActiveSessionIndex"
                     if(currentSessionIndex >= ParentConnection.highestActiveSessionIndex)
                        ParentConnection.highestActiveSessionIndex = currentSessionIndex + 1;
						System.out.println("highestActiveSessionIndex : " + ParentConnection.highestActiveSessionIndex);
                     ParentConnection.ActiveSessions[currentSessionIndex] = new IDACSSession(IDACSCommon.byteArrayToInt(remoteComputerAddress.getAddress()), IDACSCommon.DEFAULT_SENDER_IN_PORT, IDACSCommon.DEFAULT_LISTENER_IN_PORT);							
                  }
               }		
            	
               IDACSSession currentSession = ParentConnection.ActiveSessions[currentSessionIndex];//xiao
            	// indicate that session is not idle
               currentSession.KillIdleSessionTimeout.reset();
            
            	//=====================
            	//	Single Data packet
            	//=====================
               if((type == IDACSCommon.DATA_MESSAGE_TYPE) && (flightSize == 1) && (currentSession.flightActive == false))
               {	
			   //System.out.println("SINGLE......");
               	// save Packet in RecievedQueue
				byte[] FileData = theInPacket.getData();
				byte data[] = new byte[FileData.length - 9];
				System.arraycopy(FileData, 9, data, 0, FileData.length - 9);
				InetAddress RemoteAddress = theInPacket.getAddress();
                IDACSPacketRecord currentPacketRecord = new IDACSPacketRecord(sequenceNumber, flightSize, dataOffset, flightInitialSequenceNumber, IDACSCommon.ALL_FLAGS_UNSET,theInPacket,data,RemoteAddress);
               
               	// if there is still room left in "Received Queue"
                  if((currentSession.ReceivedQueueRightEdge + 1) % currentSession.ReceivedQueue.length != currentSession.ReceivedQueueLeftEdge)
                  {
                     int currentPacketPostion = currentSession.ReceivedQueueRightEdge;
                     currentSession.ReceivedQueue[currentSession.ReceivedQueueRightEdge] = currentPacketRecord;
                     currentSession.ReceivedQueueRightEdge = (currentSession.ReceivedQueueRightEdge + 1) % currentSession.ReceivedQueue.length;
                  	
                  	// send ACK message
                     ParentConnection.sendACK(sequenceNumber + 1, remoteComputerAddress, IDACSCommon.DEFAULT_SENDER_IN_PORT);
                     currentSession.ReceivedQueue[currentPacketPostion].Flags |= IDACSCommon.ACKED_FLAG;							
                  }
				  
                  else
                  {
                  	// DROP RECEIVED PACKET
                  }												
               }
               	         	
               //============================
               //	Data Packet from a flight 
               //============================
               else if((type == IDACSCommon.DATA_MESSAGE_TYPE) && (flightSize > 1))
               {
            	  
            	  if(ST.containsKey(Client) == false){
            		  ST.put(Client, System.currentTimeMillis());
            	  }
            	  
               	// check if there is an active flight or if this packet is in the active flight
                  if(((currentSession.flightActive == false) && (flightInitialSequenceNumber != currentSession.ActiveFlightInitialSequenceNumber)) 
                  		|| ((currentSession.flightActive == true) && (flightInitialSequenceNumber == currentSession.ActiveFlightInitialSequenceNumber)))
                  {
					byte[] FileData = new byte[theInPacket.getData().length];
					byte data[] = new byte[FileData.length - 9];
					System.arraycopy(theInPacket.getData(), 0, FileData, 0, theInPacket.getData().length);
					System.arraycopy(FileData, 9, data, 0, FileData.length - 9);
					InetAddress RemoteAddress = theInPacket.getAddress();
                     IDACSPacketRecord currentPacketRecord = new IDACSPacketRecord(sequenceNumber, flightSize, dataOffset, flightInitialSequenceNumber, IDACSCommon.ALL_FLAGS_UNSET, theInPacket,data,RemoteAddress);
                  
                  	// first packet received for this flight
                     if(currentSession.flightActive == false)
                     {
                        int numSpacesLeftInBuffer = (currentSession.ReceivedQueueLeftEdge + currentSession.ReceivedQueue.length - 1 - currentSession.ReceivedQueueRightEdge) % currentSession.ReceivedQueue.length;
                     	
                        if(numSpacesLeftInBuffer >= flightSize)
                        {
							
                           currentSession.ActiveFlightInitialSequenceNumber = flightInitialSequenceNumber;
                        
                        	// move ReceivedQueueRightEdge
                           int oldReceivedQueueRightEdge = currentSession.ReceivedQueueRightEdge;
                           currentSession.ReceivedQueueRightEdge = (currentSession.ReceivedQueueRightEdge + flightSize) % currentSession.ReceivedQueue.length;
                        	
                        	// set all spaces expecting flight packets to "null"
                           for(int index = 0; index < flightSize; index++)
                           {
                              currentSession.ReceivedQueue[(oldReceivedQueueRightEdge + index) % currentSession.ReceivedQueue.length] = null;
                           }
                        	
                        	// save the packet in ReceivedQueue
							int index11 = (oldReceivedQueueRightEdge + dataOffset) % currentSession.ReceivedQueue.length;
							
                           currentSession.ReceivedQueue[index11] = currentPacketRecord;
						 
						   //still no problem here
                           
                        	// set "sendSACKTimer"
                           currentSession.sendSACKTimer.reset();
                           currentSession.flightActive = true;
                           currentSession.readyToSendSACKS = true;
                        	
                        	// set "cancelCurrentFlightTimer"
                           currentSession.cancelCurrentFlightTimer.reset();			
                           
                        	
                        	/*
                        	//	MAINTAIN CONSTANT UDP BUFFER SIZE FOR SERVER
                        	//
                        	// if number of packets expected is greater than current buffer size, boost the buffer size
                           try
                           {
                              if(flightSize*IDACSCommon.MAX_UDP_PACKET_LENGTH > standardBufferSize)
                              {
                                 int newBufferSize = (flightSize*IDACSCommon.MAX_UDP_PACKET_LENGTH) / 3;
                              
                                 if(newBufferSize > IDACSCommon.MAX_UDP_BUFFER_SIZE)
                                    newBufferSize = IDACSCommon.MAX_UDP_BUFFER_SIZE;
                              	
                                 ListenerSocket.setReceiveBufferSize(newBufferSize);
                              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                 System.out.println("UDP Receive Buffer Size: " + ListenerSocket.getReceiveBufferSize());
                              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                              }
                           }
                              catch(SocketException se){System.out.println(se);}
                        	*/
                        	
                        	// Thread priorities									
                           ParentConnection.setListenerPriority_HIGH(initialThreadPriority);
                           System.out.println("Listener Priority : " + Thread.currentThread().getPriority());
						   
                        }
                        // buffer overflow based on incoming flight size
                        else
                        {
                        	// DROP PACKET
                        	// consider sending NACK with "buffer overflow" message
                        }							
                     }
                     
                     // not the first packet for this flight
                     else
                     {	
						
                     	// find beginning of flight in the buffer
                        int flightBeginningIndex = (currentSession.ReceivedQueueRightEdge - flightSize + currentSession.ReceivedQueue.length) % currentSession.ReceivedQueue.length;
                     	
                     	// save the packet in ReceivedQueue
						int index00 = (flightBeginningIndex + dataOffset) % currentSession.ReceivedQueue.length;
						
						
                        currentSession.ReceivedQueue[index00] = currentPacketRecord;
						
						
						// problem occurs here
						
							
                     	// set "sendSACKTimer"                        
                        currentSession.sendSACKTimer.reset();								
                        currentSession.flightActive = true;							
                        currentSession.readyToSendSACKS = true;
                     	
                     	// set timer to cancel receiving the flight
                        currentSession.cancelCurrentFlightTimer.reset();
                        
						
                     } 
                  	
                  	
                  	
                  	// check if the flight is complete - ACK if it is
                     int flightBeginningIndex = (currentSession.ReceivedQueueRightEdge - flightSize + currentSession.ReceivedQueue.length) % currentSession.ReceivedQueue.length;
                  	
                     if((currentSession.ReceivedQueue[flightBeginningIndex] != null) && (flightInitialSequenceNumber != currentSession.ReceivedQueue[flightBeginningIndex].SequenceNumber))
                     {
                     	// ERROR
                       // System.out.println("RECEIVE BUFFER ERROR!");
                     }
                     else
                     {
                        currentSession.flightComplete = true;
                     
                        for(int index = 0; index < flightSize; index++)
                        {
                           if(currentSession.ReceivedQueue[(flightBeginningIndex + index) % currentSession.ReceivedQueue.length] == null)
                           {
                              currentSession.flightComplete = false;
                              break;
                           }
                        }
                     	
                     	// send the ACK
                        if(currentSession.flightComplete == true)
                        {
                        	// send the ACK
                           ParentConnection.sendACK(flightInitialSequenceNumber + flightSize, remoteComputerAddress, IDACSCommon.DEFAULT_SENDER_IN_PORT);
                        	
                        	// mark packets as ACKed
                           for(int index = 0; index < flightSize; index++)
                           {
                              currentSession.ReceivedQueue[(flightBeginningIndex + index) % currentSession.ReceivedQueue.length].Flags |= IDACSCommon.ACKED_FLAG;
                           }
                        	
                        	// indicate the flight is complete
                           currentSession.flightActive = false;
                           currentSession.readyToSendSACKS = false;
                        	
                        	/*
                        	//	MAINTAIN CONSTANT UDP BUFFER SIZE FOR SERVER
                        	//
                        	// restore UDP buffer size
                           try
                           {
                              if(ListenerSocket.getReceiveBufferSize() > standardBufferSize)
                              {
                                 ListenerSocket.setReceiveBufferSize(standardBufferSize);
                              
                              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                 System.out.println("UDP Receive Buffer Size: " + ListenerSocket.getReceiveBufferSize());
                              //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                              }	
                           }
                              catch(SocketException se){System.out.println(se);}							
                        	*/               	
                        	
                        	// restore thread priority - make sure there are not any other active Sessions that require high priority at the moment
                           boolean restorePriority = true;
                           for(int innerindex = 0; innerindex < ParentConnection.highestActiveSessionIndex; innerindex++)
                           {
                              if((ParentConnection.ActiveSessions[innerindex] != null) && (ParentConnection.ActiveSessions[innerindex].flightActive == true))
                              {
                                 restorePriority = false;
                                 break;
                              }
                           }
                           if(restorePriority == true)						
                              ParentConnection.setListenerPriority_NORMAL(initialThreadPriority);
                        
                        }
                     }
                  								                                   
                  }
                  
                  // there is an active flight and packet falls outside of this flight
                  else
                  {
                  	// DROP PACKET
					if((currentSession.flightActive == false) && (flightInitialSequenceNumber == currentSession.ActiveFlightInitialSequenceNumber) && (currentSession.flightComplete == true)){
						System.out.println("respond ACKs have lost, re-transmit again");
						ParentConnection.sendACK(flightInitialSequenceNumber + flightSize, remoteComputerAddress, IDACSCommon.DEFAULT_SENDER_IN_PORT);
					}
					System.out.println("no active flight");
					System.out.println("DROP PACKET.....................");
                  }
               }
               
               //=================================
               //	Packet is an ACK/SACK message
               //=================================
			   
               else if((type == IDACSCommon.ACK_MESSAGE_TYPE) || (type == IDACSCommon.ACK_WITH_SACK_MESSAGE_TYPE))     	
               {
               						 
               	//========================
               	// mark ACKed packets
               	//========================
               	
               	// find packet
                  int ACKedPacketIndex = -2;				// index of packet indicated by ACK Sequence Number in packet 
                  int ACKedPacketIndexMinusOne = -2;	// index of packet indicated by Sequence Nuber immediately preceding ACK Sequence Number in packet
               	
                  for(int index = currentSession.SentQueueLeftEdge; index != currentSession.SentQueueRightEdge; index = (index + 1) % currentSession.SentQueue.length)
                  {
                     IDACSPacketRecord blah = currentSession.SentQueue[index];
                  
                  	// blah is the packet with Sequence Number ONE MORE that the actual packet being ACKed
                     if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber))
                     {
                        ACKedPacketIndex = index;
                        break;
                     }
                     // blah is the packet actually beig ACKed
                     else if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber - 1))
                     {
                        ACKedPacketIndexMinusOne = index;
                        break;
                     }
                  }
               	// found the packet with Sequence Number ONE MORE than actual packet being ACKed
                  if((ACKedPacketIndex != -2) && (ACKedPacketIndexMinusOne == -2))
                  {
                     IDACSPacketRecord blah = currentSession.SentQueue[(ACKedPacketIndex - 1 + currentSession.SentQueue.length) % currentSession.SentQueue.length];
                     if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber - 1))
                     {ACKedPacketIndexMinusOne = (ACKedPacketIndex - 1 + currentSession.SentQueue.length) % currentSession.SentQueue.length;}
                  }
                  // found the packet actually being ACKed
                  else if((ACKedPacketIndexMinusOne != -2) && (ACKedPacketIndex == -2))
                  {
                     IDACSPacketRecord blah = currentSession.SentQueue[(ACKedPacketIndexMinusOne + 1) % currentSession.SentQueue.length];
                     if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber + 1))
                     {ACKedPacketIndex = (ACKedPacketIndexMinusOne + 1) % currentSession.SentQueue.length;}
                  }
               	
               	
               	
               	//************************************************************************************																		
               	// packet not found OR ACKed packet is first packet in flight, so nothing is ACKed
               	//************************************************************************************
                  if(((ACKedPacketIndex == -2) && (ACKedPacketIndexMinusOne == -2))
                  	|| ((ACKedPacketIndex != -2) && (currentSession.SentQueue[ACKedPacketIndex].DataOffset == 0)))
                  {
                  	// ignore packet
                  }												
                  //*****************************
                  // packet is a single packet
                  //*****************************
                  else if((type == IDACSCommon.ACK_MESSAGE_TYPE) && (ACKedPacketIndexMinusOne != -2) && (currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize == 1))
                  {
                     currentSession.SentQueue[ACKedPacketIndexMinusOne].Flags |= IDACSCommon.ACKED_FLAG;
                     currentSession.CurrentSendingFlightACKed = true;						
                  }
                  //***********************************************
                  // packet is part of a flight, no SACKs present
                  //***********************************************
                  else if((ACKedPacketIndexMinusOne != -2) && (currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize > 1))
                  {
                  	// find the first packet in the flight
                     int flightStartIndex = (ACKedPacketIndexMinusOne - currentSession.SentQueue[ACKedPacketIndexMinusOne].DataOffset + currentSession.SentQueue.length) % currentSession.SentQueue.length;
                  	
                  	// if the packet at "SentQueue[flightStartIndex]" does not match the expected sequence number, ERROR
                     if(currentSession.SentQueue[flightStartIndex].SequenceNumber != currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightInitialSequenceNumber)
                     {
                        System.out.println("SENT BUFFER ERROR! CANNOT FIND BEGINNING OF FLIGHT!");
                     }
                     // register ACKS from the first packet in the flight to ACKedPacketIndexMinusOne
                     else
                     {
                        for(int index = flightStartIndex; index != (ACKedPacketIndexMinusOne + 1) % currentSession.SentQueue.length; index = (index + 1) % currentSession.SentQueue.length)
                        {
                           currentSession.SentQueue[index].Flags |= IDACSCommon.ACKED_FLAG;
                        }
                     }
                  	
                  	// if entire flight has been ACKed
                     if(currentSession.SentQueue[ACKedPacketIndexMinusOne].DataOffset + 1 == currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize)
                        currentSession.CurrentSendingFlightACKed = true;
                  }
               	//*********************************************
               	// packet is part of a flight, SACKS present
               	//*********************************************
               			// This case is covered by the previous case, except for one sub-case:
               			// if there are SACKs present but the first packet in the flight
               			//	was not received, then the ACK Sequence Number will be of the
               			//	first packet in the flight.  In this sub-case, no packets will
               			//	be ACKed.  Therefore, no need for action.
               
               	
               	
               	
               	//========================       	
               	// mark SACKed packets
               	//========================
                  if (type == IDACSCommon.ACK_WITH_SACK_MESSAGE_TYPE)
                  {
                     for(int index = 0; index < SACKLength; index++)
                     {
                     	// identify the sequence number range of the SACK
                        int firstSACKSequenceNumber = SACKEdges[2*index];
                        int lastSACKSequenceNumber = SACKEdges[2*index + 1] - 1;
                        int firstSACKIndex = -1;
                        int lastSACKIndex = -1;
                     	
                     	// search for the packet records for the SACK packets in "SentQueue"
                        for(int index2 = 0; index2 < currentSession.SentQueue.length; index2++)
                        {
                           if(currentSession.SentQueue[index2].SequenceNumber == firstSACKSequenceNumber)
                           {
                              firstSACKIndex = index2;
                              break;
                           }
                           else if (currentSession.SentQueue[index2].SequenceNumber == lastSACKSequenceNumber)
                           {
                              lastSACKIndex = index2;
                              break;
                           }
                        }
                     	// firstSACKIndex found, looking for lastSACKIndex
                        if(firstSACKIndex != -1) 
                        {
                           int potentialLastSACKIndex = (firstSACKIndex + Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber)) % currentSession.SentQueue.length;								
                           if((currentSession.SentQueue[potentialLastSACKIndex] != null) && (currentSession.SentQueue[potentialLastSACKIndex].SequenceNumber == lastSACKSequenceNumber))
                           {
                              lastSACKIndex = (firstSACKIndex + Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber)) % currentSession.SentQueue.length;
                           }
                        }
                     	// lastSACKIndex found, looking for firstSACKIndex
                        if(lastSACKIndex != -1)
                        { 
                           int potentialFirstSACKIndex = (lastSACKIndex - Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber) + currentSession.SentQueue.length) % currentSession.SentQueue.length;
                           if((currentSession.SentQueue[potentialFirstSACKIndex] != null) && (currentSession.SentQueue[potentialFirstSACKIndex].SequenceNumber == firstSACKSequenceNumber))
                           {
                              firstSACKIndex = (lastSACKIndex - Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber) + currentSession.SentQueue.length) % currentSession.SentQueue.length;
                           }
                        }
                     	
                     	// make sure we found the whole SACK range
                        if((firstSACKIndex == -1) || (lastSACKIndex == -1))
                        {
                           System.out.println("BUFFER ERROR! CANNOT FIND SACK RANGE!");
                           continue;
                        }
                     	
                     	// mark all packets in the SACK range as SACKed
                        for(int index3 = firstSACKIndex; index3 != (lastSACKIndex + 1) % currentSession.SentQueue.length; index3 = (index3 + 1) % currentSession.SentQueue.length)
                        {																	
                           currentSession.SentQueue[index3].Flags |= IDACSCommon.SACKED_FLAG;
                        }
                     	
                     // end 'for' loop looping through all SACK ranges
                     }
                  // end 'if' statement for SACKing packets							
                  }
               	
               	//==========================================================
               	// Set timer for retransmitting non-ACK or SACKed packets
               	//==========================================================               		
                  currentSession.retransmitNonACK_SACKedPacketsTimer.reset();
                  currentSession.retransmitNonACK_SACKedPacketsTimerACTIVE = true;
               	
               	
               	/*
               	//==============================================
               	// If the entire flight is ACKed
               	//==============================================
                  if(currentSession.CurrentSendingFlightACKed == true)
                  {						
                     try
                     {
                     	// kill 'retransmitNonACKedPacketsTimer'
                        ParentConnection.retransmitNonACK_SACKedPacketsTimer.interrupt();
                     
                     	// kill 'WaitForACKTimeout'
                        ParentConnection.WaitForACKTimeout.interrupt();
                     }
                        catch(NullPointerException npe){}
                  }	
               	*/					
               	
               // end 'if(type == ACK) || (type == ACK_PLUS_SACK)	
						
               }
            
            }
         
         }
      	//==========================
      	//	end "while(true)" loop
      	//==========================
      
      }
   	
   }
