package twitterlib;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import services.users.Register;
import supportingClasses.OAuth;

/**
 * 
 * @author giagulei
 *
 */
public class TwitterConnector {
	
	private static final boolean DEBUG=true;
	
	protected String endpointURL;
	protected HashMap<String, String> postParameters;
	protected HashMap<String, String> getParameters;
	protected String httpMethod;
	
	protected Authentication auth;
	
	public TwitterConnector(){auth = new Authentication();auth.setCallback(Register.twitterCallback);}
	
	public TwitterConnector(String endpointURL,String httpMethod){
		this.endpointURL = endpointURL;
		this.httpMethod = httpMethod;
		auth = new Authentication(endpointURL,httpMethod);
		auth.setCallback(Register.twitterCallback);
	}
	
	public String getEndpointURL() {
		return endpointURL;
	}

	public void setEndpointURL(String endpointURL) {
		this.endpointURL = endpointURL;
		this.auth.setEndpointUrl(endpointURL);
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
		this.auth.setHttpMethod(httpMethod);
	}

	public HashMap<String, String> getPostParameters() {
		return postParameters;
	}

	public void setPostParameters(HashMap<String, String> parameters) {
		this.postParameters = parameters;
		this.auth.setPostRequestParams(parameters);
	}
	
	public HashMap<String, String> getGETParams(){
		return getParameters;
	}
	
	//TODO: na mpoun kai post params sto Authentication
	public void setGETParams(HashMap<String, String> params){
		this.getParameters = params;
		this.auth.setGetRequestParams(params);
	}

	public Authentication getAuth() {
		return auth;
	}

	public void setAuth(Authentication auth) {
		this.auth = auth;
	}

	public String makeTwitterRequest(boolean authorized) throws Exception {
		
		if(getParameters!=null){
			endpointURL=endpointURL+"?";
			
			Iterator it = getParameters.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry param = (Map.Entry) it.next();
				endpointURL+=param.getKey()+"="+param.getValue()+"&";
			}
			endpointURL = endpointURL.substring(0, endpointURL.length()-1);
		}
		URL obj = new URL(endpointURL);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setRequestMethod(httpMethod);
		String authHeader = null;
		if(authorized){
			authHeader = auth.createTwitterAuthorizationHeader(); 
		    if(authHeader!=null)
		    	con.setRequestProperty("Authorization", authHeader);
		    else
		    	throw new NotAuthorizedException();
		}
		con.setRequestProperty("User-Agent", "OAuth gem v0.4.4");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Host", "api.twitter.com");
		con.setRequestProperty("Connection", "close");
		con.setDoOutput(true);
		String paramsToSend ="";
		if(httpMethod.equals("POST")&&postParameters!=null){
			
			paramsToSend ="";
			
			Iterator it = postParameters.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry param = (Map.Entry) it.next();
				paramsToSend+=param.getKey()+"="+OAuth.percentEncode((String)param.getValue())+"&";
			}
			paramsToSend = paramsToSend.substring(0, paramsToSend.length()-1);
			
			System.out.println("Esteila: "+paramsToSend);
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(paramsToSend);
			wr.flush();
			wr.close();
		}
		
		int responseCode = con.getResponseCode();
		
		if(DEBUG){
			System.out.println("Authorization header: "+authHeader);
			System.out.println("\nSending 'POST' request to URL : " + endpointURL);
			System.out.println("Sent POST params: "+paramsToSend);
			System.out.println("Twiiter Servers UTC Date: "+con.getHeaderField("Date"));
			System.out.println("Response Code : " + responseCode);
			System.out.println("Response Message : "+con.getResponseMessage());
		}
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		if(DEBUG)
			System.out.println(response.toString());
		return response.toString();
	}
	
}
