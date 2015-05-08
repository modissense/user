package services.users;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import modisusers.FriendIDs;
import modisusers.FriendsInfo;
import modisusers.SNFriendsGetter;
import gr.ntua.ece.cslab.modissense.queries.containers.UserIdStruct;

import org.apache.hadoop.hbase.client.Put;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import session.SessionManagementIM;
import snstorage.UserGraph;
import supportingClasses.Utils;
//import twitter4j.PagableResponseList;
//import twitter4j.Twitter;
//import twitter4j.TwitterFactory;
//import twitter4j.TwitterException;
//import twitter4j.conf.ConfigurationBuilder;
import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.SNDetails;
import entities.User;
//import fi.foyt.foursquare.api.FoursquareApi;
//import fi.foyt.foursquare.api.FoursquareApiException;
//import fi.foyt.foursquare.api.Result;
//import fi.foyt.foursquare.api.entities.CompactUser;
//import fi.foyt.foursquare.api.entities.CompleteUser;
//import fi.foyt.foursquare.api.entities.UserGroup;
//import fi.foyt.foursquare.api.entities.UserGroups;

/**
 * Servlet implementation class UserInfo
 * returns a modissense userObject in json format
 * 
 * @author Giagkos Mytilinis
 */

@WebServlet("/UserInfo")
public class UserInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserInfo() {
        super();
        this.description = new HTMLDescription("Get User Object");
        this.description.addParameter("String", "token");
        this.description.addParameter("String", "format");
        this.description.addParameter("String", "callback");
        this.description.setReturnValue("json");
        this.description.setDescription("Web service used to get the object of a user");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("token")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			
			HttpSession session = request.getSession();
			String token = (String) request.getParameter("token");
			
			System.out.println("token = "+token);
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			
			System.out.println("uid = "+usid);
			String jsonToSend = "{\"user\":{\"username\":\"";
			
			User loggedInUser = new User();
			loggedInUser.setId(usid);
			List<HashMap<String, String>> userInfo = loggedInUser.read();
			if(userInfo.size()==1){
				jsonToSend+=userInfo.get(0).get("username")+"\"";
				
				if(userInfo.get(0).get("img_url")!=null)
					jsonToSend+=",\"image\":\""+userInfo.get(0).get("img_url")+"\"";
				else
					jsonToSend+=",\"image\":\"null\"";
				
				if(userInfo.get(0).get("primary_sn")!=null)
					jsonToSend+=",\"primary_sn\":\""+userInfo.get(0).get("primary_sn")+"\"";
				else
					jsonToSend+=",\"primary_sn\":\"null\"";
				
				jsonToSend+=",\"connections\":[";
			}else{
				System.out.println("ERROR: strange error on UserInfo");
			}
			
			SNDetails loggedInUserSN = new SNDetails();
			loggedInUserSN.setUserId(usid);
			
			List<HashMap<String, String>> results = loggedInUserSN.read();
			int rSize= results.size();
			
			
		
			for(int i=0;i<rSize;i++){
				
				UserIdStruct uStruct = null;
				
				if(results.get(i).get("sn_name").equals("twitter")){
					
					jsonToSend+="{\"network\":\"twitter\",\"friends\":[";
					uStruct = new UserIdStruct('t',Long.parseLong(results.get(i).get("sn_identifier")));
					
				}else if(results.get(i).get("sn_name").equals("facebook")){
					
					jsonToSend+="{\"network\":\"facebook\",\"friends\":[";
					uStruct = new UserIdStruct('F',Long.parseLong(results.get(i).get("sn_identifier")));
					System.out.println("UserInfo for user: "+uStruct.getC()+uStruct.getId());
					
				}else{
					jsonToSend+="{\"network\":\"foursquare\",\"friends\":[";
					uStruct = new UserIdStruct('f',Long.parseLong(results.get(i).get("sn_identifier")));
				}
				
				UserGraph hbaseUserTable = new UserGraph();
				FriendsInfo userFriends = hbaseUserTable.getUserFriendsInfo(uStruct);
				
				LinkedList<String> names;
				if(userFriends!=null&&!(names = userFriends.getFriendNames()).isEmpty()){
					System.out.println("FRIENDS FETCHED FROM HBase");
					// if information exists in HBase, retrieve it from there.
					
					//LinkedList<String> names = userFriends.getFriendNames();
					LinkedList<String> urls = userFriends.getFriendsImages();
					FriendIDs fIds = hbaseUserTable.getUserFriendsIDs(uStruct);
					LinkedList<byte[]> friendIds = fIds.getFriends();
					
					System.out.println("User Info Friends:");
					for(int n=0;n<names.size();n++){
						UserIdStruct snUser = new UserIdStruct();
						snUser.parseBytes(friendIds.get(n));
						
						System.out.print(snUser.getC()+snUser.getId()+",");
						
						jsonToSend+="{\"id\":\""+snUser.getId()+"\",\"name\":\""+names.get(n)+"\",\"url\":\"" +
				    	 		urls.get(n)+"\"}";
					}
					System.out.println();
				}else{
					System.out.println("FRIENDS FEETCHED FROM NETWORK");
					// otherwise, fetch available information from social network and persist it in database
					//System.out.println("MPIKA EDW KAI VRIKA FILOUS - "+results.get(i).get("sn_name"));
					
					jsonToSend+=retrieveFriendsFromSN(results.get(i).get("sn_name"), results.get(i).get("sn_identifier"), 
							results.get(i).get("sn_token"),results.get(i).get("sn_token_secret"));
				}
			    jsonToSend = jsonToSend.replaceAll("\\}\\{", "},{");
			    jsonToSend+="]}";
				
			
			}
			
			jsonToSend+="]}}";
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-Type", "application/json");
			response.setHeader("charset", "utf-8");
			PrintWriter out = response.getWriter();
			
			String format = request.getParameter("format");
			if(format.equals("jsonp")){
				String callback = request.getParameter("callback");
				out.write(callback+"("+jsonToSend+")");
			}
			else if(format.equals("json"))
				out.write(jsonToSend);
			
		} 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// does nothing for now
	}

	public String retrieveFriendsFromSN(String network,String snid,String token,String tokenSecret){
		LinkedList<Put> hbasePuts = new LinkedList<>();
		SNFriendsGetter snGetter;
		String jsonToReturn="";
		
		char FACEBOOK = 'F';
		char FOURSQUARE = 'f';
		char TWITTER = 't';
		
		if(network.equals("facebook")){
			snGetter = new SNFriendsGetter(snid, token);
			snGetter.setSocialNetwork(FACEBOOK);
			try {
				snGetter.getFacebookFriends();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}else if(network.equals("foursquare")){
			snGetter = new SNFriendsGetter(snid, token);
			snGetter.setSocialNetwork(FOURSQUARE);
			try {
				snGetter.getFoursquareFriends();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else{
			snGetter = new SNFriendsGetter(snid, token,tokenSecret);
			snGetter.setSocialNetwork(TWITTER);
			try {
				snGetter.getTwitterFriends();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		LinkedList<String> fids = snGetter.getFriendIDs();
		HashMap<String, String> fNames = snGetter.getFriendNames();
		HashMap<String, String> fImages = snGetter.getFriendImages();
		
		FriendIDs newFriend = new FriendIDs(snGetter.getSocialNetwork(),Long.parseLong(snid));
		FriendsInfo fInfo = new FriendsInfo(snGetter.getSocialNetwork(), Long.parseLong(snid));
		for(String id:fids){
			newFriend.addFriend(Long.parseLong(id));
			fInfo.addFriendName(fNames.get(id));
			fInfo.addFriendImage(fImages.get(id));
			
			jsonToReturn+="{\"id\":\""+id+"\",\"name\":\""+fNames.get(id)+"\",\"url\":\"" +
					fImages.get(id)+"\"}";
			
		}
		
		UserGraph usersTable = new UserGraph();
		hbasePuts.add(usersTable.storeFriend(newFriend));
		hbasePuts.add(usersTable.storeFriendInfo(fInfo));
		
		try {
			usersTable.commitUpdates(hbasePuts);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Updates are commited.");

		return jsonToReturn;
	}
	
	
	
}
