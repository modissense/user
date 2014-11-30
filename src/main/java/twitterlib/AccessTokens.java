package twitterlib;

import java.util.HashMap;

public class AccessTokens extends TwitterConnector {
	
	public static final String twitterRequestTokenURL= "https://api.twitter.com/oauth/request_token";
	private static final String twitterAuthRedirectURL= "https://api.twitter.com/oauth/authorize?oauth_token=";
	public static final String twitterAccessTokenURL = "https://api.twitter.com/oauth/access_token";
	
	private static final boolean DEBUG = true;
	
	private String oauthToken=null;
	private String oauthTokenSecret=null;
	
	private String requestToken=null;
	private String requestTokenSecret=null;
	
	private String userID;
	private String screenName;
	
	public AccessTokens(){
		super();
	}
	
	public AccessTokens(String endpointURL,String httpMethod){
		super(endpointURL,httpMethod);
	}
	
	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
	
	public String getOauthTokenSecret() {
		return oauthTokenSecret;
	}

	public void setOauthTokenSecret(String oauthTokenSecret) {
		this.oauthTokenSecret = oauthTokenSecret;
	}
	
	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String rToken) {
		this.requestToken = rToken;
	}

	public String getRequestTokenSecret() {
		return requestTokenSecret;
	}

	public void setRequestTokenSecret(String rTokenSecret) {
		this.requestTokenSecret = rTokenSecret;
	}
	
	
	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void getTwitterRequestToken(){
		this.setHttpMethod("POST");
		this.setEndpointURL(twitterRequestTokenURL);
		//this.auth.setEndpointUrl(endpointURL);
		//this.auth.setHttpMethod(httpMethod);
		try {
			String response = this.makeTwitterRequest(true);
			String[] responseParams = response.split("&");
			setRequestToken(responseParams[0].split("=")[1]);
			setRequestTokenSecret(responseParams[1].split("=")[1]);
			
			if(DEBUG){
				System.out.println("Request token = "+requestToken);
				System.out.println("Request token secret = "+requestToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getAccessToken(String verifier){
		try {
			String parameters="oauth_token="+requestToken+"&oauth_token_secret="+requestTokenSecret+
					"&oauth_verifier="+verifier;
			
			HashMap<String, String> postParams = new HashMap<>();
			postParams.put("oauth_token",requestToken);
			postParams.put("oauth_token_secret",requestTokenSecret);
			postParams.put("oauth_verifier",verifier);
			
			this.setHttpMethod("POST");
			this.setEndpointURL(twitterAccessTokenURL);
			//this.auth.setEndpointUrl(endpointURL);
			//this.auth.setHttpMethod(httpMethod);
			if(DEBUG)
				System.out.println("Access token params: "+parameters);
			this.setPostParameters(postParams);
			String response = this.makeTwitterRequest(true);
			if(DEBUG)
				System.out.println(response);
			String[] responseParams = response.split("&");
			setOauthToken(responseParams[0].split("=")[1]);
			setOauthTokenSecret(responseParams[1].split("=")[1]);
			setUserID(responseParams[2].split("=")[1]);
			setScreenName(responseParams[3].split("=")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getRedirectURL(){
		return twitterAuthRedirectURL+requestToken;
	}

}
