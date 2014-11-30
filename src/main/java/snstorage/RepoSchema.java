package snstorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.hadoop.hbase.client.HTableUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import facebook.entities.FbCheckinObject;
import facebook.entities.FbStatus;
import foursquare.entities.FoursquareCheckin;

/**
* HBase schema for Modissense
* unprocessed data
* 
* @author Giagkos Mytilinis
*/

public class RepoSchema extends ModissenseTable{
	
	/*
	 *  We assume the following schema:
	 *  
	 *  There is one table("SNData") which consists of only one column
	 *  family("data"). There is one row for each user. The key of the row is:
	 *  ["fb"/"fq"/"tw"].toBytes+snid.toBytes. 
	 *  Each new checkin or update is inserted in the appropriate row, as a new column.
	 *  The qualifier of the row is the checkin or status ID.  
	 */

	public static String TABLE_NAME = "SNData";
	public static String COLUMN_FAMILY = "d"; // d stands for data.
	
	/*
	 *  The thrown IOException of the createTable and dropTable methods can be
	 *  MasterNotRunningException, ZooKeeperConnectionException
	 *  or some else IOException. This info is useful for debugging messages.
	 */
	
	public Put createPutFbCheckin(FbCheckinObject fbCheckin){
		
		//System.out.println(fbCheckin.toString());
		
		String rowKey = "fb"+fbCheckin.getUserID();
		//System.out.println("Row key: "+rowKey);
		
		Put put = new Put(Bytes.toBytes(rowKey));
        try {
			put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes(fbCheckin.getID()), fbCheckin.getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        return put;
	}
	
	public Put createPutFbStatus(FbStatus fbStatus){
		
		String rowKey = "fb"+fbStatus.getUser();
		
		Put put = new Put(Bytes.toBytes(rowKey));
        try {
			put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes(fbStatus.getID()), fbStatus.getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        return put;
	}
	
	
	public Put createPutFqCheckin(FoursquareCheckin checkin){
		
		String rowKey = "fq"+checkin.getUserID();
		
		Put put = new Put(Bytes.toBytes(rowKey));
        try {
			put.add(Bytes.toBytes(COLUMN_FAMILY), Bytes.toBytes(checkin.getID()), checkin.getBytes());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        return put;
	}
	
	public void insertUpdates(List<Put> updates) throws IOException{
		
		this.createConnectionToHTable();
		table.setAutoFlush(false); // If autoFlush = false, these messages are not sent until the write-buffer is filled.
		HTableUtil.bucketRsPut(table, updates); // Group Puts by RegionServer
		this.closeConnectionToHTable();
	}
	
	public static void main(String[] args) {
		
		System.out.println("This program manages the SNData Hbase table.");
		System.out.println("In order to create SNData table press:\tc/C");
		System.out.println("In order to delete SNData table press:\td/D");
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String option= br.readLine();
			
			if(option.equals("c")||option.equals("C")){
				System.out.println("Creating SNData...");
				RepoSchema schema = new RepoSchema();
				schema.createTable();
			}
			else if(option.equals("d")||option.equals("D")){
				System.out.println("Delete SNData.");
				RepoSchema schema = new RepoSchema();
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
