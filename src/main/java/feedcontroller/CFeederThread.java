package feedcontroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;

import sentiment.Classifier;
import snetworks.SocialNetworkEntity;
import facebook.FbCrawler;
import facebook.entities.FbCheckinObject;
import facebook.entities.FbComments;
import facebook.entities.FbPOI;
import facebook.entities.FbStatus;
import foursquare.FriendsCheckins;
import foursquare.entities.FoursquareCheckin;
import foursquare.entities.FoursquareVenue;
import gr.ntua.ece.cslab.modissense.queries.clients.InsertPOIVisitClient;
import gr.ntua.ece.cslab.modissense.queries.clients.InsertUserPOITextClient;
import gr.ntua.ece.cslab.modissense.queries.containers.ModissenseText;
import gr.ntua.ece.cslab.modissense.queries.containers.UserIdStruct;
import gr.ntua.ece.cslab.modissense.queries.containers.UserPoiStruct;

/**
* Thread which reads user ids and social network tokens
* from postgres and returns, from the connected social networks,
* updated info for the selected users.
* 
* @author giagulei
*/

public class CFeederThread extends Thread{
	
	private List<HashMap<String, String>> snresults;
	private int startIndex;
	private int endIndex;
	private String since;
	private String until;
	private HashSet<Put> hbasePuts;
	private static boolean DEBUG = true;
	private int threadID;
	
	public CFeederThread(int threadID){
		this.threadID = threadID;
		this.hbasePuts = new HashSet<Put>();
	}
	
	public void setResults(List<HashMap<String, String>> snresults){
		this.snresults = snresults;
	}
	
	public void setStartIndex(int startIndex){
		this.startIndex = startIndex;
	}
	
	public void setEndIndex(int endIndex){
		this.endIndex = endIndex;
	}
	
	public void setSince(String since){
		this.since = since;
	}
	
	public void setUntil(String until){
		this.until = until;
	}
	
	public HashSet<Put> getPuts(){
		return hbasePuts;
	}
	
	/**
	 * gets as input a poi entity(name,x,y) and searches postgres poi table
	 * if the specific entity exists. If yes, it returns the unique id assigned
	 * by the postgres table. If not, the entity is inserted to postgres and the
	 * newly assigned id is returned.
	 * @param p
	 * @return
	 */
	public long getPoiID(entities.POI p){
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
	
	public void run(){
		if(DEBUG) System.out.println("CFeederThread "+threadID+":Results["+startIndex+", "+endIndex+"] running.");
		
		for(int i=startIndex;i<=endIndex;i++){
			String fbid = snresults.get(i).get("sn_identifier");
			String token = snresults.get(i).get("sn_token");
			//String uid = snresults.get(i).get("user_id");
			String network = snresults.get(i).get("sn_name");
						
			long start = System.currentTimeMillis();
			if(network.equals("facebook")){
				FbCrawler fbFeed = new FbCrawler(fbid,token,since,until);
				LinkedList<SocialNetworkEntity> fbEntities = fbFeed.getFbFeed();
				
				for(SocialNetworkEntity e:fbEntities){
					if(e instanceof FbCheckinObject){
						FbCheckinObject checkin = (FbCheckinObject) e;
						System.out.println(checkin.toString());
						FbPOI checkinPlace = checkin.getPlace(); 
						String newPoiName = checkinPlace.getName();
						double checkinX = checkinPlace.getLatitude();
						double checkinY = checkinPlace.getLongitude();
						
						String userID = checkin.getUserID();
						
						entities.POI p = new entities.POI(newPoiName, checkinX, checkinY);
						long poiid = this.getPoiID(p);
						
						gr.ntua.ece.cslab.modissense.queries.containers.POI poi = 
								new gr.ntua.ece.cslab.modissense.queries.containers.POI();
						poi.setId(poiid);
						poi.setName(newPoiName);
						poi.setX(checkinX);poi.setY(checkinY);
						poi.setTimestamp(checkin.getCreatedAt().getTime());
						
						UserIdStruct modisUser = new UserIdStruct('F', Long.parseLong(userID));
						InsertPOIVisitClient visit = new InsertPOIVisitClient();
						visit.setPoi(poi);
						visit.setUserId(modisUser);
						//visit.executeQuery();
						
						
						LinkedList<FbComments> comments = checkin.getComments();
						for(FbComments c:comments){
							UserPoiStruct userPOIKey = new UserPoiStruct('f', Long.parseLong(c.getUser()), poiid);
							ModissenseText commentText = new ModissenseText();
							commentText.setText(c.getComment());
							commentText.setTimestamp(checkin.getCreatedAt());
							
							Configuration conf = new Configuration(true);
							try {
								System.out.println("I am in try-catch");
								Classifier classifier = new Classifier(conf);
								int score = classifier.classify(c.getComment());
								System.out.println("text: "+c.getComment()+" Score:"+score);
							} catch (IOException e1) {
								System.out.println("Shit happen");
								e1.printStackTrace();
							}
							
							InsertUserPOITextClient userPoiText = new InsertUserPOITextClient();
							userPoiText.setUserPOIkey(userPOIKey);
							userPoiText.setText(commentText);
							//userPoiText.executeQuery();
						}
					}
					if(e instanceof FbStatus){
						FbStatus status = (FbStatus) e;
						//TODO: identify poi from status kai meta ta leme
					}
				}
				
				if(DEBUG) System.out.println("Facebook update: "+(System.currentTimeMillis()-start)+" ms");
			}else if(network.equals("foursquare")){
				FriendsCheckins fc = new FriendsCheckins(token);
				LinkedList<SocialNetworkEntity> fsqEntities = fc.getFriendsCheckins();
				for(SocialNetworkEntity e: fsqEntities){
					FoursquareCheckin checkin = (FoursquareCheckin) e;
					String userID = checkin.getUserID();
					UserIdStruct modisUser = new UserIdStruct('f', Long.parseLong(userID));
					
					FoursquareVenue venue = checkin.getVenue();
					double checkinX = venue.getLatitude();
					double checkinY = venue.getLongitude();
					String venueName = venue.getName();
					
					entities.POI p = new entities.POI(venueName, checkinX, checkinY);
					long poiid = this.getPoiID(p);
					
					gr.ntua.ece.cslab.modissense.queries.containers.POI poi = 
							new gr.ntua.ece.cslab.modissense.queries.containers.POI();
					poi.setId(poiid);
					poi.setName(venueName);
					poi.setX(checkinX);poi.setY(checkinY);
					poi.setTimestamp(checkin.getCreatedAt().getTime());
					
					InsertPOIVisitClient visit = new InsertPOIVisitClient();
					visit.setPoi(poi);
					visit.setUserId(modisUser);
					//visit.executeQuery();
				}
				if(DEBUG) System.out.println("Foursquare update: "+(System.currentTimeMillis()-start)+" ms");
			}else{
				//Twitter case
				System.out.println("Twitter mechanism is not ready yet.");
			}
		}
	}
	
	
}
