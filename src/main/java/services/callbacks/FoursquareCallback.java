package services.callbacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import datastore.client.PersistentHashMapClient;
import services.users.Register;
import session.SessionManagementIM;
import supportingClasses.RunningWebService;
import supportingClasses.Utils;
import entities.SNDetails;
import entities.User;
/**/
/**
 * Servlet implementation class CallbackServlet
 * 
 * @author Giagkos Mytilinis
 */

public class FoursquareCallback extends RunningWebService {
	
private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FoursquareCallback() {
        super();
        serviceName = "Foursquare Callback: ";
        DEBUG = true;
    }

    
    private String getAccessToken(String code) throws IOException{
    	URL url = new URL("https://foursquare.com/oauth2/access_token?client_id="+Register.fsqClientId+
    			"&client_secret="+Register.fsqClientSecret+"&grant_type=authorization_code"+
    			"&redirect_uri="+Register.fsqCallback+"&code="+code);
  		HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
  		conn.setDoOutput(true);
  	     
  	    StringBuffer answer = new StringBuffer();
  	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  	    String line;
  	    
  	     while ((line = reader.readLine()) != null) {
  	            answer.append(line);
  	     }
  	     reader.close();
  	     String tokenAnswer = answer.toString();
  	     
  	    JSONObject tokenObj =(JSONObject) JSONValue.parse(tokenAnswer);
	    String accessToken = (String) tokenObj.get("access_token");
	    return accessToken;
    }
    
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
//        FoursquareApi foursquareApi = new FoursquareApi(Register.getFoursquareClientID()
//        		,Register.getFoursquareClientSecret(),Register.getFoursquareCallback());

        String code=request.getParameter("code");
        
        HttpSession session = request.getSession();
        
     //   try {

//              foursquareApi.authenticateCode(code);
//              String accessToken = foursquareApi.getOAuthToken();
//               
//              Result<CompleteUser> result = foursquareApi.user("self");
//              if (result.getMeta().getCode() == 200) {
//                  CompleteUser fUser = result.getResult();
//                  if(DEBUG)System.out.println(serviceName+"foursquare user "+fUser.getId());
                 
        	
        		String accessToken = getAccessToken(code);
        		
        		URL url = new URL("https://api.foursquare.com/v2/users/self?oauth_token="+accessToken+"&v=20130815"+
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
        		JSONObject responseObj = (JSONObject) userObj.get("response");
        		JSONObject user = (JSONObject) responseObj.get("user"); 
        		
        		String fUid = (String) user.get("id");
        		String fFirstName = (String) user.get("firstName");
        		String fLastName = (String) user.get("lastName");
        		
        		JSONObject friendPicture = (JSONObject) user.get("photo");
   	    	 	String photoPrefix = (String) friendPicture.get("prefix");
   	    	 	String photoSuffix = (String) friendPicture.get("suffix");
   	    	 	String pictureURL = photoPrefix+"300x300"+photoSuffix;
        	
                /*
      			 * Search in the DB according to social network and userID. This search can return
      			 * either one or none results.
      			 */

                SNDetails socialNetworkInfo = new SNDetails();
      			socialNetworkInfo.setSnName("foursquare");
      			//socialNetworkInfo.setSnIdentifier(fUser.getId());
      			socialNetworkInfo.setSnIdentifier(fUid);    			
      			
      			List<HashMap<String, String>> results = socialNetworkInfo.read();
      			

    			String token = null;
    			PersistentHashMapClient hashClient = new PersistentHashMapClient();
    			
    			if(results.size()==0){
    				
    				/*
    				 *  If there are no results, we can either add a new Modissense account
    				 *  or connect a social network account to an existing one.
    				 */
    				
    				socialNetworkInfo.setSnToken(accessToken);
    				
    				if(!((String)session.getAttribute("uidtoken")).equals("null")){
    					
    					if(DEBUG)System.out.println(serviceName+"Connect Foursquare account to existing Modissense user.");
    					
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
    					String foursquareName="";
    					if(fFirstName!=null)
    						foursquareName+=fFirstName;
    					if(fLastName!=null)
    						foursquareName+=" "+fLastName;
    					newUser.setUsername(foursquareName);
    					newUser.setPrimarySN("foursquare");
    					newUser.setImageURL(pictureURL);
    					if(newUser.create()){
    						if(DEBUG)System.out.println(serviceName+"user entry created successfully.");
    						
    						
    						User theSameUser = new User(foursquareName);
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
    				/*
    				 * In the DB there is a twitter account with the specific user id. Update twitter access token
    				 * as it may have expired and check if there is a valid session token for this user.
    				 */
    				
	    			HashMap<String, String> tokensToUpdate = new HashMap<String, String>();
					tokensToUpdate.put("sn_token", "'"+accessToken+"'");
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

//              }
//              else {
//                  System.out.println(serviceName+"Error occured: ");
//                  System.out.println(serviceName+"  code: " + result.getMeta().getCode());
//                  System.out.println(serviceName+"  type: " + result.getMeta().getErrorType());
//                  System.out.println(serviceName+"  detail: " + result.getMeta().getErrorDetail());
//              }
              
//        } catch (FoursquareApiException e) {
//              e.printStackTrace();
//        }		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
