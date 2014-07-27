package VariedLengthMappingTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import biz.source_code.base64Coder.Base64Coder;
import GeneralClass.*;

public class VariedLengthMappingTable {
	//store the random number and save to file
	private byte[][] RNDArray = new byte[95][32];
	private HashMap<String, String> CharacterMap = new HashMap<String, String>();
	private HashMap<String, Byte[]> characterMap_inByte = new HashMap<String, Byte[]>();
	private HashMap<String, String> KeysetMap = new HashMap<String,String>();
	private String[][] keySetStrings;
	private String[] keySetMappedStrings;
	private byte[][] MutLenRandArray;
	/*******************
	 * note:if RNDArray.txt exist load this file, if not create a new RNDArray
	 * @throws IOException
	 */
	public VariedLengthMappingTable(String[][] keyset) throws IOException{
		this.keySetStrings = keyset;
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
	/*********
	 * another constructor for temp use
	 * @throws IOException
	 */
	public VariedLengthMappingTable() throws IOException{
		
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
	/***********
	 * Generate mapping sequences for given key set, key set is passed though the constructor
	 */
	private void GenKeySetMappingTable(){
		Hash hash = new Hash();
		keySetMappedStrings = null;
		String[][] temp_CharacterSequenceStrings = new String[keySetStrings.length][];
		for(int index=0;index<keySetStrings.length;index++){
			for(int jndex=0;jndex<keySetStrings[index].length;jndex++){
				temp_CharacterSequenceStrings[index][jndex] = LookUpMappingSequence(keySetStrings[index][jndex]); 
			}
		}
		for(int index = 0; index<temp_CharacterSequenceStrings.length;index++){
			keySetMappedStrings[index] = Base64Coder.encodeLines(hash.GetHash_KeySetMappingString(temp_CharacterSequenceStrings[index]));
		}
		
	}
	public String[] GetKeySetMappedSequence(){
		GenKeySetMappingTable();
		return keySetMappedStrings;
	}
	/***********************
	 * this method is used to initializing, this will prepare the HashMap used for looking up corresponding
	 * random sequence
	 */
	public void initiation(){
		byte[][] a =this.GeneRandSeqArray();;
		byte[][] newa = this.MutLen(a);
		String[] temp_string_array_mapped_sequenceStrings = this.BytesToString(newa);
		for(int index=0; index<95;index++){
			this.CharacterMap.put(((char)(index+32)+ ""),temp_string_array_mapped_sequenceStrings[index]);
		}
		
	}
	/***********
	 * look up a corresponding random sequence of a given character, this method should be private, public
	 * is for temp use
	 * @param C
	 * @return
	 */
	public String LookUpMappingSequence(String C){
		return this.CharacterMap.get(C);
	}
	public String LookUpKeySetMappedSequence(String[] keyset){
		Hash hash = new Hash();
		byte[] HashedSequence = hash.GetHash_KeySetMappingString(keyset);
		return Base64Coder.encodeLines(HashedSequence);
	}
	public String LookUpKeySetMappedSequence(ArrayList<String> keyset){
		String[] keysetStrings = new String[keyset.size()];
		for(int i=0;i<keysetStrings.length;i++){
			keysetStrings[i] = this.LookUpMappingSequence(keyset.get(i));
		}
		Hash hash = new Hash();
		byte[] HashedSequence = hash.GetHash_KeySetMappingString(keysetStrings);
		return Base64Coder.encodeLines(HashedSequence);
	}
	/*************
	 * convert byte[][] to String[]
	 * @param b
	 * @return
	 */
	private String[] BytesToString(byte[][] b){
		String[] temp_string_array = new String[b.length];
		for(int i=0;i<b.length;i++){
			temp_string_array[i] = Base64Coder.encodeLines(b[i]);
		}
		return temp_string_array;
	}
	/***************
	 * 
	 * @return Random HashArray[95][32]
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
	/**************
	 * note:update RNDArray and save to RNDArray.txt
	 * @throws IOException
	 */
	public void update() throws IOException{
		GenRNDArray();
	}
	/*************
	 * note:Generate RNDArray and save to RNDArray.txt, this file is used to recover the Random Seq Table. And this is also
	 * used to generate random sequence Array
	 * @throws IOException
	 */
	private void GenRNDArray() throws IOException{
		byte[] RND = new byte[32];
		for(int i=0;i<95;i++){
			RND = RandNum();
			System.arraycopy(RND, 0, RNDArray[i], 0, 32);
		}
		SaveRNDArray();
		
	}
	/*************
	 * 
	 * note:Generate original RNDArray(32 bytes * 95,the sequence in the table), used in MutLen()
	 * @return Random Sequence Array, the original Array, then is mutated length in MutLen()
	 */
	private byte[][] GeneRandSeqArray(){
		byte[][] HashArray = new byte[95][32];
		for(int i=0;i<HashArray.length;i++){
			byte[] hash = getHash(RNDArray[i]);
		
			System.arraycopy(hash, 0, HashArray[i], 0, hash.length);
		}
		return HashArray;
	}
	/********************
	 * note:this random number byte[] is used to generate Random Sequence by hash
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
	//has not been finished yet, need to be improved
	private byte[] RandNum(long Seed){
		RND rand = new RND();
		return rand.getRandom_GenRandTable(Seed);
		
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
	private byte[][] MutLen(byte[][] RandArray,int Len){
		int mod = 32;
		int len = 0;
		byte[][] newRandArray = new byte[95][];
		for(int index=0;index<RandArray.length;index++){
			len = getHash(RandArray[index],Integer.toString(Len).getBytes())[0]%mod;
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
	private byte[][] GetMutLenRandArray(){
		byte[][] a = GeneRandSeqArray();
		MutLenRandArray = MutLen(a);
		return MutLenRandArray;
	}
	/*************
	 * 
	 * @return varied length random String[]
	 */
	private String[] GetMutLenRandString(){
		byte[][] a = GeneRandSeqArray();
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
			input = digest.digest(input);
		}
		return input;
	}
/***************
 * test code for class VariedLengthMappingTable
 * looks good
 * @param args
 * @throws IOException
 */
public static void main(String[] args) throws IOException{
	VariedLengthMappingTable VLMT = new VariedLengthMappingTable();
	VLMT.initiation();
	String A = VLMT.LookUpMappingSequence("A");
	System.out.println("A Mapped To : " + A);
	VLMT.update();
	VLMT.initiation();
	A = VLMT.LookUpMappingSequence("A");
	System.out.println("A Mapped To : " + A);
	
	}
}
