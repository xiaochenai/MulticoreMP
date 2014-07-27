package PC;

import javax.swing.JOptionPane;

import biz.source_code.base64Coder.Base64Coder;

import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class keyListenerTest {
	
	private static String servIP = null;
    private static int servPort = 2228;
    private static InetAddress serverIPAddress;
    private static ServerListener listener;
	private static Sender sender;
	public static String ServerIP = "192.168.0.103";
	
    
	/**
	 * Launch the application.
	 */

	/**
	 * Create the application.
	 */
	public static  void getServerIP(Sender sender,ServerListener listener) {
		
        servIP = JOptionPane.showInputDialog("Please input server IP address Here!");
        listener = listener;
        sender = sender;
		
	}

	  /* input: payload to send
    return: payload from server
 funcation: implement UDP SOCKET communication. send user data to server and wait packets sent back*/
	protected static String sends( String keytrans ) throws IOException
	{
		
		//serverIPAddress =  InetAddress.getByName(servIP);
		DatagramSocket socket = new DatagramSocket();
		byte[] outData = keytrans.getBytes();
		byte[] inData = new byte[1400];
		byte[] readdata = null;
		int inPacketLength;
		sender.init(servIP);
		boolean result = sender.SendData(sender.PreparePacket(outData));
		System.out.println("Sending data result ...... " + result);
   /*construct packet to send*/
		//DatagramPacket outPacket = new DatagramPacket(outData, outData.length, serverIPAddress, servPort);
		/*send UDP datagram through socket*/
		//socket.send(outPacket);
		
		System.out.println("Data sent already!");
		/*recieve packet from server*/
		//DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
		while((readdata = listener.ReadData())==null);
		
		//socket.receive(inPacket);
		//inPacketLength = inPacket.getLength();
		
		//socket.close();
   /*return the payload in the packet in String*/
		return new String(Base64Coder.decodeLines(new String(readdata)));
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
		
	//remove non meaningful bytes from byte[] buffer	
    protected static byte[] findNulls(byte[] buffer)
		{
			int terminationPoint = findLastMeaningfulByte(buffer);
			byte[] output;
			output = new byte[terminationPoint + 1];
			System.arraycopy(buffer, 0, output, 0, terminationPoint + 1);
			return output;
		}
	
	
	//convert the keystroke character to random string;
	/***********************************************
	add a method to replace the table here, so this method can not be protected anymore
	************************************************/
     protected static String transform(char key){
		String keystring = null;
		
		switch (key) {
        case 'A': case 'a':  keystring = "3475475d";
                 break;
        case 'B': case 'b':  keystring = "7ca15fe6";
                 break;
        case 'C': case 'c':  keystring = "5ef1eeb4";
                 break;
        case 'D': case 'd':  keystring = "67d27b7e";
                 break;
        case 'E': case 'e':  keystring = "54937c9f";
                 break;
        case 'F': case 'f':  keystring = "7ecc4da6";
                 break;
        case 'G': case 'g':  keystring = "c332e32f";
                 break;
        case 'H': case 'h':  keystring = "8c1a5b7d";
                 break;
        case 'I': case 'i':  keystring = "eaf9773f";
                 break;
        case 'J': case 'j': keystring = "74064690";
                 break;
        case 'K': case 'k': keystring = "44745a2e";
                 break;
        case 'L': case 'l': keystring = "bd0625f0";
                 break;
        case 'M': case 'm': keystring = "6283d093";
                 break;
        case 'N': case 'n':  keystring = "2744c3e8";
                 break;
        case 'O': case 'o':  keystring = "3e04f0c5";
                 break;
        case 'P': case 'p':  keystring = "b5faba59";
                 break;
        case 'Q': case 'q':  keystring = "432ff018";
                 break;
        case 'R': case 'r':  keystring = "ed9c4981";
                 break;
        case 'S': case 's':  keystring = "61784a64";
                 break;
        case 'T': case 't':  keystring = "b450eeae";
                 break;
        case 'U': case 'u':  keystring = "e975639b";
                 break;
        case 'V': case 'v':  keystring = "723917a9";
                 break;
        case 'W': case 'w': keystring = "801f4eba";
                 break;
        case 'X': case 'x': keystring = "fada19f3";
                 break;
        case 'Y': case 'y': keystring = "9aea5a69";
                 break;
        case 'Z': case 'z': keystring = "8d7fe4ee";
                 break;
        case '1': keystring = "0a2f483c";
                 break;
        case '2': keystring = "9856a01a";
                 break;
        case '3': keystring = "ae2930db";
                 break;
        case '4': keystring = "e665c59a";
                 break;
        case '5': keystring = "ba9e1102";
                 break;
        case '6': keystring = "323d83c2";
                 break;
        case '7': keystring = "6551ad65";
                 break;
        case '8': keystring = "56654545";
                 break;
        case '9': keystring = "7175b306";
                 break;
        case '0': keystring = "280c1663";
                 break;
    }
		
		return keystring;
	}
}
