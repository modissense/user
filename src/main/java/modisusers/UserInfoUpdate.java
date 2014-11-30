package modisusers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.hbase.client.Put;

import snstorage.UserGraph;
import entities.SNDetails;


/**
 * 
 * @author giagulei
 * periodic task which contacts social networks and collects
 * information about registered user profiles
 */
public class UserInfoUpdate extends TimerTask{
	
	private static int NUM_THREADS = 4;
	/*
	 * We need a set and not a list since there is a possibility the intersection
	 * of the updates collected by different users not to be the emty set.
	 */
	private LinkedList<Put> hbasePuts;
	
	private FileOutputStream fos;
	private PrintStream ps;
	
	private static final boolean DEBUG=true;
	
	public UserInfoUpdate() throws FileNotFoundException{
		hbasePuts = new LinkedList<>();
		
		fos = new FileOutputStream(new File("uinfo_update_exception"), true);
		ps = new PrintStream(fos);
	}
	
	public void run() {
		getUpdatedUserInfo();
	}
	
	/**
	 * spawns multiple threads which contact social networks, collect information
	 * and each of them returns a list of HBase Puts.
	 * 
	 * Then, the current method, joins the returned Puts and commit them to HBase.
	 */
	public void getUpdatedUserInfo(){
		
		SNDetails socialNetworkInfo = new SNDetails();
		List<HashMap<String, String>> snresults = socialNetworkInfo.read();
			
		LinkedList<UserInfoUpdateThread> pgThreads = new LinkedList<UserInfoUpdateThread>();
		
		if(DEBUG){
			System.out.println("NUM_THREADS = "+NUM_THREADS);
			System.out.println("Results = "+snresults.size());
		}
		
		int portion = snresults.size()/NUM_THREADS;
		int index = 0;
		for(int i=0;i<NUM_THREADS;i++){
			
			UserInfoUpdateThread thread = new UserInfoUpdateThread(i);
			thread.setResults(snresults);
			
			thread.setStartIndex(index);
		
			index+=portion;
			if(snresults.size()%NUM_THREADS!=0&&i==NUM_THREADS-1)
				thread.setEndIndex(snresults.size()-1);
			else
				thread.setEndIndex(index-1);
			
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
		
		for(UserInfoUpdateThread t:pgThreads){
			hbasePuts.addAll(t.getPuts());
		}
		
		UserGraph usersTable = new UserGraph();
		try {
			usersTable.commitUpdates(hbasePuts);
		} catch (IOException e) {
			e.printStackTrace(ps);
		}
		
		System.out.println("Updates are commited.");
	}
	
	public static void main(String[] args) {
		Timer timer = new Timer("PeriodicUsersUpdates");
		UserInfoUpdate update;
		try {
			update = new UserInfoUpdate();
			//it runs once a day
			timer.scheduleAtFixedRate(update, 0, 24*3600*1000);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
