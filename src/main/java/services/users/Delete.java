package services.users;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.SNDetails;
import entities.User;

/**
 * Servlet implementation class Delete
 */
@WebServlet("/Delete")
public class Delete extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Delete() {
        super();
        this.description = new HTMLDescription("Delete user");
        this.description.addParameter("String", "token");
        this.description.addParameter("String", "format");
        this.description.addParameter("String", "network");
        this.description.setReturnValue("boolean");
        this.description.setDescription("Web service used to delete the specified user from the platform.");
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
			HttpSession session = request.getSession();
			String token = (String) request.getParameter("token");
			
			System.out.println("token = "+token);
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			System.out.println("uid = "+usid);
			
			SNDetails socialNetworks = new SNDetails();
			socialNetworks.setUserId(usid);
			
			String jsonToSend = "{\"success\":\"";
			String socialNetwork = (String) request.getParameter("network");
			if(socialNetwork!=null){
				
				System.out.println("Network Param Specified");
				
				socialNetworks.setSnName(socialNetwork);
				
				SNDetails tmpSN = new SNDetails();
				tmpSN.setUserId(usid);
				java.util.List<HashMap<String, String>> connectedNetworks = tmpSN.read();
				
				if(connectedNetworks.size()>1){
					
					System.out.println("More than one sn connected");
					
					if(socialNetworks.delete()){
						jsonToSend+="true\"}";
					}else{
						jsonToSend+="false\"}";
					}
				}else{
					
					System.out.println("Only one connected sn");
					
					if(socialNetworks.delete()){
						System.out.println("account deleted successfully");
						User invalidUser = new User(usid);
						invalidUser.setUsername("Invalid User_"+usid);
						if(invalidUser.update()){
							System.out.println("User updated successfully to invalid");
							jsonToSend+="true\"}";
						}else{
							System.out.println("User update to invalid failed.");
							jsonToSend+="false\"}";
						}
					}
					else{
						System.out.println("Problem in deleting account");
						jsonToSend+="false\"}";
					}
				}
			}else{
				
				System.out.println("Delete everything");
				
				if(socialNetworks.delete()){
					System.out.println("account deleted successfully");
					User invalidUser = new User(usid);
					invalidUser.setUsername("Invalid User_"+usid);
					if(invalidUser.update()){
						System.out.println("User updated successfully to invalid");
						jsonToSend+="true\"}";
					}else{
						System.out.println("User update to invalid failed.");
						jsonToSend+="false\"}";
					}
				}
				else{
					System.out.println("Problem in deleting account");
					jsonToSend+="false\"}";
				}
			}
			
			response.setCharacterEncoding("UTF-8");
			response.setHeader("Content-Type", "application/json");
			response.setHeader("charset", "utf-8");
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
		// do nothing for the time being
	}

}
