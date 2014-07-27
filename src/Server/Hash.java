package Server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hash {

	//hashes the input string using SHA-256
	   public static byte[] hash(byte[] input)
	   {
		    MessageDigest md = null;
			try 
			{
				//sets the hash function to SHA-1
				md = MessageDigest.getInstance("SHA-1");
			} 
			catch (NoSuchAlgorithmException e) 
			{
				e.printStackTrace();
			}
			//hashes the input string
			md.update(input);
			
			//gets the digest
		   byte[] digest = md.digest();
		   
		   return digest;
	   }
}
