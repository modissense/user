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
 * Servlet implementation class LoggedIn
 */
@WebServlet("/LoggedIn")
public class LoggedIn extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoggedIn() {
        super();
        this.description = new HTMLDescription("User LoggedIn");
        this.description.addParameter("String", "userid");
        this.description.setReturnValue("UserObject");
        this.description.setDescription("Service used to check if the user is logged in to the platform.");
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
			String message=(String) request.getSession().getAttribute(request.getParameter("userid"));
//			request.getSession().invalidate();
			if(message!=null){
				User u = new User(request.getParameter("userid"));
				response.getOutputStream().print(message);
				response.getOutputStream().print(u.toJson());
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// does nothing for now
	}

}
