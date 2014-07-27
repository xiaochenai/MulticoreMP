package MaskGeneration;

public class XNORtest {
	public String String_XNOR_Operation(String S1, String S2){
		System.out.println("XNOR Operation");
		StringBuilder sb = new StringBuilder();
		System.out.println();
		byte[] b1 = S1.getBytes();
		byte[] b2 = S2.getBytes();
		byte[] b3 = new byte[b1.length];
		for(int i = 0; i < b1.length; i++){
			String i1 = Integer.toBinaryString(b1[i]);
			String i2 = Integer.toBinaryString(b2[i]);
			String i3 = Binary_String_XNOR(i1, i2);
			System.out.println("i1 : " + i1);
			System.out.println("i2 : " + i2);
			System.out.println("i3 : " + i3);
			sb.append((char)Integer.parseInt(i3,2));
		}
		return sb.toString();
	}
	public String Imbalanced_String_XNOR_Operation(String S1,String S2){
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
	public String Imbalanced_String_AND_Operation(String S1,String S2){
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
	private String Binary_String_XNOR(String s1,String s2){
		StringBuilder sb = new StringBuilder();
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
	
	public String String_AND_Operation(String S1, String S2){
		StringBuilder sb = new StringBuilder();
		System.out.println();
		byte[] b1 = S1.getBytes();
		byte[] b2 = S2.getBytes();
		byte[] b3 = new byte[b1.length];
		System.out.println("AND Operation");
		for(int i = 0; i < b1.length; i++){
			String i1 = Integer.toBinaryString(b1[i]);
			System.out.println("i1 : " + i1);
			String i2 = Integer.toBinaryString(b2[i]);
			System.out.println("i2 : " + i2);
			String i3 = Binary_String_AND(i1, i2);
			System.out.println("i3 : " + i3);
			sb.append((char)Integer.parseInt(i3,2));
		}
		return sb.toString();
	}
	public String reverMASK(String MASK, String s1){
		return this.String_AND_Operation(MASK, s1);
	}
	public static void main(String[] args){
		XNORtest aRtest = new XNORtest();
		String a = "aabbcfff";
		String b = "ccbbaasdasdasdasd";
		String MASK = aRtest.Imbalanced_String_XNOR_Operation(a, b);
		String Processed_UKS = aRtest.Imbalanced_String_AND_Operation(a, b);
		String Reversed_UKS = aRtest.Imbalanced_String_AND_Operation(MASK, a);
		System.out.println("\r\n"+MASK + " " + MASK.length());
		System.out.println(Processed_UKS + " " + Processed_UKS.length());
		System.out.println(Reversed_UKS);
		System.out.println(Reversed_UKS.equals(Processed_UKS));
	}
}
