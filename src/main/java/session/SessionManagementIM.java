package session;

import java.util.HashMap;

/**
 * Class used to keep in memory session management system. The class provides a 
 * handful of static methods, implemented in a synchronized and thread safe way.
 * @author Giannis Giannakopoulos
 *
 */
public class SessionManagementIM {

	private static HashMap<String, Integer> 	sessionKey = new HashMap<String,Integer>();
	private static HashMap<Integer, String> 	userIdKey  = new HashMap<Integer,String>();
	
	private static Object transactionLock = new Object();
	
	public SessionManagementIM() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Adds a mapping between a session id and a user id
	 * @param session
	 * @param userId
	 */
	public static void addToSession(String session, Integer userId){
		synchronized (transactionLock) {
			SessionManagementIM.sessionKey.put(session, userId);
			SessionManagementIM.userIdKey.put(userId, session);
		}
	}
	
	/**
	 * Returns the user for the specified session id
	 * @param session
	 * @return
	 */
	public static Integer getUser(String session){
		synchronized (transactionLock) {
			return SessionManagementIM.sessionKey.get(session);
		}
	}
	
	public static String getSession(Integer userId){
		synchronized (transactionLock) {
			return SessionManagementIM.userIdKey.get(userId);
		}
	}
	
	/**
	 * Invalidated a session, based on the provided session id.
	 * @param session
	 */
	public static void invalidateSession(String session){
		synchronized (transactionLock) {
			int userId=SessionManagementIM.getUser(session);
			SessionManagementIM.sessionKey.remove(session);	
			SessionManagementIM.userIdKey.remove(userId);
		}
	}
	
	/**
	 * Invalidated a session, based on the provided session id.
	 * @param session
	 */
	public static void invalidateSession(Integer userId){
		synchronized (transactionLock) {
			String sessionId=SessionManagementIM.userIdKey.get(userId);
			SessionManagementIM.sessionKey.remove(sessionId);	
			SessionManagementIM.userIdKey.remove(userId);
		}
	}
	
	/**
	 * Invalidates ALL session : please use with care :).
	 */
	public static void invalidateSession() {
		synchronized (transactionLock) {
			SessionManagementIM.sessionKey.clear();
			SessionManagementIM.userIdKey.clear();
		}
	}
	
	/**
	 * Returns the user for the specified session id
	 * @param session
	 * @return
	 */
	public static boolean containsToken(String session){
		synchronized (transactionLock) {
			return SessionManagementIM.sessionKey.containsKey(session);
		}
	}
	
	public static boolean containsUser(int userid){
		synchronized (transactionLock) {
			return SessionManagementIM.userIdKey.containsKey(userIdKey);
		}
	}

}
