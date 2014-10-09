package PC;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.math.BigInteger;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
//import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
//used to split and merge files
public class MpSplitFiles 
{
	//DropBox Info & others Varibles
	final static private String APP_KEY = "aed7wbm37ofn56r";
	final static private String APP_SECRET = "q5dml34orcowq6r";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	private static DropboxAPI<WebAuthSession> mDBApi;
	public static long indexSplit = 0;
	public static long indexMerge=0;
	//splits the index into two files: index.bulk and index.piece
	public static void splitIndex() throws IOException
	{
		init_DropBox();
		System.out.println("start to split file");
		
		long indexSplitStart = System.currentTimeMillis();
		File source = new File("Index.encrypted");
		try
		{
			String sourceFName = source.getName();
			sourceFName = sourceFName.substring(0, sourceFName.lastIndexOf("."));
			if(source.exists())
			{
				//gets the contents of the index file
				byte[] contents = getBytesFromFile(new File("Index.encrypted"));
				byte[] pieces = new byte[contents.length/10];
				int[] randNumsA = new int[10];
				long tempSeed = Hash.hash(System.currentTimeMillis());
				int step = contents.length/20;
				for (int i = 0; i < 10; i++)
				{
					randNumsA[i] = i*step + Integer.valueOf(Math.abs(tempSeed%5)+"");
					tempSeed = Hash.hash(tempSeed);
				}
				//generates an array of random values within the length of the file
				//removes the bytes at the random value locations from the contents
				int length = contents.length/100;
				for (int i = 0; i < 10; i++)
				{
					System.arraycopy(contents, randNumsA[i], pieces, i*length, length);
					//removes the byte at the random location
					
					//replaces the byte with a random byte
					byte[] temp = new byte[length];
					System.arraycopy(temp, 0, contents, randNumsA[i], length);
				}
				
				//converts the array of random numbers into a string
				/*
				for (int i = 0; i < contents.length/5; i++)
				{
					randNums += randNumsA[i];
					if(i < (contents.length/5) - 1)
					{
						randNums += ",";
					}
				}
				*/

				//saves the random numbers to a file
				File randNum = new File("RandNums.txt");
				BufferedWriter randNumNew = new BufferedWriter(new FileWriter(randNum, false));
					
				for (int i = 0; i < 9; i++)
				{
					randNumNew.write(randNumsA[i]+",");
				}
				randNumNew.write(randNumsA[9]+"");
				randNumNew.newLine();
				randNumNew.close();
				//saves the removed bytes into a .piece file
				String pieceFile = sourceFName  + ".piece";
			    //File piece = new File(pieceFile);
			    writeDropBox(pieceFile,pieces);

					
				//saves the remaining contents to a .bulk file
				String bulkFile = sourceFName  + ".bulk";
			    File bulk = new File(bulkFile);
				FileOutputStream fos3 = null;
				fos3 = new FileOutputStream(bulk);
				fos3.write(contents);
				fos3.close();
					
				//deletes the index file
				deleteFile("Index.encrypted");
			}
			
		}
	    catch (Exception e) 
	    {
	
	    	System.out.println("Error in Splitting" + e);
	        JOptionPane.showMessageDialog(null, "Error in Splitting Index \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	    }
		indexSplit = System.currentTimeMillis() - indexSplitStart;
	}

	//gets the random numbers for the index file
	public static int[] getRandNums()
	{
		String randNumsS = null;
		
		try
		{
			//reads the random numbers from the file
  		  FileInputStream fstream = new FileInputStream("RandNums.txt");
  		  // Get the object of DataInputStream
  		  DataInputStream in = new DataInputStream(fstream);
  		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
  		  randNumsS = br.readLine();
		  
  		  //Close the input stream
  		  in.close();
		}catch (Exception e){ System.err.println("Error: " + e.getMessage());
	  	}
		
		//creates an array of the random numbers
		String[] randNumsSA = randNumsS.split(",");
		int[] randNumsIA = new int[randNumsSA.length];
		for(int i = 0; i < randNumsSA.length; i++)
		{
			randNumsIA[i] = Integer.parseInt(randNumsSA[i]);
		}
		return randNumsIA;
	}
	
	//merges the .piece and .bulk index pieces together
	public static void mergeIndex() throws IOException
	{
		init_DropBox();
		long mergeStart = System.currentTimeMillis();
		byte[] bulkContents = null;
		byte[] pieceContents = null;
		if(new File("Index.bulk").exists())
		{
			try {
				//gets the contents of the .bulk file
				bulkContents = getBytesFromFile(new File("Index.bulk"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
			//gets the contents of the .piece file
			pieceContents = getFile("Index.piece");
		
		if(bulkContents != null && pieceContents != null)
		{
			int[] randNums = new int[10];
			
			//gets the random numbers to replace the bytes that were removed
			randNums = getRandNums();
			int length = bulkContents.length/100;
			//puts the pieces back into the bulk content
			for(int j = 0; j < 10; j++)
			{
				System.arraycopy(pieceContents, j*length, bulkContents, randNums[j], length);
			}
			
			//saves the merged file
			File original = new File("Index.encrypted");
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(original);
				fos.write(bulkContents);
				fos.close();
			} catch (FileNotFoundException e) {	e.printStackTrace();
			} catch (IOException e) { e.printStackTrace();
			}
			
			//deletes the pieces and the random number file
			deleteFile("Index.bulk");
			deleteFile("Index.piece");
			deleteFile("RandNums.txt");
		}
		indexMerge = System.currentTimeMillis() - mergeStart;
	}
	
	//splits a file into a .bulk and .piece file
	public static void splitFile(String fileName, String directory)
	{
		System.out.println("start to split files");
		Random r = null;
		{
			try 
			{
			    r = SecureRandom.getInstance("SHA1PRNG");
			} 
			catch(NoSuchAlgorithmException nsae) 
			{
			}
		}
		
		if(!fileName.contains(".index") && !fileName.contains(".bulk") && !fileName.contains(".piece"))
		{
			System.out.println("start to get RND ");
			File source = new File(fileName);
			try
			{
				String sourceFName = source.getName();
				sourceFName = sourceFName.substring(0, sourceFName.lastIndexOf("."));
				
				//gets the contents from the file
				byte[] contents = getBytesFromFile(new File(fileName));
				byte[] pieces = new byte[contents.length/500];
				int[] randNumsA = new int[10];
				long tempSeed = Hash.hash(System.currentTimeMillis());
				int step = contents.length/20;
				for (int i = 0; i < 10; i++)
				{
					randNumsA[i] = i*step + Integer.valueOf(Math.abs(tempSeed%5)+"");

					tempSeed = Hash.hash(tempSeed);
				}
				//generates an array of random values within the length of the file
				//removes the bytes at the random value locations from the contents
				int length = contents.length/5000;
				for (int i = 0; i < 10; i++)
				{
					System.arraycopy(contents, randNumsA[i], pieces, i*length, length);
					//removes the byte at the random location
					
					//replaces the byte with a random byte
					byte[] temp = new byte[length];
					System.arraycopy(temp, 0, contents, randNumsA[i], length);
				}
				
				//adds the random locations used for the pieces in the index file
				File index = new File("Index.index");
				BufferedWriter indexNew = new BufferedWriter(new FileWriter(index, true));
				for (int i = 0; i < 9; i++)
				{
					indexNew.write(randNumsA[i]+",");
				}
				indexNew.write(randNumsA[9]+"");
				indexNew.newLine();
				indexNew.close();
				//saves the removed pieces
				String pieceFile = (directory + "\\").replace("\\\\", "\\") + sourceFName  + ".piece";
		        File piece = new File(pieceFile);
				FileOutputStream fos2 = null;
				fos2 = new FileOutputStream(piece);
				fos2.write(pieces);
				fos2.close();
				
				//saves the contents with the pieces removed
				String bulkFile = (directory + "\\").replace("\\\\", "\\") + sourceFName  + ".bulk";
		        File bulk = new File(bulkFile);
				FileOutputStream fos3 = null;
				fos3 = new FileOutputStream(bulk);
				fos3.write(contents);
				fos3.close();
				
				//deletes the ciphertext file
				deleteFile(fileName);
			}
	        catch (Exception e) 
	        {
	
	            System.out.println("Error in Splitting" + e);
	            JOptionPane.showMessageDialog(null, "Error in Splitting File \n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
		}
		else
		{
			JOptionPane.showMessageDialog(null,  "Cannot split .index, .bulk, or .piece files.");
		}
	}
	
	//gets the bytes from a file
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

	  
	  //makes sure the file is completely read
	private static void extracted(File file) throws IOException {
		throw new IOException("Could not completely read file "+file.getName());
	}
	    
	
	//merges the .piece and .bulk files back together
	public static void mergeFile(String fileName, String[] randNums)
	{

		byte[] bulkContents = null;
		byte[] pieceContents = null;
		try {
			//gets the contents of the .bulk file and .piece file
			bulkContents = getBytesFromFile(new File(fileName.replace(".txt", ".bulk")));
			pieceContents = getBytesFromFile(new File(fileName.replace(".txt", ".piece")));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int length = bulkContents.length/5000;
		//puts the pieces back in the bulk content
		for(int j = 0; j < 10; j++)
		{
			//System.out.println("randNums length : " + randNums.length + " bulk length : " + bulkContents.length + " randNums : " + Integer.parseInt(randNums[j]) + " j: " + j + " piece length : " + pieceContents.length);
			System.arraycopy(pieceContents, j*length, bulkContents, Integer.valueOf(randNums[j]), length);
		}
		
		//saves the merged contents into the original ciphertext file
		File original = new File(fileName.replace(".txt", ".encrypted"));
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(original);
			fos.write(bulkContents);
			fos.close();
		} catch (FileNotFoundException e) {	e.printStackTrace();
		} catch (IOException e) { e.printStackTrace();
		}
		
		//deletes the pieces
		deleteFile(fileName.replace(".txt", ".bulk"));
		deleteFile(fileName.replace(".txt", ".piece"));
	}
	
	//deletes a file
	 private static void deleteFile(String file)
	 {
		  File f1 = new File(file);
		  f1.delete();
	 }

	    /**
	     * Adds the JsafeJCE provider if it has not been added already.
	     *
	     * @throws Exception On failure.
	     */
	    public static void addJsafeJCE() throws Exception {

	        // Remove provider if it's already registered, or insert will fail.
	        Security.removeProvider("JsafeJCE");

	        // Create a new provider object for the JsafeJCE provider.
	        Provider jsafeProvider = new com.rsa.jsafe.provider.JsafeJCE();

	        // Register provider in 1st position.
	        int position = Security.insertProviderAt(jsafeProvider, 1);
	        if (position != 1) {
	            throw new RuntimeException(
	                    "Failed to insert provider at first position");
	        }
	    }
		/**
		 * Gets file from DropBox
		 * @param name  -- filename to get
		 * @return byte array of file
		 */
		private static byte[] getFile(String name)
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try
			{
				mDBApi.getFile(name, null, outputStream, null);
			}catch (DropboxException e) {
				System.out.println("Something went wrong while downloading "+name+ ": " + e.toString());}

			return outputStream.toByteArray();
		}
		
		
		/**
		 * Initiates DropBox API
		 * 
		 * @throws IOException
		 */
		private static void init_DropBox() throws IOException
		{
			WebAuthSession session;
			AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
			File key = new File("keypair");
			File secret = new File("secret");
			
			if(key.exists() && secret.exists())
			{
				String Key = new String(getBytesFromFile(key));
				String Secret = new String(getBytesFromFile(secret));
				session = new WebAuthSession(appKeys, ACCESS_TYPE, new AccessTokenPair(Key, Secret));
			}
			else	
				session = new WebAuthSession(appKeys, ACCESS_TYPE);


			if(!session.isLinked())
			{

				try
				{
					WebAuthInfo authInfo = session.getAuthInfo();
					RequestTokenPair pair = authInfo.requestTokenPair;
					String url = authInfo.url;

					Desktop.getDesktop().browse(new URL(url).toURI());
					JOptionPane.showMessageDialog(null, "Press ok to continue once you have authenticated.");
					session.retrieveWebAccessToken(pair);

					AccessTokenPair tokens = session.getAccessTokenPair();
					FileOutputStream output = new FileOutputStream("keypair");  
					output.write(tokens.key.getBytes());
					output.close();

					output = new FileOutputStream("secret");  
					output.write(tokens.secret.getBytes());
					output.close();



				}catch(Exception e)
				{System.out.println("Fail Sauce");}
			}

			mDBApi = new DropboxAPI<WebAuthSession>(session);


		}
		
			/** Writes data to DropBox
		 * 
		 * @param name  -- File name
		 * @param input	-- Data
		 * @throws IOException
		 */
		public static void writeDropBox(String name, byte[] input) throws IOException
		{
			ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
			try
			{
				mDBApi.putFileOverwrite(name, inputStream, input.length, null);
			} catch (DropboxUnlinkedException e) {
				// User has unlinked, ask them to link again here.
				System.out.println("User has unlinked");
			} catch (DropboxException e) {
				System.out.println("Something went wrong while uploading.");
			}
			
		}
	    //starts the program
	 public static void main(String[] args) throws Exception {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) 
			{
				//sets the way the program looks
			    if ("Nimbus".equals(info.getName())) 
			    {
			        UIManager.setLookAndFeel(info.getClassName());
			        break;
			    }
			}
			
	    	// Add the JsafeJCE provider.
	        addJsafeJCE();

			new FilePanel(1);
	    }
}
