package modisusers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.hbase.client.Put;

import snstorage.UserGraph;

public class LoadGraphFromFile {
	
	public static void main(String[] args) {
		
		if(args.length!=2){
			System.out.println("Wrong arguments: GraphFile and Social Network expected.");
			System.exit(1);
		}
		
		String graphFileToLoad = args[0];
		char socialNetwork = args[1].charAt(0);
		
		System.out.println("SN = "+socialNetwork);
		
		LinkedList<Put> usersToInsert = new LinkedList<Put>();
		
		 BufferedReader br = null;
         try {
			br = new BufferedReader(new FileReader(graphFileToLoad));
			String line;
			int counter = 0;
			UserGraph userSchema = new UserGraph();
	         while ((line = br.readLine())!=null){
	        	 counter++;
	        	 String[] keyValue = line.split("\t");
	        	 
	        	 String key = keyValue[0];
	        	 FriendIDs newUser = new FriendIDs(socialNetwork,Long.parseLong(key));
	        	 
	        	 if(keyValue.length == 2){
		        	 String value = keyValue[1];
		        	 String[] friends = value.split(",");
		        	 for(int i=0;i<friends.length;i++){
		        		 newUser.addFriend(Long.parseLong(friends[i]));
		        	 }
	        	 }
	        	 
	        	 usersToInsert.add(userSchema.storeFriend(newUser));
	        	 if(counter%UserGraph.MAX_USERS_BATCH==0){
	        		 userSchema.commitUpdates(usersToInsert);
	        		 usersToInsert = new LinkedList<Put>();
	        	 }
	         }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
         
       
	}
}
