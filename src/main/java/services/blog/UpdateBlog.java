package services.blog;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.imageio.plugins.common.BogusColorSpace;

import datastore.client.PersistentHashMapClient;
import description.HTMLDescription;
import description.ServiceDescription;
import entities.MicroBlog;

public class UpdateBlog extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateBlog() {
        super();
        description = new HTMLDescription("Update Micro Blog");
        description.addParameter("String", "token");
        description.addParameter("String", "format");
        description.addParameter("String", "callback");
        description.addParameter("Date", "date");
        description.addParameter("Integer", "seqid");
        description.addParameter("Boolean", "delete");
        description.addParameter("Integer", "seqid");
        description.addParameter("Integer", "(optional) poid");
        description.addParameter("Integer", "(optional) newseq");
        description.addParameter("Date", "(optional) arrived");
        description.addParameter("Date", "(optional) off");
        description.addParameter("String", "(optional) comment");
        description.setReturnValue("json String");
        description.setDescription("This service is used to update the blog when the user performs an edit action.");
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if(request.getParameter("info")!=null)
			response.getOutputStream().print(this.description.serialize());
		else{
			
			String token = (String) request.getParameter("token");
			
			PersistentHashMapClient hashClient = new PersistentHashMapClient();
			int usid = hashClient.getUserId(token);
			
			String date = (String) request.getParameter("date");
			
			int seqID = Integer.parseInt((String) request.getParameter("seqid"));
			
			MicroBlog blogToUpdate = new MicroBlog();
			
			blogToUpdate.setSeqNumber(seqID);
			blogToUpdate.setUserID(usid);
			
			response.setHeader("Content-Type", "application/json");
			response.setHeader("charset", "utf-8");
			response.setCharacterEncoding("UTF-8");
			PrintWriter out = response.getWriter();
			String format = request.getParameter("format");
			String answer="test";
			
			
				blogToUpdate.setDate(date);
				List<HashMap<String, String>> results = blogToUpdate.read();
				
				if(results.size()==1){
				System.out.println("right path");
				if(request.getParameter("delete").equals("false")){
					String arrived,off,comment;
					Integer newSeqId,newPoiID;
					if(request.getParameter("arrived")!=null)
						arrived=(String) request.getParameter("arrived");
					else arrived = null;
					if(request.getParameter("off")!=null)
						off=(String) request.getParameter("off");
					else off = null;
					if(request.getParameter("comment")!=null)
						comment=(String) request.getParameter("comment");
					else comment = null;
					if(request.getParameter("newseq")!=null)
						newSeqId =Integer.parseInt((String) request.getParameter("newseq"));
					else newSeqId = null;
					if(request.getParameter("poid")!=null)
						newPoiID =Integer.parseInt((String) request.getParameter("poid"));
					else newPoiID = null;
					
					if(blogToUpdate.updateBlog(newSeqId,newPoiID, arrived, off, comment)){
						answer="{\"result\":\"true\"}";
					}else{
						System.out.println("gamw to kerato");
						answer="{\"result\":\"false\"}";
					}
				}else{
					if(blogToUpdate.deleteBlog()){
						answer="{\"result\":\"true\"}";
					}else{
						answer="{\"result\":\"false\"}";
					}
				}
				}else{
					System.out.println("what the hell??");
					answer="{\"result\":\"false\"}";
				}
				
				if(format.equals("jsonp")){
					String callback = request.getParameter("callback");
					out.write(callback+"("+answer+")");
				}
				else if(format.equals("json"))
					out.write(answer);
				
			
			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
