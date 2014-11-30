package services.callbacks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
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
import supportingClasses.OAuth;
import supportingClasses.RunningWebService;
import supportingClasses.Utils;
import entities.SNDetails;
import entities.User;

/**
 * 
 * Servlet implementation class CallbackServlet
 * 
 * @author Giagkos Mytilinis
 */

public class FbCallback extends RunningWebService {
	
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FbCallback() {
        super();
        serviceName = "Facebook Callback: ";
        DEBUG = true;
    }

	/**
	 * @throws IOException 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
    private String getBigProfilePic(String accessToken) throws IOException{
    	URL url = new URL("https://graph.facebook.com/me/picture?type=large&redirect=false&access_token="+accessToken);
    	System.out.println(url);
    	HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
    	conn.setDoOutput(true);
     
    	StringBuffer answer = new StringBuffer();
    	BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    	String line;
    	while ((line = reader.readLine()) != null) {
            	answer.append(line);
    	}
    	reader.close();
    	String pictureAnswer = answer.toString();
    	System.out.println(pictureAnswer);
    	JSONObject pictureObj =(JSONObject) JSONValue.parse(pictureAnswer);
    	JSONObject dataObj = (JSONObject) pictureObj.get("data");
	    String picUrl = (String) dataObj.get("url");
	    System.out.println(picUrl);
	    return picUrl;
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession session = request.getSession();
		
		String code = request.getParameter("code");
		
		if(DEBUG){
			System.out.println(serviceName+"-----Parameters-----");
			Enumeration<String> enm = request.getParameterNames();
			while(enm.hasMoreElements()){
				String pname = enm.nextElement();
				System.out.print(serviceName+"param: "+pname+"\n");
				if(request.getParameter(pname)!=null)
					System.out.println(serviceName+"value: "+request.getParameter(pname));
			}
		}
		
		if(((String) session.getAttribute("state")).equals(request.getParameter("state"))){
			 if(request.getParameter("error")!=null){
				 System.out.println(serviceName+"The user denied to authorize your app");
				 System.out.println(serviceName+request.getParameter("error_reason"));
				 System.out.println(serviceName+request.getParameter("error"));
				 System.out.println(serviceName+request.getParameter("error_description"));
			 }else{
				 URL url = new URL("https://graph.facebook.com/oauth/access_token?client_id="+Register.getFbAppID()+"&" +
							"redirect_uri="+OAuth.percentEncode(Register.getFbRedirectURI())+"&client_secret="+Register.getFbAppSecret() +
							"&code="+code);
				 HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
				 conn.setDoOutput(true);
			     
			     StringBuffer answer = new StringBuffer();
			     BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			     String line;
			     while ((line = reader.readLine()) != null) {
			            answer.append(line);
			     }
			     reader.close();
			   
			     String accessAnswer = answer.toString();
			     String [] answerParts = accessAnswer.split("=");
			     String accessToken = answerParts[1];
			     
			     if(DEBUG)System.out.println(serviceName+"fb access token = "+accessToken);
			     url = new URL("https://graph.facebook.com/me?access_token="+accessToken);
				 conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
				 conn.setDoOutput(true);
			     
			     answer = new StringBuffer();
			     reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			     
			     while ((line = reader.readLine()) != null) {
			            answer.append(line);
			     }
			     reader.close();
			    
			     String userAnswer = answer.toString();
			     
			     System.out.println(userAnswer);
			     
			     JSONObject userObj =(JSONObject) JSONValue.parse(userAnswer);
			     String userId = (String) userObj.get("id");
			     String name = (String) userObj.get("name");
			    // String username = (String) userObj.get("username");
			     //String email = (String) userObj.get("email");
			     
			     //System.out.println("USERNAME = "+username);
			     /*if(name==null)
			    	 name = email;*/
			     
			     /*
				  * Search in the DB according to social network and userID. This search can return
				  * either one or none results.
				  */
			     
			     SNDetails socialNetworkInfo = new SNDetails();
				 socialNetworkInfo.setSnName("facebook");
				 socialNetworkInfo.setSnIdentifier(userId);
					
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
					
							if(DEBUG)System.out.println(serviceName+"Connect Facebook account to existing Modissense user.");
							
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
		
							newUser.setUsername(name);
							newUser.setPrimarySN("facebook");
							newUser.setImageURL(this.getBigProfilePic(accessToken));
							if(newUser.create()){
								if(DEBUG)System.out.println(serviceName+"user entry created successfully.");
								User theSameUser = new User(name);
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
	    				 * In the DB there is a facebook account with the specific user id. Update facebook access token
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
			     
			 }
		}
	
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}

