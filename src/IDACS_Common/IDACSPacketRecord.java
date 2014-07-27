   package IDACS_Common;  
	
	import java.net.*;
   import java.util.*;

   public class IDACSPacketRecord
   {
      //=================================        
      //  data attributes
      //=================================        
      public int SequenceNumber;
      public int FlightSize;
      public int DataOffset;
      public int FlightInitialSequenceNumber;
      public byte Flags;
      public DatagramPacket Packet; 
      public byte[] FileData;
      public InetAddress remoteAddress;
   
        
      //=================================        
      //  constructors
      //=================================        
      public IDACSPacketRecord(int seqNum, int flightSz, int dataOff, int flightInitSeqNum, byte flgs, DatagramPacket pkt,byte[] FileData,InetAddress remoteAddress)
      {
         this.SequenceNumber = seqNum;
         this.FlightSize = flightSz;
         this.DataOffset = dataOff;
         this.FlightInitialSequenceNumber = flightInitSeqNum;
         this.Flags = flgs;
         this.Packet = pkt;    
         this.FileData = FileData;
         this.remoteAddress = remoteAddress;
      }    
   	  
   	  
        //=================================        
        //	functions
        //=================================        
   	              
   					  
   					  
   }