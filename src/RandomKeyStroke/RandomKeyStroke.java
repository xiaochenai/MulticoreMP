package RandomKeyStroke;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.text.html.HTMLDocument.Iterator;

import com.rsa.shareCrypto.j.it;
//32-126
import GeneralClass.*;
public class RandomKeyStroke {
	/*******************
	 * All time seed need to be saved
	 */
	private long RKS_Determination_peerSeed;
	private long RKS_Determination_localSeed;
	private HashMap<Integer,String> CharacterMap = new HashMap<Integer,String>();
	private HashMap<String,byte[]> StrokeRNDMap = new HashMap<String,byte[]>();
	private byte[] DeterminationBytes;
	private int DeterminationIndex=0;
	private long RKS_Character_Determination_Seed;
	private int RKS_Determination_Bytes_Index=0;
	private byte[] RKS_Determination_Bytes;
	/***********
	 * this function is used to initiate InsertRKSLocal()
	 * @param RKS_Determination_peerSeed
	 * @param RKS_Determination_localSeed
	 * @throws NoSuchAlgorithmException
	 */
	public void InsertRKSLocalInitiate(long RKS_Determination_peerSeed, long RKS_Determination_localSeed) throws NoSuchAlgorithmException{
		this.RKS_Determination_localSeed = RKS_Determination_localSeed;
		this.RKS_Determination_peerSeed = RKS_Determination_peerSeed;
		RND RKSDetermineRnd = new RND();
		DeterminationBytes = RKSDetermineRnd.getRandom_RKS_Determination(this.RKS_Determination_peerSeed,this.RKS_Determination_localSeed);
	}
	/***************
	 * this function is used for determine which device inserts the RKS. In process, the function will pick up bytes in 
	 * DeterminationBytes sequencelly and mod 2 to make decision. If all bytes in DeterminationBytes are used it will 
	 * hash the old DeterminationBytes to generate a new one.
	 * notes: the same seeds will generate the same result
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public boolean InsertRKSLocal() throws NoSuchAlgorithmException{
		if(DeterminationIndex < DeterminationBytes.length){
			int result = (DeterminationBytes[DeterminationIndex]%2);
			System.out.println("byte : " +DeterminationBytes[DeterminationIndex]+ " Result : " + result);
			DeterminationIndex++;
			return (result==0?true:false);
			
		}
		else{
			DeterminationIndex=0;
			Hash HashF = new Hash();
			DeterminationBytes = HashF.GetHash(DeterminationBytes, 2);
			int result = (DeterminationBytes[DeterminationIndex]%2);
			System.out.println("byte : " +DeterminationBytes[DeterminationIndex]+ " Result : " + result);
			DeterminationIndex++;
			return (result==0?true:false);
		}
		
	}
	/********************
	 * this function is used to prepare for RKSGeneration
	 * @param seed
	 */
	public void RKSGeneration_Preparation(long seed){
		this.RKS_Character_Determination_Seed = seed;
		for(int index=0; index<95;index++){
			this.CharacterMap.put(new Integer(index), ((char)(index+32)+ ""));
		}
		RND rnd = new RND();
		RKS_Determination_Bytes = rnd.getRandom_RKS_Determination(this.RKS_Character_Determination_Seed);
		System.out.println("RKS_DETERMINATION BYTES : " + RKS_Determination_Bytes[0]);
		System.out.println("RKS_DETERMINATION BYTES LENGth : " + RKS_Determination_Bytes.length);
		
	}
	/**************
	 * this function is used to generate the RKSs in String array
	 * every time when need to insert RKSs just call this method and it will return a String array.
	 * @return
	 */
	public String[] RKS_Determination(){
		int[] temp_characters_array;
		String[] temp_character_string_array;
		if(RKS_Determination_Bytes_Index < RKS_Determination_Bytes.length - 4){
			int temp_length=0;
			temp_length = (RKS_Determination_Bytes[RKS_Determination_Bytes_Index] % 4) + 1;
			if(temp_length <= 0)
				temp_length = temp_length + 4;
			byte[] temp_determination_bytes_array = new byte[temp_length];
			System.arraycopy(RKS_Determination_Bytes, RKS_Determination_Bytes_Index, temp_determination_bytes_array, 0, temp_length);
			temp_characters_array = new int[temp_length];
			for(int index=0;index<temp_length;index++){
				temp_characters_array[index] = (temp_determination_bytes_array[index] % 95);
				if(temp_characters_array[index] < 0)
					temp_characters_array[index] = temp_characters_array[index] + 95;
			}
			RKS_Determination_Bytes_Index = RKS_Determination_Bytes_Index + temp_length;
		}
		else{

			RKS_Determination_Bytes_Index = 0;
			Hash HashF = new Hash();
			RKS_Determination_Bytes = HashF.GetHash(RKS_Determination_Bytes, 2);
			int temp_length = 0;
			temp_length = (RKS_Determination_Bytes[RKS_Determination_Bytes_Index] % 4) + 1;
			if(temp_length <= 0)
				temp_length = temp_length + 4;
			byte[] temp_determination_bytes_array = new byte[temp_length];
			System.arraycopy(RKS_Determination_Bytes, RKS_Determination_Bytes_Index, temp_determination_bytes_array, 0, temp_length);
			temp_characters_array = new int[temp_length];
			for(int index=0;index<temp_length;index++){
				temp_characters_array[index] = (temp_determination_bytes_array[index] % 95);
				if(temp_characters_array[index] < 0)
					temp_characters_array[index] = temp_characters_array[index] + 95;
				
			}
			RKS_Determination_Bytes_Index = RKS_Determination_Bytes_Index + temp_length;
		}
		temp_character_string_array = new String[temp_characters_array.length];
		for(int index=0;index<temp_character_string_array.length;index++){
			temp_character_string_array[index] = CharacterMap.get(temp_characters_array[index]);
		}
		return temp_character_string_array;
	}
	/***********
	 * this method is used to print out all elements in 
	 */
	public void PrintOutCharacterMap(){
		java.util.Iterator<Entry<Integer, String>> it = CharacterMap.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pairs = (Map.Entry)it.next();
	        System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
		System.out.println("MAP SIZE : " + CharacterMap.size());
	}
	/************
	 * according to user input the RKS will be changed
	 * this method should be called when the button is clicked
	 * @param keySet
	 */
	public void UpdateRKS_with_UKS(String[] keySet){
		Hash hash = new Hash();
		byte[] temp_hash = hash.GetHash(keySet,32);
		for(int i=0;i<RKS_Determination_Bytes.length;i++){
			RKS_Determination_Bytes[i] = (byte) (RKS_Determination_Bytes[i] ^ temp_hash[i]);
		}
		
	}
	public void UpdateRKS_with_UKS(String keyset){
		Hash hash = new Hash();
		byte[] temp_hash = hash.GetHash(keyset);
		for(int i=0;i<RKS_Determination_Bytes.length;i++){
			RKS_Determination_Bytes[i] = (byte) (RKS_Determination_Bytes[i] ^ temp_hash[i]);
		}
	}
	/*********
	 * for test bytes XOR operation
	 * @param b1
	 * @param b2
	 * @return
	 */
	public byte[] Bytes_XOR_Operator(byte[] b1, byte[] b2){
		for(int i=0;i<b1.length;i++){
			b1[i] = (byte) (b1[i] ^ b2[i]);
		}
		return b1;
	}
	public void UpdateRKS_with_UKS(ArrayList<String> keySet){
		String[] temp_Strings = new String[keySet.size()];
		for(int i=0;i<temp_Strings.length;i++){
			temp_Strings[i] = keySet.get(i);
		}
		this.UpdateRKS_with_UKS(temp_Strings);
	}
	/********
	 * this part of code is used to test InsertRKSLocalInitiate() and InsertRKSLocal() and the statistic
	 * result shows that it is random and both parties have the same chance to insert RKS.
	 * @param args
	 * @throws NoSuchAlgorithmException
	 */
//	public static void main(String[] args) throws NoSuchAlgorithmException{
//		long s1 = 1;
//		int local=0;
//		int peer=0;
//		int a=0;
//		a++;
//		a++;
//		long s2 = 1;
//		RandomKeyStroke RKS = new RandomKeyStroke();
//		RKS.InsertRKSLocalInitiate(s1, s2);
//		for(int i=0;i<10;i++){
//			if(RKS.InsertRKSLocal()){
//				System.out.println("Local Insert....");
//				local++;
//			}
//			else {
//				System.out.println("Peer Insert....");
//				peer++;
//			}
//		}
//		System.out.println("statistic result : " + (double)((double)local/(double)peer));
//	}
	/************
	 * this part of code is used to test RKSGeneration_Preparation() and RKS_Determination(). looks good
	 * correct but the symbol ` and " can not be recognized in EXCEL.
	 * if the time seed has not been changed, the output RKS will not be changed
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		long RKS_Character_Determination_Seed = 2;
		RandomKeyStroke RKS = new RandomKeyStroke();
		RKS.RKSGeneration_Preparation(RKS_Character_Determination_Seed);
		String[] aString = RKS.RKS_Determination();
		for(int i=0;i<aString.length;i++){
			System.out.println(aString[i]);
		}
		FileOutputStream fos = new FileOutputStream(new File("characters statistics.txt"),true);
			

		RKS.PrintOutCharacterMap();
//		for(int i=0;i<50000;i++){
//			String[] RKS_Characters = RKS.RKS_Determination();
//			for(int j=0;j<RKS_Characters.length;j++){
//				//System.out.print(RKS_Characters[j] + " ");
//				fos.write((RKS_Characters[j] + "\r\n").getBytes());
//			}
//
//			
//		}
		fos.close();
		
	}
}
