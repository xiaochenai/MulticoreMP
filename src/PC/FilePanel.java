package PC;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane; 
import javax.swing.JPasswordField;

import biz.source_code.base64Coder.Base64Coder;

import com.rsa.shareCrypto.j.lo;

import GraphicKeyboard.Login;
import Sender_Receiver.Sender;
import Sender_Receiver.ServerListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//creates the GUI for the user to interact with the program
public class FilePanel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int FONTSIZE = 24;
	//sets up two threads for the encrypt and decrypt buttons
	private volatile Thread encryptThread = null, decryptThread = null;
	private boolean encThrDone = true, decThrDone = true;
	private JFrame frame;
	public final ArrayList<ArrayList<String>> receivedStrings = new ArrayList<ArrayList<String>>();
	//creates the frame for the GUI objects
	private JFrame frame_1 = new JFrame("TPM Enhanced Crypto System");
	
	//objects for the first GUI window
	private JLabel list = new JLabel("Please enter your password");
	private JLabel line = new JLabel("------------------------------");
	//private JPasswordField pin = new JPasswordField(4);
	private JPasswordField passwordField;
	//objects for the second GUI window
	private JLabel TPMStatus1 = new JLabel("TPM Status:");
	private JLabel TPMStatus2 = new JLabel("Initialed");
	private JLabel connectStatus1 = new JLabel("Connection Status:");
	private JLabel fileStatus1 = new JLabel("File Status:");
	private JLabel sendStatus1 = new JLabel("# of Files Sent:");
	private JLabel receiveStatus1 = new JLabel("# of Files Received:");
	private JLabel connectStatus2 = new JLabel("Not Connected");
	private JLabel fileStatus2 = new JLabel("Plaintext");
	private JLabel sendStatus2 = new JLabel("None");
	private JLabel receiveStatus2 = new JLabel("None");
	private boolean isEncrypted = false;

	//buttons for the second GUI window
	private JButton encrypt = new JButton("Protect");
	private JButton decrypt = new JButton("Unprotect");
	private JButton reset = new JButton("Reset");
	
	//GUI panels used to organize second window
	private JPanel gui = new JPanel();
	private JPanel gui1 = new JPanel();
	private JPanel gui2 = new JPanel();
	private GridLayout status = new GridLayout(6, 2);
	private GridLayout buttons = new GridLayout(1,3);
	private static ServerListener listener = new ServerListener();
	private static Sender sender = new Sender();
	public static String AndroidIP = "192.168.0.107";
	public static String ServerIP = "192.168.0.105";
	public String tRSecret="1";
	public String nRSecret="2";
	FileOutputStream fos = new FileOutputStream(new File("TimeStamp.txt"));
	Server2 server;
	//global variables used by the program
	byte[] key = null;
	int servPort = 2223;

	// Displays the desired window for the user
	// frameNum = 1 is the window asking for the password
	// frameNum = 2 is the window with the buttons and statuses
   public FilePanel(int frameNum) throws NoSuchAlgorithmException, IOException
   {  
	  frame = new JFrame("TPM Enhanced Crypto System");
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  
	  setFonts();
	  ImagePanel background = new ImagePanel();
	  Color trans = new Color(0,0,0,0);
	  gui.setOpaque(false);
	  gui.setLayout(new BoxLayout(gui,  BoxLayout.PAGE_AXIS));
	  gui.setPreferredSize(new Dimension(600, 200));
	  gui.setBackground(trans);
	  gui1.setOpaque(false);
	  gui1.setLayout(status);
	  gui1.setPreferredSize(new Dimension(600, 300));
	  gui1.setBackground(trans);
	  gui2.setOpaque(false);
	  gui2.setLayout(buttons);
	  gui2.setPreferredSize(new Dimension(600, 60));
	  gui2.setBackground(trans);
	  Login login=null;
	  ArrayList<String> key = new ArrayList<String>();
	  ArrayList<String> FL = new ArrayList<String>();
	  ArrayList<String> FN = new ArrayList<String>();
	  ArrayList<String> FC = new ArrayList<String>();
	  ArrayList<String> Finished = new ArrayList<String>();
	  ArrayList<String> Others = new ArrayList<String>();
	  receivedStrings.add(key);
	  receivedStrings.add(FL);
	  receivedStrings.add(FN);
	  receivedStrings.add(FC);
	  receivedStrings.add(Finished);
	  receivedStrings.add(Others);
	  
	  switch (frameNum)
	  {
	  	case 1:
	  		 String serverIP = JOptionPane.showInputDialog("Please input server IP address Here!");
	  		 AndroidIP = JOptionPane.showInputDialog("Please input Android IP address Here!");
	  		 server = new Server2(sender,listener,receivedStrings,AndroidIP);
	  		 login = new Login(frame, true,this,serverIP);
	  		 tRSecret = login.tRSecret;
	  		 nRSecret = login.nRSecret;
	  		 login.setVisible(true);
			 close();
			  //Password Listener: converts password digits into random number sequence and sends them to sever for verification
			  
	  		
			
			
	  	case 2:
	  		System.out.println("this round secret : " + Base64Coder.encodeLines(tRSecret.getBytes()));
	  		System.out.println("next round secret : " + Base64Coder.encodeLines(nRSecret.getBytes()));
	  		int nrOfProcessors = Runtime.getRuntime().availableProcessors();
	        ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);
	        eservice.execute(new RunReceiver(receivedStrings));
	  		  System.out.println("Panel 2");
			  background.removeAll();
			  gui1.removeAll();
			  gui2.removeAll();
			  background.setLayout(new BorderLayout());
		      background.add(gui1, BorderLayout.NORTH);
		      background.add(gui2, BorderLayout.SOUTH);
		      gui1.add(TPMStatus1);		      
			  TPMStatus1.setForeground(Color.white);
			  TPMStatus1.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(TPMStatus2);
			  TPMStatus2.setForeground(Color.green);
			  TPMStatus2.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(connectStatus1);
			  connectStatus1.setForeground(Color.white);
			  connectStatus1.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(connectStatus2);
			  connectStatus2.setForeground(Color.red);
			  connectStatus2.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(fileStatus1);
			  fileStatus1.setForeground(Color.white);
			  fileStatus1.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(fileStatus2);
			  fileStatus2.setForeground(Color.red);
			  fileStatus2.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(sendStatus1);
			  sendStatus1.setForeground(Color.white);
			  sendStatus1.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(sendStatus2);
			  sendStatus2.setForeground(Color.red);
			  sendStatus2.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(receiveStatus1);
			  receiveStatus1.setForeground(Color.white);
			  receiveStatus1.setAlignmentX(Component.CENTER_ALIGNMENT);
			  gui1.add(receiveStatus2);
			  receiveStatus2.setForeground(Color.red);
			  receiveStatus2.setAlignmentX(Component.CENTER_ALIGNMENT);
		      encrypt.setAlignmentX(Component.CENTER_ALIGNMENT);
		      gui2.add(encrypt);
		      encrypt.addActionListener(new encryptListener());
		      decrypt.setAlignmentX(Component.CENTER_ALIGNMENT);
		      gui2.add(decrypt);
		      decrypt.addActionListener(new decryptListener());
			  reset.setAlignmentX(Component.CENTER_ALIGNMENT);
		      gui2.add(reset);
		      reset.addActionListener(new resetListener());
			  frame.getContentPane().add(background);
			  frame.pack();
			  frame.setVisible(true);
			  frame.setResizable(false);
			  frame.setLocationRelativeTo(null);
			  System.out.println("panel 2 finish");
	  		  break;
	  		  
	  }
   }
   
   //sets the size and color of the fonts
   private void setFonts()
   {
	   Font f = new Font("monospaced", Font.BOLD, FONTSIZE);
		list.setFont(f);
		line.setFont(f);
		TPMStatus1.setFont(f);
		connectStatus1.setFont(f);
		fileStatus1.setFont(f);
		sendStatus1.setFont(f);
		receiveStatus1.setFont(f);
		TPMStatus2.setFont(f);
		connectStatus2.setFont(f);
		fileStatus2.setFont(f);
		sendStatus2.setFont(f);
		receiveStatus2.setFont(f);
		//pin.setFont(f);
		encrypt.setFont(f);
		decrypt.setFont(f);
		reset.setFont(f);
   }
   
   //closes the current GUI window
	public void close()
	{
		frame.setVisible(false);
		frame.dispose();
	}
	
	//resets the program to its initial state
	private class resetListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			//stops the encrypt thread if running
			if(encryptThread != null)
			{
				encryptThread.interrupt();
			}
			//stops the decrypt thread if running
			if(decryptThread != null)
			{
				decryptThread.interrupt();
			}
			
			//deletes all files created by the program
			File indexBulk = new File("Index.bulk");
			File indexPiece = new File("Index.piece");
			File indexEncrypt = new File("Index.encrypted");
			File randNums = new File("RandNums.txt");
		    File key1 = new File("Key1.txt");
		    File iv1 = new File("IV1.txt");
		    File iv2 = new File("IV2.txt");
		    File Index = new File("Index.index");
		    Index.delete();
		    randNums.delete();
		    key1.delete();
		    iv1.delete();
		    iv2.delete();
		    indexBulk.delete();
		    indexPiece.delete();
		    indexEncrypt.delete();

		    //replaces all .txt files for project demonstration
			replaceFiles();
			
			//deletes the index file
			File index = new File("Index.index");
			index.delete();
			
			//closes the socket
			if(server.socketServer != null)
			{
				server.socketServer.close();
			}
			
			//closes the GUI window
			close();
			try 
			{
				//restarts the program
				SplitFiles.main(null);
			} catch (Exception e1) {e1.printStackTrace();
			}
			
		}
		
		//replaces the .txt files with example files for demonstration
		private void replaceFiles()
		{
			//file location of .txt files
//			File folder1 = new File("C:\\Users\\Will\\Desktop\\Encrypted Files\\folder 1");
			File folder1 = new File("C:\\Users\\123\\Desktop\\Encrypted Files\\folder 1");
			
			//gets all files in folder1
			File[] files1 = folder1.listFiles();
			//replaces all .piece, .bulk. and .encrypted files with .txt files
			for(int i = 0; i < files1.length; i++)
			{
				if(files1[i].getPath().endsWith(".piece"))
				{
					files1[i].delete();
				}
				else if(files1[i].getPath().endsWith(".bulk"))
				{
					File newFile = new File(files1[i].getPath().replace(".bulk", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This is test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files1[i].delete();
				}
				else if(files1[i].getPath().endsWith(".encrypted"))
				{
					File newFile = new File(files1[i].getPath().replace(".encrypted", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This is test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files1[i].delete();
				}
			}
			
			//same as folder1
//			File folder2 = new File("C:\\Users\\Will\\Desktop\\Encrypted Files\\folder 2");
			File folder2 = new File("C:\\Users\\Panda\\Desktop\\Encrypted Files\\folder 2");
			File[] files2 = folder2.listFiles();
			for(int i = 0; i < files2.length; i++)
			{
				if(files2[i].getPath().endsWith(".piece"))
				{
					files2[i].delete();
				}
				else if(files2[i].getPath().endsWith(".bulk"))
				{
					File newFile = new File(files2[i].getPath().replace(".bulk", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This is test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files2[i].delete();
				}
				else if(files2[i].getPath().endsWith(".encrypted"))
				{
					File newFile = new File(files2[i].getPath().replace(".encrypted", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This a is test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files2[i].delete();
				}
			}
			
			//same as folder1
//			File folder3 = new File("C:\\Users\\Will\\Desktop\\Encrypted Files\\folder 3");
			File folder3 = new File("C:\\Users\\Panda\\Desktop\\Encrypted Files\\folder 3");
			File[] files3 = folder3.listFiles();
			for(int i = 0; i < files3.length; i++)
			{
				if(files3[i].getPath().endsWith(".piece"))
				{
					files3[i].delete();
				}
				else if(files3[i].getPath().endsWith(".bulk"))
				{
					File newFile = new File(files3[i].getPath().replace(".bulk", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This a is test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files3[i].delete();
				}
				else if(files3[i].getPath().endsWith(".encrypted"))
				{
					File newFile = new File(files3[i].getPath().replace(".encrypted", ".txt"));
					BufferedWriter fileWriter;
					try {
						fileWriter = new BufferedWriter(new FileWriter(newFile, true));
						fileWriter.write("This is a test run");
						fileWriter.close();
					} catch (IOException e) { e.printStackTrace();
					}
					files3[i].delete();
				}
			}
		}
	}
   
   //the code that is executed by the encrypt thread
   Runnable enc = new Runnable()
   {	   
	   String[] fileNames = null;
	   int numberOfFiles = 0;
	   public void run()
	   {		   
		   //creates a GCM_ENCRYPT object
		   long start = System.currentTimeMillis();
		   try {
			   fos.write(( "***************************************\r\n").getBytes());
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		   GCM_ENCRYPT ge = new GCM_ENCRYPT();
		   //creates a RND object
			RND genKey = new RND();
			String fileName = "";
	    	String dir = "";
	    	
	    	//sets default location for file manager to open to
//	    	JFileChooser c = new JFileChooser("C:\\Users\\Will\\Desktop\\Encrypted Files\\");
	    	JFileChooser c = new JFileChooser("C:\\Users\\123\\Desktop\\encrypted file\\");
	    	
	    	//allows for selection of multiple objects
	    	c.setMultiSelectionEnabled(true);
	    	//allows the selection of files and folders
	    	c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    	
	        // Demonstrate "Open" dialog:
	        int rVal = c.showOpenDialog(FilePanel.this);
	        //creates an array of all objects selected in file manager
	        File files[] = c.getSelectedFiles();
	        
	        long startTime = System.currentTimeMillis();
	          for( int i = 0; i < files.length; i++)
	    	  {
	        	  //gets the file name and directory
		          if (rVal == JFileChooser.APPROVE_OPTION) {
		        	fileName = files[i].getName();
		            dir = c.getCurrentDirectory().toString() + "\\";
		          }	          
		          
		          //checks if the selected object is a directory
		          if(files[i].isDirectory())
		          {
		        	  //calls the function to encrypt a directory
		        	  try {
						ge.encryptDirectory(files[i], dir);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		          }
		          //encrypts the file selected
		          else
		          {
		        	  File openFile = new File(dir + fileName);
		        	  //makes sure the file exists
			          if(openFile.exists())
			          {
				  	    	try {
				  	    		//generates a random key for encryption/decryption
								key = genKey.getRandom();
							} catch (NoSuchAlgorithmException e1) { e1.printStackTrace();
							}
				  	    	//calls the function to encrypt the file
				  	    	ge.gcmEncrypt(dir+fileName, key);
			          }
			          //shows message of file not existing
			          else
			          {
			             int wrong = JOptionPane.showConfirmDialog(null,  "Could not find file. Please try again.");           
			             if (wrong == JOptionPane.YES_OPTION)
			             {
			                try {
								new FilePanel(2);
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			             }
			             else
			             {
			                System.exit(0);
			             }
			          }
		          }
	    	}
	      
	        try {
				fos.write(("File encrypt and split Time Stamp : " + (System.currentTimeMillis() - startTime) + "\r\n").getBytes());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        System.out.println("file encrypt and splited");
	          //lets the user know when the files are encrypted and split
	        JOptionPane.showMessageDialog(null,  "Files encrypted and split.");
	        
	        File index = new File("Index.index");
	        //makes sure the index exists
	        if(index.exists())
	        {
	        	//gets the filenames from the index file
			    fileNames = getFileNames();
			    //gets the number of files in the index file
			    numberOfFiles = fileNames.length;
			    System.out.println("number of files: " + numberOfFiles);
			    System.out.println("File names: " + fileNames[0]);
			   
			    
			    startTime = System.currentTimeMillis();
			    //creates a connection to the android device
				try {
					JOptionPane.showMessageDialog(frame, "Send key from android");
					if(server.makeEConnection(fileNames))
					{
						System.out.println("makeEConnection finished");
						//changes the statuses in the GUI window
						fileStatus2.setText("Encrypted");
						fileStatus2.setForeground(Color.green);
						connectStatus2.setText("Connected");
						connectStatus2.setForeground(Color.green);
						if(numberOfFiles == 1)
						{
							sendStatus2.setText(numberOfFiles + " file sent");
							sendStatus2.setForeground(Color.green);
						}
						else
						{
							sendStatus2.setText(numberOfFiles + " files sent");
							sendStatus2.setForeground(Color.green);
						}
					}
					else
					{
						connectStatus2.setText("Timed Out");
						connectStatus2.setForeground(Color.red);
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BindException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	       
	        try {
				fos.write(("Piece transmit end time stamp : " + (System.currentTimeMillis()-startTime-server.keyTransmit) + "\r\n").getBytes());
				fos.write((server.keyTransmit+"\r\n").getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //encrypts the index
	        startTime = System.currentTimeMillis();
	        System.out.println("server.key : " + Base64Coder.encodeLines(server.key));
	        ge.indexEncrypt(server.key,nRSecret);
	        isEncrypted = true;
	        long indexencrypt = System.currentTimeMillis()-startTime;
	        //splits the index
	        //System.out.println("Index Start Time Stamp : " + System.currentTimeMillis());
	        try {
	        	fos.write(("Index encrypt End Time Stamp : " + ( indexencrypt)+ "\r\n").getBytes());
	        	fos.write(("Key binding end time stamp : " + ge.Keybinding + "\r\n").getBytes());
				JOptionPane.showMessageDialog(frame, "Index enctyption Finished");			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        startTime = System.currentTimeMillis();
	        try {
				SplitFiles.splitIndex();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        
	        //System.out.println("Index End Time Stamp : " + System.currentTimeMillis());
	        long indexsplit = (System.currentTimeMillis()-startTime);
	        try {
	        	fos.write(("Index split End Time Stamp : " + (indexsplit )+ "\r\n").getBytes());
	        	fos.write(("Index split and encrypt Time Stamp : " + (indexsplit+indexencrypt )+ "\r\n").getBytes());
				JOptionPane.showMessageDialog(frame, "Index Splitting Finished");			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
   };
   
   //code that is executed by the decrypt thread
   Runnable dec = new Runnable()
   {
	   public void run()
	   {		   
		   boolean allDecrypted = true;
		   //creates a GCM_ENCRYPT object
			GCM_DECRYPT gd = new GCM_DECRYPT();
			String fileName = "";
			String indexContents[] = null;
			int numFiles = 0;
			long t2=0;
			//TpmIDVerifyDEC.signID(sender,listener);
			
			try {
				JOptionPane.showMessageDialog(frame, "Send Key from android");
				long t1 = System.currentTimeMillis();
				if(!server.getAndroidKey()){
			
					JOptionPane.showMessageDialog(decrypt, "Can't get the Android Key!");
					System.exit(-1);
					
				}
				t2 = System.currentTimeMillis() - t1;
			} catch (BindException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (HeadlessException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (UnknownHostException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long startTime = System.currentTimeMillis();
			connectStatus2.setText("Connected");
			connectStatus2.setForeground(Color.green);
			//merges the index pieces back together
			System.out.println("merge File");
			try {
				SplitFiles.mergeIndex();
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			//decrypts the index
			//System.out.println("Index end time stamp : " + System.currentTimeMillis());
			long merge = 0;
			try {
				merge = System.currentTimeMillis()-startTime;
				fos.write(("Index Merge end time stamp : " + (System.currentTimeMillis()-startTime) + "\r\n").getBytes());			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			System.out.println("decrypt index file");
			System.out.println("server.key : " + Base64Coder.encodeLines(server.key));
			startTime = System.currentTimeMillis();
			if(isEncrypted){
				gd.indexDecrypt(server.key,nRSecret);
			}else{
				gd.indexDecrypt(server.key,tRSecret);
			}
			try {
				long decrypt = System.currentTimeMillis()-startTime;
				fos.write(("Index Decrypt end time stamp : " + (System.currentTimeMillis()-startTime) + "\r\n").getBytes());
				fos.write(("Index Merge and Decrypt end time stamp " + (decrypt+merge)+ "\r\n").getBytes());
				fos.write(("Key unBinding end time stamp : " + gd.KeyunBinding + "\r\n").getBytes());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			
			try 
			{
				//checks if the index exists
				if(new File("Index.index").exists())
				{
					//gets the contents of the index file
					indexContents = readFile("Index.index").split("\\r?\\n");
					//gets the number of files in the index
					numFiles = indexContents.length/4;
				}
			} catch (IOException e1) { e1.printStackTrace();
			}
			//index value for content arrays
	        int fileCounter = 0;
	        //array for all filenames in the index file
	        String fileNames[] = new String[numFiles];
	        //array for all keys in the index file
	        String keys[] = new String[numFiles];
	        //array for all IVs in the index file
	        String IVs[] = new String[numFiles];
	        //array for all random number in the index file
	        String randNums[] = new String[numFiles];
	        //checks if there is anything in the index
	        if(indexContents != null)
	        {
	        	//loops through all contents of the index file
		        for(int i = 0; i < indexContents.length-1; i++)
		        {
		        	//new file is every 4 lines
		        	if(i%4==0)
		        	{
		        		//adds the filename to the array
		    	        fileNames[fileCounter] = indexContents[i];
		    	        i++;
		    	        //adds the key to the array
		    	        keys[fileCounter] = indexContents[i];
		    	        i++;
		    	        //adds the IV to the array
		    	        IVs[fileCounter] = indexContents[i];
		    	        i++;
		    	        //adds the random numbers to the array
		    	        randNums[fileCounter] = indexContents[i];
		    	        fileCounter++;
		        	}
		        	
		        }
	   		}

	        
	        System.out.println("File name:"+indexContents[0]);
	        System.out.println("File name:"+fileNames[0]);
	        System.out.println("number of files:"+numFiles);
	        
	        //creates connection with the android device to receive pieces
	        System.out.println("waiting for coming files");
	        
	       
	        long decrypt=0;
	        startTime =  System.currentTimeMillis();
			try {
				if(server.makeDConnection(fileNames))
				{				
					//System.out.println("Receive piece end time stamp : " + System.currentTimeMillis());
					try {
						fos.write(("Receive piece end time stamp : " + (t2 + System.currentTimeMillis()-startTime) + "\r\n").getBytes());					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//merges and decrypts all files
					
					startTime = System.currentTimeMillis();
				      for( int i = 0; i < numFiles; i++)
					  {
				    	  	fileName = fileNames[i];
				    	  	//merges current file back together
					    	SplitFiles.mergeFile(fileName, randNums[i].split(","));
					    	//decrypts the current file
					    	merge = System.currentTimeMillis()-startTime;
					    	fos.write(("File Merge end time stamp : " + (System.currentTimeMillis()-startTime) + "\r\n").getBytes());
					    	startTime = System.currentTimeMillis();
					    	if(!gd.gcmDecrypt(fileName, keys[i], IVs[i]))
					    	{
					    		allDecrypted = false;
					    	}
					  }      
				      //System.out.println("Un-protection end time stamp : " + System.currentTimeMillis());
				      try {
				    	  decrypt = System.currentTimeMillis()-startTime;
				    	  fos.write(("File decrypt end time stamp : " + (System.currentTimeMillis()-startTime) + "\r\n").getBytes());
				    	  fos.write(("File merge and decrypt time stamp : " +(merge+decrypt)+"\r\n").getBytes());
							fos.close();						
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				      fos.close();
				      //checks that all files decrypted
				      if(allDecrypted)
				      {
				    	  //changes statuses
				    	  fileStatus2.setText("Decrypted");
				    	  fileStatus2.setForeground(Color.green);
				    	  
				    	  if(numFiles == 1)
							{
				    		  receiveStatus2.setText(numFiles + " file received");
				        	  receiveStatus2.setForeground(Color.green);
							}
							else
							{
								receiveStatus2.setText(numFiles + " files received");
					        	receiveStatus2.setForeground(Color.green);
							}
				      }
				      //decryption failed
				      else
				      {
				    	  //changes statuses
				    	  fileStatus2.setText("Decrypted With Errors");
				    	  fileStatus2.setForeground(Color.red);
				    	  connectStatus2.setText("Disconnected");
				    	  connectStatus2.setForeground(Color.red);
				    	  if(numFiles == 1)
							{
				    		  receiveStatus2.setText(numFiles + " file received");
				        	  receiveStatus2.setForeground(Color.green);
							}
							else
							{
								receiveStatus2.setText(numFiles + " files received");
					        	receiveStatus2.setForeground(Color.green);
							}
				      }
				    
				}
				//socket timed out before connection created
				else
				{
					//changes status
					connectStatus2.setText("Timed Out");
					connectStatus2.setForeground(Color.red);
				}
				isEncrypted = false;
//				server.serverListener.close();
//				server.sender.close();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
   };
   
   //starts the encrypt thread when button is pressed
   private class encryptListener implements ActionListener
   {
		public void actionPerformed(ActionEvent e) {
			if(decThrDone)
			{			
				System.out.println("encrypt thread start");
				encryptThread = new Thread(enc);
				encThrDone = false;
				encryptThread.start();
				encThrDone = true;
			}
		}
   }
   
   //starts the decrypt thread when button is pressed
   private class decryptListener implements ActionListener
   {
		public void actionPerformed(ActionEvent e) {
			if(encThrDone)
			{
				System.out.println("decrypt thread start");
				decryptThread = new Thread(dec);
				decThrDone = false;
				decryptThread.start();
				decThrDone = true;
			}
		}
		   
   }
 
   //reads the contents of a file
	private static String readFile(String fileName) throws IOException 
    {
    	String output = "";
    	try{
    		//opens file to be read
    		  FileInputStream fstream = new FileInputStream(fileName);
    		  // Get the object of DataInputStream
    		  DataInputStream in = new DataInputStream(fstream);
    		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
    		  String strLine;
    		  
    		  //Read File Line By Line
    		  while ((strLine = br.readLine()) != null)   {
	    		  output += strLine;
	    		  output += "\n";
    		  }
    		  
    		  //Close the input stream
    		  in.close();
    	}
    	//Catch exception if any
    	catch (Exception e)
    	{
    		  System.err.println("Error: " + e.getMessage());
    	}
    	
    	//returns contents of file
    	return output;
    }
	
	//gets the filenames from the index file
	public String[] getFileNames()
	   {
		   String indexContents[] = null;
			{
				try 
				{
					//reads teh contents of the index file
					indexContents = readFile("Index.index").split("\\r?\\n");
				} catch (IOException e1) { e1.printStackTrace();
				}
			}
			//gets the number of files in the index file
			int numFiles = indexContents.length/4;
			
	       String fileNames[] = new String[numFiles];
	       int fileCounter = 0;
	       //loops through index contents to get filenames
	       for(int i = 0; i < indexContents.length-2; i++)
	       {
	       	if(i%4==0)
	       	{
	       		//saves filenames to array
	   	        fileNames[fileCounter] = indexContents[i];
	   	        i++;
	   	        i++;
	   	        i++;
	   	        fileCounter++;
	       	}
	       	
	       }
	       return fileNames;
	   }
	
	//hashes the input string using SHA-256
   public boolean hash(String input)
   {
	   boolean hashVal = false;
	   MessageDigest md = null;
		try 
		{
			//sets the hash function to SHA-256
			md = MessageDigest.getInstance("SHA-256");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			//hashes the input string
			md.update(input.getBytes("US-ASCII"));
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		//gets the digest
	   byte[] digest = md.digest();
	   StringBuffer sb = new StringBuffer();
	   //puts the digest in hex format
       for (int i = 0; i < digest.length; i++) 
       {
         sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
       }
       //checks if the input matches the correct PIN
	   
       //if(sb.toString().equals("890311d7d3496d465f64e79dc1fe32eed28b13704ee89f98175c12b27a117cfd"))
	   if(true)
       {
		   hashVal = true;
	   }
	   return hashVal;
   }
	public JFrame getFrame() {
		return frame_1;
	}
}