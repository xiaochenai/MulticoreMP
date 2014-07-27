package GeneralClass;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.security.NoSuchAlgorithmException;


/*************
 * 
 * @author 123
 * have not finish the time seed based Random Number Generator
 * need to be considered carefully 
 */
public class RND {
	
	private long salt = 0;
	private long KeySetSeed;
	
	//generates a random key
	//do not use this function, has not finished yet
	public byte[] getRandom(long TSeed){
		System.out.println("TimeSeed Received is : " + TSeed);
		byte[] FullTS = longToBytes(TSeed);
		byte[] halfTS1 = new byte[4];
		byte[] halfTS2 = new byte[4];
		System.arraycopy(FullTS, 0, halfTS1, 0, 4);
		System.arraycopy(FullTS, 4, halfTS2, 0, 4);
		int halfTSeed1 = ((0xFF & halfTS1[0]) << 24) | ((0xFF & halfTS1[1]) << 16) |
	            ((0xFF & halfTS1[2]) << 8) | (0xFF & halfTS1[3]);
		int halfTSeed2 = ((0xFF & halfTS2[0]) << 24) | ((0xFF & halfTS2[1]) << 16) |
	            ((0xFF & halfTS2[2]) << 8) | (0xFF & halfTS2[3]);
		System.out.println("Half1 : " + halfTSeed1);
		System.out.println("Half2 : " + halfTSeed2);
		int Moduloresult = halfTSeed2 % halfTSeed1;
		System.out.println("Modulo Result is : " + Moduloresult);
		return ByteBuffer.allocate(4).putInt(Moduloresult).array();
	}
	public byte[] getRandom_XOR_TimeSeed(long TSeed, byte[] RndBytes){
		ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.putLong(TSeed);
		byte[] Tempbytes = buffer.array();
		byte[] temp_bytes_array = new byte[Tempbytes.length];
		for(int i=0;i<temp_bytes_array.length;i++){
			temp_bytes_array[i] = (byte) (Tempbytes[i] ^ RndBytes[i]);
		}
		byte[] rebytes = new byte[temp_bytes_array.length];
		System.arraycopy(temp_bytes_array, 0, rebytes, 0, temp_bytes_array.length);
		return rebytes;
	}
	public void setKeySetSeed(long Seed){
		this.KeySetSeed = Seed;
	}
	public byte[] getRandom_GenRandTable(long Seed){
		Random random=null;
		byte [] testrngrn = null;
		//sets the Random object to use SHA1PRNG
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//adds the salt to the PIN hash
		addSalt();
		
		//loops through several times to get a random key
		for (int i=0; i<4; i++)
		{
			//sets the random generator seed to the current time plus
			// the salt/password combination
			random.setSeed(Seed + salt);
			
			//stores the random 32 byte array
			testrngrn = new byte [32];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}
	public byte[] getRandom_RKS_Determination(long TSeed){
		Random random=null;
		byte [] testrngrn = null;
		//sets the Random object to use SHA1PRNG
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//adds the salt to the PIN hash
		addSalt();
		
		//loops through several times to get a random key
		for (int i=0; i<4; i++)
		{
			//sets the random generator seed to the current time plus
			// the salt/password combination
			random.setSeed(TSeed + salt);
			
			//stores the random 32 byte array
			testrngrn = new byte [32];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}
	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	public byte[] getRandom_RKS_Determination(long seed1, long seed2) throws NoSuchAlgorithmException{
		Random random=null;
		byte [] testrngrn = null;
		//sets the Random object to use SHA1PRNG
		random = SecureRandom.getInstance("SHA1PRNG");
		
		//adds the salt to the PIN hash
		addSalt();
		
		//loops through several times to get a random key
		for (int i=0; i<4; i++)
		{
			//sets the random generator seed to the current time plus
			// the salt/password combination
			random.setSeed(seed1 + seed2 + salt);
			
			//stores the random 32 byte array
			testrngrn = new byte [32];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}
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
//	public static void  main(String[] args){
//		RND a = new RND();
//		for(int round=0;round<20;round++){
//			long TS = System.nanoTime();
//			byte[] tt = a.getRandom(TS);
//			for(int index=0;index< tt.length;index++){
//				System.out.print(tt[index] + " ");
//			}
//		}
//		
//	}
	public byte[] getNRandomByte(int N) throws NoSuchAlgorithmException{
		// TODO Auto-generated method stub
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
			testrngrn = new byte [N];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}
	public byte[] getOneRandomByte() throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
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
			testrngrn = new byte [1];
			random.nextBytes(testrngrn);
		}
		
		//returns the random array produced
		return testrngrn;
	}

	
}

