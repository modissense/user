package services.users;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import session.SessionManagementIM;
import supportingClasses.Utils;

import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;

/**
 * Servlet implementation class LogOut
 */
@WebServlet("/LogOut")
public class LogOut extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogOut() {
        super();
        this.description = new HTMLDescription("Logout user");
        this.description.addParameter("String", "token");
        this.description.setReturnValue("boolean");
        this.description.setDescription("Web service used to log out a user from the platform. It returns true if" +
        		"the process is finished correctly, or false if the process is terminated with an error.");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("token")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			
			String token = request.getParameter("token");
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			hashClient.remove(token);
			
			response.getOutputStream().print(true);
		} 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// does nothing for now
	}

}
