package PC;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import biz.source_code.base64Coder.Base64Coder;
import Sender_Receiver.ServerListener;
public class RunReceiver implements Runnable {
   private ArrayList<ArrayList<String>> receivedStrings;
   public RunReceiver() {}
   public RunReceiver(ArrayList<ArrayList<String>> receivedStrings) { this.receivedStrings = receivedStrings; }

public void run() {
	// TODO Auto-generated method stub
	Thread.currentThread().setPriority(6);
	ServerListener serverListener = new ServerListener();
	   try {
		serverListener.init();
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	   do{
		   byte[] data;
		try {
			data = serverListener.ReadData();
			String receivedString = new String(Base64Coder.decodeLines(new String(data)));
			 //System.out.println("received data : " + receivedString);
			 //0 is key; 1 is file length; 2 is file name; 3 is file content;4 is finished flag;5 is others
			 if(receivedString.contains("key:")){
				 receivedStrings.get(0).add(receivedString);
			 }
			 else if(receivedString.contains(":split:")){
				 receivedStrings.get(1).add(receivedString);
				 receivedStrings.get(4).add("True");
				 System.out.println("receive file content");
				 //System.out.println(receivedString);
			 }
			 else{
				 receivedStrings.get(2).add(receivedString);
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		   
	   }while(true);
}
}