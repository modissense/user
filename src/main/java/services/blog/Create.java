package services.blog;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import description.HTMLDescription;
import description.ServiceDescription;

/**
 * Servlet implementation class Create
 */
@WebServlet("/Create")
public class Create extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;

	/**
     * @see HttpServlet#HttpServlet()
     */
    public Create() {
        super();
        this.description = new HTMLDescription("Create blog");
        this.description.addParameter("String", "userid");
        this.description.addParameter("Timestamp", "start");
        this.description.addParameter("Timestamp", "end");
        this.description.setReturnValue("Text");
        this.description.setDescription("Web service used to create the auto-blog text. The service" +
        		" expects the userid and the period for which the blog will be created and it " +
        		"returns the blog as a text.");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("info")!=null){
			response.getOutputStream().print(this.description.serialize());
		} else if(request.getParameter("userid")==null || request.getParameter("start")==null || request.getParameter("end")==null){
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		} else {
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
