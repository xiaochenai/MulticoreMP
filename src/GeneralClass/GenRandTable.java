package GeneralClass;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import biz.source_code.base64Coder.Base64Coder;


public class GenRandTable {
	private byte[][] RNDArray = new byte[95][32];
	GenRandTable() throws IOException{
		File dir = new File("RNDArray.txt");
		String tempString = null;
		byte[] RND;
		int index=0;
		if(dir.exists()){
			System.out.println("RND FILE EXIST");
			BufferedReader reader = new BufferedReader(new FileReader(dir));
			while ((tempString = reader.readLine()) != null) {
                RND = Base64Coder.decodeLines(tempString);
                System.arraycopy(RND, 0, RNDArray[index], 0, 32);
                index++;
            }
		}
		else{
			System.out.println("CREATE RND");
			GenRNDArray();
		}
	}
	/***************
	 * 
	 * @return Random HashArray[36][32]
	 * @throws IOException 
	 */
	private void SaveRNDArray() throws IOException{
		FileOutputStream fos = new FileOutputStream("RNDArray.txt");
		for(int index=0;index<RNDArray.length;index++){
			String encodeString = Base64Coder.encodeLines(RNDArray[index]);
			fos.write(encodeString.getBytes());
		}
		fos.close();
	}

	public void update() throws IOException{
		GenRNDArray();
	}

	private void GenRNDArray() throws IOException{
		byte[] RND = new byte[32];
		for(int i=0;i<95;i++){
			RND = RandNum();
			System.arraycopy(RND, 0, RNDArray[i], 0, 32);
		}
		SaveRNDArray();
		
	}
	private byte[][] GeneRandArray(){
		byte[][] HashArray = new byte[95][32];
		for(int i=0;i<HashArray.length;i++){
			byte[] hash = getHash(RNDArray[i]);
		
			System.arraycopy(hash, 0, HashArray[i], 0, hash.length);
		}
		return HashArray;
	}
	/********************
	 * 
	 * @return random number byte[]
	 */
	private byte[] RandNum(){
		RND rand = new RND();

		try {
			return rand.getRandom();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/*************************************
	 * note:get the last byte of RandArray and mod 32 get a number len in (0,31),use this as mut length
	 * 
	 * @param RandArray
	 * @return varied length random Array
	 */
	private byte[][] MutLen(byte[][] RandArray){
		int mod=32;
		int len ;
		byte[][] newRandArray = new byte[95][];
		for(int index=0;index<RandArray.length;index++){
			len = RandArray[index][31]%mod;
			if(len<=0)
				len = len+32;
			newRandArray[index] = new byte[len];

			System.arraycopy(RandArray[index], 0, newRandArray[index], 0, len);
		
		}
		return newRandArray;
		
	}
	/**********************************
	 * note: another algorithm for varied length array
	 * @param RandArray
	 * @param Len
	 * @return varied length random array
	 */
	private byte[][] MulLen(byte[][] RandArray,int Len){
		int mod = 32;
		int len = 0;
		byte[][] newRandArray = new byte[95][];
		for(int index=0;index<RandArray.length;index++){
			len = getHash(RandArray[index],Integer.toString(len).getBytes())[0]%32;
			if(len<=0)
				len = len+32;
			newRandArray[index] = new byte[len];
			System.arraycopy(RandArray[index], 0, newRandArray[index], 0, len);
		}
		return newRandArray;
	}
	/******************
	 * note: outside interface to get varied length random array
	 * @return varied length random byte[] array
	 */
	public byte[][] GetMutLenRandArray(){
		byte[][] a = GeneRandArray();
		byte[][] newa = MutLen(a);
		return newa;
	}
	/*************
	 * 
	 * @return varied length random String[]
	 */
	public String[] GetMutLenRandString(){
		byte[][] a = GeneRandArray();
		byte[][] newa = MutLen(a);
		String[] MutStringArray = new String[95];
		for(int index=0;index<95;index++){
			MutStringArray[index] = Base64Coder.encodeLines(newa[index]);
		}
		return MutStringArray;
	}
	/******************
	 * 
	 * @param byte[] RND
	 * @param byte[] salt
	 * @return hash byte[],do hash 5 times
	 */
	private  byte[] getHash( byte[] RND,byte[] salt)
	{
		MessageDigest digest = null;

		byte[] input = new byte[0];
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(salt);
			input = digest.digest(RND);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("NoSuchAlgorithmException: " + e);
			System.exit(-1);
		}

		for (int i = 0; i < 5; i++)
		{
			digest.reset();
			input = digest.digest(input);
		}
		return input;
	}
	/***************
	 * 
	 * @param RND
	 * @return hash byte[], do hash 5times
	 */
	private  byte[] getHash( byte[] RND)
	{
		MessageDigest digest = null;

		byte[] input = new byte[0];
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(RND);
			input = digest.digest(RND);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("NoSuchAlgorithmException: " + e);
			System.exit(-1);
		}

		for (int i = 0; i < 5; i++)
		{
			digest.reset();
			input = digest.digest(input);
		}
		return input;
	}
	/***************************
	 * test code
	 * @param args
	 * @throws IOException
	 */
	
	public static void main(String[] args) throws IOException{
		
		GenRandTable s = new GenRandTable();
		String[] a = s.GetMutLenRandString();
		GenRandTable s1 = new GenRandTable();
		s1.update();
		String[] a1=s1.GetMutLenRandString();
		for(int i=0;i<a.length;i++){
			System.out.println(a[i]);
			System.out.println(a1[i]);
			System.out.println("*************");
		}
		
//		GenRandTable s = new GenRandTable();
//		long start1,end1,ave1,start2,end2,start3,end3,ave2,ave3;
//		ave1=ave2=ave3=0;
//		byte[][] a =s.GeneRandArray();;
//		byte[][] newa = s.MutLen(a);
//		for(int i=0;i<50;i++){
//			start1 = System.nanoTime();
//			 a =s.GeneRandArray();
//			end1 = System.nanoTime();
//			ave1 = end1-start1+ave1;
//			
//			start2 = System.nanoTime();
//			newa = s.MutLen(a);
//			end2 = System.nanoTime();
//			ave2 = ave2+end2-start2;
//			
//			start3 = System.nanoTime();
//			newa = s.MulLen(a, 32);
//			end3 = System.nanoTime();
//			ave3 = ave3+end3-start3;
//		}
//		
//		System.out.println("GEN RAND TABLE ave : " + (ave1/50));
//
//		System.out.println("GEN MUT TABLE1 ave : " + (ave2/50));
//		
//		System.out.println("GEN MUT TABLE2 : " + (ave3/50));
//		
//		System.out.println("a's LEN is : " + a.length);
//		System.out.println("newa's Len is  : " + newa.length);
//		String[] pp = s.GetMutLenRandString();
//		for(int index=0;index<a.length;index++){
//			System.out.println("OLD : " + Base64Coder.encodeLines(a[index]) + " LEN : " + a[index].length);
//			System.out.println("NEW : " + Base64Coder.encodeLines(newa[index]) + " LEN : " + newa[index].length);
//			System.out.println("NEW : " + Base64Coder.encodeLines(newa[index]) );
//			
//		}
//		FileOutputStream fos = new FileOutputStream("MutLenRandTable.txt");
//		for(int index=0;index<newa.length;index++){
//			String encodeString = Base64Coder.encodeLines(newa[index]);
//			fos.write(encodeString.getBytes());
//		}
//		fos.close();
//
//		
//		
//		fos = new FileOutputStream("RandTable.txt");
//		for(int index=0;index<a.length;index++){
//			String encodeString = Base64Coder.encodeLines(a[index]);
//			fos.write(encodeString.getBytes());
//		}
//		fos.close();
//		fos = new FileOutputStream("NumTable.txt");
//		for(int i=65;i<91;i++){
//			char cap=(char)i;
//			char l = (char)(i+32);
//			String aa = cap + "&"+l+"\r\n";
//			fos.write(aa.getBytes());
//		}
//		for(int i=48;i<58;i++){
//			char l=(char)i;
//			String aa=l+"\r\n";
//			fos.write(aa.getBytes());
//		}
//		fos.close();
//	
//		
	}
}
