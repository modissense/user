package services;

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
@WebServlet("/")
public class IndexService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServiceDescription description;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.getWriter().print("<html>\n" +
"<head>\n" +
"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
"<title>Web services</title>\n" +
"</head>\n" +
"<body>\n" +
"<h2>Available web services</h2>\n" +
"<table>\n" +
"<tr><td><h3>User Management</h3></td></tr>\n" +
"<tr><td> <a href='/user/register/?info'>User Registration</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/logout/?info'>Log out</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/getnetworks/?info'>Get user networks</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/userinfo/?info'>Get user info from sn</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/blog/getblogs/?info'>Get all user blogs</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/blog/getmicroblog/?info'>Get one blog</a></td><td><b>Done</b></td></tr>\n" +
"<tr><td> <a href='/user/blog/updateblog/?info'>Update micro blog</a></td><td><b>Done</b></td></tr>\n" +
"</table>\n" +
"<br>\n" +
"<span>You may find the poi related API calls <a href=\"/poi/\">here</a>.</span>\n" +
"<!-- \n" +
"\n" +
" -->\n" +
"</body>\n" +
"</html>");
	}
}
