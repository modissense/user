package modisusers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import snstorage.Compressor;

/**
 * 
 * @author giagulei
 * 
 * structure with holds a list with the names and profile pictures 
 * of friends a user may have.
 * 
 * Serialization/Deserialization are supported.
 */
public class FriendsInfo extends Compressor{
	
	public static final int USER_KEY_LENGTH = (Long.SIZE+Character.SIZE)/8;
	
	private byte[] key;
	private LinkedList<String> friendNames;
	private LinkedList<String> friendsImages;
	
	private char socialNetwork;
	
	public FriendsInfo(){
		friendNames = new LinkedList<String>();
		friendsImages = new LinkedList<String>();
	}

	public FriendsInfo(char sn,long id){
		
		key = new byte[USER_KEY_LENGTH];
		friendNames = new LinkedList<String>();
		friendsImages = new LinkedList<String>();
	
		ByteBuffer buffer = ByteBuffer.wrap(key);
		buffer.putChar(sn);
		buffer.putLong(id);
		setSocialNetwork(sn);
	}
	
	/**
	 * 
	 * @return
	 * returns the current user's key.
	 */
	public byte[] getKey(){
		return key;
	}
	
	/**
	 * 
	 * @param key
	 * sets current user's key.
	 */
	public void setKey(byte[] key){
		this.key = key;
	}
	
	/**
	 * 
	 * @return
	 * return the list of names of current user's friends.
	 */
	public LinkedList<String> getFriendNames() {
		return friendNames;
	}

	/**
	 * 
	 * @param friendNames
	 * sets the list of names of current user's friends.
	 */
	public void setFriendNames(LinkedList<String> friendNames) {
		this.friendNames = friendNames;
	}

	/**
	 * 
	 * @return
	 * return the list of pictures of current user's friends.
	 */
	public LinkedList<String> getFriendsImages() {
		return friendsImages;
	}

	/**
	 * 
	 * @param friendsImages
	 * sets the list of pictures of current user's friends.
	 */
	public void setFriendsImages(LinkedList<String> friendsImages) {
		this.friendsImages = friendsImages;
	}

	/**
	 * 
	 * @return
	 * return the user's social network
	 */
	public char getSocialNetwork() {
		return socialNetwork;
	}

	/**
	 * 
	 * @param socialNetwork
	 * sets user's social network.
	 */
	public void setSocialNetwork(char socialNetwork) {
		this.socialNetwork = socialNetwork;
	}

	/**
	 * 
	 * @param name
	 * adds a friend's name to the corresponding list(friendNames)
	 */
	public void addFriendName(String name){
		friendNames.add(name);
	}
	
	/**
	 * 
	 * @param image
	 * adds a friend's picture to the corresponding list(friendImages)
	 */
	public void addFriendImage(String image){
		friendsImages.add(image);
	}
	
	/**
	 * Deserializer of the class.
	 */
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		this.friendNames = new LinkedList<>();
		int sizeOfFriendNamesList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfFriendNamesList;i++) {
			int sizeOfName = buffer.getInt(index);
			index+= Integer.SIZE/8;
			String name = new String(bytes, index, sizeOfName,"UTF-8");
			index+= sizeOfName;
			friendNames.add(name);
		}
		
//		System.out.println("names are ok!");
//		for(String s:friendNames){
//			System.out.print(s);
//		}
		
		this.friendsImages = new LinkedList<>();
		int sizeOfFriendImgList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfFriendImgList;i++) {
			int sizeOfLink = buffer.getInt(index);
			index+= Integer.SIZE/8;
			String img = new String(bytes, index, sizeOfLink,"UTF-8");
			index+= sizeOfLink;
			friendsImages.add(img);
		}
	}

	/**
	 *  Serializer of the class.
	 */
	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8;
		for(String s :friendNames)
			totalSize+= Integer.SIZE/8 + s.getBytes("UTF-8").length;
		
		totalSize += Integer.SIZE/8;
		for(String s:friendsImages)
			totalSize+= Integer.SIZE/8 + s.getBytes("UTF-8").length;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		
		buffer.putInt(friendNames.size());
		for(String fn: friendNames) {
			byte[] nameBytes = fn.getBytes("UTF-8");
			buffer.putInt(nameBytes.length);
			buffer.put(nameBytes);
		}
		buffer.putInt(friendsImages.size());
		for(String fl: friendsImages) {
			byte[] imgBytes = fl.getBytes("UTF-8");
			buffer.putInt(imgBytes.length);
			buffer.put(imgBytes);
		}
		return serializable;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println();
		FriendsInfo myUser = new FriendsInfo('f', 123L);
		
		for(int i=0;i<10;i++){
			myUser.addFriendName("test kostas");
			myUser.addFriendImage("http://myimage.com");
		}
	
		System.out.println("Initial Friend List:");
		
		for(String s:myUser.friendNames){
			System.out.print(s+" ");
		}
		
		for(String s:myUser.friendsImages){
			System.out.print(s+" ");
		}
		System.out.println();

		myUser.parseCompressedBytes(myUser.getCompressedBytes());
		
		System.out.println("Final Friend List:");
		
		for(String s:myUser.friendNames){
			System.out.print(s+" ");
		}
		
		for(String s:myUser.friendsImages){
			System.out.print(s+" ");
		}
		System.out.println();
		
	}

}
