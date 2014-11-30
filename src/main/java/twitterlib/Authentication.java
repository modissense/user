package twitterlib;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import services.users.Register;
import supportingClasses.OAuth;

public class Authentication {
	/*private static final String twitterRequestTokenURL= "https://api.twitter.com/oauth/request_token";
	private static final String twitterAuthRedirectURL= "https://api.twitter.com/oauth/authorize?oauth_token=";
	private static final String twitterAccessTokenURL = "https://api.twitter.com/oauth/access_token";*/
	
	private static final String signatureMethod = "HMAC-SHA1";
	private static final String oauthVersion = "1.0";
	//private static final String callback = "https://83.212.104.253/user/tcallback";
	
	private static final boolean DEBUG = true;
	
	private String oauthToken=null;
	private String oauthTokenSecret=null;
	
	private String requestToken=null;
	private String requestTokenSecret=null;
	
	private String nonce;
	private String timestamp;
	
	private String httpMethod;
	private String endpointURL;
	
	private String callback;
	
	private HashMap<String, String> postRequestParams;
	private HashMap<String, String> getRequestParams;
	
	public Authentication(){}
	
	public Authentication(String endpoint,String httpMethod){
		this.endpointURL = endpoint;
		this.httpMethod = httpMethod;
	}
	
	public String getCallback(){
		return callback;
	}
	
	public void setCallback(String callback){
		this.callback = callback;
	}
	
	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}

	public String getEndpointUrl() {
		return endpointURL;
	}

	public void setEndpointUrl(String url) {
		this.endpointURL = url;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getOauthTokenSecret() {
		return oauthTokenSecret;
	}

	public void setOauthTokenSecret(String oauthTokenSecret) {
		this.oauthTokenSecret = oauthTokenSecret;
	}

	public String getRToken() {
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
	
	public HashMap<String, String> getPostRequestParams() {
		return postRequestParams;
	}

	public void setPostRequestParams(HashMap<String, String> requestParams) {
		this.postRequestParams = requestParams;
	}

	public HashMap<String, String> getGetRequestParams() {
		return getRequestParams;
	}

	public void setGetRequestParams(HashMap<String, String> getRequestParams) {
		this.getRequestParams = getRequestParams;
	}

	public String createTwitterAuthorizationHeader(){
		timestamp = Long.toString(System.currentTimeMillis()/1000L);
		setNonce(generateNonce());
		
		String authHeaderStr="OAuth ";
		if(endpointURL.equals(AccessTokens.twitterAccessTokenURL)||endpointURL.equals(AccessTokens.twitterRequestTokenURL))
			authHeaderStr+=OAuth.percentEncode("oauth_callback")+"=\""+OAuth.percentEncode(callback)+"\", ";
		authHeaderStr+=OAuth.percentEncode("oauth_consumer_key")+"=\""+OAuth.percentEncode(Register.twitterConsumerToken)+"\", ";
		authHeaderStr+=OAuth.percentEncode("oauth_nonce")+"=\""+OAuth.percentEncode(nonce)+"\", ";
		
		authHeaderStr+=OAuth.percentEncode("oauth_signature")+"=\""+OAuth.percentEncode(createSignature())+"\", ";
		authHeaderStr+=OAuth.percentEncode("oauth_signature_method")+"=\""+OAuth.percentEncode(signatureMethod)+"\", ";
		authHeaderStr+=OAuth.percentEncode("oauth_timestamp")+"=\""+OAuth.percentEncode(timestamp)+"\", ";
		if(oauthToken!=null)
			authHeaderStr+=OAuth.percentEncode("oauth_token")+"=\""+OAuth.percentEncode(oauthToken)+"\", ";
		
		authHeaderStr+=OAuth.percentEncode("oauth_version")+"=\""+OAuth.percentEncode(oauthVersion)+"\"";
		
		return authHeaderStr;
	}
	
	public String createSignature(){
		
		System.out.println("Ready to create signature");
		System.out.println("my http method: "+httpMethod);
		System.out.println("the endpoint: "+endpointURL);
		
		TreeMap<String, String> parametersMap = new TreeMap<>();
		
		if(postRequestParams!=null){
			Iterator it = postRequestParams.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry param = (Map.Entry) it.next();
				System.out.println("Key: "+param.getKey()+" Value:"+ param.getValue());
				parametersMap.put(OAuth.percentEncode((String)param.getKey()), OAuth.percentEncode((String)param.getValue()));
			}
		}
		
		if(getRequestParams!=null){
			Iterator it = getRequestParams.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry param = (Map.Entry) it.next();
				parametersMap.put(OAuth.percentEncode((String)param.getKey()), OAuth.percentEncode((String)param.getValue()));
			}
		}
		if(endpointURL.equals(AccessTokens.twitterAccessTokenURL)||endpointURL.equals(AccessTokens.twitterRequestTokenURL))
			parametersMap.put(OAuth.percentEncode("oauth_callback"), OAuth.percentEncode(callback));
		parametersMap.put(OAuth.percentEncode("oauth_consumer_key"), OAuth.percentEncode(Register.twitterConsumerToken));
		parametersMap.put(OAuth.percentEncode("oauth_nonce"), OAuth.percentEncode(nonce));
		parametersMap.put(OAuth.percentEncode("oauth_signature_method"), OAuth.percentEncode(signatureMethod));
		parametersMap.put(OAuth.percentEncode("oauth_timestamp"), OAuth.percentEncode(timestamp));
		parametersMap.put(OAuth.percentEncode("oauth_version"), OAuth.percentEncode(oauthVersion));
		if(oauthToken!=null)
			parametersMap.put(OAuth.percentEncode("oauth_token"), OAuth.percentEncode(oauthToken));
		
		
		String parameter="";
		NavigableSet<String> keyParams = parametersMap.navigableKeySet();
		String lastKey = parametersMap.lastKey();
		for(String s:keyParams){
			parameter+=s+"="+parametersMap.get(s);
			if(!s.equals(lastKey))
				parameter+="&";
		}
		
		String signatureBase=httpMethod+"&"+OAuth.percentEncode(endpointURL)+"&"+OAuth.percentEncode(parameter);
		
		String signingKey = OAuth.percentEncode(Register.twitterConsumerSecret)+"&";
		if(oauthTokenSecret!=null)
			signingKey+=OAuth.percentEncode(oauthTokenSecret);
		
		if(DEBUG){
			System.out.println("Parameters = "+parameter);System.out.println();
			System.out.println("SignatureBase String: "+signatureBase);System.out.println();
			System.out.println("Signing key = "+signingKey);System.out.println();
			System.out.println(Base64.encode(hmacSha1(signatureBase, signingKey)));System.out.println();
		}
		return Base64.encode(hmacSha1(signatureBase, signingKey));
	}
	
	private byte[] hmacSha1(String value, String key) {
        try {
            byte[] keyBytes = key.getBytes();           
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes());            
            return rawHmac;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
	private String generateNonce(){
		SecureRandom random = new SecureRandom();
		BigInteger nonceRand = new BigInteger(256, random);
		String nonce = Base64.encode(nonceRand.toByteArray());
		String newNonce = "";
		for(int i=0;i<nonce.length();i++){
			char c = nonce.charAt(i);
			if((c>='0'&&c<='9')||(c>='A'&&c<='Z')||(c>='a'&&c<='z'))
				newNonce+=Character.toString(c);
		}
		return newNonce;
	}
	
	/*public void getRequestToken(){
		this.setHttpMethod("POST");
		tConnector.setHttpMethod("POST");
		this.setEndpointUrl(twitterRequestTokenURL);
		tConnector.setEndpointURL(twitterRequestTokenURL);
		tConnector.setAuthorizationHeader(createTwitterAuthorizationHeader());
		try {
			String response = tConnector.makeTwitterRequest(true);
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
			this.setHttpMethod("POST");
			tConnector.setHttpMethod("POST");
			this.setEndpointUrl(twitterAccessTokenURL);
			tConnector.setEndpointURL(twitterAccessTokenURL);
			if(DEBUG)
				System.out.println("Access token params: "+parameters);
			tConnector.setParameters(parameters);
			tConnector.setAuthorizationHeader(createTwitterAuthorizationHeader());
			String response = tConnector.makeTwitterRequest(true);
			if(DEBUG)
				System.out.println(response);
			String[] responseParams = response.split("&");
			setOauthToken(responseParams[0].split("=")[1]);
			setOauthTokenSecret(responseParams[1].split("=")[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	/*public String getRedirectURL(){
		return twitterAuthRedirectURL+requestToken;
	}*/
}
