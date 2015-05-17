package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import modisusers.FriendIDs;
import modisusers.FriendsInfo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;

import snstorage.UserGraph;

public class UserDetailsTable {

	public static final int USER_KEY_LENGTH = (Long.SIZE+Character.SIZE)/8;

	public static final String TABLE_NAME = "UserDetailsTable";
	public static final String COLUMN_FAMILY = "cf"; 
	public static final String QUALIFIER = "f";

	public static final int KEY_OFFSET = 50000; // how many users are expected to exist in each initial region

	public static final int INITIAL_REGIONS = 4; // as many as the num of nodes in the cluster

	private HTable table;

	public void createTable() throws IOException{
		HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		if(admin.tableExists(TABLE_NAME)){
			admin.disableTable(TABLE_NAME);
			admin.deleteTable(TABLE_NAME);
		}

		HTableDescriptor descripton = new HTableDescriptor(TABLE_NAME);

		descripton.addFamily(new HColumnDescriptor(COLUMN_FAMILY.getBytes()));

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

	
	public static void populate() throws IOException{
		Scan s = new Scan();
		s.addColumn(Bytes.toBytes(UserGraph.COLUMN_FAMILY1), Bytes.toBytes("f"));
		s.addColumn(Bytes.toBytes(UserGraph.COLUMN_FAMILY2), Bytes.toBytes("f"));

		UserGraph userTable = new UserGraph();
		userTable.createConnectionToHTable();
		ResultScanner scanner = userTable.getTable().getScanner(s);

		LinkedList<Put> commits = new LinkedList<Put>();
		for (Result rr = scanner.next(); rr != null; rr = scanner.next()) {

			byte[] ids = rr.getValue(Bytes.toBytes(UserGraph.COLUMN_FAMILY1), Bytes.toBytes("f"));
			byte[] restInfo = rr.getValue(Bytes.toBytes(UserGraph.COLUMN_FAMILY2), Bytes.toBytes("f"));

			if(ids!=null&&restInfo!=null){

				FriendIDs fids = new FriendIDs();
				fids.parseCompressedBytes(ids);
				
				FriendsInfo finfo = new FriendsInfo();
				finfo.parseCompressedBytes(restInfo);
				
				int listSize = fids.getFriends().size();
				for(int i=0;i<listSize;i++){
//					UserIdStruct u = new UserIdStruct();
//					u.parseBytes(fids.getFriends().get(i));
//					System.out.println(u.toString()+" "+finfo.getFriendNames().get(i)+" "+finfo.getFriendsImages().get(i));
					
					Put userPut = new Put(fids.getFriends().get(i));
					userPut.add(Bytes.toBytes("cf"), Bytes.toBytes("f"), 
							getInfoBytes(finfo.getFriendNames().get(i), finfo.getFriendsImages().get(i)));
					
					commits.add(userPut);
					
				}
			}
		}
		scanner.close();
		userTable.closeConnectionToHTable();
		
		UserDetailsTable table = new UserDetailsTable();
		table.commitUpdates(commits);
	}
	
	public static byte[] getInfoBytes(String name, String photo) throws UnsupportedEncodingException{
		int totalSize = Integer.SIZE/8 + name.getBytes("UTF-8").length
				+ Integer.SIZE/8 + photo.getBytes("UTF-8").length;

		byte[] serializable = new byte[totalSize];

		ByteBuffer buffer = ByteBuffer.wrap(serializable);

		byte[] nameBytes = name.getBytes("UTF-8");
		buffer.putInt(nameBytes.length);
		buffer.put(nameBytes);
		byte[] imgBytes = photo.getBytes("UTF-8");
		buffer.putInt(imgBytes.length);
		buffer.put(imgBytes);
		
		return serializable;
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


	public static void main(String[] args) {

		System.out.println("This program manages the UserDetailsTable Hbase table.");
		System.out.println("In order to create UserDetailsTable table press:\tc/C");
		System.out.println("In order to delete UserDetailsTable table press:\td/D");
		System.out.println("In order to populate press:\tp");

		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String option= br.readLine();

			if(option.equals("c")||option.equals("C")){
				System.out.println("Creating UserDetailsTable...");
				UserDetailsTable schema = new UserDetailsTable();
				schema.createTable();
			}
			else if(option.equals("d")||option.equals("D")){
				System.out.println("Delete UserDetailsTable.");
				UserDetailsTable schema = new UserDetailsTable();
				schema.dropTable();
			}
			else if(option.equals("p")){
				populate();
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
