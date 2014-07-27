package GeneralClass;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hash {
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
	public byte[] GetHash_KeySetMappingString(String[] Source){
		MessageDigest digest = null;
		byte[] input = new byte[0];
		for(int index=0;index<Source.length;index++){
			try{
				digest = MessageDigest.getInstance("SHA-256");
				digest.reset();
				digest.update(Source[index].getBytes());
				input = digest.digest(input);
			}catch (NoSuchAlgorithmException e){
				System.out.println("NoSuchAlgorithmException: " + e);
				System.exit(-1);
			}
		}
		
		return input;
	}
	public byte[] GetHash(String Source){
		MessageDigest digest = null;
		byte[] input = new byte[0];
		for(int index=0;index<Source.length();index++){
			try{
				digest = MessageDigest.getInstance("SHA-256");
				digest.reset();
				digest.update((Source.charAt(index)+"").getBytes());
				input = digest.digest(input);
			}catch (NoSuchAlgorithmException e){
				System.out.println("NoSuchAlgorithmException: " + e);
				System.exit(-1);
			}
		}
		
		return input;
	}
	public byte[] GetHash(String[] Source,int length){
		MessageDigest digest = null;
		byte[] input = new byte[length];
		for(int index=0;index<Source.length;index++){
			try{
				digest = MessageDigest.getInstance("SHA-256");
				digest.reset();
				digest.update(Source[index].getBytes());
				input = digest.digest(input);
			}catch (NoSuchAlgorithmException e){
				System.out.println("NoSuchAlgorithmException: " + e);
				System.exit(-1);
			}
		}
		
		return input;
	}
	public byte[] GetHash(byte[] Source,int round,byte[] salt){
		MessageDigest digest = null;

		byte[] input = new byte[0];
		try
		{
			digest = MessageDigest.getInstance("SHA-256");
			digest.reset();
			digest.update(salt);
			input = digest.digest(Source);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("NoSuchAlgorithmException: " + e);
			System.exit(-1);
		}

		for (int i = 0; i < round; i++)
		{
			digest.reset();
			input = digest.digest(input);
		}
		return input;
	}
}
