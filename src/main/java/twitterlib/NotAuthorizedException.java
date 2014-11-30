package twitterlib;

public class NotAuthorizedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1054695642720488768L;

	private static final String errorMessage = "There has not been created authorization header for this twitter request."
			+ " Create authorization header or make unauthorized request.";
	
	public NotAuthorizedException(){
		super(errorMessage);
	}
	
}
