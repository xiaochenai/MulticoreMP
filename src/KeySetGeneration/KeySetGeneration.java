package KeySetGeneration;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.Iterator;

import VKEGeneration.KeySet;


import  GeneralClass.*;

public class KeySetGeneration {
	private String[][] chSet = new String[10][10];
	private String[][] chSethalf = new String[19][5];
	private String[] chSetSplit = new String[95];
	private String[][] keySet = new String[9][11];
	private ArrayList<Integer> IntList = new ArrayList<Integer>();
	private ArrayList<Integer> PointList = new ArrayList<Integer>();
	private ArrayList<Integer> positionList = new ArrayList<Integer>();
	private String[] CharacterSet = {" ","!","\"","#","$","%","&","'","(",")","*","+",",","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","<","=",">","?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","\\","]","^","_","`","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","{","|","}","~",};
	
	/*************
	 * not be used
	 */
	private void GenerateHalfKeyset(){
		for(int index=0; index<this.chSetSplit.length;index++){
			this.chSethalf[index/5][index%5]=this.chSetSplit[index];
		}
	}
	/********
	 * generate un-processed key set
	 * @throws NoSuchAlgorithmException
	 */
 	public void GenerateKeyset() throws NoSuchAlgorithmException{
 		File dirFile = new File("KeySet.txt");
 		if(dirFile.exists()){
 			System.out.println("Keyset.txt exist");
 		}
 		else{
 			int j=0;
 			RND rnd = new RND();
 			while(j<95){
 				byte[] RNDBytes = rnd.getOneRandomByte();
 				int residue = RNDBytes[0]%95;
 				
 				if(residue < 0)
 					residue = residue + 95;
 				if(IntList.contains(residue) == false){
 					IntList.add(residue);
 					System.arraycopy(CharacterSet, residue, chSetSplit, j, 1);
 					j++;
 				}
 			}
 		}
		
	}
 	/****************
 	 * print out the unprocessed character set
 	 */
	public void PrintchSetSplit(){
		for(int index=0;index<chSetSplit.length;index++){
			if(index%10==0)
				System.out.println();
			//System.out.print(chSetSplit[index] + " ");
		}
	}
	public void UpdateKeySet() throws NoSuchAlgorithmException, IOException{
		RND rnd = new RND();
		byte[] firstRndByte = rnd.getOneRandomByte();
		int FirstSet = firstRndByte[0] % 8;
		if(FirstSet < 0)
			FirstSet = FirstSet + 8;
		byte[] secondRndByte = rnd.getOneRandomByte();
		int SecondSet = secondRndByte[0] % 8;
		if(SecondSet < 0)
			SecondSet = SecondSet + 8;
		while(SecondSet == FirstSet){
			secondRndByte = rnd.getOneRandomByte();
			SecondSet = secondRndByte[0] % 8;
			if(SecondSet < 0)
				SecondSet = SecondSet + 8;
		}
		String[] temp_Set1 = new String[keySet[FirstSet].length], temp_Set2 = new String[keySet[SecondSet].length];
		System.arraycopy(keySet[FirstSet], 0, temp_Set1, 0, keySet[FirstSet].length);
		System.arraycopy(keySet[SecondSet], 0, temp_Set2, 0, keySet[SecondSet].length);
		String[] temp_Set1_1 = new String[temp_Set1.length/2];
		String[] temp_Set1_2 = new String[temp_Set1.length - temp_Set1.length/2];
		String[] temp_Set2_1 = new String[temp_Set2.length/2];
		String[] temp_Set2_2 = new String[temp_Set2.length - temp_Set2.length/2];
		rnd = new RND();
		ArrayList<Integer> RndPositionArray1 = new ArrayList<Integer>();
		ArrayList<Integer> RndPositionArray2 = new ArrayList<Integer>();
		int a;
		do{
			a = rnd.getOneRandomByte()[0] % keySet[FirstSet].length;
			if(a < 0)
				a = a + keySet[FirstSet].length;
			if(RndPositionArray1.contains(a) == false)
				RndPositionArray1.add(a);
		}while(!(RndPositionArray1.size() == temp_Set1.length));
		
		do{
			a = rnd.getOneRandomByte()[0] % keySet[SecondSet].length;
			if(a < 0)
				a = a + keySet[SecondSet].length;
			if(RndPositionArray2.contains(a) == false)
				RndPositionArray2.add(a);
		}while(!(RndPositionArray2.size() == temp_Set2.length));
		//System.out.println("Set1 Size : " + keySet[FirstSet].length + " Set2 Size : " + keySet[SecondSet].length);
		java.util.Iterator<Integer> iterator1 = RndPositionArray1.iterator();
		java.util.Iterator<Integer> iterator2 = RndPositionArray2.iterator();
		//System.out.println("Array1 : ");
		int index=0;
		while(iterator1.hasNext()){
			int next = iterator1.next();
			//System.out.print(next + " ");
			//System.out.print("*" + index + "*");
			if(index < temp_Set1_1.length)
				System.arraycopy(temp_Set1, next, temp_Set1_1, index, 1);
			else {
				System.arraycopy(temp_Set1, next, temp_Set1_2, index-temp_Set1_1.length, 1);
			}
			index++;
		}
		for(int i=0;i<temp_Set1_1.length;i++){
			//System.out.print(temp_Set1_1[i] + " ");
		}
		for(int i=0;i<temp_Set1_2.length;i++){
			//System.out.print(temp_Set1_2[i] + " ");
		}
		//System.out.println();
		for(int i=0;i<keySet[FirstSet].length;i++){
			//System.out.print(keySet[FirstSet][i] + " ");
		}
		//System.out.println("\r\nArray2 : ");
		index=0;
		while(iterator2.hasNext()){
			int next = iterator2.next();
			//System.out.print(iterator2.next() + " ");
			if(index < temp_Set2_1.length)
				System.arraycopy(temp_Set2, next, temp_Set2_1, index, 1);
			else {
				System.arraycopy(temp_Set2, next, temp_Set2_2, index-temp_Set2_1.length, 1);
			}
			index++;
		}
		for(int i=0;i<temp_Set2_1.length;i++){
			//System.out.print(temp_Set2_1[i] + " ");
		}
		for(int i=0;i<temp_Set2_2.length;i++){
			//System.out.print(temp_Set2_2[i] + " ");
		}
		//System.out.println();
		for(int i=0;i<keySet[SecondSet].length;i++){
			//System.out.print(keySet[SecondSet][i] + " ");
		}
		keySet[FirstSet] = new String[temp_Set1_1.length + temp_Set2_2.length];
		keySet[SecondSet] = new String[temp_Set2_1.length + temp_Set1_2.length];
		System.arraycopy(temp_Set1_1, 0, keySet[FirstSet], 0, temp_Set1_1.length);
		System.arraycopy(temp_Set2_2, 0, keySet[FirstSet], temp_Set1_1.length, temp_Set2_2.length);
		System.arraycopy(temp_Set1_2, 0, keySet[SecondSet], 0, temp_Set1_2.length);
		System.arraycopy(temp_Set2_1, 0, keySet[SecondSet], temp_Set1_2.length, temp_Set2_1.length);
		//System.out.println();
		for(int i=0;i<keySet[FirstSet].length;i++){
			//System.out.print(keySet[FirstSet][i] + " ");
		}
		//System.out.println();
		for(int i=0;i<keySet[SecondSet].length;i++){
			System.out.print(keySet[SecondSet][i] + " ");
		}
		//System.out.println("\r\nUPDATED KEYSET");
		
		for(int i=0;i<keySet.length;i++){
			for(int j=0;j<keySet[i].length;j++){
				//System.out.print(keySet[i][j] + " ");
			}
			//System.out.println();
		}
		//SaveKeySet2File();
	}
	public void UpdateKeySet(String[] UKS){
		
	}
	/*************
	 * process the original key set
	 * @throws NoSuchAlgorithmException
	 */
	private void chSet_Process() throws NoSuchAlgorithmException{
		RND rand = new RND();
		int temp_position ;
		while(positionList.size()<5){
			byte[] temp_byte = rand.getOneRandomByte();
			temp_position = temp_byte[0]%9;
			if(temp_position < 0)
				temp_position = temp_position + 9;
			if(positionList.contains(temp_position) == false)
				positionList.add(temp_position);
		}
//		System.out.println("The PositionList Is : ");
//		for(int i=0;i<5;i++){
//			System.out.print(positionList.get(i));
//		}
//		System.out.println();
//		System.out.println();
//		System.out.println("Size Is : " + positionList.size());
		for(int i=0;i<9;i++){
			//System.out.println("i is : " + i);
			System.arraycopy(chSetSplit, i*10, keySet[i], 0, 10);
		}
		for(int i=0;i<5;i++){
			keySet[positionList.get(i)][10] = chSetSplit[90+i];
		}
	}
	public String[][] GetFinalKeySet(String FileName) throws NoSuchAlgorithmException, IOException{
		if(KeySetFileExist(FileName)){
			this.LoadExistKeySet(FileName);
			System.out.println("LOAD " + FileName);
		}
		else{
			chSet_Process();
		}
		return keySet;
	}
	private String[] getchSetSplit(){
		return chSetSplit;
	}
	public void SaveKeySet2File(String FileName) throws IOException{
		FileOutputStream fos = new FileOutputStream(new File(FileName));
		for(int i=0;i<keySet.length;i++){
			for(int j=0;j<keySet[i].length;j++){
				if(keySet[i][j] != null)
					fos.write((keySet[i][j] + "  ").getBytes());
			}
			fos.write("\r\n".getBytes());
		}
		fos.flush();
		fos.close();
		System.gc();
	}
	//it works but not perfect
	private void LoadExistKeySet(String FileName) throws IOException{
		
		BufferedReader fin = new BufferedReader(new FileReader(FileName));	
		for(int i=0;i<keySet.length;i++){
				
				String temp = fin.readLine();
				String[] temparray = temp.split("  ");
				keySet[i] = new String[temparray.length];
				for(int j=0;j<temparray.length;j++){
					
					keySet[i][j] = temparray[j];
					//System.out.print(temparray[j] + " ");
				}
				//System.out.println();	
		}
	}
	public boolean KeySetFileExist(String FileName){
		File dir = new File(FileName);
		if(dir.exists())
			return true;
		else {
			return false;
		}
	}
	/***************
	 * test KeySetGeneration.GenerateKeyset, KeySetGeneration.SaveKeySet2File, KeySetGeneration.LoadExistKeySet
	 * and KeySetGeneration.UpdateKeySet
	 * @param args
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
//	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
//		KeySetGeneration KSG  =new KeySetGeneration();
//		KSG.GenerateKeyset();
//		//KSG.PrintchSetSplit();
//		String[] a = KSG.getchSetSplit();
//		String[][] FinalKeySet = KSG.GetFinalKeySet();
//		//KSG.SaveKeySet2File();
//		//System.out.println("The Final KeySet is : ");
//		for(int i=0;i<FinalKeySet.length;i++){
//			for(int j=0;j<FinalKeySet[i].length;j++){
//				//System.out.print(FinalKeySet[i][j] + " ");
//			}
//			//System.out.println();
//		}
//
//	}
}
