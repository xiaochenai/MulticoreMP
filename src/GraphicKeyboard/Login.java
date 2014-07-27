package GraphicKeyboard;


import javax.swing.*;

import BackGround.BackGround;
import KeySetGeneration.KeySetGeneration;
import PC.FilePanel;
import RandomKeyStroke.RandomKeyStroke;
import VariedLengthMappingTable.VariedLengthMappingTable;

import java.awt.*;
import java.awt.event.*;
import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*	Will Hodges Keyboard

	--only function used is:
	
		--actionPerformed(ActionEvent e)
		
			this determines if a button was pressed and which button was pressed
			
			if the submit button was pressed then it terminates the keyboard and clears the fields
			
			if a key was pressed it adds the character to the masterdevice char stream
	
*/

public class Login extends JDialog implements ActionListener, MouseListener, Runnable
{
    // Graphics constants
    public static final int SIZE = 500;
    public static final int RINGS = 1;
    public static final int SECTIONS_PER_RING = 9;
    public static final String BUTTON_TEXT = "Submit";
    public String[] STRINGS = {"R6!%`e[b'AI","m@#g=E^Wd1X","5p{ayZ +j.",";z423qK-7sV","Y:)D~/(o}T","\"M8l9>nht|\\",
            "f_v<k0B&O$","*c?iwruLQx","N,FHJSGUCP]"};
    public String[] NEXTROUNDSTRINGS;
    public String tRSecret;
    public String nRSecret;
    private VariedLengthMappingTable NVLMT;
    private VariedLengthMappingTable VLMT;
    private RandomKeyStroke RKS;
    private BackGround BG;
    private String[] Processed_UKS;
    private String[] MASK;
    // Networking constants
    public static final int PORT_NUM = 1040;
    public static final int BACKLOG  = 20;
    
    // Graphical components
    private JLayeredPane layeredPane;
    private RadialBackground radialBg;
    private CenterButton centerButton; 
    private JPanel inputPanel;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JTextField passwordField;
    private JButton resetButton;
    private static ExecutorService exec = Executors.newCachedThreadPool();
    private RadialInputButton[] bts;
    private RadialInputButton bt1;
    private RadialInputButton bt2;
    private RadialInputButton bt3;
    private RadialInputButton bt4;
    private RadialInputButton bt5;
    private RadialInputButton bt6;
    private RadialInputButton bt7;
    private RadialInputButton bt8;
    private RadialInputButton bt9;
    private boolean flag=false;
    private long localSeed=0;
    private long peerSeed=0;
    private FilePanel FP;
    private boolean submitted = false,authResult=false;
    private String ServerIP;

    // Password buffer
    private StringBuffer passwordBuffer;

    public Login(Frame parent, boolean modal,FilePanel FP, String serverIP) throws NoSuchAlgorithmException, IOException
    {
        super(parent,modal);
        this.FP = FP;
        this.ServerIP = serverIP;
        System.out.println("Login Server IP:" + this.ServerIP);
        init();
        
    }
    public void run(){

    }
    private void graphic_keyboard_initializetion() throws IOException, NoSuchAlgorithmException{
    	KeySetGeneration KSG = new KeySetGeneration();
    	KSG.GenerateKeyset();
		String[][] FinalKeySet = KSG.GetFinalKeySet("Keyset.txt");
		KSG.SaveKeySet2File("Keyset1.txt");
		System.out.println("The Final KeySet is : ");
		for(int i=0;i<FinalKeySet.length;i++){
			for(int j=0;j<FinalKeySet[i].length;j++){
				System.out.print(FinalKeySet[i][j] + " ");
			}
			System.out.println();
		}
		this.STRINGS = new String[FinalKeySet.length];
		String[] temp_stringStrings = new String[FinalKeySet.length];
		for(int i=0;i<FinalKeySet.length;i++){
			StringBuilder sb = new StringBuilder();
			for(int j=0;j<FinalKeySet[i].length;j++){
				sb.append(FinalKeySet[i][j]);
			}
			temp_stringStrings[i] = sb.toString();
		}
		System.arraycopy(temp_stringStrings, 0, STRINGS, 0, temp_stringStrings.length);
		
		KSG.UpdateKeySet();
		KSG.SaveKeySet2File("Keyset2.txt");
		//KSG.SaveKeySet2File("Keyset.txt");
		KeySetGeneration KSG1 = new KeySetGeneration();
		KSG1.GenerateKeyset();
		FinalKeySet = KSG1.GetFinalKeySet("Keyset2.txt");
		//KSG1.UpdateKeySet();
		//KSG1.SaveKeySet2File("Keyset3.txt");
		temp_stringStrings = new String[FinalKeySet.length];
		this.NEXTROUNDSTRINGS = new String[FinalKeySet.length];
		for(int i=0;i<FinalKeySet.length;i++){
			StringBuilder sb = new StringBuilder();
			for(int j=0;j<FinalKeySet[i].length;j++){
				sb.append(FinalKeySet[i][j]);
			}
			temp_stringStrings[i] = sb.toString();
		}
		System.arraycopy(temp_stringStrings, 0, NEXTROUNDSTRINGS, 0, temp_stringStrings.length);
    }
    public void MappingTableInitilization() throws IOException{
    	this.VLMT = new VariedLengthMappingTable();
    	VLMT.initiation();
    }
    public void RKS_Initialization() throws NoSuchAlgorithmException{
    	long RKS_Character_Determination_Seed = 2;
    	this.RKS = new RandomKeyStroke();
    	RKS.InsertRKSLocalInitiate(peerSeed, localSeed);
    	RKS.RKSGeneration_Preparation(RKS_Character_Determination_Seed);
    }
    public void Print_Out_STRINGS(){
    	for(int i=0;i<STRINGS.length;i++){
    		System.out.println(STRINGS[i]);
    	}
    }
    public void init() throws NoSuchAlgorithmException, IOException 
    {
        this.setSize(500, 500);
        this.graphic_keyboard_initializetion();
        //this.Print_Out_STRINGS();
        this.MappingTableInitilization();
        this.RKS_Initialization();
        BG = new BackGround();
        BG.getServerIP(this.ServerIP);
        BG.Background_Parameter_trainsit(STRINGS, NEXTROUNDSTRINGS,VLMT, RKS);
        passwordBuffer = new StringBuffer();
        layeredPane = new JLayeredPane();
        System.out.println("a mapped to : " + VLMT.LookUpMappingSequence("a"));




        centerButton = new CenterButton(BUTTON_TEXT,BG);
        centerButton.setDiameter((SIZE-2*radialBg.BORDER)/(RINGS+4));
        centerButton.setBounds(0, 0, SIZE, SIZE);
        centerButton.addActionListener(this);
        centerButton.setActionCommand("submit");
        layeredPane.add(centerButton, new Integer(1));

       // Create password input buttons
        double radius = SIZE/2 - radialBg.BORDER;
        //ringWidth is the radius length of each ring
        double ringWidth = radius/(RINGS+0);
        //the radians of each ring
        double sectionRadians = Math.PI * 2 / SECTIONS_PER_RING;
        bts = new RadialInputButton[9];

        for(int i = 0; i < SECTIONS_PER_RING; ++i)
        {
            for(int j = 0; j < RINGS; ++j)
            {
                //so here j always is 1
                RadialInputButton inputter;
                int stringsIndex = i*RINGS+j;
                if(stringsIndex < STRINGS.length)
                {
                    bts[i] = new RadialInputButton(STRINGS[stringsIndex],BG);
                    bts[i].setActionCommand(STRINGS[stringsIndex]);
                    bts[i].addActionListener(this);
                    bts[i].addMouseListener(this);
                   // addMouseListener(bts[i]);


                }
                else
                {
                    inputter = new RadialInputButton("",BG);
                }
                double subRadius = radius - ringWidth*j;
                bts[i].setShape(subRadius-ringWidth, subRadius, sectionRadians, sectionRadians*i);
                bts[i].setBounds(0, 0, SIZE, SIZE);
                layeredPane.add(bts[i], new Integer(1));
                exec.execute(bts[i]);
            }
        }


        add(layeredPane);
        inputPanel = new JPanel();
        usernameLabel = new JLabel("");
        inputPanel.add(usernameLabel);
        usernameField = new JTextField(20);
        inputPanel.add(usernameField);


        add(inputPanel, BorderLayout.NORTH);


    }
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("submit"))
        {
            // Submit the username and password
            try {
				submit();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
        }
        else
        {

            // Add a character to the password
//            for(int i=0;i<9;i++){
//                bts[i].SetSpeed(1);
//            }

            //System.out.println(e.getActionCommand());
            //MainScreen.mD.pack.addChar((int)(e.getActionCommand().charAt(0)-33));
            usernameField.setText(usernameField.getText()+"*");
        }
    }
    public void mousePressed(MouseEvent e){
       // System.out.println("TIME : " + System.currentTimeMillis()) ;
    }
    public void mouseReleased(MouseEvent e){


    }
    public void mouseEntered(MouseEvent e){
        for(int i=0;i<9;i++){
            bts[i].SetSpeed(100000000);
        }
    }
    public void mouseExited(MouseEvent e){
        for(int i=0;i<9;i++){
            bts[i].SetSpeed(1000000);
        }
    }
    public void mouseClicked(MouseEvent e){
    	

    }


    private void submit() throws IOException, NoSuchAlgorithmException
    {
    	authResult = BG.SubmitClicked();
    	submitted=true;
    	if(submitted && authResult){
    		System.out.println("verification successful");
    		JOptionPane.showMessageDialog(this, "Verified");
        	usernameField.setText("");
        	tRSecret = BG.tRsecret;
        	nRSecret = BG.nRsecret;
        	FP.nRSecret = nRSecret;
        	FP.tRSecret = tRSecret;
        	System.out.println("BG tRsecret : " + tRSecret);
        	System.out.println("BG nRsecret : " + nRSecret);
        	dispose();
//        	new FilePanel(2);
        	
    	}else {
    		System.out.println("Verification Failed");
    		JOptionPane.showMessageDialog(this, "Verified Failed");
    		System.exit(1);
		}
    	
    	
    	//MainScreen.setKeyboard(false);
    	System.out.println("Keyboard Submit Clicked");
    }
//    public static void main(String[] args) throws NoSuchAlgorithmException, IOException{
//        JFrame frame = new JFrame();
//        final Login lo = new Login(frame,true,"aa","bb");
//        lo.setTitle("Login");
//        //lo.setVisible(true);
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                lo.setLocationRelativeTo(lo.getParent());
//                lo.addWindowListener(new WindowAdapter() {
//                    @Override
//                    public void windowClosing(WindowEvent e)
//                    {
//                        System.exit(0);
//                    }
//                });
//                lo.setVisible(true);
//            }
//        });
//
//    }
}

    