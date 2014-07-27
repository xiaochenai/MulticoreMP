package BackGround;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import biz.source_code.base64Coder.Base64Coder;
import GeneralClass.Hash;
import MaskGeneration.Candidates;
import RandomKeyStroke.RandomKeyStroke;
import VariedLengthMappingTable.VariedLengthMappingTable;

public class BackGround {
	private String[] oldKeySet;
	private String[] newKeySet;
	private String[][] OldKeySet;
	private String[][] NewKeySet;
	private VariedLengthMappingTable VLMT;
	private RandomKeyStroke RKS;
	private int[] UKS;
	private String[] MASK;
	private String[] Processed_UKS;
	private ArrayList<Integer> uKS_ArrayList = new ArrayList<Integer>();
	private Candidates candidates;
	private String[] oldMask;
	private String[] old_Processed_UKS;
	private long LocalSeed;
	private long PeerSeed;
	private int uKS_Null_Counter=0;
	private ArrayList<String[]> rKSArrayList = new ArrayList<String[]>(); 
	private boolean Verified = false;
	private boolean allPacketReceived=false;
	//public ServerListener serverListener = new ServerListener();
	private String replyFromServer = "false";
	private String ServerIP = "192.168.0.106";
	public String tRsecret;
	public String nRsecret;
	public void Background_Parameter_trainsit(String[] OldKeySet, String[] NewKeySet, VariedLengthMappingTable VLMT,RandomKeyStroke RKS){
		this.oldKeySet = OldKeySet;
		this.newKeySet = NewKeySet;
		this.VLMT = VLMT;
		this.RKS = RKS;
	}
	public void getServerIP(String ServerIP){
		this.ServerIP = ServerIP;
		System.out.println("********************************Server IP : " + this.ServerIP);
	}
	public void Background_Key_Pressed_transit(String keyset){
		System.out.println("Mouse Clicked, keystring is : " + keyset);
		System.out.println("INDEX is : " + this.GetIndex(keyset, oldKeySet));
		uKS_ArrayList.add(this.GetIndex(keyset, oldKeySet));
		System.out.println("CURRENT ARRAYLIST");
		for(Integer i:uKS_ArrayList){
			System.out.println(i);
		}
	}
	public void Recover_old_processed_UKS(){
		
	}
	private void PrintOut(String[] S){ 
		for(String s:S){
			System.out.println(s);
		}
	}
	private void PrintOut(ArrayList<String> A){
		for(String s:A){
			System.out.println(s);
		}
	}
	private int GetIndex(String keyset,String[] keysets){
		for(int i=0;i<keysets.length;i++){
			if(keyset.endsWith(keysets[i])){
				return i;
			}
			
		}
		return -1;
	}
	private void generateOldRKS(String[] RUKS){
		int Index=0;
		for(int i=0;i<rKSArrayList.size();i++){
			if(rKSArrayList.get(i) == null){
				System.out.print("NULL");
			}else {
				RKS.UpdateRKS_with_UKS(RUKS[Index]);
				Index++;
				if(Index == RUKS.length)
					Index=0;
				String[] tempRKS = RKS.RKS_Determination();
				rKSArrayList.set(i, tempRKS);
				for(int j=0;j<rKSArrayList.get(i).length;j++){
					System.out.print(rKSArrayList.get(i)[j] + " " );
				}
				
			}
			System.out.println();
		}
	}
	private void generateNewRKS(String[] PUKS){
		int Index=0;
		for(int i=0;i<rKSArrayList.size();i++){
			if(rKSArrayList.get(i) == null){
				System.out.print("NULL");
			}else {
				RKS.UpdateRKS_with_UKS(PUKS[Index]);
				Index++;
				if(Index == PUKS.length)
					Index=0;
				String[] tempRKS = RKS.RKS_Determination();
				rKSArrayList.set(i, tempRKS);
				for(int j=0;j<rKSArrayList.get(i).length;j++){
					System.out.print(rKSArrayList.get(i)[j] + " " );
				}
				
			}
			System.out.println();
		}
	}

	public boolean SubmitClicked() throws IOException, NoSuchAlgorithmException{
		//this need to be changed
		Hash hash = new Hash();
		//serverListener.init();
		this.UKS = new int[uKS_ArrayList.size()-uKS_Null_Counter];
		for(int i=0;i<uKS_ArrayList.size();i++){
			if(uKS_ArrayList.get(i) != -1)
				UKS[i] = uKS_ArrayList.get(i);
		}
		for(int a:UKS){
			System.out.println(a);
		}
		//String[] RUKS = new String[5];
		
		candidates_pre_process();
		
		candidates = new Candidates(OldKeySet, NewKeySet, UKS,VLMT);
		//now we are going to generate the UKS for authentication
		int MaskLength = candidates.LoadExistMask("MASK.txt");
		if(MaskLength != UKS.length){
			System.out.println("Authentication Failed");
			System.exit(0);
		}
		String[] RUKS = candidates.doReverseMASK();
		Save2File("RUKS.txt", RUKS);
		String[] SUKS = this.ReadProcessedUKS("Processed_UKS.txt");
		String[] RRUKSBASE64 = new String[RUKS.length];
		for(int i=0;i<RUKS.length;i++){
			RRUKSBASE64[i] = Base64Coder.encodeString(RUKS[i]);
		}
		//System.out.println("Check RUKS");
		for(int i=0;i<RUKS.length;i++){
			//System.out.println("****************");
			//System.out.println(RRUKSBASE64[i] + "\r\n" + SUKS[i]);
			//System.out.println("****************");
			if(RRUKSBASE64[i].equals(SUKS[i])){
				System.out.println("same");
			}else {
				System.out.println("different");
			}
		}
		
		generateOldRKS(RUKS);
		System.out.println("This Round RKS");
		for(int i=0;i<rKSArrayList.size();i++){
			System.out.print("RKS : ");
			if(rKSArrayList.get(i) == null){
				System.out.print("NULL");
			}else {
				for(int j=0;j<rKSArrayList.get(i).length;j++){
					System.out.print(rKSArrayList.get(i)[j] + " " );
				}
				System.out.print( " " + VLMT.LookUpMappingSequence(rKSArrayList.get(i)[0]));
			}
			System.out.println();
		}
		System.out.println("Recovered Processed UKS of this Round");
		for(String s:RUKS){
			System.out.println(Base64Coder.encodeLines(s.getBytes()));
		}
		Authentication(RUKS);
		
		
		System.out.println("reply from server : " + replyFromServer);
		//replyFromServer = "Verified";
		if(replyFromServer.equals("Verified")){
			VLMT.update();
			VLMT.initiation();
			System.gc();
			System.out.println("NEW a mapped to : " + VLMT.LookUpMappingSequence("a"));
			File f1 = new File("Keyset.txt");
			File f2 = new File("Keyset2.txt");
			if(f1.exists()){
				System.out.println("Keyset.txt exists");
				if(f1.delete()){
					System.out.println("keyset.txt deleted");
				}
				else{
					System.out.println("keyset.txt deleted failed");
				}
			
			}
			if(f2.exists()){
				if(f2.renameTo(new File("Keyset.txt"))){
					System.out.println("keyset2 renamed to keyset");
				}
				else{
					System.out.println("rename failed");
				}
			}
			
			//generate Mask and Processed_UKS
			candidates = new Candidates(OldKeySet, NewKeySet, UKS,VLMT);
			candidates.Generate_Tree();
			candidates.Generate_Mask();//Mask should be save to a file for next round verification
			candidates.Generate_Processed_UKS();//if this round verification passed, UKS should be send to server
			MASK = candidates.Get_MASK();
			Processed_UKS = candidates.Get_Processed_UKS();
			rKSArrayList.clear();
			RKS = new RandomKeyStroke();
			RKS.InsertRKSLocalInitiate(0, 0);
	    	RKS.RKSGeneration_Preparation(2);
	    	for(int i=0;i<Processed_UKS.length;i++){
	    		Insert_RKS();
	    	}
	    	generateNewRKS(Processed_UKS);
	    	System.out.println("Next Round RKS");
			for(int i=0;i<rKSArrayList.size();i++){
				System.out.print("RKS : ");
				if(rKSArrayList.get(i) == null){
					System.out.print("NULL");
				}else {
					for(int j=0;j<rKSArrayList.get(i).length;j++){
						System.out.print(rKSArrayList.get(i)[j] + " " );
					}
					System.out.print( " " + VLMT.LookUpMappingSequence(rKSArrayList.get(i)[0]));
				}
				System.out.println();
			}
			System.out.println("Processed USK for Next Round");
			for(String s:Processed_UKS){
				System.out.println(Base64Coder.encodeLines(s.getBytes()));
			}
	    	NextRoundAuthentication(Processed_UKS);
	    	
	    	System.out.println("reply from server : " + replyFromServer);
	    	//replyFromServer = "allreceived";
	    	if(replyFromServer.equals("allreceived")){
	    		candidates.SaveMask2File("MASK.txt");
	    		candidates.SaveProcessedUKS2File("Processed_UKS.txt");
	    		this.tRsecret = new String(hash.GetHash_KeySetMappingString(RUKS));
	    		this.nRsecret = new String(hash.GetHash_KeySetMappingString(Processed_UKS));

	    		
	    		return true;
	    	}
	    	
		}
		else {

			this.tRsecret = new String(hash.GetHash_KeySetMappingString(RUKS));
			this.nRsecret = new String(hash.GetHash_KeySetMappingString(Processed_UKS));
			JOptionPane.showMessageDialog(null,  "Failed");
			return false;
		}

		
		this.tRsecret = new String(hash.GetHash_KeySetMappingString(RUKS));
		this.nRsecret = new String(hash.GetHash_KeySetMappingString(Processed_UKS));
		System.out.println("bg : " + tRsecret);
		System.out.println("bg : " + nRsecret);
		return Verified;
  
	}
	private String[] ReadProcessedUKS(String FileName) throws IOException{
		BufferedReader fin = new BufferedReader(new FileReader(FileName));
		String aString = null;
		ArrayList<String> tempAL = new ArrayList<String>();
		while((aString = fin.readLine())!=null){
			
			//System.out.println("READ IN :" + aString);
			tempAL.add(aString);
		}
		fin.close();
		String[] aUKS = new String[tempAL.size()];
		for(int i=0;i<tempAL.size();i++){
			aUKS[i] = tempAL.get(i);
		}
		return aUKS;
	}
	public void Save2File(String FileName,String[] Content) throws IOException{
		FileOutputStream fos = new FileOutputStream(new File(FileName));
		for(int i=0;i<Content.length;i++){
			fos.write(Base64Coder.encodeString(Content[i]).getBytes());
			fos.write("\n".getBytes());
		}
		fos.close();
	}
	public void Generate_This_Round_Processed_UKS(){
		
	}
	public String[] GetMASK(){
		return MASK;
	}
	public String[] GetProcessedUKS(){
		return Processed_UKS;
	}
	private void candidates_pre_process(){
		OldKeySet = new String[oldKeySet.length][];
		for(int i=0;i<oldKeySet.length;i++){
			OldKeySet[i] = new String[oldKeySet[i].length()];
			for(int j=0;j<oldKeySet[i].length();j++){
				OldKeySet[i][j] = oldKeySet[i].charAt(j)+"";
			}
		}
		NewKeySet = new String[newKeySet.length][];
		for(int i=0;i<oldKeySet.length;i++){
			NewKeySet[i] = new String[newKeySet[i].length()];
			for(int j=0;j<newKeySet[i].length();j++){
				NewKeySet[i][j] = newKeySet[i].charAt(j)+"";
			}
		}
	}
	public void UpdateRKS(String keyset){
		this.RKS.UpdateRKS_with_UKS(keyset);
	}
	public boolean Insert_RKS() throws NoSuchAlgorithmException{
		if(RKS.InsertRKSLocal() == true){
			String[] tempRKS = RKS.RKS_Determination();
			rKSArrayList.add(tempRKS);
			return true;
		}else {
			rKSArrayList.add(null);
			return false;
		}
		
	}
	public void CheckFriendDeviceStatuBuffer() throws NoSuchAlgorithmException{
		int[] statusBuffer = GetPeerStatusBuffer();
		if(statusBuffer != null){
			for(int i=0;i<statusBuffer.length;i++){
				if((RKS.InsertRKSLocal() == true) && (statusBuffer[i] == -2)){
					String[] tempRKS = RKS.RKS_Determination();
					rKSArrayList.add(tempRKS);
					System.out.println("Key in Peer, RKS insert Local");
				}else if((RKS.InsertRKSLocal() == false) && (statusBuffer[i] == -1)){
					rKSArrayList.add(null);
					System.out.println("Key in Peer, RKS insert Peer");
				}
				uKS_ArrayList.add(-1);
				uKS_Null_Counter++;
			}
		}
	}
	private int[] GetPeerStatusBuffer(){
		int[] status = {};
		return status;
	}

	private void Authentication(String[] RUKS) throws IOException{
		System.out.println("RKS length : " + rKSArrayList.size() + " UKS size : " + uKS_ArrayList.size());
		int RUKS_Index=0;
		ArrayList<String> data2ServerArrayList = new ArrayList<String>();
		//push all ProcessedUKS and RKS to a ArrayList
		for(int i=0;i<rKSArrayList.size();i++){
			if(rKSArrayList.get(i) == null){
				if(uKS_ArrayList.get(i) == -1){
					
				}else {
					data2ServerArrayList.add(RUKS[RUKS_Index]);
					RUKS_Index++;
				}
			}else {
				if(uKS_ArrayList.get(i) == -1){
					for(String s:rKSArrayList.get(i)){
						data2ServerArrayList.add(VLMT.LookUpMappingSequence(s));
					}
					
				}else {
					data2ServerArrayList.add(RUKS[RUKS_Index]);
					for(String s:rKSArrayList.get(i)){
						data2ServerArrayList.add(VLMT.LookUpMappingSequence(s));
					}
					RUKS_Index++;
				}
			}
		}
		Socket client = new Socket(ServerIP, 1234);
		PrintStream out = new PrintStream(client.getOutputStream());
		BufferedReader buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
		String dataString = "PAuth:";
		for(int i=0;i<data2ServerArrayList.size()-1;i++){
			dataString = dataString + Base64Coder.encodeString(data2ServerArrayList.get(i)) + ":";
		}
		dataString = dataString + Base64Coder.encodeString(data2ServerArrayList.get(data2ServerArrayList.size()-1));
		System.out.println("AuthString : " + dataString);
		out.println(dataString);
		client.close();
		ServerSocket Server = new ServerSocket(4321);
		Socket authenResponse = Server.accept();
		BufferedReader bufResponse = new BufferedReader(new InputStreamReader(authenResponse.getInputStream()));
		replyFromServer = bufResponse.readLine();
	}
	private void NextRoundAuthentication(String[] Processed_UKS) throws IOException{
		System.out.println("RKS length : " + rKSArrayList.size() + " UKS size : " + uKS_ArrayList.size());
		int RUKS_Index=0;
		ArrayList<String> data2ServerArrayList = new ArrayList<String>();
		//push all ProcessedUKS and RKS to a ArrayList
		for(int i=0;i<rKSArrayList.size();i++){
			if(rKSArrayList.get(i) == null){
				if(uKS_ArrayList.get(i) == -1){
					
				}else {
					data2ServerArrayList.add(Processed_UKS[RUKS_Index]);
					RUKS_Index++;
				}
			}else {
				if(uKS_ArrayList.get(i) == -1){
					for(String s:rKSArrayList.get(i)){
						data2ServerArrayList.add(VLMT.LookUpMappingSequence(s));
					}
					
				}else {
					data2ServerArrayList.add(Processed_UKS[RUKS_Index]);
					for(String s:rKSArrayList.get(i)){
						data2ServerArrayList.add(VLMT.LookUpMappingSequence(s));
					}
					RUKS_Index++;
				}
			}
		}
		Socket client = new Socket(ServerIP, 1234);
		PrintStream out = new PrintStream(client.getOutputStream());
		BufferedReader buf =  new BufferedReader(new InputStreamReader(client.getInputStream()));
		String dataString = "PNAuth:";
		for(int i=0;i<data2ServerArrayList.size()-1;i++){
			dataString = dataString + Base64Coder.encodeString(data2ServerArrayList.get(i)) + ":";
		}
		dataString = dataString + Base64Coder.encodeString(data2ServerArrayList.get(data2ServerArrayList.size()-1));
		System.out.println("NAuthString : " + dataString);
		out.println(dataString);
		replyFromServer = buf.readLine();
		client.close();
	}
	
}
