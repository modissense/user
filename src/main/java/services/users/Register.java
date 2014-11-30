package services.users;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import supportingClasses.OAuth;
import twitterlib.Twitter;
import twitterlib.TwitterConnector;
import description.HTMLDescription;
import description.ServiceDescription;
//import fi.foyt.foursquare.api.FoursquareApi;

/**
 * Servlet implementation class Register
 *
 * @author Giagkos Mytilinis
 *
 */

@WebServlet("/Register")
public class Register extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
	public static final String WEBSERVER_IP = "83.212.104.253";
	
	public static final String twitterConsumerToken = "xJkcRYwkcyfi6H7cc25JNQ";
	public static final String twitterConsumerSecret = "dzo4wR2zBn1gbhFgxhhFVcZHVe8UKMS4VwL1FqI0Cgk";
	
	public static final String twitterCallback = "https://"+WEBSERVER_IP+"/user/tcallback";
	
	public static final String fsqClientId = "PJB10MU45PJUL3VBHFCT4U33TNWY3C0S00TZ4SQE0QYHXWNM";
	public static final String fsqClientSecret = "YOCYLYFQ0M0PFUJIAEF5F25OTDYOEAFLU1GNMXRUVXQIPUG0";
	public static final String fsqCallback= "https://"+WEBSERVER_IP+"/user/fsqcallback";
	
	public static final String fbAppID = "639597272729984";
	public static final String fbRedirectUri = "https://"+WEBSERVER_IP+"/user/fbcallback";
	public static final String fbAppSecret = "ad3863c9da1454434841e1bc870bcc1e";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        description = new HTMLDescription("User Register");
        description.addParameter("String", "token");
        description.addParameter("String", "network");
        description.addParameter("String", "callback");
        description.setReturnValue("boolean");
        description.setDescription("This service is used to register a user to the ModisSense platform.");   
    }

    public static String getTwitterConsumerToken(){
    	return twitterConsumerToken;
    }
    
    public static String getTwitterConsumerSecret(){
    	return twitterConsumerSecret;
    }
    
    public static String getFoursquareClientID(){
    	return fsqClientId;
    }
    
    public static String getFoursquareClientSecret(){
    	return fsqClientSecret;
    }
    
    public static String getFoursquareCallback(){
    	return fsqCallback;
    }
    
    public static String getFbAppID(){
    	return fbAppID;
    }
    
    public static String getFbAppSecret(){
    	return fbAppSecret;
    }
    
    public static String getFbRedirectURI(){
    	return fbRedirectUri;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("token")==null || request.getParameter("network")==null || request.getParameter("callback")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			
			String userID = request.getParameter("token");
			String socialNetwork = request.getParameter("network");
			String callbackURL = request.getParameter("callback");
			
			HttpSession session = request.getSession();
			
			session.setAttribute("uidtoken",userID);
			session.setAttribute("callback", callbackURL);
			session.setAttribute("network", socialNetwork);
			
			if(socialNetwork.equals("twitter")){
				/*ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setDebugEnabled(true)
				  .setIncludeEntitiesEnabled(true)
				  .setUseSSL(true);
				
				Twitter twitter = new TwitterFactory(cb.build()).getInstance();
				
				twitter.setOAuthConsumer(twitterConsumerToken, twitterConsumerSecret);
				
				try {
					RequestToken twitterRequestToken = twitter.getOAuthRequestToken();
					String token = twitterRequestToken.getToken();
					String tokenSecret = twitterRequestToken.getTokenSecret();
						
					session.setAttribute("token", token);
					session.setAttribute("tokenSecret", tokenSecret);
					session.setAttribute("twitter", false);
						
					String authUrl = twitterRequestToken.getAuthorizationURL();
						
					response.sendRedirect(authUrl);

				} catch (TwitterException e) {
					e.printStackTrace();
				}*/
				Twitter twitter = new Twitter(twitterCallback);
				response.sendRedirect(twitter.goToAuthPage());
				session.setAttribute("requestToken", twitter.getTwitterTokens().getRequestToken());
				session.setAttribute("requestTokenSecret", twitter.getTwitterTokens().getRequestTokenSecret());
				
				
				
				
			}else if(socialNetwork.equals("foursquare")){
				
				try{
//					FoursquareApi foursquareApi = new FoursquareApi(fsqClientId,fsqClientSecret,fsqCallback);
//					response.sendRedirect(foursquareApi.getAuthenticationUrl());
					
					response.sendRedirect("https://foursquare.com/oauth2/authenticate?client_id="+fsqClientId+
							"&response_type=code&redirect_uri="+fsqCallback);
				}catch(Exception e){
						e.printStackTrace();
				}
				
			}else if(socialNetwork.equals("facebook")){
					 String code = request.getParameter("code");
					 
					 if(code==null){
						 SecureRandom random = new SecureRandom();
					     String state = (new BigInteger(130, random).toString(32));
						 session.setAttribute("state", state);
						 
						 String permissions = "read_friendlists,read_stream,publish_actions,email,user_photos,user_friends";
						 //permissions+=",read_mailbox";
						 
						 response.sendRedirect("https://www.facebook.com/dialog/oauth?client_id="+fbAppID+"&redirect_uri="+
								 OAuth.percentEncode(fbRedirectUri)+"&state="+state+"&scope="+permissions);
					 }
					
			}
					
		}
			
			
		
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

}
