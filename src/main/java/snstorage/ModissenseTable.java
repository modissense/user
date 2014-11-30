package snstorage;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;

public abstract class ModissenseTable {
	
	public static String TABLE_NAME;
	public static String COLUMN_FAMILY; 
	public static int INITIAL_REGIONS = 4;
	
	protected HTable table;
	
	public void createTable() throws IOException{
		HBaseAdmin admin = new HBaseAdmin(HBaseConfiguration.create());
		if(admin.tableExists(TABLE_NAME)){
			admin.disableTable(TABLE_NAME);
			admin.deleteTable(TABLE_NAME);
		}

		HTableDescriptor descripton = new HTableDescriptor(TABLE_NAME);
		descripton.addFamily(new HColumnDescriptor(COLUMN_FAMILY.getBytes()));
		
		admin.createTable(descripton);
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

}
