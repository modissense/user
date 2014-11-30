package facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import snetworks.SocialNetworkEntity;
import facebook.entities.FbCheckinObject;
import facebook.entities.FbComments;
import facebook.entities.FbPOI;
import facebook.entities.FbStatus;

/**
 * crawls the facebook profile of a specified account.
 * @author giagulei
 *
 */
public class FbCrawler {

	private String facebookID;
	private String accessToken;
	private String since;
	private String until;
	
	private LinkedList<SocialNetworkEntity> entitiesToReturn;
	
	public FbCrawler(String fbid,String accessToken,String since, String until){
		this.facebookID = fbid;
		this.accessToken = accessToken;
		this.since = since;
		this.until = until;
		entitiesToReturn = new LinkedList<SocialNetworkEntity>();
	}
	
	/**
	 * crawls facebook profile for a specified account and returns a list with all checkin and
	 * status updates.
	 * 
	 * sample fb api call:
	 * https://graph.facebook.com/PAGEGRAPHOBJECTID/posts/&since=2011-07-01&until=2012-08-08&access_token=ACCESSTOKEN&limit=100
	 * @return
	 */
	public LinkedList<SocialNetworkEntity> getFbFeed(){
	
		URL url;
		try {
			if(since!=null)
				url = new URL("https://graph.facebook.com/"+facebookID+"/home?since="+since+"&until="+until+"&access_token="+
						accessToken+"&limit=100");
			else
				url = new URL("https://graph.facebook.com/"+facebookID+"/home?until="+until+"&access_token="+
						accessToken+"&limit=1000");
						
			HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
			conn.setDoOutput(true);
		     
		    StringBuffer answer = new StringBuffer();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
		    String line;
		    
		     while ((line = reader.readLine()) != null) {
		            answer.append(line);
		     }
		     reader.close();
		    
		     String userAnswer = answer.toString();
		     
		     JSONObject userObj =(JSONObject) JSONValue.parse(userAnswer);
		     JSONArray data = (JSONArray) userObj.get("data");
		     
		     Iterator<JSONObject> jsonArrayIterator = (Iterator<JSONObject>) data.iterator();
		     		     
		     while(jsonArrayIterator.hasNext()){
		    	 JSONObject nextObject = jsonArrayIterator.next();
		    	 /*
		    	  *  Checkins are not the only kind of entities which contain "place" objects. However,
		    	  *  since there is a place information which indicates that the user had been there, we
		    	  *  treat all these entities as checkins. Thus, a photo upload with tagged location, is treated
		    	  *  by modissense as a check-in.
		    	  */
		    	 if(nextObject.get("place")!=null){
		    		 
		    		 FbCheckinObject checkin = new FbCheckinObject((String) nextObject.get("id"));
		    		 //PostgresPOI newPoi = new PostgresPOI();
		    		 //TODO:add ID
		    		 
		    		 // in a photo for example message may be null.
		    		 if(nextObject.get("message")!=null){
		    			 checkin.setMessage((String) nextObject.get("message"));
		    		 }
		    		 
		    		 JSONObject user = (JSONObject) nextObject.get("from");
		    		 checkin.setUserID((String) user.get("id"));
		    		 
		    		 JSONObject moreUsersWrapper = (JSONObject) nextObject.get("with_tags");
		    		 JSONArray moreUsers;
		    		 LinkedList<String> withUser = new LinkedList<String>();
		    		 Iterator<JSONObject> moreUsersIterator;
		    		 if(moreUsersWrapper!=null){
			    		 moreUsers = (JSONArray) moreUsersWrapper.get("data");
			    		 
			    		 moreUsersIterator = (Iterator<JSONObject>) moreUsers.iterator();
			    		 while(moreUsersIterator.hasNext()){
			    			 JSONObject nextUser = moreUsersIterator.next();
			    			 withUser.add((String) nextUser.get("id"));
			    		 }
		    		 }
		    		 if(withUser.size()>0)
		    			 checkin.setWithUsers(withUser);
		    		 
		    		 checkin.setDateFromFacebookString((String) nextObject.get("created_time"));
		    		 
		    		 JSONObject place = (JSONObject) nextObject.get("place");
		    		 FbPOI fbplace = new FbPOI((String)place.get("id"));
		    		 
		    		 fbplace.setName((String)place.get("name"));
		    		 
		    		 /*
		    		  * logika kai null na einai kapoio apo ta parakatw, apla 8a 8etei null
		    		  * to antistoixo pedio tou fbpoi k apla den 8a to kanei serialize, alla to be tested
		    		  */
		    		 try{
		    			 JSONObject location = (JSONObject) place.get("location");
		    			 fbplace.setStreet((String) location.get("street"));
			    		 fbplace.setCity((String) location.get("city"));
			    		 fbplace.setState((String) location.get("state"));
			    		 fbplace.setCountry((String) location.get("country"));
			    		 fbplace.setZip((String) location.get("zip"));
			    		 fbplace.setLatitude((Double) location.get("latitude"));
			    		 fbplace.setLongitude((Double) location.get("longitude"));
		    		 }catch (java.lang.ClassCastException e) {
						System.out.println("Exception caught");
						System.out.println((String) place.get("location"));
						continue;
						//TODO: to location mporei na einai mono ena string kai na mhn exei \
						//olh thn plhroforia.
					}
		    		  
		    		 checkin.setFbPOI(fbplace);
		    		 
		    		 moreUsersWrapper = (JSONObject) nextObject.get("likes");
		    		 LinkedList<String> usersLikes = new LinkedList<String>();
		    		 if(moreUsersWrapper!=null){
			    		 moreUsers = (JSONArray) moreUsersWrapper.get("data");
			    		 
			    		 moreUsersIterator = (Iterator<JSONObject>) moreUsers.iterator();
			    		 while(moreUsersIterator.hasNext()){
			    			 JSONObject nextUser = moreUsersIterator.next();
			    			 usersLikes.add((String) nextUser.get("id"));
			    		 }
		    		 }
		    		 checkin.setUserLikes(usersLikes);
		    		 
		    		 LinkedList<FbComments> comments = new LinkedList<FbComments>();
		    		 moreUsersWrapper = (JSONObject) nextObject.get("comments");
		    		 if(moreUsersWrapper!=null){
			    		 moreUsers = (JSONArray) moreUsersWrapper.get("data");
			    		 
			    		 moreUsersIterator = (Iterator<JSONObject>) moreUsers.iterator();
			    		 while(moreUsersIterator.hasNext()){
			    			 
			    			 JSONObject nextComment = moreUsersIterator.next();
			    			 JSONObject commentuser = (JSONObject) nextComment.get("from");
			    			 FbComments comment = new FbComments((String) commentuser.get("id"), (String) nextComment.get("message"));
			    			 comments.add(comment);
			    		 }
		    		 }
		    		 checkin.setComments(comments);
		    		 System.out.println(checkin.toString());
		    		 this.entitiesToReturn.add(checkin);
		    	 }else{
			    	 String type = (String) nextObject.get("type");
			    	 String statusType = (String) nextObject.get("status_type");
			    	 if(type!=null&&type.equals("status")&&statusType!=null&&(statusType.equals("wall_post")
			    			 ||statusType.equals("mobile_status_update"))){
			    		 
			    		 FbStatus status = new FbStatus((String) nextObject.get("id"));
			    		 
			    		 JSONObject user = (JSONObject) nextObject.get("from");
			    		 
			    		 status.setUser((String) user.get("id"));
			    		 
			    		 status.setMessage((String) nextObject.get("message"));
			    		 System.out.println(status.toString());
			    		 this.entitiesToReturn.add(status);
			    		 
			    	 }
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
	
	
	
//	public static void main(String[] args) {
//		/*
//		 *  Every five minutes the PeriodicFbCollector is activated
//		 */
//		Timer timer1 = new Timer("PeriodicFbCollector");
//		PeriodicFbCollector collector = new PeriodicFbCollector(1);
//		timer1.scheduleAtFixedRate(collector, 0, 5*60*1000);
//	}

}
