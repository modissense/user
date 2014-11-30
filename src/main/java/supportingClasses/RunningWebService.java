package supportingClasses;

import javax.servlet.http.HttpServlet;

public abstract class RunningWebService extends HttpServlet {
	
	protected static final long serialVersionUID = 1L;
	protected String serviceName;
	protected boolean DEBUG;
	
}
