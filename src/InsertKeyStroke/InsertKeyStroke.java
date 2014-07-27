package InsertKeyStroke;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import biz.source_code.base64Coder.Base64Coder;
public class InsertKeyStroke {
	private HashMap<Integer,String> CharacterMap = new HashMap<Integer,String>();
	private HashMap<String,byte[]> StrokeRNDMap = new HashMap<String,byte[]>();
	private List<Byte> HashRand = new ArrayList<Byte>();
	private byte[][] RandByteArray = new byte[36][32];
	private String[] RandSeq = new String[36];

	/*************
	 * note: if RandByteArray exist then load it, if not then create a new Rand Byte Array and save it to file
	 * @param Randseq: the mutated length Random sequence Array
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public InsertKeyStroke(String[] Randseq) throws NoSuchAlgorithmException, IOException{
		System.arraycopy(Randseq, 0, RandSeq, 0, Randseq.length);
//		for(int index=0;index<RandSeq.length;index++){
//			System.out.println(RandSeq[index]);
//		}
		for(int i=0;i<26;i++){
			this.CharacterMap.put(new Integer(i), ((char)(i+65)+ ""));
		}

		for(int i=26;i<36;i++){
			this.CharacterMap.put(new Integer(i), ((char)(i+22)+ ""));
		}
		File dir = new File("RandByteArray.txt");
		String tempString = null;
		byte[] RND;
		int index=0;
		if(dir.exists()){
			//System.out.println("RandByteArray FILE EXIST");
			BufferedReader reader = new BufferedReader(new FileReader(dir));
			while ((tempString = reader.readLine()) != null) {
                RND = Base64Coder.decodeLines(tempString);
                System.arraycopy(RND, 0, RandByteArray[index], 0, 32);
                index++;
            }
		}
		else{
			//System.out.println("CREATE RandByteArray");
			GenRandByteArray();
		}
		
		ArrayToMap();
	}
	/****************
	 * note:update the Random Byte Array and save to file
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public void update() throws IOException, NoSuchAlgorithmException{
		GenRandByteArray();
		ArrayToMap();
	}
	private void GenRandByteArray() throws IOException, NoSuchAlgorithmException{
		RND rand = new RND();
		byte[] RND = new byte[32];
		for(int i=0;i<36;i++){
			RND = rand.getRandom();
			System.arraycopy(RND, 0, RandByteArray[i], 0, 32);
		}
		SaveRNDArray();
	}
	/*******************
	 * note:transform Random Sequence Array to Map
	 */
	private void ArrayToMap(){
		for(int index=0;index<36;index++){
			StrokeRNDMap.put(RandSeq[index], RandByteArray[index]);
		}
	}
	/**********************
	 * note:save Random Byte Array 
	 * @throws IOException
	 */
	private void SaveRNDArray() throws IOException{
		FileOutputStream fos = new FileOutputStream("RandByteArray.txt");
		for(int index=0;index<RandByteArray.length;index++){
			String encodeString = Base64Coder.encodeLines(RandByteArray[index]);
			fos.write(encodeString.getBytes());
		}
		fos.close();
	}
	/***************
	 * note: test method, print out the Character Map
	 */
	public void PrintCharacterMap(){
		System.out.println("221");
		for(int i=0;i<this.CharacterMap.size();i++){
			System.out.println("Key : " + i + "String" +this.CharacterMap.get(new Integer(i)));
		}
	}
	/************
	 * 
	 * @param RandSeq: the Mutated length String
	 * @return last byte of Hash result,this byte is used to determine whether insert key Stroke. used in getMappedCharacter()
	 * @throws NoSuchAlgorithmException
	 */
	private byte GetRandByte(String RandSeq) throws NoSuchAlgorithmException{
		Hash hash = new Hash();
		byte[] hashR = hash.GetHash(Base64Coder.decodeLines(RandSeq), 10,StrokeRNDMap.get(RandSeq));
		HashRand.add(hashR[hashR.length-1]);
		return hashR[hashR.length-1];
	}
	/**************
	 * 
	 * @param RandSeq:the mutated length Random Sequence
	 * @return 50% is the mapped character, and 50% is null
	 * @throws NoSuchAlgorithmException
	 */
	public String getMappedCharacter(String RandSeq) throws NoSuchAlgorithmException{
		byte RandByte = GetRandByte(RandSeq);
		int mod=72;
		int Result=0;
		if((Result=RandByte % mod )<0)
			Result = Result + 72;
		if(Result <36){
			return CharacterMap.get(new Integer(Result));
		
		}
		else
			return null;
			
	}

}
