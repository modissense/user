package facebook.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
 * Facebook Status Object
 * @author giagulei
 */

public class FbStatus extends SocialNetworkEntity{
	
	private String id; //it is not a long. It contains an underscore character as well.
	private String user; //user's id is a long
	private String message;
	private int isProcessed; // binary variable. Value 0 indicates unprocessed data
	//	while a value of 1 indicates processed. 
	private String entityKind = "status";
	
	private static boolean DEBUG = true;
	
	public FbStatus(){}
	
	public FbStatus(String id){
		this.id = id;
	}
	
	public void setID(String id){
		this.id = id;
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setIsProcessed(int isProcessed){
		this.isProcessed = isProcessed;
	}
	
	public String getID(){
		return id;
	}
	
	public String getUser(){
		return user;
	}
	
	public String getMessage(){
		return message;
	}
	
	public int getIsProcessed(){
		return isProcessed;
	}
	
	public String getEntityKind(){
		return entityKind;
	}
	
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		
		int sizeOfID = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.id = new String(bytes, index, sizeOfID);
		index+=this.id.getBytes("UTF-8").length;
		
		int sizeOfUser = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.user = new String(bytes, index, sizeOfUser);
		index+=this.user.getBytes("UTF-8").length;
		
		int sizeOfMessage = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.message = new String(bytes, index, sizeOfMessage);
		index+=this.message.getBytes("UTF-8").length;
		
		this.isProcessed = buffer.getInt(index);
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + id.getBytes("UTF-8").length				 	
							+ Integer.SIZE/8 + user.getBytes("UTF-8").length
							+ Integer.SIZE/8 + message.getBytes("UTF-8").length;
		
		/*if(DEBUG){
			System.out.println("getBytes: ");
			System.out.println("\t totalSize = "+totalSize);
			System.out.println("\t id length = "+this.id.length()+" should be "+this.id.getBytes().length);
			System.out.println("\t user length = "+this.user.length()+" should be "+this.user.getBytes().length);
			System.out.println("\t message length = "+this.message.length()+" should be "+this.message.getBytes().length);
			System.out.println();
		}*/
		
		totalSize+= Integer.SIZE/8;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.id.getBytes("UTF-8").length));
		buffer.put(this.id.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.user.getBytes("UTF-8").length));
		buffer.put(this.user.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.message.getBytes("UTF-8").length));
		buffer.put(this.message.getBytes("UTF-8"));
		
		buffer.putInt(this.isProcessed);
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String status = "Status (";
		status+="id= "+id+"): user= "+user+" \n";
		status+="posted message = "+message+"\n\n";
		return status;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		
		FbStatus st = new FbStatus("1234567");
		st.setUser("1020383003");
		st.setMessage("Χρονια πολλα κοπελαρα μου,να τα εκατοστησεις!!Να εχεις υγεια ευτυχια και οτι επιθυμεις να το αποκτησεις!!Να σαι παντα καλα..!!! :D");
		//st.setMessage("You are the best the best the best the best the best the best the best the best the best the best the best the best of all!!!!!");
		System.out.println(st.toString());
		
		FbStatus st2 = new FbStatus();
		if(DEBUG)
			try {
				System.out.println("Bytes created: "+st.getBytes().length);
				
				st2.parseBytes(st.getBytes());
				
				System.out.println(st2.toString());
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
	}

}
