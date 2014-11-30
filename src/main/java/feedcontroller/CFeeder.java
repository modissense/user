package feedcontroller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.client.Put;

import snstorage.RepoSchema;
import entities.SNDetails;

/**
 * 
 * @author giagulei
 * 
 */

public class CFeeder extends Feeder{
	
	private static int NUM_THREADS = 4;
	/*
	 * We need a set and not a list since there is a possibility the intersection
	 * of the updates collected by different users not to be the emty set.
	 */
	private HashSet<Put> hbasePuts;
	private List<Put> putsToCommit;
	
	private FileOutputStream fos;
	private PrintStream ps;
	
	public CFeeder() throws FileNotFoundException{
		hbasePuts = new HashSet<Put>();
		putsToCommit = new LinkedList<Put>();
		
		fos = new FileOutputStream(new File("cfeeder_exception"), true);
		ps = new PrintStream(fos);
	}
	
	public void run() {
		getFeed();
	}
	
	public void getFeed(){
		
		until = Integer.toString((int)(System.currentTimeMillis()/1000L));
		
		SNDetails socialNetworkInfo = new SNDetails();
		List<HashMap<String, String>> snresults = socialNetworkInfo.read();
			
		/*System.out.println("RESULTS");
		for(HashMap<String, String> h:snresults){
			System.out.println(h.get("sn_identifier"));
		}*/
		
		LinkedList<CFeederThread> pgThreads = new LinkedList<CFeederThread>();
		
		System.out.println("NUM_THREADS = "+NUM_THREADS);
		System.out.println("Results = "+snresults.size());
		
		int portion = snresults.size()/NUM_THREADS;
		int index = 0;
		for(int i=0;i<NUM_THREADS;i++){
			
			CFeederThread thread = new CFeederThread(i);
			thread.setResults(snresults);
			//if(i!=0)
				thread.setStartIndex(index);
			//else thread.setStartIndex(index);
			index+=portion;
			if(snresults.size()%NUM_THREADS!=0&&i==NUM_THREADS-1)
				thread.setEndIndex(snresults.size()-1);
			else
				thread.setEndIndex(index-1);
			thread.setSince(since);
			thread.setUntil(until);
			pgThreads.add(thread);
		}
		
		for(Thread t:pgThreads){
			t.start();
		}
		
		for(Thread t:pgThreads){
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace(ps);
			}
		}
		
		since = Integer.toString((int)(System.currentTimeMillis()/1000L));
		
		for(CFeederThread t:pgThreads){
			hbasePuts.addAll(t.getPuts());
		}
		
		this.putsToCommit.addAll(hbasePuts);
		/*RepoSchema sch = new RepoSchema();
		try {
			sch.insertUpdates(putsToCommit);
		} catch (IOException e) {
			e.printStackTrace(ps);
		}*/
	}
	
	/*public void getFeed(){
		
		User users = new User();
		List<HashMap<String, String>> results = users.read();
		
		until = Integer.toString((int)(System.currentTimeMillis()/1000L));
		
		
		 * We need a set and not a list since there is a possibility the intersection
		 * of the updates collected by different users not to be the emty set.
		 
		HashSet<Put> hbasePuts = new HashSet<Put>();
		
		int rsize=results.size(); 
		for(int i=0;i<rsize;i++){
			SNDetails socialNetworkInfo = new SNDetails();
			socialNetworkInfo.setSnName("facebook");
			socialNetworkInfo.setUserId(Integer.parseInt(results.get(i).get("id")));
			List<HashMap<String, String>> snresults = socialNetworkInfo.read();
			
			if(!snresults.isEmpty()){
				String fbid = snresults.get(0).get("sn_identifier");
				String token = snresults.get(0).get("sn_token");

				GetFeed fbFeed = new GetFeed(fbid,token,since,until);
				LinkedList<Put> putsReturned = fbFeed.getFbFeed();
				if(putsReturned!=null&&putsReturned.size()>0)
					hbasePuts.addAll(putsReturned);
			}
		}
		List<Put> finalPuts = new LinkedList<>();
		for(Put p:hbasePuts){
			finalPuts.add(p);
		}
		// commit the puts to hbase
		RepoSchema schema = new RepoSchema();
		try {
			schema.insertFbUpdates(finalPuts);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		since = Integer.toString((int)(System.currentTimeMillis()/1000L));
	}
	*/

}
