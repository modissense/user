package utils;

import gr.ntua.ece.cslab.modissense.queries.clients.InsertPOIVisitClient;
import gr.ntua.ece.cslab.modissense.queries.clients.InsertUserPOITextClient;
import gr.ntua.ece.cslab.modissense.queries.containers.ModissenseText;
import gr.ntua.ece.cslab.modissense.queries.containers.POI;
import gr.ntua.ece.cslab.modissense.queries.containers.UserPoiStruct;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;

import snstorage.TextRepo;
import snstorage.UserGraph;
import modisusers.FriendIDs;
import gr.ntua.ece.cslab.modissense.queries.containers.UserIdStruct;


public class LoadVisits {
	
	public static final long startFirstWinterDate = 1356998400; // 01/01/2013
	public static final long endFirstWinterDate = 1367366400; // 31/04/2013
	
	public static final long startFirstSummerDate = 1367409600; // 01/05/2013
	public static final long endFirstSummerDate = 1380628800; // 01/10/2013
	
	public static final long startSecondWinterDate = 1383307200L; // 01/11/2013
	public static final long endSecondWinterDate = 1398946947L; // 01/05/2014
	
	public static final long startSecondSummerDate = 1401625347; // 01/06/2014
	public static final long endSecondSummerDate = 1420028547; // 31/10/2014
	
	//public static final long usersFBID = 100008415518168L; //annie
	//public static final long usersFBID = 100008467024903L; //ioannis
	
	private HashMap<Integer, Long> users;
	private char socialNetwork='F';
	private boolean winter;
	
	private long uid;
	
	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public LoadVisits(boolean winter){
		this.winter = winter;
	}

	public boolean isWinter() {
		return winter;
	}

	public void setWinter(boolean winter) {
		this.winter = winter;
	}

	public static long generateLong(long x, long y){
		Random r = new Random();
        return x+((long)(r.nextDouble()*(y-x)));
	}
	
	public void loadFriends() throws IOException{
		users = new HashMap<>();
		int userIndex = 0;
		UserIdStruct uStruct = new UserIdStruct(socialNetwork,uid);
		UserGraph hbaseUserTable = new UserGraph();
		FriendIDs fIds = hbaseUserTable.getUserFriendsIDs(uStruct);
		
		if(fIds!=null){
			LinkedList<byte[]> friendIds = fIds.getFriends();
			System.out.println("User Info Friends:");
			for(int n=0;n<friendIds.size();n++){
				UserIdStruct snUser = new UserIdStruct();
				snUser.parseBytes(friendIds.get(n));
				System.out.print("F"+snUser.getId()+",");
				users.put(new Integer(userIndex), snUser.getId());
				userIndex++;
			}
			System.out.println();
		}
	}
	
	public void printFriends(){
		for(Entry<Integer, Long> e:users.entrySet()){
			System.out.println("Friend"+e.getKey()+": "+e.getValue());
		}
	}
	
	public void loadDataVisitsDataset(String poiName,double lat,double longitude){
		
		POI poi = new POI();
		poi.setName(poiName);
		poi.setX(lat);
		poi.setY(longitude);
		HashSet<String> keywords = new HashSet<>();
		poi.setKeywords(keywords);
		
		String postgresName = poiName.replaceAll("'", "");
		entities.POI p = new entities.POI(postgresName, lat, longitude);
		
		long poiid = LoadPOIs.getPoiID(p);
		poi.setId(poiid);
		
		int numOfFriends = users.size();
		Random r = new Random();
		
		BufferedReader br = null;
		String line;
		try {
			String fileName = poiName.replaceAll(" ", "\\ ");
			br = new BufferedReader(new FileReader("comments/"+fileName));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					String[] parts = line.split("\t");
					double score = Double.parseDouble(parts[0]);
					System.out.println("parsed score: "+score);
					long timestamp;
					if(winter){
						timestamp = generateLong(startSecondWinterDate, endSecondWinterDate);
					}else{
						timestamp = generateLong(startSecondSummerDate, endSecondSummerDate);
					}
					
					poi.setScore(score);
					poi.setTimestamp(timestamp*1000);
					
					UserIdStruct uid = new UserIdStruct();
					uid.setC(socialNetwork);
					
					long userID = users.get(r.nextInt(numOfFriends));
					uid.setId(userID);
					InsertPOIVisitClient insertVisit = new InsertPOIVisitClient();
					insertVisit.setPoi(poi);
					insertVisit.setUserId(uid);
					
					insertVisit.openConnection(UserCheckinsTable.TABLE_NAME);
					insertVisit.executeQuery();
					insertVisit.closeConnection();
					
					InsertUserPOITextClient insertText = new InsertUserPOITextClient();
					UserPoiStruct textTableKey = new UserPoiStruct('F', userID,poiid);
										
					ModissenseText text = new ModissenseText();
					text.setText(parts[1]);
					text.setScore(score);
					text.setTimestamp(timestamp);
					
					insertText.setText(text);
					insertText.setUserPOIkey(textTableKey);
					
					insertText.openConnection(TextRepo.TABLE_NAME);
					insertText.executeQuery();
					insertText.closeConnection();
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void getVisitedPois(String poiFile){
		BufferedReader br = null;
		String line;
		
		try {
			br = new BufferedReader(new FileReader(poiFile));
			while ((line = br.readLine()) != null) {
				if(!line.equals("")){
					String[] parts = line.split("\t");
					String poiName = parts[0];
					double lat = Double.parseDouble(parts[1]);
					double longitude = Double.parseDouble(parts[2]);
					this.loadDataVisitsDataset(poiName, lat, longitude);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	

	public static void main(String[] args) throws IOException {
		
		if(args.length!=3){
			System.out.println("Args: winter(boolean) poisFile user");
		}
		
		Boolean winter = Boolean.parseBoolean(args[0]);
		String poiFile = args[1];
		
		String user = args[2];
		long userID=0;
		if(user.equals("annie")){
			System.out.println("ANNIE");
			userID = 100008415518168L;
		}else if(user.equals("ioannis")){
			System.out.println("IOANNIS");
			userID = 100008467024903L;
		}else{
			System.out.println("No such user. User should be annie or ioannis");
			System.exit(1);
		}
		
		if(winter) System.out.println("WINTERRRR");
		
		LoadVisits lv = new LoadVisits(winter);
		lv.setUid(userID);
		
		lv.loadFriends();
		lv.getVisitedPois(poiFile);
		
	}

}
