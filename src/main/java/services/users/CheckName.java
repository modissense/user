package services.users;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import description.HTMLDescription;
import description.ServiceDescription;
import entities.User;

/**
 * Servlet implementation class CheckName
 */
@WebServlet("/CheckName")
public class CheckName extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckName() {
        super();
        this.description = new HTMLDescription("Check user name");
        this.description.addParameter("String", "userid");
        this.description.setReturnValue("boolean");
        this.description.setDescription("Web service used to check if the given user id is already registered" +
        		"or not. The return value is true if the userid is already registered or false if it isn't.");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("userid")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			User user = new User(new Integer(request.getParameter("userid")));
			response.getOutputStream().print(user.isSynced());			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
