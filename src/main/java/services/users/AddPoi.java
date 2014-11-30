package services.users;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import description.HTMLDescription;
import description.ServiceDescription;

/**
 * Servlet implementation class Add
 */
@WebServlet("/Add")
public class AddPoi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AddPoi() {
        super();
        this.description = new HTMLDescription("Add user POI");
        this.description.addParameter("String", "userid");
        this.description.addParameter("Integer", "poiid");
        this.description.setReturnValue("boolean");
        this.description.setDescription("Web service used to update the user's profile by adding the " +
        		"specified POI.");

    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("userid")==null || request.getParameter("poiid")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.getOutputStream().print(true);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
