   package IDACS_Connection;
 
   import IDACS_Common.*;
   import IDACS_Connection.*;

 
   import java.io.*;
   import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.util.*;

   public class IDACSListener extends Thread
   {
   
      private int ListenerPort;
      private IDACSConnection ParentConnection;
      //private boolean flightActive = false;
      //private boolean flightComplete = false;
      //private boolean readyToSendSACKS = false;
      private int initialThreadPriority = 0;
      private DatagramSocket ListenerSocket = null;
      private boolean Closed = false;
      //private Timeout sendSACKTimer;
      //private Timeout cancelCurrentFlightTimer;
   	
   
   	//========================
   	//	Constructor
   	//========================
      public IDACSListener( int port, IDACSConnection theConnection)
      {
         ListenerPort = port;
         ParentConnection = theConnection;
         initialThreadPriority = 5;      	
      }
   
   
      public boolean isSocketClose(){
    	  return this.ListenerSocket.isClosed();
      }
      public void free_port(){
    	  this.ListenerSocket.close();
    	  Closed = true;
    	  Thread.currentThread().interrupt();
      }
      public void run()
      {
         int flightSize = 0;
         int dataOffset = 0;
         int flightInitialSequenceNumber = 0;
         int ACKSequenceNumber = 0;
         int SACKLength = 0;
         int[] SACKEdges = null;
         //int ActiveFlightInitialSequenceNumber = 0;
         InetAddress remoteComputerAddress = null;
         int standardBufferSize = 0;
      	
      	
      	
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
            ListenerSocket.setReuseAddress(true);
            ListenerSocket.setSoTimeout( IDACSCommon.LISTENER_TIMEOUT_INTERVAL );	
         	
         	// set initial buffer size
            standardBufferSize = ListenerSocket.getReceiveBufferSize() * 10;
            ListenerSocket.setReceiveBufferSize(standardBufferSize);
         }
            catch(SocketException se)
            {
               se.printStackTrace();
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
            if((ParentConnection.currentSession.readyToSendSACKS == true) && (!ParentConnection.currentSession.sendSACKTimer.isRunning()) && (ParentConnection.currentSession.flightActive == true))
            {
               ParentConnection.sendSACKs(ParentConnection.currentSession.ActiveFlightInitialSequenceNumber);
               ParentConnection.currentSession.readyToSendSACKS = false;
            }
         
         
         	//=============================================================
         	//	service 'cancelCurrentFlightTimer' if it has expired
         	//	- this timer expiring indicates that a flight is currently
         	//	active but we have not received any packets from this flight
         	//	for a while, so abandon this flight
         	//=============================================================
            if((ParentConnection.currentSession.flightActive == true) && (ParentConnection.currentSession.flightComplete == false) && (!ParentConnection.currentSession.cancelCurrentFlightTimer.isRunning()))
            {
               ParentConnection.currentSession.flightActive = false;
               ParentConnection.currentSession.flightComplete = false;
            	
              	// adjust "ReceivedQueueLeftEdge"
               ParentConnection.currentSession.ReceivedQueueLeftEdge = (ParentConnection.currentSession.ReceivedQueueLeftEdge + ParentConnection.currentSession.ReceivedQueue[ParentConnection.currentSession.ReceivedQueueLeftEdge].FlightSize) % ParentConnection.currentSession.ReceivedQueue.length;  					
            	
            	// restore thread priority
               ParentConnection.setListenerPriority_NORMAL(initialThreadPriority);
            	
               System.out.println("Cancelled flight due to Timeout.");
            }
         
         
         
            //=====================
            //	Receive a Packet
            //=====================
            try
            {
               receiveTimeoutExpired = false;
               ListenerSocket.setSoTimeout( IDACSCommon.LISTENER_TIMEOUT_INTERVAL );
               ListenerSocket.receive( theInPacket );
               //System.out.println("Remote Out Port : " + theInPacket.getPort());
            }                  
               catch (SocketTimeoutException ste)
               {                     
                  receiveTimeoutExpired = true;
               }  			
               catch (IOException ioe){}
         
         
            //=============================================
            //	If no packet received and no active flight
            //=============================================
            if((receiveTimeoutExpired == true) && (ParentConnection.currentSession.flightActive == false))
            {
               try
               {Thread.sleep(IDACSCommon.NO_PACKET_RECEIVED_SLEEP_TIME);}
                  catch(InterruptedException ie){}		
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
            	
               int sequenceNumber = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 0, 4));
               byte type = packetData[4];
               remoteComputerAddress = theInPacket.getAddress();
            	
            	
               switch(type)
               {
               	// DATA packet
                  case IDACSCommon.DATA_MESSAGE_TYPE:
                  
                     flightSize = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 5, 2));
                     dataOffset = IDACSCommon.byteArrayToInt(IDACSCommon.getSubArray(packetData, 7, 2));
                     flightInitialSequenceNumber = sequenceNumber - dataOffset;
                  	
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                     //System.out.println("Data packet received.  Sequence number: " + sequenceNumber);
                  	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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
            
            
            
            	//=====================
            	//	Single Data packet
            	//=====================
               if((type == IDACSCommon.DATA_MESSAGE_TYPE) && (flightSize == 1) && (ParentConnection.currentSession.flightActive == false))
               {
               	// save Packet in RecievedQueue
            	   byte[] FileData = theInPacket.getData();
            	   InetAddress remoteAddress = theInPacket.getAddress();
                  IDACSPacketRecord currentPacketRecord = new IDACSPacketRecord(sequenceNumber, flightSize, dataOffset, flightInitialSequenceNumber, IDACSCommon.ALL_FLAGS_UNSET, theInPacket,FileData,remoteAddress);
               
               	// if there is still room left in "Received Queue"
                  if((ParentConnection.currentSession.ReceivedQueueRightEdge + 1) % ParentConnection.currentSession.ReceivedQueue.length != ParentConnection.currentSession.ReceivedQueueLeftEdge)
                  {
                     int currentPacketPostion = ParentConnection.currentSession.ReceivedQueueRightEdge;
                     ParentConnection.currentSession.ReceivedQueue[ParentConnection.currentSession.ReceivedQueueRightEdge] = currentPacketRecord;
                     ParentConnection.currentSession.ReceivedQueueRightEdge = (ParentConnection.currentSession.ReceivedQueueRightEdge + 1) % ParentConnection.currentSession.ReceivedQueue.length;
                  	
                  	// send ACK message
                     ParentConnection.sendACK(sequenceNumber + 1, remoteComputerAddress);
                     ParentConnection.currentSession.ReceivedQueue[currentPacketPostion].Flags |= IDACSCommon.ACKED_FLAG;							
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
               	// check if there is an active flight or if this packet is in the active flight
                  if(((ParentConnection.currentSession.flightActive == false) && (flightInitialSequenceNumber != ParentConnection.currentSession.ActiveFlightInitialSequenceNumber)) || ((ParentConnection.currentSession.flightActive == true) && (flightInitialSequenceNumber == ParentConnection.currentSession.ActiveFlightInitialSequenceNumber)))
                  {
                	 byte[] FileData = theInPacket.getData();
                	 InetAddress remoteAddress = theInPacket.getAddress();
                     IDACSPacketRecord currentPacketRecord = new IDACSPacketRecord(sequenceNumber, flightSize, dataOffset, flightInitialSequenceNumber, IDACSCommon.ALL_FLAGS_UNSET, theInPacket,FileData,remoteAddress);
                  
                  	// first packet received for this flight
                     if(ParentConnection.currentSession.flightActive == false)
                     {
                        int numSpacesLeftInBuffer = (ParentConnection.currentSession.ReceivedQueueLeftEdge + ParentConnection.currentSession.ReceivedQueue.length - 1 - ParentConnection.currentSession.ReceivedQueueRightEdge) % ParentConnection.currentSession.ReceivedQueue.length;
                     	
                        if(numSpacesLeftInBuffer >= flightSize)
                        {
                           ParentConnection.currentSession.ActiveFlightInitialSequenceNumber = flightInitialSequenceNumber;
                        
                        	// move ReceivedQueueRightEdge
                           int oldReceivedQueueRightEdge = ParentConnection.currentSession.ReceivedQueueRightEdge;
                           ParentConnection.currentSession.ReceivedQueueRightEdge = (ParentConnection.currentSession.ReceivedQueueRightEdge + flightSize) % ParentConnection.currentSession.ReceivedQueue.length;
                        	
                        	// set all spaces expecting flight packets to "null"
                           for(int index = 0; index < flightSize; index++)
                           {
                              ParentConnection.currentSession.ReceivedQueue[(oldReceivedQueueRightEdge + index) % ParentConnection.currentSession.ReceivedQueue.length] = null;
                           }
                        	
                        	// save the packet in ReceivedQueue
                           ParentConnection.currentSession.ReceivedQueue[(oldReceivedQueueRightEdge + dataOffset) % ParentConnection.currentSession.ReceivedQueue.length] = currentPacketRecord;
                        	
                        	// set "sendSACKTimer"
                           ParentConnection.currentSession.sendSACKTimer.reset();
                           ParentConnection.currentSession.flightActive = true;
                           ParentConnection.currentSession.readyToSendSACKS = true;
                        	
                        	// set "cancelCurrentFlightTimer"
                           ParentConnection.currentSession.cancelCurrentFlightTimer.reset();									
                        	
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
                        		
                        	
                        	// Thread priorities									
                           ParentConnection.setListenerPriority_HIGH(initialThreadPriority);
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
                        int flightBeginningIndex = (ParentConnection.currentSession.ReceivedQueueRightEdge - flightSize + ParentConnection.currentSession.ReceivedQueue.length) % ParentConnection.currentSession.ReceivedQueue.length;
                     	
                     	// save the packet in ReceivedQueue
                        ParentConnection.currentSession.ReceivedQueue[(flightBeginningIndex + dataOffset) % ParentConnection.currentSession.ReceivedQueue.length] = currentPacketRecord;
                     	
                     	// set "sendSACKTimer"								
                        ParentConnection.currentSession.sendSACKTimer.reset();								
                        ParentConnection.currentSession.flightActive = true;							
                        ParentConnection.currentSession.readyToSendSACKS = true;
                     	
                     	// set timer to cancel receiving the flight
                        ParentConnection.currentSession.cancelCurrentFlightTimer.reset();
                     } 
                  	
                  	
                  	
                  	// check if the flight is complete - ACK if it is
                     int flightBeginningIndex = (ParentConnection.currentSession.ReceivedQueueRightEdge - flightSize + ParentConnection.currentSession.ReceivedQueue.length) % ParentConnection.currentSession.ReceivedQueue.length;
                  	
                     if((ParentConnection.currentSession.ReceivedQueue[flightBeginningIndex] != null) && (flightInitialSequenceNumber != ParentConnection.currentSession.ReceivedQueue[flightBeginningIndex].SequenceNumber))
                     {
                     	// ERROR
                        System.out.println("RECEIVE BUFFER ERROR!");
                     }
                     else
                     {
                        ParentConnection.currentSession.flightComplete = true;
                     
                        for(int index = 0; index < flightSize; index++)
                        {
                           if(ParentConnection.currentSession.ReceivedQueue[(flightBeginningIndex + index) % ParentConnection.currentSession.ReceivedQueue.length] == null)
                           {
                              ParentConnection.currentSession.flightComplete = false;
                              break;
                           }
                        }
                     	
                     	// send the ACK
                        if(ParentConnection.currentSession.flightComplete == true)
                        {
                        	// send the ACK
                           ParentConnection.sendACK(flightInitialSequenceNumber + flightSize, remoteComputerAddress);
                        	
                        	// mark packets as ACKed
                           for(int index = 0; index < flightSize; index++)
                           {
                              ParentConnection.currentSession.ReceivedQueue[(flightBeginningIndex + index) % ParentConnection.currentSession.ReceivedQueue.length].Flags |= IDACSCommon.ACKED_FLAG;
                           }
                        	
                        	// indicate the flight is complete
                           ParentConnection.currentSession.flightActive = false;
                           ParentConnection.currentSession.readyToSendSACKS = false;
                        	
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
                        	
                        	// restore thread priority
                           ParentConnection.setListenerPriority_NORMAL(initialThreadPriority);
                        }
                     }
                  								                                   
                  }
                  
                  // there is an active flight and packet falls outside of this flight
                  else
                  {
                  	// DROP PACKET
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
               	
                  for(int index = ParentConnection.currentSession.SentQueueLeftEdge; index != ParentConnection.currentSession.SentQueueRightEdge; index = (index + 1) % ParentConnection.currentSession.SentQueue.length)
                  {
                     IDACSPacketRecord blah = ParentConnection.currentSession.SentQueue[index];
                  
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
                     IDACSPacketRecord blah = ParentConnection.currentSession.SentQueue[(ACKedPacketIndex - 1 + ParentConnection.currentSession.SentQueue.length) % ParentConnection.currentSession.SentQueue.length];
                     if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber - 1))
                     {ACKedPacketIndexMinusOne = (ACKedPacketIndex - 1 + ParentConnection.currentSession.SentQueue.length) % ParentConnection.currentSession.SentQueue.length;}
                  }
                  // found the packet actually being ACKed
                  else if((ACKedPacketIndexMinusOne != -2) && (ACKedPacketIndex == -2))
                  {
                     IDACSPacketRecord blah = ParentConnection.currentSession.SentQueue[(ACKedPacketIndexMinusOne + 1) % ParentConnection.currentSession.SentQueue.length];
                     if((blah != null) && (blah.SequenceNumber == ACKSequenceNumber + 1))
                     {ACKedPacketIndex = (ACKedPacketIndexMinusOne + 1) % ParentConnection.currentSession.SentQueue.length;}
                  }
               	
               	
               	//************************************************************************************																		
               	// packet not found OR ACKed packet is first packet in flight, so nothing is ACKed
               	//************************************************************************************
                  if(((ACKedPacketIndex == -2) && (ACKedPacketIndexMinusOne == -2))
                  	|| ((ACKedPacketIndex != -2) && (ParentConnection.currentSession.SentQueue[ACKedPacketIndex].DataOffset == 0)))
                  {
                  	// ignore packet
                  }												
                  //*****************************
                  // packet is a single packet
                  //*****************************
                  else if((type == IDACSCommon.ACK_MESSAGE_TYPE) && (ACKedPacketIndexMinusOne != -2) && (ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize == 1))
                  {
                     ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].Flags |= IDACSCommon.ACKED_FLAG;
                     ParentConnection.currentSession.CurrentSendingFlightACKed = true;						
                  }
                  //***********************************************
                  // packet is part of a flight, no SACKs present
                  //***********************************************
                  else if((ACKedPacketIndexMinusOne != -2) && (ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize > 1))
                  {
                  	// find the first packet in the flight
                     int flightStartIndex = (ACKedPacketIndexMinusOne - ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].DataOffset + ParentConnection.currentSession.SentQueue.length) % ParentConnection.currentSession.SentQueue.length;
                  	
                  	// if the packet at "SentQueue[flightStartIndex]" does not match the expected sequence number, ERROR
                     if(ParentConnection.currentSession.SentQueue[flightStartIndex].SequenceNumber != ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightInitialSequenceNumber)
                     {
                        System.out.println("SENT BUFFER ERROR! CANNOT FIND BEGINNING OF FLIGHT!");
                     }
                     // register ACKS from the first packet in the flight to ACKedPacketIndexMinusOne
                     else
                     {
                        for(int index = flightStartIndex; index != (ACKedPacketIndexMinusOne + 1) % ParentConnection.currentSession.SentQueue.length; index = (index + 1) % ParentConnection.currentSession.SentQueue.length)
                        {
                           ParentConnection.currentSession.SentQueue[index].Flags |= IDACSCommon.ACKED_FLAG;
                        }
                     }
                  	
                  	// if entire flight has been ACKed
                     if(ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].DataOffset + 1 == ParentConnection.currentSession.SentQueue[ACKedPacketIndexMinusOne].FlightSize)
                     {
                        ParentConnection.currentSession.CurrentSendingFlightACKed = true;
                     }
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
                        for(int index2 = 0; index2 < ParentConnection.currentSession.SentQueue.length; index2++)
                        {
                           if(ParentConnection.currentSession.SentQueue[index2].SequenceNumber == firstSACKSequenceNumber)
                           {
                              firstSACKIndex = index2;
                              break;
                           }
                           else if (ParentConnection.currentSession.SentQueue[index2].SequenceNumber == lastSACKSequenceNumber)
                           {
                              lastSACKIndex = index2;
                              break;
                           }
                        }
                     	// firstSACKIndex found, looking for lastSACKIndex
                        if(firstSACKIndex != -1) 
                        {
                           int potentialLastSACKIndex = (firstSACKIndex + Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber)) % ParentConnection.currentSession.SentQueue.length;								
                           if((ParentConnection.currentSession.SentQueue[potentialLastSACKIndex] != null) && (ParentConnection.currentSession.SentQueue[potentialLastSACKIndex].SequenceNumber == lastSACKSequenceNumber))
                           {
                              lastSACKIndex = (firstSACKIndex + Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber)) % ParentConnection.currentSession.SentQueue.length;
                           }
                        }
                     	// lastSACKIndex found, looking for firstSACKIndex
                        if(lastSACKIndex != -1)
                        { 
                           int potentialFirstSACKIndex = (lastSACKIndex - Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber) + ParentConnection.currentSession.SentQueue.length) % ParentConnection.currentSession.SentQueue.length;
                           if((ParentConnection.currentSession.SentQueue[potentialFirstSACKIndex] != null) && (ParentConnection.currentSession.SentQueue[potentialFirstSACKIndex].SequenceNumber == firstSACKSequenceNumber))
                           {
                              firstSACKIndex = (lastSACKIndex - Math.abs(lastSACKSequenceNumber - firstSACKSequenceNumber) + ParentConnection.currentSession.SentQueue.length) % ParentConnection.currentSession.SentQueue.length;
                           }
                        }
                     	
                     	// make sure we found the whole SACK range
                        if((firstSACKIndex == -1) || (lastSACKIndex == -1))
                        {
                           System.out.println("BUFFER ERROR! CANNOT FIND SACK RANGE!");
                           continue;
                        }
                     	
                     	// mark all packets in the SACK range as SACKed
                        for(int index3 = firstSACKIndex; index3 != (lastSACKIndex + 1) % ParentConnection.currentSession.SentQueue.length; index3 = (index3 + 1) % ParentConnection.currentSession.SentQueue.length)
                        {																	
                           ParentConnection.currentSession.SentQueue[index3].Flags |= IDACSCommon.SACKED_FLAG;
                        }
                     	
                     // end 'for' loop looping through all SACK ranges
                     }
                  // end 'if' statement for SACKing packets							
                  }
               	
               	//==========================================================
               	// Set timer for retransmitting non-ACK or SACKed packets
               	//==========================================================
                  ParentConnection.currentSession.retransmitNonACK_SACKedPacketsTimer.reset();
                  ParentConnection.currentSession.retransmitNonACK_SACKedPacketsTimerACTIVE = true;               						
               	
               // end 'if(type == ACK) || (type == ACK_PLUS_SACK)	
               }
            
            }
         
         }
      	//==========================
      	//	end "while(true)" loop
      	//==========================
      
      }
   	
   }
