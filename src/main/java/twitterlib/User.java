package twitterlib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import supportingClasses.OAuth;

public class User extends TwitterConnector {
	
	private static final String twitterUsersShowEndpoint = "https://api.twitter.com/1.1/users/show.json";
	private String userID;
	private String screenName;
	private String userName;
	
	private String profileImg;
	private String requestURL;
	
	//private String accessToken;
	
	private HashMap<String,String> params;
	
	private Long nextCursor=-1L;

	public User(String userID,String accessToken,String accessTokenSecret){
		super();
		this.userID = userID;
		setEndpointURL(twitterUsersShowEndpoint);
		setHttpMethod("GET");
		
		params = new HashMap<>();
		params.put("user_id", userID);
		params.put("include_entities", "true");
		
		setGETParams(params);
		
		this.auth.setOauthToken(accessToken);
		this.auth.setOauthTokenSecret(accessTokenSecret);
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getProfileImg() {
		return profileImg;
	}

	public void setProfileImg(String profileImg) {
		this.profileImg = profileImg;
	}

	public Long getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(Long nextCursor) {
		this.nextCursor = nextCursor;
	}

	public void getUserProfileInfo() throws Exception{
		String profileJSON = this.makeTwitterRequest(true);
		JSONObject profileObj =(JSONObject) JSONValue.parse(profileJSON);
		
		userName = (String) profileObj.get("name");
		profileImg = (String) profileObj.get("profile_image_url");
		
		
	}
	
	public String getFriends(long cursor) throws Exception{

		setEndpointURL("https://api.twitter.com/1.1/friends/ids.json");
		params=new HashMap<>();
		params.put("user_id", this.userID);
		params.put("cursor", Long.toString(cursor));
		params.put("count", "100");
		setGETParams(params);
		String ids = this.makeTwitterRequest(true);
		
		JSONObject friendIdsObj =(JSONObject) JSONValue.parse(ids);
		JSONArray idsArray = (JSONArray) friendIdsObj.get("ids");
		Iterator<Long> jsonArrayIterator = (Iterator<Long>) idsArray.iterator();
	    
		String friends="";
	    while(jsonArrayIterator.hasNext()){
	    	String nextFriend = Long.toString(jsonArrayIterator.next());
	    	friends+=nextFriend+",";
	    }
	    friends=friends.substring(0, friends.length()-1);
	    
	    setEndpointURL("https://api.twitter.com/1.1/users/lookup.json");
	    params=new HashMap<>();
	    params.put("user_id", friends);
	    params.put("include_entities", "true");
	    setGETParams(params);
	    String fInfo = this.makeTwitterRequest(true);
		System.out.println("twitter friends info = "+fInfo);
		nextCursor = (Long) friendIdsObj.get("next_cursor");
		
		return fInfo;
	}
	
	public void postStatus(String message) throws Exception{
		setEndpointURL("https://api.twitter.com/1.1/statuses/update.json");
		setHttpMethod("POST");
		params = new HashMap<>();
		params.put("status", message);
		setPostParameters(params);
		setGETParams(null);
		String updateStatusResponse = this.makeTwitterRequest(true);
	}
	
}
