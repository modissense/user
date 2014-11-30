package utils;

import gr.ntua.ece.cslab.modissense.queries.clients.InsertUserPOITextClient;
import gr.ntua.ece.cslab.modissense.queries.containers.ModissenseText;
import gr.ntua.ece.cslab.modissense.queries.containers.UserPoiStruct;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import snstorage.TextRepo;

public class LoadPoiUserText {
	
	public static long generateLong(long x, long y){
		Random r = new Random();
        return x+((long)(r.nextDouble()*(y-x)));
	}
	
	public static void main(String[] args) {
		InsertUserPOITextClient client = new InsertUserPOITextClient();
		
		BufferedReader br = null;
		 
		long startDate = 1368553762L;
		long endDate = 1400089762L;
 
		String sCurrentLine;
		client.openConnection(TextRepo.TABLE_NAME);
		try {
			br = new BufferedReader(new FileReader(args[0]));
			while ((sCurrentLine = br.readLine()) != null) {
				if(!sCurrentLine.equals("")){
				String[] parts = sCurrentLine.split("\t");
				Long poiid = Long.parseLong(parts[0]);
				String userString = parts[1];
				char sn = userString.charAt(0);
				Long uid = Long.parseLong(userString.substring(1));
				System.out.println(poiid+" "+userString+" "+uid);
				UserPoiStruct key = new UserPoiStruct(sn, uid,poiid);
				
				long timestamp = generateLong(startDate, endDate);
				
				ModissenseText text = new ModissenseText();
				text.setText(parts[2]);
				
				client.setText(text);
//				client.setTimestamp(timestamp);
//				client.setUserId(key);
				
				client.executeQuery();
				}
			}
			br.close();
			client.closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		

	}

}
