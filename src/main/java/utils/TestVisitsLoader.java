package utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import gr.ntua.ece.cslab.modissense.queries.containers.POI;
import gr.ntua.ece.cslab.modissense.queries.containers.UserIdStruct;

public class TestVisitsLoader {
	
	public void seeVisitsOfUser(long userID){
		UserIdStruct user = new UserIdStruct('F', userID);
		Configuration config = HBaseConfiguration.create();
		HTable table = null;
		try {
			table = new HTable(config, "UserCheckins50k");
			Get get = new Get(user.getBytes());
			Result r = table.get(get);
			
			for(Map.Entry<byte[], byte[]> e : r.getFamilyMap("cf".getBytes()).entrySet()) {
                POI p = new POI();
                long timestamp = Bytes.toLong(e.getKey());
                java.sql.Timestamp tstamp = new Timestamp(timestamp);
                System.out.println("UNIX time: "+timestamp);
                System.out.println("Date: "+tstamp);
                p.parseBytes(e.getValue());
                System.out.println(p.getName()+" ("+p.getX()+","+p.getY()+")with score: "+p.getScore());
                System.out.println();
            }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		TestVisitsLoader tv = new TestVisitsLoader();
		tv.seeVisitsOfUser(605109625);

	}

}
