package GeneralClass;
import java.io.IOException;
import java.util.HashMap;


public class test {
	public static String[] RandSTable;
	public static HashMap RandTable = new HashMap();
	public static void main(String[] args) throws IOException{
		String aString = "ac";
		String bString = "bb";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < aString.length(); i++)
		    sb.append((char)(aString.charAt(i) & bString.charAt(i % bString.length())));
		String result = sb.toString();
		System.out.println(result);
	}
    public static void GeneRandTable(String[] randNum){
    	for(int i=0;i<36;i++){
    		RandTable.put(i, randNum[i]);
    	}
    }
    public static String transform(char key){
		String keystring = null;
		
		switch (key) {
       case 'A': case 'a':  keystring = (String) RandTable.get(0);
                break;
       case 'B': case 'b':  keystring = (String) RandTable.get(1);
                break;
       case 'C': case 'c':  keystring = (String) RandTable.get(2);
                break;
       case 'D': case 'd':  keystring = (String) RandTable.get(3);
                break;
       case 'E': case 'e':  keystring = (String) RandTable.get(4);
                break;
       case 'F': case 'f':  keystring = (String) RandTable.get(5);
                break;
       case 'G': case 'g':  keystring = (String) RandTable.get(6);
                break;
       case 'H': case 'h':  keystring = (String) RandTable.get(7);
                break;
       case 'I': case 'i':  keystring = (String) RandTable.get(8);
                break;
       case 'J': case 'j': keystring =  (String) RandTable.get(9);
                break;
       case 'K': case 'k': keystring =  (String) RandTable.get(10);
                break;
       case 'L': case 'l': keystring = (String) RandTable.get(11);
                break;
       case 'M': case 'm': keystring = (String) RandTable.get(12);
                break;
       case 'N': case 'n':  keystring = (String) RandTable.get(13);
                break;
       case 'O': case 'o':  keystring = (String) RandTable.get(14);
                break;
       case 'P': case 'p':  keystring = (String) RandTable.get(15);
                break;
       case 'Q': case 'q':  keystring = (String) RandTable.get(16);
                break;
       case 'R': case 'r':  keystring = (String) RandTable.get(17);
                break;
       case 'S': case 's':  keystring = (String) RandTable.get(18);
                break;
       case 'T': case 't':  keystring = (String) RandTable.get(19);
                break;
       case 'U': case 'u':  keystring = (String) RandTable.get(20);
                break;
       case 'V': case 'v':  keystring = (String) RandTable.get(21);
                break;
       case 'W': case 'w': keystring = (String) RandTable.get(22);
                break;
       case 'X': case 'x': keystring = (String) RandTable.get(23);
                break;
       case 'Y': case 'y': keystring = (String) RandTable.get(24);
                break;
       case 'Z': case 'z': keystring = (String) RandTable.get(25);
                break;
       case '1': keystring = (String) RandTable.get(26);
                break;
       case '2': keystring = (String) RandTable.get(27);
                break;
       case '3': keystring = (String) RandTable.get(28);
                break;
       case '4': keystring = (String) RandTable.get(29);
                break;
       case '5': keystring = (String) RandTable.get(30);
                break;
       case '6': keystring = (String) RandTable.get(31);
                break;
       case '7': keystring = (String) RandTable.get(32);
                break;
       case '8': keystring = (String) RandTable.get(33);
                break;
       case '9': keystring = (String) RandTable.get(34);
                break;
       case '0': keystring = (String) RandTable.get(35);
                break;
   }
		
		return keystring;
	}
}
