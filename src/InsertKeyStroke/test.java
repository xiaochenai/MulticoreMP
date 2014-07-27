package InsertKeyStroke;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import GenRandomTable.GenRandTable;

public class test {
	public static String[] RandSTable;
	public static HashMap RandTableMap = new HashMap();
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
		
		GenRandTable s = new GenRandTable();
		String[] a = s.GetMutLenRandString();
		GenRandTableMap(a);
		FileOutputStream fos1 = new FileOutputStream("Character with Stroke.txt"),fos2 = new FileOutputStream("Random Seq with Stroke.txt");
		for(int index=65;index<91;index++){
			System.out.println((char)(index));
			String StringAfTrans = transform((char)(index));
			InsertKeyStroke IKS = new InsertKeyStroke(a);
			String KS = IKS.getMappedCharacter(StringAfTrans);
			if(KS != null){
				fos1.write(((char)index + "&"+(char)(index+32) +"¡ª"+ KS + "\r\n").getBytes());
				fos2.write((StringAfTrans.replace("\r\n", "") + transform(KS.charAt(0))).getBytes());
				System.out.println("Rand Seq With RS " + StringAfTrans.replace("\r\n", "") + transform(KS.charAt(0)));
			}
			else{
				System.out.println("Rand Seq Without RS  " + StringAfTrans);
				fos1.write(((char)index +"&"+(char)(index+32)+    "\r\n").getBytes());
				fos2.write((StringAfTrans).getBytes());
			}
			
		}
		
		for(int index = 48;index<58;index++){
			System.out.println((char)(index));
			String StringAfTrans = transform((char)(index));
			InsertKeyStroke IKS = new InsertKeyStroke(a);
			String KS = IKS.getMappedCharacter(StringAfTrans);
			if(KS != null){
				fos1.write(((char)index + "&" + KS + "\r\n").getBytes());
				fos2.write((StringAfTrans.replace("\r\n", "") + transform(KS.charAt(0))).getBytes());
				System.out.println("Rand Seq With RS " + StringAfTrans.replace("\r\n", "") + transform(KS.charAt(0)));
			}
			else{
				System.out.println("Rand Seq Without RS  " + StringAfTrans);
				fos1.write(((char)index +   "\r\n").getBytes());
				fos2.write((StringAfTrans).getBytes());
			}
		}
		fos1.close();
		fos2.close();
	}
		
	public static void GenRandTableMap(String[] RandTable){
		for(int index=0;index<RandTable.length;index++){
			RandTableMap.put(index,RandTable[index]);
		}
	}
    public static String transform(char key){
		String keystring = null;
		
		switch (key) {
       case 'A': case 'a':  keystring = (String) RandTableMap.get(0);
                break;
       case 'B': case 'b':  keystring = (String) RandTableMap.get(1);
                break;
       case 'C': case 'c':  keystring = (String) RandTableMap.get(2);
                break;
       case 'D': case 'd':  keystring = (String) RandTableMap.get(3);
                break;
       case 'E': case 'e':  keystring = (String) RandTableMap.get(4);
                break;
       case 'F': case 'f':  keystring = (String) RandTableMap.get(5);
                break;
       case 'G': case 'g':  keystring = (String) RandTableMap.get(6);
                break;
       case 'H': case 'h':  keystring = (String) RandTableMap.get(7);
                break;
       case 'I': case 'i':  keystring = (String) RandTableMap.get(8);
                break;
       case 'J': case 'j': keystring =  (String) RandTableMap.get(9);
                break;
       case 'K': case 'k': keystring =  (String) RandTableMap.get(10);
                break;
       case 'L': case 'l': keystring = (String) RandTableMap.get(11);
                break;
       case 'M': case 'm': keystring = (String) RandTableMap.get(12);
                break;
       case 'N': case 'n':  keystring = (String) RandTableMap.get(13);
                break;
       case 'O': case 'o':  keystring = (String) RandTableMap.get(14);
                break;
       case 'P': case 'p':  keystring = (String) RandTableMap.get(15);
                break;
       case 'Q': case 'q':  keystring = (String) RandTableMap.get(16);
                break;
       case 'R': case 'r':  keystring = (String) RandTableMap.get(17);
                break;
       case 'S': case 's':  keystring = (String) RandTableMap.get(18);
                break;
       case 'T': case 't':  keystring = (String) RandTableMap.get(19);
                break;
       case 'U': case 'u':  keystring = (String) RandTableMap.get(20);
                break;
       case 'V': case 'v':  keystring = (String) RandTableMap.get(21);
                break;
       case 'W': case 'w': keystring = (String) RandTableMap.get(22);
                break;
       case 'X': case 'x': keystring = (String) RandTableMap.get(23);
                break;
       case 'Y': case 'y': keystring = (String) RandTableMap.get(24);
                break;
       case 'Z': case 'z': keystring = (String) RandTableMap.get(25);
                break;
       case '0': keystring = (String) RandTableMap.get(26);
                break;
       case '1': keystring = (String) RandTableMap.get(27);
                break;
       case '2': keystring = (String) RandTableMap.get(28);
                break;
       case '3': keystring = (String) RandTableMap.get(29);
                break;
       case '4': keystring = (String) RandTableMap.get(30);
                break;
       case '5': keystring = (String) RandTableMap.get(31);
                break;
       case '6': keystring = (String) RandTableMap.get(32);
                break;
       case '7': keystring = (String) RandTableMap.get(33);
                break;
       case '8': keystring = (String) RandTableMap.get(34);
                break;
       case '9': keystring = (String) RandTableMap.get(35);
                break;
   }
		
		return keystring;
	}
}
