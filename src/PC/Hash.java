package PC;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hash {

	//hashes the input string using SHA-256
	   public static long hash(long timeSeed){
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
			md.update(longToBytes(timeSeed));
			
			//gets the digest
		   byte[] digest = md.digest();
		   
		   return bytesToLong(digest);
	   }
	   public static byte[] longToBytes(long x) {
		    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
		    buffer.putLong(x);
		    return buffer.array();
		}

		public static long bytesToLong(byte[] bytes) {
		    ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE);
		    buffer.put(bytes);
		    buffer.flip();//need flip 
		    return buffer.getLong();
		}
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
	   public byte[] GetHash(byte[] Source,int round){
			MessageDigest digest = null;

			byte[] input = new byte[0];
			try
			{
				digest = MessageDigest.getInstance("SHA-256");
				digest.reset();
				digest.update(Source);
				input = digest.digest(Source);
			}
			catch (NoSuchAlgorithmException e)
			{
				System.out.println("NoSuchAlgorithmException: " + e);
				System.exit(-1);
			}

			for (int i = 0; i <round; i++)
			{
				digest.reset();
				input = digest.digest(input);
			}
			return input;
		}
}
