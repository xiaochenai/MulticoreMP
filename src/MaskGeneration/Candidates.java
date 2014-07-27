package MaskGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.Iterator;


import biz.source_code.base64Coder.Base64Coder;

import com.rsa.shareCrypto.j.it;
import com.rsa.shareCrypto.j.sb;

import KeySetGeneration.KeySetGeneration;
import KeySetGeneration.keySet;
import VariedLengthMappingTable.VariedLengthMappingTable;

public class Candidates {
	private String[][] oldKeys;
	private String[][] newKeys;
	private int[] UKS;
	private keySet OldKeySet;
	private keySet NewKeySet;
	private ArrayList<ArrayList<Integer>> childTree = new ArrayList<ArrayList<Integer>>();
	private String[] MASK;
	private String[] Processed_UKS;
	private VariedLengthMappingTable VLMT;
	private String[] OldMask;
	private String[] Old_Processed_UKS;
	private String[] Reversed_UKS;
	/*************
	 * Constructor
	 * @param oldKeySet
	 * @param newKeySet
	 * @param UKS
	 * @throws IOException
	 */
	public Candidates(String[][] oldKeySet,String[][] newKeySet, int[] UKS) throws IOException {
		// TODO Auto-generated constructor stub
		OldKeySet = new keySet(oldKeySet); 
		NewKeySet = new keySet(newKeySet);
		this.UKS = UKS;
		VLMT = new VariedLengthMappingTable();
		VLMT.initiation();
//		System.out.println("OLD : ");
//		OldKeySet.Printout();
//		System.out.println("NEW : ");
//		NewKeySet.Printout();
//		System.out.println("****************");
	}
	public Candidates(String[][] oldKeySet,String[][] newKeySet, int[] UKS, VariedLengthMappingTable VLMT){
		OldKeySet = new keySet(oldKeySet); 
		NewKeySet = new keySet(newKeySet);
		this.UKS = UKS;
		this.VLMT = VLMT;

		VLMT.initiation();
//		System.out.println("OLD : ");
//		OldKeySet.Printout();
//		System.out.println("NEW : ");
//		NewKeySet.Printout();
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
	}
	/*************
	 * generate the parent-child tree of old-new keyset
	 */
	public void Generate_Tree(){
		ArrayList<Integer> A = new ArrayList<Integer>();
		for(int i=0;i<UKS.length;i++){
			ArrayList<Integer> temp_A = new ArrayList<Integer>();
			ArrayList<String> temp_SAL = OldKeySet.Get_KeySet(UKS[i]);
			java.util.Iterator<String> iterator = temp_SAL.iterator();
			while(iterator.hasNext()){
				String tempString = iterator.next();
				//System.out.print(tempString + "*");
				int index = NewKeySet.Get_Index(tempString);
				if(temp_A.contains(index) == false){
					//System.out.println("\r\n$$" + tempString + "-" +index);
					temp_A.add(index);
				}
			}
			//System.out.println();
			childTree.add(temp_A);
		}
//		System.out.println("CHILD TREE");
//		for(int i=0;i<childTree.size();i++){
//			for(int j=0;j<childTree.get(i).size();j++){
//				System.out.print(childTree.get(i).get(j) + " ");
//			}
//			System.out.println();
//		}
//		System.out.println();
//		System.out.println("Size of UKS : " + UKS.length);
//		System.out.println("Size of childTree : " + childTree.size());
	}
	public void MASKCHECK(){
		for(int i=0;i<MASK.length;i++){
			System.out.println(MASK[i] + "\r]\n" + OldMask[i]);
			System.out.println("*************");
			
			if(MASK[i].equals(OldMask[i]))
				System.out.println("same");
			else {
				System.out.println("different");
			}
			System.out.println("*************");
		}
	}
	public void Printout_Parents_Child(){
		for(int i=0;i<UKS.length;i++){
//			System.out.println();
//			System.out.println("Parents : " );
			ArrayList<String> temp_Parent_String_ArrayList = OldKeySet.Get_KeySet(UKS[i]);
			java.util.Iterator<String> iterator = temp_Parent_String_ArrayList.iterator();
//			while(iterator.hasNext()){
//				System.out.print(iterator.next()+ " ");
//			}
			//System.out.println("Child is :");
			ArrayList<Integer> child_Index = childTree.get(i);
			if(child_Index.size()<2){
				//System.out.println("Has not changed");
				ArrayList<String> temp_Child_String_ArrayList = NewKeySet.Get_KeySet(child_Index.get(0));
				iterator = temp_Child_String_ArrayList.iterator();
				while(iterator.hasNext()){
					//System.out.print(iterator.next()+ " ");
				}
			}
			else{
				//System.out.println("Has been changed");
				ArrayList<String> temp_Child_String_ArrayList = NewKeySet.Get_KeySet(child_Index.get(0));
				iterator = temp_Child_String_ArrayList.iterator();
				while(iterator.hasNext()){
					//System.out.print(iterator.next()+ " ");
				}
				//System.out.println();
				temp_Child_String_ArrayList = NewKeySet.Get_KeySet(child_Index.get(1));
				iterator = temp_Child_String_ArrayList.iterator();
				while(iterator.hasNext()){
					//System.out.print(iterator.next()+ " ");
				}
			}
			
			
			
		}
	}
	private String String_XNOR_Operation(String S1, String S2){
		//System.out.println("XNOR Operation");
		StringBuilder sb = new StringBuilder();
		//System.out.println();
		byte[] b1 = S1.getBytes();
		byte[] b2 = S2.getBytes();
		byte[] b3 = new byte[b1.length];
		for(int i = 0; i < b1.length; i++){
			String i1 = Integer.toBinaryString(b1[i]);
			String i2 = Integer.toBinaryString(b2[i]);
			//System.out.println("i1 : " + i1 + " " + i1.length() + " " +b1[i]);
			//System.out.println("i2 : " + i2 + " " + i2.length() + " " +b2[i]);
			if(i1.length() < i2.length()){
				i1 = "0" + i1;
			}
			else if(i2.length()<i1.length()){
				i2 = "0" + i2;
			}
			//System.out.println("i1 : " + i1 + " " + i1.length() + " " +b1[i]);
			//System.out.println("i2 : " + i2 + " " + i2.length() + " " +b2[i]);
			String i3 = Binary_String_XNOR(i1, i2);
			//System.out.println("i3 : " + i3);
			
			
			sb.append((char)Integer.parseInt(i3,2));
		}
		return sb.toString();
	}
	private String Binary_String_XNOR(String s1,String s2){
		StringBuilder sb = new StringBuilder();
		//System.out.println("s1 :" + s1.length() + " s2 : " + s2.length());
		for(int i=0;i<s1.length();i++){
			if(s1.charAt(i) == s2.charAt(i)){
				sb.append("1");
			}
			else {
				sb.append("0");
			}
		}
		String result = sb.toString();
		return result;
	}
	private String Binary_String_AND(String s1,String s2){
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<s1.length();i++){
			if(s1.charAt(i) == '1'&& s2.charAt(i)=='1'){
				sb.append("1");
			}
			else {
				sb.append("0");
			}
		}
		String result = sb.toString();
		return result;
	}
	
	private String String_AND_Operation(String S1, String S2){
		StringBuilder sb = new StringBuilder();
		System.out.println();
		byte[] b1 = S1.getBytes();
		byte[] b2 = S2.getBytes();
		byte[] b3 = new byte[b1.length];
		//System.out.println("AND Operation");
		for(int i = 0; i < b1.length; i++){
			String i1 = Integer.toBinaryString(b1[i]);
			//System.out.println("i1 : " + i1);
			String i2 = Integer.toBinaryString(b2[i]);
			//System.out.println("i2 : " + i2);
			if(i1.length() < i2.length()){
				int ceil = i2.length()-i1.length();
				for(int j=0;j<(ceil);j++){
					i1 = "0" + i1;
				}
			}
			else if(i2.length()<i1.length()){
				int ceil = i1.length()-i2.length();
				for(int j=0;j<(ceil);j++){
					i2 = "0" + i2;
					
				}
				
			}
//			System.out.println("i1** : " + i1);
//			System.out.println("i2** : " + i2);
			String i3 = Binary_String_AND(i1, i2);
			//System.out.println("i3 : " + i3);
			sb.append((char)Integer.parseInt(i3,2));
		}
		return sb.toString();
	}
	private String Imbalanced_String_XNOR_Operation(String S1,String S2){
		if(S1.length() < S2.length()){
			String temp_String = this.String_XNOR_Operation(S1, S2.substring(0, S1.length()));
			temp_String = temp_String +S2.substring(S1.length());
			return temp_String;
		}
		else{
			String temp_String = this.String_XNOR_Operation(S1.substring(0, S2.length()), S2);
			temp_String = temp_String + S1.substring(S2.length());
			return temp_String;
		}
	}
	private String Imbalanced_String_AND_Operation(String S1,String S2){
		if(S1.length() < S2.length()){
			String temp_String = this.String_AND_Operation(S1, S2.substring(0,S1.length()));
			temp_String = temp_String + S2.substring(S1.length());
			return temp_String;
		}
		else{
			String temp_String = this.String_AND_Operation(S1.substring(0,S2.length()), S2);
			temp_String = temp_String + S1.substring(S2.length());
			return temp_String;
		}
		
	}
	private void reverMASKtest(String[] MASK,int[] a){
		Reversed_UKS = new String[MASK.length];
		for(int i=0;i<MASK.length;i++){
			String temp = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(childTree.get(i).get(0)));
			Reversed_UKS[i]= this.Imbalanced_String_AND_Operation(temp,MASK[i]);
		}
	}
	private void ReversedMASK(String[] MASK, String[] Keysets){
		Reversed_UKS = new String[MASK.length];
		for(int i=0;i<MASK.length;i++){
			Reversed_UKS[i]= this.Imbalanced_String_AND_Operation(Keysets[i],MASK[i]);
			System.out.println("S : " + Keysets[i]);
		}
	}
	public String[] doReverseMASK(){
		String[] keysets = new String[UKS.length];
		for(int i=0;i<UKS.length;i++){
			keysets[i] = VLMT.LookUpKeySetMappedSequence(OldKeySet.Get_KeySet(UKS[i]));//note need to be changed
		}
		ReversedMASK(this.OldMask, keysets);
		return Reversed_UKS;
	}
	public String[] doReverseMASKtest(){
		reverMASKtest(this.MASK, this.UKS);
		return Reversed_UKS;
	}
	public void SaveMask2File(String FileName) throws IOException{
		FileOutputStream fos = new FileOutputStream(new File(FileName));
		for(int i=0;i<MASK.length;i++){
			
			fos.write(Base64Coder.encodeString(MASK[i]).getBytes());
			fos.write("\n".getBytes());
		}
		fos.close();
	}
	public void SaveProcessedUKS2File(String FileName) throws IOException{
		FileOutputStream fos = new FileOutputStream(new File(FileName));
		for(int i=0;i<Processed_UKS.length;i++){
			fos.write(Base64Coder.encodeString(Processed_UKS[i]).getBytes());
			fos.write("\n".getBytes());
		}
		fos.close();
	}
	public int LoadExistMask(String FileName) throws IOException{
		BufferedReader fin = new BufferedReader(new FileReader(FileName));
		ArrayList<String> tempAL = new ArrayList<String>();
		String ppString = null;
		while((ppString = fin.readLine())!=null){
			String aString = Base64Coder.decodeString((ppString));
			tempAL.add(aString);
		}
		fin.close();
		OldMask = new String[tempAL.size()];
		for(int i=0;i<tempAL.size();i++){
			OldMask[i] = tempAL.get(i);
		}
		return OldMask.length;
	}
	/*************
	 * Generate the processed UKS which will be send to server for authentication
	 */
	public void Generate_Processed_UKS(){
		Processed_UKS = new String[UKS.length];
		int UKSi=0;
		java.util.Iterator<ArrayList<Integer>> it = childTree.iterator();
		while(it.hasNext()){
			ArrayList<Integer> temp_int_ArrayList = it.next(); 
			if(temp_int_ArrayList.size() < 2){
				String S1 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				String S2 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				System.out.println("S1 : " + S1);
				System.out.println("S2 : " + S2);
				Processed_UKS[UKSi] = this.Imbalanced_String_AND_Operation(S1, S2);
			}
			else{
				String S1 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				String S2 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(1)));
				System.out.println("S1 : " + S1);
				System.out.println("S2 : " + S2);
				Processed_UKS[UKSi] = this.Imbalanced_String_AND_Operation(S1, S2);
			}
			UKSi++;
		}
	}

	/************
	 * Get Processed UKS
	 * @return
	 */
	public String[] Get_Processed_UKS(){
		return Processed_UKS;
	}
	/********
	 * get MASK
	 * @return
	 */
	public String[] Get_MASK(){
		return MASK;
	}
	public void Reverse_MASK(){
		
	}
	public void SaveMASK2File(){
		
	}
	/**************
	 * generate MASK
	 */
	public void Generate_Mask(){
		MASK = new String[UKS.length];
		int MASKi=0;
		java.util.Iterator<ArrayList<Integer>> it = childTree.iterator();
		while(it.hasNext()){
			ArrayList<Integer> temp_int_ArrayList = it.next(); 
			if(temp_int_ArrayList.size() < 2){
				String S1 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				String S2 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				MASK[MASKi] = this.Imbalanced_String_XNOR_Operation(S1, S2);
				//System.out.println("do not changed mask");
				//System.out.println(MASK[MASKi]);
			}
			else{
				//System.out.println("changed MASK");
				String S1 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(0)));
				String S2 = VLMT.LookUpKeySetMappedSequence(NewKeySet.Get_KeySet(temp_int_ArrayList.get(1)));
				MASK[MASKi] = this.Imbalanced_String_XNOR_Operation(S1, S2);
			}
			MASKi++;
		}
	}
	/***********
	 * print out the generated parents-child tree 
	 */

	/**********
	 * test code
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		KeySetGeneration KSG = new KeySetGeneration();
		KSG.GenerateKeyset();
		String[][] temp_OldKeySet = KSG.GetFinalKeySet("Keyset1.txt");
		String[][] OldKeySet = new String[temp_OldKeySet.length][];
		for(int i=0;i<temp_OldKeySet.length;i++){
			OldKeySet[i] = new String[temp_OldKeySet[i].length];
			System.arraycopy(temp_OldKeySet[i], 0, OldKeySet[i], 0, temp_OldKeySet[i].length);
		}
		//System.out.println("Old Key Set");
//		for(int i=0;i<OldKeySet.length;i++){
//			for(int j=0;j<OldKeySet[i].length;j++){
//				System.out.print(OldKeySet[i][j] + " ");
//			}
//			System.out.println();
//		}
		KSG.UpdateKeySet();
		KeySetGeneration KSG1 = new KeySetGeneration();
		KSG1.GenerateKeyset();
		String[][] NewKeySet = KSG1.GetFinalKeySet("Keyset1.txt");
//		System.out.println("NEW KEY SET");
//		for(int i=0;i<NewKeySet.length;i++){
//			for(int j=0;j<NewKeySet[i].length;j++){
//				System.out.print(NewKeySet[i][j] + " ");
//			}
//			System.out.println();
//		}
//		System.out.println("Old Key Set");
//		for(int i=0;i<OldKeySet.length;i++){
//			for(int j=0;j<OldKeySet[i].length;j++){
//				System.out.print(OldKeySet[i][j] + " ");
//			}
//			System.out.println();
//		}
		int[] UKS_INT_ARRAY = {0,1,1,2};
		Candidates can = new Candidates(OldKeySet, NewKeySet, UKS_INT_ARRAY);
		can.Generate_Tree();
		can.Printout_Parents_Child();
		can.Generate_Mask();
		can.Generate_Processed_UKS();
		String[] MASK = can.Get_MASK();
		String[] Processed_UKS = can.Get_Processed_UKS();
//		System.out.println("MASK");
//		for(int i=0;i<MASK.length;i++){
//			System.out.println(MASK[i] );
//		}
//		System.out.println("\r\nUKS");
//		for(int i=0;i<Processed_UKS.length;i++){
//			System.out.println(Processed_UKS[i]);
//		}
//		System.out.println();
//		System.out.println("Reversed UKS");
//		String[] aaStrings = can.doReverseMASKtest();
//		for(int i=0;i<MASK.length;i++){
//			System.out.print(aaStrings[i] );
//		}
	}
	//tomorrow work on the connect of GUI and the compelated functionnal program and verify RKS function
	
}
