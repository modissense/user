package services.blog;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//import org.postgresql.geometric.PGpoint;

import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.Configuration;
import entities.MicroBlog;

/**
 * Servlet implementation class CallbackServlet
 * 
 * 
 * @author Giagkos Mytilinis
 */
public class GetMicroBlog extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMicroBlog() {
        super();
        description = new HTMLDescription("Get Micro Blog");
        description.addParameter("String", "token");
        description.addParameter("String", "date");
        description.addParameter("String", "format");
        description.addParameter("String", "callback");
        description.setReturnValue("json String");
        description.setDescription("This service is used to get a micro blog selected by the user.");
    }

	
    
    public static String getDuration(String timestamp1,String timestamp2){
    	
    	String time1 = timestamp1.split(" ")[1];
    	String time2 = timestamp2.split(" ")[1];
    	
    	int hours1= Integer.parseInt(time1.split(":")[0]);
    	int min1= Integer.parseInt(time1.split(":")[1]);
    	
    	int hours2= Integer.parseInt(time2.split(":")[0]);
    	int min2= Integer.parseInt(time2.split(":")[1]);
    	
    	int hours,minutes;
    	
    	hours = ((hours2*60+min2)-(hours1*60+min1))/60;
    	minutes = ((hours2*60+min2)-(hours1*60+min1))%60;
    	
    	String answer = Integer.toString(hours);
    	if(hours==1)
    		answer+=" ώρα";
    	else if(hours==0)
    		answer="";
    	else
    		answer+=" ώρες";
    	
    	if(minutes==1){
    		if(hours>0)
    			answer+=" και "+minutes+" λεπτό";
    		else
    			answer+= minutes+" λεπτό";
    	}
    	else if(minutes==0)
    		answer+="";
    	else{
    		if(hours>0)
    			answer+=" και "+minutes+" λεπτά";
    		else
    			answer+=minutes+" λεπτά";
    	}
    	
    	return answer;
    }
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("info")!=null)
			response.getOutputStream().print(this.description.serialize());
		else{
			
			String token = (String) request.getParameter("token");
			
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			
			String date = (String) request.getParameter("date");
			
			MicroBlog blog = new MicroBlog();
			
			List<HashMap<String, String>> results = blog.getBlogTrajectories(usid, date);
			
			String jsonToSend = "{\"blog\":[";
			String description="";
			String[] sundesmoi={"Στη συνέχεια, πήγα ","Μετά πήγα ","Αφού έφυγα από εκεί, πήγα ",
					"Μετά, το πρόγραμμα έιχε "};
			int rSize = results.size();
			
			
			for(int i=0;i<rSize;i++){
				
				jsonToSend+="{\"seqid\":\""+results.get(i).get("seq_number")+"\"" +
						",\"start\":\"";
				
				if(results.get(i).get("arrived")!=null)
					jsonToSend+= results.get(i).get("arrived");
				else jsonToSend+="null";
				
				jsonToSend+="\",\"end\":\"";
				
				if(results.get(i).get("off")!=null)
					jsonToSend+= results.get(i).get("off");
				else jsonToSend+="null";
				
				jsonToSend+="\"," +"\"poi_id\":\""+results.get(i).get("poi_id")+"\",\"name\":\""+results.get(i).get("name")+"\",";
				
				if(i==0)
					description+="Πήγα ";
				else{
					Random rnd = new Random();
					description+=sundesmoi[rnd.nextInt(sundesmoi.length)];
				}
				
				description+=results.get(i).get("name");
				
				if(results.get(i).get("comment")!=null)
					description+=" ("+results.get(i).get("comment")+")";
				
				if(results.get(i).get("arrived")!=null&&results.get(i).get("off")!=null){
					description+=" και παρέμεινα εκεί "+getDuration(results.get(i).get("arrived"),results.get(i).get("off"));
				}
				description+=".";
				
				if(results.get(i).get("st_astext")!=null){
					
					String temp[] = results.get(i).get("st_astext").split(" ");
					double lat=new Double(temp[0].substring(6));
					double longitude=new Double(temp[1].substring(0,temp[1].length()-1));
					jsonToSend+="\"lat\":\""+lat+"\",\"long\":\""+longitude+"\",";
				}

				jsonToSend+="\"comment\":\"";
				
				if(results.get(i).get("comment")!=null)
					jsonToSend+=results.get(i).get("comment");
				else jsonToSend+="null";
				
				jsonToSend+="\",\"publicity\":\""+results.get(i).get("publicity")+"\"" +
						",\"interest\":\""+results.get(i).get("interest")+"\",\"hotness\":\""+results.get(i).get("hotness")+"\"" +
						",\"keywords\":[";
				
				if(results.get(i).get("keywords")!=null){
					String[] keywords = results.get(i).get("keywords").split(",");
					
					for(int j=0;j<keywords.length;j++){
						jsonToSend+="\""+keywords[j]+"\"";
						if(j!=keywords.length-1){
							jsonToSend+=",";
						}
					}
				}
				
				jsonToSend+= "]}";
				if(i!=rSize-1)
					jsonToSend+=",";
			}
			
			jsonToSend+="],\"description\":\""+description+"\"}";
			
			response.setHeader("Content-Type", "application/json");
			response.setHeader("charset", "utf-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			
			String format = request.getParameter("format");
			if(format.equals("jsonp")){
				String callback = request.getParameter("callback");
				out.write(callback+"("+jsonToSend+")");
			}
			else if(format.equals("json"))
				out.write(jsonToSend);
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}