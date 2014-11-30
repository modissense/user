package twitterlib;

public class Twitter {
	
	private AccessTokens twitterTokens;
	
	private String accessToken;
	private String accessTokenSecret;
	
	private String userID;
	
	public Twitter(String callback){
		twitterTokens = new AccessTokens();
		twitterTokens.getAuth().setCallback(callback);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}

	
	public AccessTokens getTwitterTokens() {
		return twitterTokens;
	}

	public void setTwitterTokens(AccessTokens twitterTokens) {
		this.twitterTokens = twitterTokens;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String goToAuthPage(){
		twitterTokens.getTwitterRequestToken();
		return twitterTokens.getRedirectURL();
	}

	public void getAccessTokens(String verifier){
		twitterTokens.getAccessToken(verifier);
		setAccessToken(twitterTokens.getOauthToken());
		setAccessTokenSecret(twitterTokens.getOauthTokenSecret());
		setUserID(twitterTokens.getUserID());
	}

}
