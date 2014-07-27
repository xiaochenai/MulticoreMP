package VKEGeneration;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import  GeneralClass.*;

public class NewKeysetGeneration {
	private String[][] chSet = new String[10][10];
	private String[][] chSethalf = new String[19][5];
	private String[] chSetSplit = new String[95];
	private String[][] keySet = new String[9][11];
	private ArrayList<Integer> IntList = new ArrayList<Integer>();
	private ArrayList<Integer> PointList = new ArrayList<Integer>();
	private ArrayList<Integer> positionList = new ArrayList<Integer>();
	private String[] CharacterSet = {" ","!","\"","#","$","%","&","'","(",")","*","+",",","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","<","=",">","?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","\\","]","^","_","`","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","{","|","}","~",};
	
	
	private void GenerateHalfKeyset(){
		for(int index=0; index<this.chSetSplit.length;index++){
			this.chSethalf[index/5][index%5]=this.chSetSplit[index];
		}
	}
	public void PrintHalfKeyset(){
		
	}
 	private void GenerateKeyset() throws NoSuchAlgorithmException{
		int j=0;
		while(j<95){
			
			RND rnd = new RND();
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
	public void PrintchSetSplit(){
		for(int index=0;index<chSetSplit.length;index++){
			if(index%10==0)
				System.out.println();
			System.out.print(chSetSplit[index] + " ");
			
		}
	}
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
		System.out.println("The PositionList Is : ");
		for(int i=0;i<5;i++){
			System.out.print(positionList.get(i));
		}
		System.out.println();
		System.out.println();
		System.out.println("Size Is : " + positionList.size());
		for(int i=0;i<9;i++){
			//System.out.println("i is : " + i);
			System.arraycopy(chSetSplit, i*10, keySet[i], 0, 10);
		}
		for(int i=0;i<5;i++){
			keySet[positionList.get(i)][10] = chSetSplit[90+i];
		}
	}
	public String[][] GetFinalKeySet() throws NoSuchAlgorithmException{
		chSet_Process();
		return keySet;
	}
	public String[] getchSetSplit(){
		return chSetSplit;
	}
	public static void main(String[] args) throws NoSuchAlgorithmException{
		NewKeysetGeneration KSG  =new NewKeysetGeneration();
		KSG.GenerateKeyset();
		KSG.PrintchSetSplit();
		String[] a = KSG.getchSetSplit();
		String[][] FinalKeySet = KSG.GetFinalKeySet();
		System.out.println("The Final KeySet is : ");
		for(int i=0;i<FinalKeySet.length;i++){
			for(int j=0;j<FinalKeySet[i].length;j++){
				System.out.print(FinalKeySet[i][j] + " ");
			}
			System.out.println();
		}
	}
}
