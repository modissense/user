package snstorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import modisusers.FriendIDs;
import modisusers.FriendsInfo;
import modisusers.UserIdStruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 
 * @author giagulei
 *	
 * Table used for storing user graph/profile related information
 */

public class UserGraph{
	
	public static final int USER_KEY_LENGTH = (Long.SIZE+Character.SIZE)/8;

	public static final String TABLE_NAME = "ModisUsers";
	public static final String COLUMN_FAMILY1 = "ids"; //only the ids of the friends-useful for indexing
	public static final String COLUMN_FAMILY2 = "mi"; //more info-names and pictures of friends
	public static final String QUALIFIER = "f";
	
	public static final int KEY_OFFSET = 50000; // how many users are expected to exist in each initial region
	public static final int MAX_USERS_BATCH = 100;
	
	public static final int INITIAL_REGIONS = 4; // as many as the num of nodes in the cluster
	
	private HTable table;
	
	public void createTable() throws IOException{
		HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		if(admin.tableExists(TABLE_NAME)){
			admin.disableTable(TABLE_NAME);
			admin.deleteTable(TABLE_NAME);
		}

		HTableDescriptor descripton = new HTableDescriptor(TABLE_NAME);
		
		descripton.addFamily(new HColumnDescriptor(COLUMN_FAMILY1.getBytes()));
		descripton.addFamily(new HColumnDescriptor(COLUMN_FAMILY2.getBytes()));
		
		byte[][] splitKeys = new byte[INITIAL_REGIONS][USER_KEY_LENGTH];
		int foursquareIndex=0,facebookIndex=0,twitterIndex=0;
		for(int i=0;i<INITIAL_REGIONS;i++){
			ByteBuffer buffer = ByteBuffer.wrap(splitKeys[i]);
			if(i<INITIAL_REGIONS/3){
				buffer.putChar('F'); //F for facebook
				buffer.putLong(facebookIndex);
				facebookIndex += KEY_OFFSET;
			}
			else if(i>=INITIAL_REGIONS/3&&i<=2*INITIAL_REGIONS/3){
				buffer.putChar('f'); //f for foursquare
				buffer.putLong(foursquareIndex);
				foursquareIndex += KEY_OFFSET;
			}
			else {
				buffer.putChar('t'); //t for twitter
				buffer.putLong(twitterIndex);
				twitterIndex += KEY_OFFSET;
			}
		}
		
		admin.createTable(descripton, splitKeys);
		admin.close();
	}
	
	public void dropTable() throws IOException{
		HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		admin.disableTable(TABLE_NAME);
		admin.deleteTable(TABLE_NAME);
		admin.close();
	}
	
	public void createConnectionToHTable(){
		Configuration config = HBaseConfiguration.create();
		HTable table = null;
		try {
			table = new HTable(config, TABLE_NAME);
			this.table = table;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnectionToHTable(){
		try {
			this.table.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns a Put with the user's key and a list with her friends' keys.
	 * @param info
	 * @return
	 */
	public Put storeFriendInfo(FriendsInfo info){
		Put put = new Put(info.getKey());
		put.add(Bytes.toBytes(UserGraph.COLUMN_FAMILY2), Bytes.toBytes(UserGraph.QUALIFIER), info.getCompressedBytes());
		return put;
	}
	
	/**
	 * Returns a Put with the user's key and a list with her friends' names and pictures.
	 * @param user
	 * @return
	 */
	public Put storeFriend(FriendIDs user){
		Put put = new Put(user.getKey());
		put.add(Bytes.toBytes(UserGraph.COLUMN_FAMILY1), Bytes.toBytes(UserGraph.QUALIFIER), user.getCompressedBytes());
		return put;
	}
	
	/**
	 * Given a list of Puts, it commits all the necessary updates.
	 * @param updates
	 * @throws IOException
	 */
	public void commitUpdates(LinkedList<Put> updates) throws IOException{
		if(updates.size()>0){
			createConnectionToHTable();
			table.put(updates);
			closeConnectionToHTable();
		}
	}
	
	/**
	 * Given a user, it returns a structure with her friends' names and images.
	 * @param user
	 * @return
	 * @throws IOException
	 */
	public FriendsInfo getUserFriendsInfo(UserIdStruct user) throws IOException{
		createConnectionToHTable();
		Get get = new Get(user.getBytes());
		get.addFamily(Bytes.toBytes(COLUMN_FAMILY2));
		Result r = table.get(get);
		byte[] value = r.getValue(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(QUALIFIER));
		if(value!=null){
			FriendsInfo info = new FriendsInfo(user.getC(), user.getId());
			info.parseCompressedBytes(value);
			closeConnectionToHTable();
			return info;
		}
		return null;
	}
	
	/**
	 * Given a user, it returns a structure with her friends' ids.
	 * @param user
	 * @return
	 * @throws IOException
	 */
	public FriendIDs getUserFriendsIDs(UserIdStruct user) throws IOException{
		createConnectionToHTable();
		Get get = new Get(user.getBytes());
		Result r = table.get(get);
		byte[] value = r.getValue(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(QUALIFIER));
		if(value!=null){
			FriendIDs ids = new FriendIDs(user.getC(), user.getId());
			ids.parseCompressedBytes(value);
			closeConnectionToHTable();
			return ids;
		}
		return null;
	}
	
	/**
	 * get a specific row.This method is useful only for testing
	 * purposes.
	 * @param key
	 * @throws IOException 
	 */
	public void getSpecificRow(String str) throws IOException{
		createConnectionToHTable();
		char sn = str.charAt(0);
		long id = Long.parseLong(str.substring(1));
		System.out.println(sn);
		System.out.println(id);
		UserIdStruct user = new UserIdStruct(sn, id);
		Get get = new Get(user.getBytes());
		Result r = table.get(get);
		byte[] value = r.getValue(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(QUALIFIER));
		FriendIDs ids = new FriendIDs(user.getC(), user.getId());
		ids.parseCompressedBytes(value);
		System.out.println("ids");
		for(byte[] b:ids.getFriends()){
			UserIdStruct u = new UserIdStruct();
			u.parseBytes(b);
			System.out.println(u.toString());
		}
		byte[] value1 = r.getValue(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(QUALIFIER));
	
		FriendsInfo info = new FriendsInfo(user.getC(), user.getId());
		info.parseCompressedBytes(value1);
		System.out.println("names");
		for(String s:info.getFriendNames()){
			System.out.println(s);
		}
		System.out.println("images");
		for(String s:info.getFriendsImages()){
			System.out.println(s);
		}
		closeConnectionToHTable();
	}
	
	public static void main(String[] args) {
		
		System.out.println("This program manages the ModisUsers Hbase table.");
		System.out.println("In order to create ModisUsers table press:\tc/C");
		System.out.println("In order to delete ModisUsers table press:\td/D");
		System.out.println("In order to get a specific row press:\tg");
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String option= br.readLine();
			
			if(option.equals("c")||option.equals("C")){
				System.out.println("Creating ModisUsers...");
				UserGraph schema = new UserGraph();
				schema.createTable();
			}
			else if(option.equals("d")||option.equals("D")){
				System.out.println("Delete ModisUsers.");
				UserGraph schema = new UserGraph();
				schema.dropTable();
			}
			else if(option.equals("g")){
				if(args.length!=1){
					System.out.println("Row key is needed as argument");
					System.exit(1);
				}
				UserGraph table = new UserGraph();
				table.getSpecificRow(args[0]);
			}
			else System.out.println("Invalid option.");
			
	 
		}catch(IOException io){
			System.out.println("Look at the traces.");
			System.out.println("MasterNotRunningException, ZooKeeperConnectionException and stdin read ");
			System.out.println("are some possible issues.");System.out.println();
			io.printStackTrace();
		}	

	}

}
