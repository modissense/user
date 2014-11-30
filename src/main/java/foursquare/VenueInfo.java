package foursquare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class VenueInfo {

	private String accessToken;
	private String venueID;
	
	public VenueInfo(String venueID,String accessToken){
		this.accessToken = accessToken;
		this.venueID = venueID;
	}
	
	public void getVenueInfo(){
	
		URL url;			
		try {
			url = new URL("https://api.foursquare.com/v2/venues/"+venueID+"?&oauth_token="+accessToken);
			HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("GET");
			conn.setDoOutput(true);
		     
		    StringBuffer answer = new StringBuffer();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    
		     while ((line = reader.readLine()) != null) {
		            answer.append(line);
		     }
		     reader.close();
		    
		     String venueAnswer = answer.toString();
		     System.out.println(venueAnswer);
		     
		     /*
		      * rating
		      * 
		      * friendVisits
		      * 
		      * beenHere
		      * 
		      * tips
		      * 
		      * tags
		      * 
		      */
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
	
		VenueInfo vInfo = new VenueInfo("4a7efe5cf964a5206cf21fe3", "K0O0TLTM4T51MIYRO5PLP3LM2DZFKVP22MBKHVOQMMY5CUDZ");
		vInfo.getVenueInfo();
				
	}

}
