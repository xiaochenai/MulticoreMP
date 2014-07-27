package VKEGeneration;

import GeneralClass.*;
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


public class KeySet {
	private String[][] chSet = new String[10][10];
	private String[][] chSethalf = new String[19][5];
	private String[] chSetSplit;
	private String[][] ReadchSet(String FilePath) throws IOException{
		FileInputStream fis = new FileInputStream(FilePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String line;
		int index=0;
		while((line = br.readLine()) != null){
			this.chSetSplit = line.split(", ");
			System.arraycopy(this.chSetSplit, 0, this.chSet[index], 0, this.chSetSplit.length);
			index++;
		}
		fis.close();
		return chSethalf;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		ArrayList<Integer> IntList = new ArrayList<Integer>();
		ArrayList<Integer> PointList = new ArrayList<Integer>();
		String[] CharacterSet = {" ","!","\"","#","$","%","&","'","(",")","*","+",",","-",".","/","0","1","2","3","4","5","6","7","8","9",":",";","<","=",">","?","@","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","[","\\","]","^","_","`","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","{","|","}","~",};
		RND rnd = new RND();
		byte[] RNDBytes = new byte[1];
		byte[] RNDpoint = new byte[1];
		int i=0,j=0,index=0;
		String line;
		String[][] chSet = new String[10][10];
		String[][] chSethalf = new String[19][5];
		String[] chSetSplit;
		FileInputStream fis = new FileInputStream("keyset.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		while((line = br.readLine()) != null){
			chSetSplit = line.split(", ");
			System.arraycopy(chSetSplit, 0, chSet[index], 0, chSetSplit.length);
			index++;
		}
		fis.close();
		System.out.println("*****************" + "chSet" + "*******************");
		for(int index1=0;index1<9;index1++){
			for(int index2=0;index2<10;index2++)
				System.out.print(chSet[index1][index2] + " ");
			System.out.println();
		}
		for(int index1=0;index1<9;index1++){
			int counter=0;
			PointList.clear();
			while(counter<10){
				RNDpoint = rnd.getOneRandomByte();
				int point = RNDpoint[0]%10;
				if(point < 0)
					point = point + 10;
				if(PointList.contains(point)==false){
					if(counter<5)
						chSethalf[2*index1][counter] =  chSet[index1][point];
					else if(index1!=9)
						chSethalf[2*index1 + 1][counter-5] =  chSet[index1][point];
					PointList.add(point);
					counter++;
				}
			}
		}
		System.out.println("*****************" + "chSethalf" + "*******************");
		for(int index1=0;index1<18;index1++){
			for(int index2=0;index2<chSethalf[index1].length;index2++)
				System.out.print(chSethalf[index1][index2] + " ");
			System.out.println();
		}
				
		PointList.clear();
		FileOutputStream fos = new FileOutputStream("keyset.txt");
		System.out.println("*****************" + " new chSethalf" + "*******************");
		while(j<18){
			RNDpoint = rnd.getNRandomByte(2);
			int point1 = RNDpoint[0]%18;
			int point2 = RNDpoint[1]%18;
			

			do{
				RNDpoint = rnd.getNRandomByte(2);
				point1 = RNDpoint[0]%18;
				if(point1 < 0)
					point1 = point1 + 18;
			}while(PointList.contains(point1) == true);
			PointList.add(point1);
			do{
				RNDpoint = rnd.getNRandomByte(2);
				point2 = RNDpoint[0]%18;
				if(point2 < 0)
					point2 = point2 + 18;
			}while(PointList.contains(point2) == true);
			PointList.add(point2);
			
			if(true){
				j=j+2;
				for(int index1=0;index1<5;index1++){
					System.out.print(chSethalf[point1][index1]+" ");
					fos.write((chSethalf[point1][index1]+", ").getBytes());
				}
				for(int index1=0;index1<5;index1++){
					System.out.print(chSethalf[point2][index1]+" ");
					fos.write((chSethalf[point2][index1]+", ").getBytes());
				}
				System.out.println();
				fos.write("\r\n".getBytes());
			}
		}
		fos.close();
		PointList.clear();
		IntList.clear();
//		j=0;
//		while(j<95){
//			
//			rnd = new RND();
//			RNDBytes = rnd.getOneRandomByte();
//	
//			int residue = RNDBytes[0]%95;
//			
//			if(residue < 0)
//				residue = residue + 95;
//			if(IntList.contains(residue) == false){
//				IntList.add(residue);
//	
//				System.out.print(CharacterSet[residue]+", ");
//				fos.write((CharacterSet[residue]+", ").getBytes());
//				j++;
//				if(j%10 == 0){
//					fos.write("\r\n".getBytes());
//					System.out.println();
//				}
//			}
//		}
//		fos.close();

		
		
	}
}
