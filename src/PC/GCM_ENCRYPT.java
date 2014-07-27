package PC;

import java.io.*;

import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.JOptionPane;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


public class GCM_ENCRYPT 
{
	public long Keybinding = 0;
	//boolean to indicate if file is the index file or not
	boolean isIndex = false;
	
	//encryption function that has two inputs
	// input is the filename as a string
	// inputKey is the 32 byte array used to generate the key
	public void gcmEncrypt(String input, byte[] inputKey)
	{
		isIndex = false;
		
		//changes the output name to be the filename + .encrypted
		String _FileEnc = input.replace(".txt", ".encrypted");
		
		//creates the key for encryption
	    SecretKey key = null;
	    try
	    {     
	    	key = new SecretKeySpec(inputKey, "AES");	  
	    }
	    catch(Exception e)
	    {
	    	System.out.println("Error in creating key");
	    }
	    
	    //adds the filename to the index file
	    addToIndex(input);
	    
	    //converts the 32 byte input into hex
	    String keyString = null;
	    try 
	    {
			keyString = ToHEX(inputKey);
		} catch (UnsupportedEncodingException e1) { e1.printStackTrace();
		} catch (IOException e) { e.printStackTrace();
		}
	    
	    //adds the hex format of the 32 byte input in the index file
	    addToIndex(keyString);
	    
	    //creates a new Encrypter object with the generated key
	    Encrypter encrypter = new Encrypter(key, isIndex, 0);
	    try 
	    {
	    	//encrypts the input file
	    	encrypter.encrypt(getBytesFromFile(new File(input)),_FileEnc);
	    	//System.out.println(_FileEnc);
	    	
	    } 
	    catch (IOException e) 
	    {
	    	   e.printStackTrace();
	    }
	    
	    //deletes the plaintext version of the input file
	    deleteFile(input);
	    System.out.println("file encrypted, start to split file");
	    //gets the directory of the input file
	    String directory = input.substring(0, input.lastIndexOf("\\"));
	    
	    //splits the input file into a .bulk and .piece file
	    SplitFiles.splitFile(_FileEnc, directory);
	    System.out.println("File splited");
	    //deletes the .encrypted version of the input file
	    deleteFile(_FileEnc);
	}
	
	//encrypts each file inside of a directory
	public void encryptDirectory(File directory, String path) throws NoSuchAlgorithmException, IOException
	 {
		//gets the list of all files in the directory
		 File[] dirFiles = directory.listFiles();
		 RND genKey = new RND();
		 
		 //gets the path of the directory
		 path += directory.getName() + "\\";
		 for( int i = 0; i < dirFiles.length; i++)
  	  	 {
			 //checks if the File object is a file
			 if(dirFiles[i].isFile())
	         {
		          File openFile = new File(path + dirFiles[i].getName());
		          //checks if the file exists
		          if(openFile.exists())
		          {
		        	  //generates a new key to encrypt the file
		        	  	byte[] key = null;
						try {
							key = genKey.getRandom();
						} catch (NoSuchAlgorithmException e) { e.printStackTrace();
						}
						
						//encrypts the file
						gcmEncrypt(path + dirFiles[i].getName(), key);
		          }
		          else
		          {
		             int wrong = JOptionPane.showConfirmDialog(null,  "Could not find file. Please try again.");           
		             if (wrong == JOptionPane.YES_OPTION)
		             {
		                new FilePanel(1);
		             }
		             else
		             {
		                System.exit(0);
		             }
		          }
	         }
			 //otherwise the File object is a directory
	         else
	         {
	        	 encryptDirectory(dirFiles[i], path);
	         }
  	  	 }
	 }
	
	//used to encrypt the index file after all other files have been encrypted
	// keyin is a byte array generated by the Android device
	public void indexEncrypt(byte[] keyin,String secret)
	{
		RND keyGen = new RND();
		byte[] inputKey1 = null;
		byte[] inputKey2 = null;
		SecretKey key_1 = null;
		isIndex = true;
		long start = 0;
		try 
		{
			start = System.currentTimeMillis();
			//Create key 1 for encrypting the Index file
			inputKey1 = keyGen.getRandom();
			inputKey2 = keyin; //Key from android device
			System.out.println("inputkey1"+new String(inputKey1));
			System.out.println("inputkey2"+new String(inputKey2));
			//creates a new 32 byte array
			byte[] inputKey = new byte[inputKey1.length];
			
			//XORs the two generated keys to create a master key
			try{
				for(int i = 0; i < inputKey1.length; i++)
				{
					inputKey[i] = (byte) (inputKey1[i]^inputKey2[i]);
				}
			}
			catch(Exception e){
				
			}
			
			//creates the master key
			key_1 = new SecretKeySpec(inputKey1, "AES");//aaa
			
			//encrypts the byte array for the first key needed for the master key
			inputKey1 = TPMEncData.encryptData(inputKey1,secret);
			
			//saves the byte array for the first key needed for the master key
			File key1 = new File("Key1.txt");
			BufferedWriter out = new BufferedWriter(new FileWriter(key1, false));
			out.write(ToHEX(inputKey1));
			out.close();
			
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		Keybinding = System.currentTimeMillis() - start;
		//encrypts the index file using the master key
		Encrypter encrypter = new Encrypter(key_1, isIndex, 1);
		try 
	       {
			if(new File("Index.index").exists())
			{
				//encrypt the index file
				encrypter.encrypt(getBytesFromFile(new File("Index.index")),"Index.encrypted");
			}
	       }catch (IOException e){ e.printStackTrace();
	       }
		//deletes the plaintext index file
	    deleteFile("Index.index");
	}
	
	//deletes a file
	 private static void deleteFile(String file)
	 {
		  File f1 = new File(file);
		  f1.delete();
	 }
	 
	 //converts a byte array to hex format
	    public static String ToHEX(byte[] text) throws IOException{
			
		 	   String hexString = "";
		 		for(int i = 0; i < text.length; i++)
		 		{
		 			String hex = Integer.toHexString(text[i]&0xFF );
		 			if (hex.length() == 1) {
		 			    hex = "0" + hex;
		 			}
		 			hexString = hexString + hex;
		 		}   
		 		return hexString;
		    }

	    //adds the input string to the index file as a new line
	 public static void addToIndex(String input)
	 {
		 //checks if the index file exists
		 File index = new File("Index.index");
		 if(!index.exists())
		 {
		   	try 
		   	{
				index.createNewFile();
			} 
		   	catch (IOException e) 
		   	{
				e.printStackTrace();
			}
		 }
		 
		 //writes the input into the index file
		 try 
		 {
				BufferedWriter out = new BufferedWriter(new FileWriter(index, true));
				out.write(input);
				out.newLine();
				out.close();
		 } 
		 catch (IOException e) 
		 {
			 e.printStackTrace();
		 }
	  }

	 //reads the bytes from a file
  public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            extracted(file);
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

  //checks that the entire file was read
private static void extracted(File file) throws IOException {
	throw new IOException("Could not completely read file "+file.getName());
}
    
}
    
//encrypter class used to encyrpt files
class Encrypter {
    Cipher ecipher;
   
    Encrypter(SecretKey key, boolean isIndex, int keyNum) 
    {
      
    	  byte[] iv = new byte[]{ 
             // This is the authentication tag length (12).  This indicates the number of bytes in hash output.
            (byte) 0x0c,

            // This is the length of the authenticated data (10). The authenticated data will not be encrypted 
			// but will be authenticated.
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0a,
          };			
            
	      try {  
	    	  //creates a new IV and converts it to hex format
				byte[] biv = createIV();
				byte[] newIV = new byte[iv.length + biv.length];
				System.arraycopy(iv, 0, newIV, 0, iv.length);
				System.arraycopy(biv, 0, newIV, iv.length, biv.length);
				String IVString = ToHEX(newIV);
				
				if(isIndex)
				{
					if(keyNum == 1)
					{
						//saves the generated IV to a text file
						File iv1 = new File("IV1.txt");
						BufferedWriter out = new BufferedWriter(new FileWriter(iv1, false));
						out.write(IVString);
						out.close();
					}
				}
				
				//adds the generated index to the index file
				else
				{
					addToIndex(IVString);
				}

				//sets the encryption mode to AES-GCM
		        ecipher = Cipher.getInstance("AES/GCM/NoPadding","JsafeJCE");
		        IvParameterSpec params = new IvParameterSpec(newIV);
		        
		        //initializes the cipher object
		        ecipher.init(Cipher.ENCRYPT_MODE, key, params);
	        } catch (javax.crypto.NoSuchPaddingException e) { System.out.println(e.getMessage());
	        } catch (java.security.NoSuchAlgorithmException e) { System.out.println(e.getMessage());
	        } catch (java.security.InvalidKeyException e) { System.out.println(e.getMessage());
	        } catch (IllegalArgumentException e) { System.out.println(e.getMessage());
	        } catch (Exception e){
	            System.out.println(e.getMessage());
	        }
	    }
		
    //creates the IV used for encryption/decryption
		private static byte[] createIV() throws IOException
		{
			SecureRandom random = null;
			try
			{
				random = SecureRandom.getInstance("SHA1PRNG");
			}
			catch (NoSuchAlgorithmException e)
			{
				System.out.println("NoSuchAlgorithmException: " + e);
				System.exit(-1);
			}
			
			//randomly generates a 12 byte IV
			byte[] bIV = new byte[12];
			random.nextBytes(bIV);
			
			return bIV;
		}
		
		//encrypts the input byte array and outputs a string
	    public void encrypt(byte[] in, String out) {
	        try {
	                        
	            byte[] encryptedMessage =new byte[ecipher.getOutputSize(in.length)];
	            
	            byte[] authenticatedData = new byte[10];
	            int outputLenUpdate = ecipher.update(authenticatedData,0, authenticatedData.length, encryptedMessage, 0);
	            
	            outputLenUpdate += ecipher.update(in, 0, in.length, encryptedMessage, outputLenUpdate);           
	            ecipher.doFinal(encryptedMessage,outputLenUpdate);
	            System.out.println("Encryption Successful. Data Written to: " + out);
	            
	            FileOutputStream output = new FileOutputStream(out);  
	            output.write(encryptedMessage);
	            output.close();
	        
	        } catch (Exception e){System.out.println("Encryption Failed: " + e.getMessage());
	        }
	    }
	    
	    //converts a byte array to hex format
	    public static String ToHEX(byte[] text) throws IOException{
			
	 	   String hexString = "";
	 		for(int i = 0; i < text.length; i++)
	 		{
	 			String hex = Integer.toHexString(text[i]&0xFF );
	 			if (hex.length() == 1) {
	 			    hex = "0" + hex;
	 			}
	 			hexString = hexString + hex;
	 		}   
	 		return hexString;
	    }
	    
	    //adds a line to the index file
		 public static void addToIndex(String input)
		 {
			 File index = new File("Index.index");
			 if(!index.exists())
			 {
			   	try 
			   	{
					index.createNewFile();
				} 
			   	catch (IOException e) 
			   	{
					e.printStackTrace();
				}
			 }
			 try 
			 {
				 //adds the input to the index file
					BufferedWriter out = new BufferedWriter(new FileWriter(index, true));
					out.write(input);
					out.newLine();
					out.close();
			 } 
			 catch (IOException e) 
			 {
				 e.printStackTrace();
			 }
		  }
}
