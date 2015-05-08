package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
/**
 * 
 * @author giagulei
 *	
 * Table used for storing user graph/profile related information
 */

public class UserCheckinsTable{
	
	public static final int USER_KEY_LENGTH = (Long.SIZE+Character.SIZE)/8;

	public static final String TABLE_NAME = "UserCheckins50k";
	public static final String COLUMN_FAMILY = "cf"; 
	
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
	
	public static void main(String[] args) {
		
		System.out.println("This program manages the UserCheckins50k Hbase table.");
		System.out.println("In order to create ModisUsers table press:\tc/C");
		System.out.println("In order to delete ModisUsers table press:\td/D");
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String option= br.readLine();
			
			if(option.equals("c")||option.equals("C")){
				System.out.println("Creating ModisUsers...");
				UserCheckinsTable schema = new UserCheckinsTable();
				schema.createTable();
			}
			else if(option.equals("d")||option.equals("D")){
				System.out.println("Delete ModisUsers.");
				UserCheckinsTable schema = new UserCheckinsTable();
				schema.dropTable();
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
