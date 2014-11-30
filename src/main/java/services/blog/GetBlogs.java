package services.blog;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
public class GetBlogs extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
	   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetBlogs() {
        super();
        description = new HTMLDescription("Get Blogs");
        description.addParameter("String", "token");
        description.addParameter("String", "format");
        description.addParameter("String", "callback");
        description.setReturnValue("json String");
        description.setDescription("This service is used to all the available blogs for a specific user");
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
			
			MicroBlog blog = new MicroBlog();
			blog.setUserID(usid);
			
			List<HashMap<String, String>> results = blog.getBlogs();
			
			String jsonToSend = "{\"blogs\":[";
			
			int rSize = results.size();
			for(int i=0;i<rSize;i++){
				jsonToSend+="{\"date\":\""+results.get(i).get("date").split(" ")[0]+"\"}";
				if(i!=rSize-1)
					jsonToSend+=",";
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}