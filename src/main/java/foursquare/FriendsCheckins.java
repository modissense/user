package foursquare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import snetworks.SocialNetworkEntity;
import foursquare.entities.FoursquareCheckin;
import foursquare.entities.FoursquareVenue;

/**
* Foursquare Checkin Update Mechanism
* @author giagulei
*/

public class FriendsCheckins {

	private String accessToken;
	private LinkedList<SocialNetworkEntity> entitiesToReturn;
	
	public FriendsCheckins(String token){
		this.accessToken = token;
		entitiesToReturn = new LinkedList<SocialNetworkEntity>();
	}
	
	public LinkedList<SocialNetworkEntity> getFriendsCheckins(){
		

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int month = Calendar.getInstance().get(Calendar.MONTH);
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		String ms = String.valueOf(month);
		if(month<10) ms = "0"+ms;
		String ds = String.valueOf(day);
		if(day<10) ds = "0"+ds;
		
		String version = String.valueOf(year)+ms+ds;
		
		URL url;
		try {
			url = new URL("https://api.foursquare.com/v2/checkins/recent?&oauth_token="+accessToken+"&v="+version);
			HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
			conn.setDoOutput(true);
		     
		    StringBuffer answer = new StringBuffer();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
		    String line;
		    
		     while ((line = reader.readLine()) != null) {
		            answer.append(line);
		     }
		     reader.close();
		    
		     String checkinAnswer = answer.toString();
		     
		     JSONObject checkinsObj =(JSONObject) JSONValue.parse(checkinAnswer);
		     JSONObject meta = (JSONObject) checkinsObj.get("meta");
		     
		     if(((Long)meta.get("code")).equals(Long.parseLong("200"))){
		    	 
		    	 JSONObject response = (JSONObject) checkinsObj.get("response");
		    	 JSONArray recent = (JSONArray) response.get("recent");
		    	 
		    	 if(recent==null) return entitiesToReturn;
		    	 
		    	 Iterator<JSONObject> recentIterator = (Iterator<JSONObject>) recent.iterator();
			     		    	 
			     while(recentIterator.hasNext()){
			   
			    	 JSONObject nextCheckin = recentIterator.next();
			    	 
			    	 FoursquareCheckin checkin = new FoursquareCheckin();
			    	 checkin.setID((String) nextCheckin.get("id"));
			    	 checkin.setDateFromUnixTimestamp((Long) nextCheckin.get("createdAt"));
			    	 
			    	 if(nextCheckin.get("shout")!=null)
			    		 checkin.setShout((String) nextCheckin.get("shout"));
			    	 
			    	 JSONObject user = (JSONObject) nextCheckin.get("user");
			    	 
			    	 checkin.setUserID((String) user.get("id"));
			    
			    	 //TODO: useful but i need to find a sample json first
			    	 
			    	 /*JSONArray with = (JSONArray) nextCheckin.get("with");
			    	 if(with!=null){
				    	 Iterator<JSONObject> withIterator = (Iterator<JSONObject>) with.iterator();
				    	 
				    	 while(withIterator.hasNext()){
				    		 JSONObject nextUser = withIterator.next();
				    		 System.out.println("user id: "+(String) nextUser.get("id"));
					    	 System.out.println("user first name: "+(String) nextUser.get("firstName"));
					    	 System.out.println("user last name: "+(String) nextUser.get("lastName"));
				    	 }
			    	 }
			    	 */
			    	 
			    	 FoursquareVenue fvenue = new FoursquareVenue();
			    	 JSONObject venue = (JSONObject) nextCheckin.get("venue");
			    	 
			    	 fvenue.setID((String) venue.get("id"));
			    	 fvenue.setName((String) venue.get("name"));
			    	 
			    	 JSONObject location = (JSONObject) venue.get("location");
			    	 
			    	 fvenue.setLatitude((Double) location.get("lat"));
			    	 fvenue.setLongitude((Double) location.get("lng"));
			    	 
			    	 if(location.get("address")!=null)
			    		 fvenue.setAddress((String) location.get("address"));
			    	 
			    	 if(location.get("city")!=null)
			    		 fvenue.setCity((String) location.get("city"));
			    	 
			    	 if(location.get("state")!=null)
			    		 fvenue.setState((String) location.get("state"));
			    	 
			    	 if(location.get("country")!=null)
			    		 fvenue.setCountry((String) location.get("country"));
			    	 
			    	 checkin.setVenue(fvenue);

			    	 if(nextCheckin.get("likes")!=null){
				    	 JSONObject checkinLikes = (JSONObject) nextCheckin.get("likes");
				    	 
				    	
				    	 if(checkinLikes.get("groups")!=null){
					    	 JSONArray likeGroups = (JSONArray) checkinLikes.get("groups");
					    	 
					    	 Iterator<JSONObject> groupsIterator = (Iterator<JSONObject>) likeGroups.iterator();
					    	 LinkedList<String> likes = new LinkedList<String>();
					    	 
					    	 while(groupsIterator.hasNext()){
					    		 
					    		 JSONObject nextGroup = groupsIterator.next();
					    		 
					    		 if(nextGroup.get("items")!=null){
						    		 JSONArray groupItems = (JSONArray) nextGroup.get("items");
						    		 Iterator<JSONObject> itemsIterator = (Iterator<JSONObject>) groupItems.iterator();
						    		 
						    		 while(itemsIterator.hasNext()){
						    			 JSONObject nextItem = itemsIterator.next();
						    			 likes.add((String) nextItem.get("id"));
						    		 }
					    		 }
					    	 }
					    	 checkin.setLikes(likes);
				    	 }
			    	 }
			    	//TODO: (to be decided)More information can be included in the checkin struct. 
			    	 /*JSONArray categories = (JSONArray) venue.get("categories");
			    	 
			    	 
			    	 
			    	 Iterator<JSONObject> categoriesIterator = (Iterator<JSONObject>) categories.iterator();
			    	 
			    	 while(categoriesIterator.hasNext()){
			    		 JSONObject nextCategory = categoriesIterator.next();
			    		 System.out.println("category name: "+(String) nextCategory.get("name"));
			    	 }
			    	 
			    	 JSONObject stats = (JSONObject) venue.get("stats");
			    	 System.out.println("checkins count: "+(Long) stats.get("checkinsCount"));
			    	 System.out.println("users count: "+(Long) stats.get("usersCount"));
			    	 System.out.println("tipCount"+(Long) stats.get("tipCount"));
			    	 
			    	 JSONObject event = (JSONObject) venue.get("event");
			    	 if(event!=null){
			    		 System.out.println("event name: "+(String) event.get("name"));
			    		 JSONArray eventCategories = (JSONArray) event.get("categories");
			    		 Iterator<JSONObject> eventCategoriesIterator = (Iterator<JSONObject>) eventCategories.iterator();
				    	 
				    	 while(eventCategoriesIterator.hasNext()){
				    		 JSONObject nextCategory = categoriesIterator.next();
				    		 System.out.println("category name: "+(String) nextCategory.get("name"));
				    	 }
			    	 }*/
			    	 
			    	 this.entitiesToReturn.add(checkin);
			     }
			     
		     }
		     return this.entitiesToReturn;
		     
		     
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*public static class PeriodicFsqCollector extends TimerTask{
		
		public void run() {
			User users = new User();
			List<HashMap<String, String>> results = users.read();
			
			int rsize=results.size(); 
			for(int i=0;i<rsize;i++){
				SNDetails socialNetworkInfo = new SNDetails();
				socialNetworkInfo.setSnName("foursquare");
				socialNetworkInfo.setUserId(Integer.parseInt(results.get(i).get("id")));
				List<HashMap<String, String>> snresults = socialNetworkInfo.read();
				
				if(!snresults.isEmpty()){
					String token = snresults.get(0).get("sn_token");
					FriendsCheckins fc = new FriendsCheckins(token);
					fc.getFriendsCheckins();
				}
			}			
		}
		
	}
	
	
	public static void main(String[] args) {
		
		Timer timer1 = new Timer("PeriodicFsqCollector");
		PeriodicFsqCollector collector = new PeriodicFsqCollector();
		timer1.scheduleAtFixedRate(collector, 0, 5*60*1000);
	}*/

}
