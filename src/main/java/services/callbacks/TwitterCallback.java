package services.callbacks;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import datastore.client.PersistentHashMapClient;
import entities.SNDetails;
import entities.User;
import services.users.Register;
import supportingClasses.RunningWebService;
import supportingClasses.Utils;
import twitterlib.Twitter;



/**
 * Servlet implementation class CallbackServlet
 * 
 * 
 * @author Giagkos Mytilinis
 */
public class TwitterCallback extends RunningWebService {
	
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public TwitterCallback() {
        super();
        serviceName = "Twitter Callback: ";
        DEBUG = true;
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	
		HttpSession session = request.getSession();
		
		Twitter twitter = new Twitter(Register.twitterCallback);
		twitter.getTwitterTokens().setRequestToken((String)session.getAttribute("requestToken"));
		twitter.getTwitterTokens().setRequestTokenSecret((String)session.getAttribute("requestTokenSecret"));
		twitter.getAccessTokens((String)request.getParameter("oauth_verifier"));
		
		session.setAttribute("accessToken", twitter.getAccessToken());
		session.setAttribute("accessTokenSecret", twitter.getAccessTokenSecret());
		twitterlib.User u = new twitterlib.User(twitter.getUserID(), twitter.getAccessToken(),twitter.getAccessTokenSecret());
		try {
			u.getUserProfileInfo();
			System.out.println("User: "+u.getUserName());
			System.out.println("Img: "+u.getProfileImg());
			
			/*
			 * Search in the DB according to social network and userID. This search can return
			 * either one or none results.
			 */
			
			SNDetails socialNetworkInfo = new SNDetails();
			socialNetworkInfo.setSnName("twitter");
			socialNetworkInfo.setSnIdentifier(u.getUserID());
					
			List<HashMap<String, String>> results = socialNetworkInfo.read();
			
			String token = null; 
			
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			
			if(results.size()==0){
				
				 /*  If there are no results, we can either add a new Modissense account
				  *  or connect a social network account to an existing one.
				  */
				 
				socialNetworkInfo.setSnToken(twitter.getAccessToken());
				socialNetworkInfo.setSnTokenSecret(twitter.getAccessTokenSecret());
				
				if(!((String)session.getAttribute("uidtoken")).equals("null")){
						
					if(DEBUG)System.out.println(serviceName+"Connect Twitter account to existing Modissense user.");
					
					token = (String) session.getAttribute("uidtoken");
					if(DEBUG)System.out.println(serviceName+"user token = "+token);
					
					int usid = hashClient.getUserId(token);
						
					socialNetworkInfo.setUserId(usid);
					if(socialNetworkInfo.create()){
						System.out.println(serviceName+"sn_list entry created successfully.");
					}
					else
						System.out.println(serviceName+"sn_list entry failed");
				}else{
					if(DEBUG)System.out.println(serviceName+"Create new Modissense user.");
					
					User newUser = new User();
					newUser.setUsername(u.getUserName());
					newUser.setPrimarySN("twitter");
					newUser.setImageURL(u.getProfileImg());
					if(newUser.create()){
						if(DEBUG)System.out.println(serviceName+"user entry created successfully.");
						User theSameUser = new User(u.getUserName());
						socialNetworkInfo.setUserId(theSameUser.getId());
						
						token = Utils.randomStringGenerator(32); 
						if(DEBUG)System.out.println(serviceName+"New user acquired token = "+token);
						hashClient.add(token, theSameUser.getId());
						
						if(socialNetworkInfo.create()){
							System.out.println(serviceName+"sn_list entry created successfully.");
						}
						else
							System.out.println(serviceName+"sn_list entry failed");
					}
					else
						System.out.println(serviceName+"user entry failed.");
				}
			} else{
				
				 /* In the DB there is a twitter account with the specific user id. Update twitter access token
				  * as it may have expired and check if there is a valid session token for this user.
				  */
				 
				HashMap<String, String> tokensToUpdate = new HashMap<String, String>();
				tokensToUpdate.put("sn_token", "'"+twitter.getAccessToken()+"'");
				tokensToUpdate.put("sn_token_secret", "'"+twitter.getAccessTokenSecret()+"'");
				socialNetworkInfo.update(tokensToUpdate);
				
				if(hashClient.getSession(socialNetworkInfo.getUserId()).equals("null")){
						
					token = Utils.randomStringGenerator(32); 
					hashClient.add(token, socialNetworkInfo.getUserId());
					if(DEBUG)System.out.println(serviceName+"Existing user got new token. Token = "+token);
				}else{
					token=hashClient.getSession(socialNetworkInfo.getUserId());
					if(DEBUG)System.out.println(serviceName+"Existing user logged in. Valid token= "+token);
				}
			}
			
			
		    response.sendRedirect(response.encodeRedirectURL((String)session.getAttribute("callback")+"&uid="+token));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
			
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}