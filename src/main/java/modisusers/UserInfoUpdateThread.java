package modisusers;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;

import snstorage.UserGraph;


/**
* Thread which reads user ids and social network tokens
* from postgres and returns, from the connected social networks,
* updated info for the selected users.
* 
* @author giagulei
*/

public class UserInfoUpdateThread extends Thread{
	
	private static final char FACEBOOK = 'F';
	private static final char FOURSQUARE = 'f';
	private static final char TWITTER = 't';
	private static final boolean DEBUG = true;
	
	private List<HashMap<String, String>> snresults;
	private int startIndex;
	private int endIndex;
	
	private LinkedList<Put> hbasePuts;
	private int threadID;
	
	public UserInfoUpdateThread(int threadID){
		this.threadID = threadID;
		this.hbasePuts = new LinkedList<>();
	}
	
	/**
	 * sets all postgres results and from them reads only specified rows. 
	 * @param snresults
	 */
	public void setResults(List<HashMap<String, String>> snresults){
		this.snresults = snresults;
	}
	
	/**
	 * sets the index, in the postgres table, of the start row of the range for which
	 * the current thread is responsible.
	 * @param startIndex
	 */
	public void setStartIndex(int startIndex){
		this.startIndex = startIndex;
	}
	
	/**
	 * sets the index, in the postgres table, of the last row of the range for which
	 * the current thread is responsible.
	 * @param endIndex
	 */
	public void setEndIndex(int endIndex){
		this.endIndex = endIndex;
	}
	
	/**
	 * returns the list of HBase Puts which is created after contacting
	 * the different social networks.
	 * @return
	 */
	public LinkedList<Put> getPuts(){
		return hbasePuts;
	}
	
	
	public void run(){
		if(DEBUG) System.out.println("Thread "+threadID+":Results["+startIndex+", "+endIndex+"] running.");
		
		for(int i=startIndex;i<=endIndex;i++){
			String snid = snresults.get(i).get("sn_identifier");
			String token = snresults.get(i).get("sn_token");
			String tokenSecret = snresults.get(i).get("sn_token_secret");
			String network = snresults.get(i).get("sn_name");
			
			long start = System.currentTimeMillis();
			SNFriendsGetter snGetter;
			if(network.equals("facebook")){
				snGetter = new SNFriendsGetter(snid, token);
				snGetter.setSocialNetwork(FACEBOOK);
				try {
					snGetter.getFacebookFriends();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(DEBUG) System.out.println("Facebook update: "+(System.currentTimeMillis()-start)+" ms");
			}else if(network.equals("foursquare")){
				snGetter = new SNFriendsGetter(snid, token);
				snGetter.setSocialNetwork(FOURSQUARE);
				try {
					snGetter.getFoursquareFriends();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(DEBUG) System.out.println("Foursquare update: "+(System.currentTimeMillis()-start)+" ms");
			}else{
				snGetter = new SNFriendsGetter(snid, token,tokenSecret);
				snGetter.setSocialNetwork(TWITTER);
				try {
					snGetter.getTwitterFriends();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			LinkedList<String> fids = snGetter.getFriendIDs();
			HashMap<String, String> fNames = snGetter.getFriendNames();
			HashMap<String, String> fImages = snGetter.getFriendImages();
			
			FriendIDs newFriend = new FriendIDs(snGetter.getSocialNetwork(),Long.parseLong(snid));
			FriendsInfo fInfo = new FriendsInfo(snGetter.getSocialNetwork(), Long.parseLong(snid));
			for(String id:fids){
				newFriend.addFriend(Long.parseLong(id));
				fInfo.addFriendName(fNames.get(id));
				fInfo.addFriendImage(fImages.get(id));
			}
			
			UserGraph usersTable = new UserGraph();
			hbasePuts.add(usersTable.storeFriend(newFriend));
			hbasePuts.add(usersTable.storeFriendInfo(fInfo));
			
		}
	}
	
	
}
