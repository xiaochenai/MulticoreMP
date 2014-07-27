package KeySetGeneration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.omg.CORBA.PUBLIC_MEMBER;



public class keySet {
	private String[][] keySet;
	private ArrayList<ArrayList<String>> stringArrayList = new ArrayList<ArrayList<String>>();
	private HashSet<HashSet<String>> stringHashSet = new HashSet<HashSet<String>>();
	public keySet(String[][] keySet) {
		// TODO Auto-generated constructor stub
		this.keySet = keySet;
		for(int i=0;i<keySet.length;i++){
			ArrayList<String> temp_String_ArrayList = new ArrayList<String>();
			for(int j=0;j<keySet[i].length;j++){
				temp_String_ArrayList.add(keySet[i][j]);
			}
			stringArrayList.add(temp_String_ArrayList);
			//stringHashSet.add(temp_String_HashSet);
			}
	}
	//if the ArrayList does not have order then this method will work
	public boolean exist(String[] find_String){
		ArrayList<String> tempStrings = new ArrayList<String>();
		for(int i=0;i<find_String.length;i++){
			tempStrings.add(find_String[i]);
		}
		return stringArrayList.contains(tempStrings);
	}
	
	public boolean exist_HashSet(String[] find_String){
		HashSet<String> tempStrings = new HashSet<String>();
		for(int i=0;i<keySet.length;i++){
				tempStrings.add(find_String[i]);
		}
		return stringHashSet.contains(tempStrings);
	}
	public boolean exist(String keyString){
		for(int i=0;i<stringArrayList.size();i++){
			for(int j=0;j<stringArrayList.get(i).size();j++){
				if(stringArrayList.get(i).contains(keyString))
					return true;
			}
		}
		return false;
	}
	public int Get_Index(String keyString){
		int position = -1;
		for(int i=0;i<stringArrayList.size();i++){
			for(int j=0;j<stringArrayList.get(i).size();j++){
				if(stringArrayList.get(i).contains(keyString)){
					position = i;
					break;
				}
			}
		}
		return position;
	}
	public ArrayList<String> Get_KeySet(int i){
		return stringArrayList.get(i);
	}
	public void Printout(){
		for(int i =0;i<stringArrayList.size();i++){
			for(int j=0;j<stringArrayList.get(i).size();j++){
				//System.out.print(stringArrayList.get(i).get(j) + " ");
			}
			//System.out.println();
		}
	}

}
