package snstorage;

import gr.ntua.ece.cslab.modissense.queries.containers.UserPoiStruct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

public class TextRepo{

	public static final String TABLE_NAME = "TextRepo";
	public static final String COLUMN_FAMILY = "t";
	public static String QUALIFIER;
		
	private static final long INIT_NUM_OF_POIS = 10000;
	private static final char SMALLEST_SN = 'F';
	
	public static final int KEY_OFFSET = 50000;
	public static final int MAX_USERS_BATCH = 100;
	
	public static final int INITIAL_REGIONS = 4;
	
	private HTable table;
	/*
	 * At table creation time, we want to pre-split keyspace to shard keys
	 * to INITIAL_REGIONS.MAX_INITIAL_KEY is computed according to the available
	 * data at the time being
	 */
	private byte[] MIN_INITIAL_KEY;
	private byte[] MAX_INITIAL_KEY;
	
	public void computeInitialKeys(){
		UserPoiStruct pu1 = new UserPoiStruct(SMALLEST_SN,0L, 0L);
		MIN_INITIAL_KEY = pu1.getBytes();
		
		UserPoiStruct pu2 = new UserPoiStruct(SMALLEST_SN, 0L,INIT_NUM_OF_POIS);
		MAX_INITIAL_KEY = pu2.getBytes();
	}
	
	public void createTable() throws IOException{
		HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		if(admin.tableExists(TABLE_NAME)){
			admin.disableTable(TABLE_NAME);
			admin.deleteTable(TABLE_NAME);
		}
		HTableDescriptor descripton = new HTableDescriptor(TABLE_NAME);
		descripton.addFamily(new HColumnDescriptor(COLUMN_FAMILY.getBytes()));
		computeInitialKeys();
		admin.createTable(descripton, MIN_INITIAL_KEY, MAX_INITIAL_KEY, INITIAL_REGIONS);
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
		
		System.out.println("This program manages the TextRepo Hbase table.");
		System.out.println("In order to create TextRepo table press:\tc/C");
		System.out.println("In order to delete TextRepo table press:\td/D");
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String option= br.readLine();
			
			if(option.equals("c")||option.equals("C")){
				System.out.println("Creating TextRepo...");
				TextRepo schema = new TextRepo();
				schema.createTable();
			}
			else if(option.equals("d")||option.equals("D")){
				System.out.println("Delete TextRepo.");
				TextRepo schema = new TextRepo();
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
