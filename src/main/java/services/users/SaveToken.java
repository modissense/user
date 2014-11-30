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
 * Servlet implementation class SaveToken
 */
@WebServlet("/SaveToken")
public class SaveToken extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SaveToken() {
        super();
        this.description = new HTMLDescription("Save Authorization token");
        this.description.addParameter("String", "token");
        this.description.setReturnValue("boolean");
        this.description.setDescription("Service used to save the authorization token that was sent by the social" +
        		" netork.<br/>(This method will probably not be exported as a webservice, but it will be used " +
        		"internally by the platform).");
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
