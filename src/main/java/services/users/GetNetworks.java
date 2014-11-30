package services.users;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import session.SessionManagementIM;
import supportingClasses.RunningWebService;
import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.SNDetails;



public class GetNetworks extends RunningWebService {
	
private static final long serialVersionUID = 1L;
private ServiceDescription description;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetNetworks() {
        super();
        
        serviceName = "Get Networks";
        
        description = new HTMLDescription("Get Networks");
        description.addParameter("String", "token");
        description.addParameter("String", "format");
        description.addParameter("String", "callback");
        description.setReturnValue("json String");
        description.setDescription("This service is used to get the networks, a user has connected to ModisSense platform.");   
   
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
		
		if(request.getParameter("info")!=null)
			response.getOutputStream().print(this.description.serialize());
		else{
			
			String token = (String) request.getParameter("token");
			
			System.out.println(serviceName+"get networks for user with token = "+token);
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			
			System.out.println(serviceName+"get networks for user = "+usid);
			
			SNDetails loggedInUserSN = new SNDetails();
			loggedInUserSN.setUserId(usid);
			
			List<HashMap<String, String>> results = loggedInUserSN.read();
			int rSize= results.size();
			
			String jsonToSend = "{\"networks\":[";
		
			for(int i=0;i<rSize;i++){
				if(i!=rSize-1)
					jsonToSend+="{\"Name\":\""+results.get(i).get("sn_name")+"\"},";
				else
					jsonToSend+="{\"Name\":\""+results.get(i).get("sn_name")+"\"}";
			}
			
			jsonToSend+="]}";
			
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException{
	}

}
