package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CreatePoidText {
	
	private HashMap<String, Long> namesToIDS;
	
	public CreatePoidText(){
		namesToIDS = new HashMap<>();
	}

	public static void main(String[] args) {
		String namesidsFile = args[0];
		String textsFile = args[1];
		
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(namesidsFile));
			CreatePoidText c = new CreatePoidText();
			while ((sCurrentLine = br.readLine()) != null) {
				String[] params = sCurrentLine.split(" ");
				String key="";
				for(int i=2;i<params.length-1;i++)
					key+=params[i]+" ";
				key+=params[params.length-1];
				//System.out.println(key);
				c.namesToIDS.put(key, Long.parseLong(params[1]));
			}
 
			br.close();
			
			br = new BufferedReader(new FileReader(textsFile));
		
			while ((sCurrentLine = br.readLine()) != null) {
				String[] params = sCurrentLine.split("\t");
				System.out.println(c.namesToIDS.get(params[0])+"\t"+params[1]);
			}
 
			br.close();
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

}
