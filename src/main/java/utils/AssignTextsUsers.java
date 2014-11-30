package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class AssignTextsUsers {
	
	private HashMap<String, LinkedList<String>> poidsToText;
	
	public AssignTextsUsers(){
		poidsToText = new HashMap<>();
	}
	
	public void addToPOITextsMapping(String poiid,String text){
		//System.out.println("Adding ("+poiid+",["+text+"])");
		LinkedList<String> temp;
		if(!poidsToText.containsKey(poiid)){
			temp = new LinkedList<String>();
		}else
			temp = poidsToText.get(poiid);
		temp.add(text);
		poidsToText.put(poiid, temp);
		//System.out.println("Insertion Completed");
	}

	public static void main(String[] args) {
		
		
		String textsFile = args[0];
		String namesidsFile = args[1];
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
			AssignTextsUsers assigner = new AssignTextsUsers();
			
			br = new BufferedReader(new FileReader(textsFile));
			while ((sCurrentLine = br.readLine()) != null) {
				String[] params = sCurrentLine.split("\t");
				String poiid = params[0];
				String text = params[1];
				//if(poiid!=null && text!=null)
				
				assigner.addToPOITextsMapping(poiid, text);
			}
			br.close();
			//System.out.println("GOOD");
			br = new BufferedReader(new FileReader(namesidsFile));
			while ((sCurrentLine = br.readLine()) != null) {
				String[] params = sCurrentLine.split(" ");
				String user = params[0];
				String poiid = params[1];
				
				if(assigner.poidsToText.containsKey(poiid)){
				LinkedList<String> temp = assigner.poidsToText.get(poiid);
				Random r = new Random();
				int textIndex = r.nextInt(temp.size());
				System.out.println(poiid+"\t"+user+"\t"+temp.get(textIndex)+"\n");
				}
				
			}
 
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

}
