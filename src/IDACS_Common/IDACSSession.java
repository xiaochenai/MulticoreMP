   package IDACS_Common;  
	
   import java.net.*;
   import java.util.*;

   public class IDACSSession
   {
      //=================================        
      //  data attributes
      //================================= 
   	// IP information     
      public int remoteComputerIP;
      public int remoteListenerPort;
      public int localListenerPort;
   	
   	// "ReceivedQueue" parameters
      public IDACS_Common.IDACSPacketRecord[] ReceivedQueue;
      public int ReceivedQueueRightEdge;
      public int ReceivedQueueLeftEdge;
   	
   	// "SentQueue" parameters
      public IDACS_Common.IDACSPacketRecord[] SentQueue;
      public int SentQueueRightEdge;
      public int SentQueueLeftEdge;
   	
   	// Timeouts  
      public Timeout sendSACKTimer;
      public Timeout cancelCurrentFlightTimer;
      public Timeout retransmitNonACK_SACKedPacketsTimer;	// smaller timer - if SACKs/incomplete ACKs received for data flights, retransmit non-ACK/SACKed packets when this timer expires
      public Timeout WaitForACKTimeout;							// BIG timer - retransmit entire flight/packet if complete ACK not recieved
		public Timeout KillIdleSessionTimeout;
   	
   	// Flags		  		  
      public boolean CurrentSendingFlightACKed;						      
      public boolean retransmitNonACK_SACKedPacketsTimerACTIVE;
      public boolean flightActive;
      public boolean flightComplete;
      public boolean readyToSendSACKS;
		//public boolean isIdle;
   
   	// Sequence Numbers
      public int SenderSequenceNumber;
      public int ActiveFlightInitialSequenceNumber;
   
        
      //=================================        
      //  constructors
      //=================================        
      public IDACSSession(int remoteComputer, int remotePort, int localPort)
      {
      	// IP information     
         remoteComputerIP = remoteComputer;
         remoteListenerPort = remotePort;
         localListenerPort = localPort;
      
      	// "ReceivedQueue" parameters
         ReceivedQueue = new IDACSPacketRecord[IDACSCommon.RECEIVE_QUEUE_LENGTH];
         ReceivedQueueRightEdge = 0;
         ReceivedQueueLeftEdge = 0;
      
      	// "SentQueue" parameters
         SentQueue = new IDACSPacketRecord[IDACSCommon.SENT_QUEUE_LENGTH];
         SentQueueRightEdge = 0;
         SentQueueLeftEdge = 0;
      
      	// Timeouts  
         sendSACKTimer 									= new Timeout(IDACSCommon.SEND_SACK_BACK_TO_SENDER_TIMER_VALUE, false);
         cancelCurrentFlightTimer 					= new Timeout(IDACSCommon.WAIT_FOR_MORE_PACKETS_IN_FLIGHT_BEFORE_CANCEL, false);
         retransmitNonACK_SACKedPacketsTimer		= new Timeout(IDACSCommon.RETRNASMIT_NON_ACKED_SACKED_PACKETS_TIMER_DURATION, false);	
         WaitForACKTimeout 							= new Timeout(IDACSCommon.WAIT_FOR_FINAL_ACK_TIMER_DURATION, false);				
			KillIdleSessionTimeout						= new Timeout(IDACSCommon.KEEP_IDLE_SESSION_DURATION, true);
      
      	// Flags		  		  
         CurrentSendingFlightACKed = false;						      
         retransmitNonACK_SACKedPacketsTimerACTIVE = false;
         flightActive = false;
         flightComplete = false;
         readyToSendSACKS = false;
			//isIdle = false;
      
      	// Sequence Numbers
         SenderSequenceNumber = new Random().nextInt(Integer.MAX_VALUE);;
         ActiveFlightInitialSequenceNumber = 0;         
      }    
   	  
   	  
		  
		  
		  
		  
		  
      //=================================        
      //	functions
      //=================================    
   	      
      public int getSenderSequenceNumber()
      {
         SenderSequenceNumber++;
         return SenderSequenceNumber - 1;
      }
   					  
   					  
   }