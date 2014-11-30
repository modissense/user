package modisusers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import twitterlib.User;


/**
 * 
 * @author giagulei
 *
 * Gets user information from social networks
 */
public class SNFriendsGetter {
	
	private char socialNetwork;
	private String snID;
	private String snUname;
	private String snToken;
	private String snSecretToken;
	
	private LinkedList<String> friendIDs;
	private HashMap<String,String> friendNames;
	private HashMap<String,String> friendImages;
	
	
	private static final boolean DEBUG = false;

	public SNFriendsGetter(String snID,String token){
		this.snID = snID;
		this.snToken = token;
		friendIDs = new LinkedList<>();
		friendNames = new HashMap<>();
		friendImages = new HashMap<>();
	}
	
	public SNFriendsGetter(String snID,String token,String tokenSecret){
		this.snID = snID;
		this.snToken = token;
		friendIDs = new LinkedList<>();
		friendNames = new HashMap<>();
		friendImages = new HashMap<>();
		this.snSecretToken = tokenSecret;
	}
	
	public char getSocialNetwork(){
		return socialNetwork;
	}
	
	public void setSocialNetwork(char socialNetwork){
		this.socialNetwork = socialNetwork;
	}
	
	public String getSnID() {
		return snID;
	}

	public void setSnID(String snID) {
		this.snID = snID;
	}
	
	public String getSnToken() {
		return snToken;
	}

	public void setSnToken(String snToken) {
		this.snToken = snToken;
	}

	public String getSnSecretToken() {
		return snSecretToken;
	}

	public void setSnSecretToken(String snSecretToken) {
		this.snSecretToken = snSecretToken;
	}
	
	public HashMap<String, String> getFriendNames(){
		return friendNames;
	}
	
	public void setFriendNames(HashMap<String, String> names){
		friendNames = names;
	}
	
	public HashMap<String, String> getFriendImages(){
		return friendImages;
	}
	
	public void setFriendImages(HashMap<String, String> images){
		friendImages = images;
	}
	
	public LinkedList<String> getFriendIDs(){
		return friendIDs;
	}
	
	public void setFriendIDs(LinkedList<String> ids){
		friendIDs = ids;
	}

	public String getSnUname() {
		return snUname;
	}

	public void setSnUname(String snUname) {
		this.snUname = snUname;
	}

	public void getFacebookFriends() throws IOException{
		URL url = new URL("https://graph.facebook.com/"+snID+"/friends?fields=username,name,picture&access_token="+snToken);
		HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
		conn.setDoOutput(true);
	     
	    StringBuffer answer = new StringBuffer();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    
	     while ((line = reader.readLine()) != null) {
	            answer.append(line);
	     }
	     reader.close();
	     String userAnswer = answer.toString();
	     
	     if(DEBUG) System.out.println(userAnswer);
	     JSONObject userObj =(JSONObject) JSONValue.parse(userAnswer);
	     JSONArray data = (JSONArray) userObj.get("data");
	     Iterator<JSONObject> jsonArrayIterator = (Iterator<JSONObject>) data.iterator();
	     
	     if(DEBUG) System.out.println("Friends of: "+snID);
	     
	     while(jsonArrayIterator.hasNext()){
	    	 JSONObject nextFriend = jsonArrayIterator.next();
	    	 String id = (String) nextFriend.get("id");
	    	 String name = (String) nextFriend.get("name");
	    	 friendIDs.add(id);
	    	 friendNames.put(id,name);
	    	
	    	 JSONObject friendPicture = (JSONObject) nextFriend.get("picture");
	    	 JSONObject pictureData = (JSONObject) friendPicture.get("data");
	    	 String pictureURL = (String) pictureData.get("url");
	    	 
	    	 friendImages.put(id, pictureURL);
	    	 if(DEBUG) {
	    		System.out.println((String) nextFriend.get("name")+"--"+pictureURL);
	    	 }
	     }
	}
	
	public void getFoursquareFriends() throws IOException{
		URL url = new URL("https://api.foursquare.com/v2/users/self/friends?oauth_token="+snToken+"&v=20130815"+
			  "&afterTimestamp=1072918861");
		HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
		conn.setDoOutput(true);
	     
	    StringBuffer answer = new StringBuffer();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    
	     while ((line = reader.readLine()) != null) {
	            answer.append(line);
	     }
	     reader.close();
	     String userAnswer = answer.toString();
	     if(DEBUG) System.out.println(userAnswer);
	     
	     JSONObject userObj =(JSONObject) JSONValue.parse(userAnswer);
	     JSONObject response = (JSONObject) userObj.get("response");
	     JSONObject friendsObj = (JSONObject) response.get("friends");
	     
	     JSONArray friends = (JSONArray) friendsObj.get("items");
	     Iterator<JSONObject> jsonArrayIterator = (Iterator<JSONObject>) friends.iterator();
	     
	     if(DEBUG) System.out.println("Friends of: "+snID);
	     
	     while(jsonArrayIterator.hasNext()){
	    	 JSONObject nextFriend = jsonArrayIterator.next();
	    	 String id = (String) nextFriend.get("id");
	    	 String firstName = (String) nextFriend.get("firstName");
	    	 String lastName = (String) nextFriend.get("lastName");
	    	 friendIDs.add(id);
	    	 friendNames.put(id,firstName+" "+lastName);
	    	
	    	 JSONObject friendPicture = (JSONObject) nextFriend.get("photo");
	    	 String photoPrefix = (String) friendPicture.get("prefix");
	    	 String photoSuffix = (String) friendPicture.get("suffix");
	    	 String pictureURL = photoPrefix+"36x36"+photoSuffix;
	    	 friendImages.put(id, pictureURL);
	    	 
	    	 if(DEBUG) {
	    		System.out.println(firstName+" "+lastName+"----"+pictureURL);
	    	 }
	     }
        
	}
	
	public void getTwitterFriends() throws Exception{
		
		User twitterUser = new User(this.snID, this.snToken, this.snSecretToken);
		
		long cursor=-1;
		
		while(twitterUser.getNextCursor()!=0){
			String friends = twitterUser.getFriends(cursor);
			
			JSONArray friendsArray =(JSONArray) JSONValue.parse(friends);
		    Iterator<JSONObject> jsonArrayIterator = (Iterator<JSONObject>) friendsArray.iterator();
		     
		     while(jsonArrayIterator.hasNext()){
		    	 JSONObject nextFriend = jsonArrayIterator.next();
		    	 String id = (String) nextFriend.get("id_str");
		    	 String name = (String) nextFriend.get("name");
		    	 String img = (String) nextFriend.get("profile_image_url_https");
		    	 friendIDs.add(id);
		    	 friendImages.put(id, img);
		    	 friendNames.put(id, name);
		     }
		     cursor = twitterUser.getNextCursor();
		}
		
		
		
	}
	
	/**
	 * downloads image from url as a byte stream
	 */
	/*private byte[] downloadUrl(URL toDownload) {
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	    try {
	        byte[] chunk = new byte[4096];
	        int bytesRead;
	        InputStream stream = toDownload.openStream();

	        while ((bytesRead = stream.read(chunk)) > 0) {
	            outputStream.write(chunk, 0, bytesRead);
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }

	    return outputStream.toByteArray();
	}*/

	

}
