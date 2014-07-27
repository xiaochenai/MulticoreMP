package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JOptionPane;


public class IDVerification {

	/*Identity Attestation Process*/
	private static DatagramSocket servSocket = null;
	static final int MAX_PACKET_SIZE = 32;
    
	//get the ENC or DEC signal and call corresponding function
	public static void IDService(){
		
		
		try {
			servSocket = new DatagramSocket(2228);
			byte[] inData = new byte[MAX_PACKET_SIZE];
			DatagramPacket choice = new DatagramPacket(inData, inData.length);
			servSocket.receive(choice);
			byte[] selection = findNulls(choice.getData());
			String selec = new String(selection);
			System.out.println(new String(selection));
			System.out.println(new String(selection).length());
			servSocket.close();
			if(selec.equals("ENC")){
				TPMIDVerifyENC.receivePubKey();
				
			}if(selec.equals("DEC")){
				TPMIDVerifyDEC.sigVerify();
			}if(!selec.equals("ENC")&&!selec.equals("DEC")){
				JOptionPane.showMessageDialog(null, "Error");
				System.exit(0);}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
}
