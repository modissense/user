package modisusers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import snstorage.Compressor;
import snstorage.UserGraph;

/**
 * 
 * @author giagulei
 * 
 * structure with holds a list with the keys of friends
 * a user may have.
 * 
 * Serialization/Deserialization are supported.
 */
public class FriendIDs extends Compressor{
	
	//public static final int USER_KEY_LENGTH = (Long.SIZE+Character.SIZE)/8;
	
	
	private byte[] key;
	private LinkedList<byte[]> friends;
	
	private char socialNetwork;
	
	public FriendIDs(){
		friends = new LinkedList<byte[]>();
	}

	public FriendIDs(char sn,long id){
		
		key = new byte[UserGraph.USER_KEY_LENGTH];
		friends = new LinkedList<byte[]>();
		
		ByteBuffer buffer = ByteBuffer.wrap(key);
		buffer.putChar(sn);
		buffer.putLong(id);
		socialNetwork = sn;
	}
	
	/**
	 * 
	 * @return
	 * returns the current user's key
	 */
	public byte[] getKey(){
		return key;
	}
	
	/**
	 * 
	 * @return
	 * returns a list with the keys of current user's friends, as they
	 * are represented in our HBase table 
	 */
	public LinkedList<byte[]> getFriends(){
		return friends;
	}
	
	/**
	 * 
	 * @param key
	 * sets the current user's key
	 */
	public void setKey(byte[] key){
		this.key = key;
	}
	
	/**
	 * 
	 * @param friends
	 * sets the list with current user's friends.
	 */
	public void setFriends(LinkedList<byte[]> friends){
		this.friends = friends;
	}
	
	/**
	 * 
	 * @param id
	 * takes the unique social network(facebook, foursquare, twitter) id of
	 * a friend and transforms it to the appropriate form, in order to be 
	 * inserted in our system. 
	 * 
	 * Then it adds the new id in the list of current user's friends.
	 */
	public void addFriend(long id){
		byte[] newFriend = new byte[UserGraph.USER_KEY_LENGTH];
		ByteBuffer buffer = ByteBuffer.wrap(newFriend);
		buffer.putChar(socialNetwork);
		buffer.putLong(id);
		friends.add(newFriend);
	}
	
	/**
	 * friend list deserializer
	 */
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		this.friends = new LinkedList<byte[]>();
		int sizeOfFriendsList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfFriendsList;i++) {
			byte[] newFriend = new byte[UserGraph.USER_KEY_LENGTH];
			for(int j=0;j< UserGraph.USER_KEY_LENGTH;j++)
				newFriend[j] = bytes[index+j];
			index+= UserGraph.USER_KEY_LENGTH;
			friends.add(newFriend);
		}
		
	}

	/**
	 * friend list serializer
	 */
	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8;
		
		for(int i=0;i<friends.size();i++)
			totalSize+= UserGraph.USER_KEY_LENGTH;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		
		buffer.putInt(friends.size());
		for(byte[] friendKey: friends) {
			buffer.put(friendKey);
		}
		
		return serializable;
	}
	
	
	public static void main(String[] args) {
		
		System.out.println();
		FriendIDs myUser = new FriendIDs('f', 123L);
		
		for(int i=0;i<10;i++)
			myUser.addFriend(i);
	
		System.out.println("Initial Friend List:");
		for(byte[] b:myUser.friends){
			ByteBuffer buffer = ByteBuffer.wrap(b);
			System.out.print(buffer.getChar(0));
			System.out.print(buffer.getLong(Character.SIZE/8)+" ");
		}
		System.out.println();

		myUser.parseCompressedBytes(myUser.getCompressedBytes());
		
		System.out.println("Final Friend List:");
		for(byte[] b:myUser.friends){
			ByteBuffer buffer = ByteBuffer.wrap(b);
			System.out.print(buffer.getChar(0));
			System.out.print(buffer.getLong(Character.SIZE/8)+" ");
		}
		System.out.println();
		
	}

}
