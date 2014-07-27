package PC;

import java.security.SecureRandom;
import java.util.Random;
import java.security.NoSuchAlgorithmException;


public class RND {
	
	private long salt = 0;
	
	//generates a random key
	public byte[] getRandom() throws NoSuchAlgorithmException
	{
		Random random=null;
		byte [] testrngrn = null;
		//sets the Random object to use SHA1PRNG
		random = SecureRandom.getInstance("SHA1PRNG");
		//gets the time of day in nanoseconds
		long nanoGMT2 = System.nanoTime();
	
		//loops through several times to get a random salt
		for (int i=0; i<4; i++)
		{
			//sets the random generator seed to the current time
			random.setSeed(nanoGMT2);
			nanoGMT2 = System.nanoTime();
			
			//sets the salt value
			salt = random.nextLong();	
		}
		
		//adds the salt to the PIN hash
		addSalt();
		
		//loops through several times to get a random key
		for (int i=0; i<4; i++)
		{
			//sets the random generator seed to the current time plus
			// the salt/password combination
			random.setSeed(nanoGMT2+salt);
			nanoGMT2 = System.nanoTime();
			
			//stores the random 32 byte array
			testrngrn = new byte [32];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}
	
	//adds the randomly generated salt value to the value of the password hash
	//the hash is used so that the actual password is never hard-coded in the program
	public void addSalt()
	{
		//hash value of PIN
		String s = "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4";
		
		//converts PIN hash value into a character array
		char[] sA = s.toCharArray();
		long strConv = 0;
		for(int i = 0; i < sA.length; i++)
		{
			//adds each character in the string together
			strConv += sA[i];
		}
		
		//adds the randomly generated salt to the long representation of the password hash
		salt = salt + strConv;
	}

	
}

