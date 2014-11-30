package services.blog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import supportingClasses.OAuth;
import twitterlib.NotAuthorizedException;
import twitterlib.User;
import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.MicroBlog;
import entities.SNDetails;

public class ShareBlog extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
	//key ths Modis google efarmoghs tou gmytilinis@gmail.com
	private static final String googleAppAPIKey = "AIzaSyBxHhGzrAIqGhmZV1mIk9nCQTZ8K9e0_vs";
	private static final String googleShortenEndpoint = "https://www.googleapis.com/urlshortener/v1/url";
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShareBlog(){
        super();
        description = new HTMLDescription("Post Blog to Facebook");
        description.addParameter("String", "token");
        description.addParameter("String", "date");
        description.addParameter("String", "network");
        description.setReturnValue("json String");
        description.setDescription("This service is used to post the blog to facebook.");
    }
    
    
    public String shortenLongURL(String longURL) throws IOException{
    	URL obj = new URL(googleShortenEndpoint+"?key="+googleAppAPIKey);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/json");
		con.setDoOutput(true);
		
		String paramsToSend = "{\"longUrl\": \""+longURL+"\"}";
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(paramsToSend);
		wr.flush();
		wr.close();
		
		int responseCode = con.getResponseCode();
		System.out.println("Google API response code: "+responseCode);
		
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		System.out.println("Google: "+response.toString());
		JSONObject gResp =(JSONObject) JSONValue.parse(response.toString());
		
		return (String) gResp.get("id");
    }

	
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setCharacterEncoding("UTF-8");
		if(request.getParameter("info")!=null)
			response.getOutputStream().print(this.description.serialize());
		else{
			
			String token = (String) request.getParameter("token");
			
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			
			String date = (String) request.getParameter("date");
			
			String[] socialNetworks = request.getParameterValues("network");
			
			HashMap<String, String> serviceResults = new HashMap<>();
			PrintWriter out = response.getWriter();
			response.setHeader("Content-Type", "application/json");
			response.setHeader("charset", "utf-8");
			
			MicroBlog blog = new MicroBlog();
			
			List<HashMap<String, String>> results = blog.getBlogTrajectories(usid, date);
			
			String description="";
			String googleMapsURL = "http://maps.googleapis.com/maps/api/staticmap?size=400x400&sensor=false&";
			String googleMapsPath = "path=color:0x0000ff|weight:5";
			
			String[] sundesmoi={"Στη συνέχεια, πήγα ","Μετά πήγα ","Αφού έφυγα από εκεί, πήγα ",
					"Μετά, το πρόγραμμα έιχε "};
			int rSize = results.size();
			
			
			for(int i=0;i<rSize;i++){
				
				if(i==0)
					description+="Πήγα ";
				else{
					Random rnd = new Random();
					description+=sundesmoi[rnd.nextInt(sundesmoi.length)];
				}
				
				description+=results.get(i).get("name")+" ("+results.get(i).get("comment")+")";
				if(results.get(i).get("arrived")!=null&&results.get(i).get("off")!=null){
					description+=" και παρέμεινα εκεί "+GetMicroBlog.getDuration(results.get(i).get("arrived"),results.get(i).get("off"));
				}
				description+=".";
				
				if(results.get(i).get("st_astext")!=null){
					
					String temp[] = results.get(i).get("st_astext").split(" ");
					double lat=new Double(temp[0].substring(6));
					double longitude=new Double(temp[1].substring(0,temp[1].length()-1));
					
					googleMapsURL+="markers=color:blue|label:"+i+"|"+lat+","+longitude+"&";
					googleMapsPath+="|"+lat+","+longitude;
					
				}

			}
			
			googleMapsURL+=googleMapsPath;
			
			for(String sn:socialNetworks){
				
				SNDetails socialNetworkInfo = new SNDetails();
				socialNetworkInfo.setSnName(sn);
				socialNetworkInfo.setUserId(usid);
						
				List<HashMap<String, String>> snResults = socialNetworkInfo.read();
				//TODO: elegxos gia to an uparxei xrhsths.alla gia na postarei sigoura uparxei
				//ara sthn arxh dokimastika den kanw kanenan elegxo.epishs kanw search me
				//primary key ara 8a exw mono ena result
				
				String snKey = snResults.get(0).get("sn_identifier");
				String accessToken = snResults.get(0).get("sn_token");
				
				String accessTokenSecret=null;
				if(sn.equals("twitter"))
					accessTokenSecret = snResults.get(0).get("sn_token_secret");
				
				try{
					if(sn.equals("facebook")){
						
						URL url = new URL("https://graph.facebook.com/"+snKey+"/feed?access_token="+accessToken);
						 
						HttpsURLConnection conn =(HttpsURLConnection) url.openConnection();conn.setRequestMethod("POST");
						conn.setRequestProperty("charset", "utf-8");
						conn.setDoOutput(true);
					     
						String postBody = "message="+description+"\n&";
						postBody+="link="+googleMapsURL;
						System.out.println(postBody);
						 
						byte[] data = postBody.getBytes("UTF-8");
						OutputStream output = conn.getOutputStream();
						output.write(data);
						output.close();
						 
					    StringBuffer answer = new StringBuffer();
			   	        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					    String line;
					    while ((line = reader.readLine()) != null) {
					           answer.append(line);
					    }
					    reader.close();
					   
					    String answerString = answer.toString();
					    System.out.println(answerString);
					    serviceResults.put("facebook", "true");
					}else if(sn.equals("twitter")){
				
						User u = new User(snKey, accessToken, accessTokenSecret);
						HttpSession session = request.getSession();
						session.setAttribute("accessToken", accessToken);
						session.setAttribute("accessTokenSecret", accessTokenSecret);
						u.postStatus(shortenLongURL(googleMapsURL));
						serviceResults.put("twitter", "true");
					}
				
				}
				catch(Exception e){
					if(sn.equals("facebook"))
						serviceResults.put("facebook", "false");
					else
						serviceResults.put("twitter", "false");
				}
			}	
			String outJSON="{";
			Iterator it = serviceResults.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry param = (Map.Entry) it.next();
				outJSON+="\""+param.getKey()+"\":\""+param.getValue()+"\",";
			}
			outJSON = outJSON.substring(0, outJSON.length()-1);
			outJSON+="}";
			
			out.write(outJSON);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}