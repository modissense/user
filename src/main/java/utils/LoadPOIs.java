package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoadPOIs {
	
	public static long getPoiID(entities.POI p){
		long poiid;
		if(p.exists()){
			poiid = p.getID();
		}else{
			if(p.create()){
				p.read();
				poiid = p.getID();
			}else
				poiid = -1;
		}
		return poiid;
	}
	
	public void loadPoisFromFile(String fileName){
		BufferedReader br = null;
		String line;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					String[] parts = line.split("\t");
					
					String poiName = parts[0];
					double longitude = Double.parseDouble(parts[2]);
					double latitude = Double.parseDouble(parts[1]);
				
					poiName = poiName.replaceAll("'", " ");
					entities.POI p = new entities.POI(poiName, latitude, longitude);
				
					p.setUserID(-2);
					p.setIsPublic(true);
					p.setHotness(0);
					p.setInterest(0);
					
					p.create();
				

				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		LoadPOIs lp = new LoadPOIs();
		lp.loadPoisFromFile(args[0]);

	}

}
