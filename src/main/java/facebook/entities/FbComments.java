package facebook.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
 * Facebook Comment Object
 * @author giagulei
 */

public class FbComments extends SocialNetworkEntity{
	
	private String user;
	private String comment;
	
	public FbComments(){}
	
	public FbComments(String user,String comment){
		this.user = user;
		this.comment = comment;
	}
	
	public String getUser(){
		return user;
	}
	
	public String getComment(){
		return comment;
	}
	
	public void setUser(String user){
		this.user = user;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		int sizeOfUser = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.user = new String(bytes, index, sizeOfUser);
		index+=this.user.getBytes("UTF-8").length;
		
		int sizeOfComment = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.comment = new String(bytes, index, sizeOfComment);
		index+=this.comment.getBytes("UTF-8").length;
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + user.getBytes("UTF-8").length 
							+ Integer.SIZE/8 + comment.getBytes("UTF-8").length;
							
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.user.getBytes("UTF-8").length));
		buffer.put(this.user.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.comment.getBytes("UTF-8").length));
		buffer.put(this.comment.getBytes("UTF-8"));
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String comment="Comment: \n";
		comment+="\t\t -user: "+user+"\n";
		comment+="\t\t -comment: "+this.comment+"\n";
		
		return comment;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		
		FbComments fcomment = new FbComments("1234567", "Xarwpa ta dyo mou xeria ta xtypw");
		System.out.println(fcomment.toString());
		
		FbComments fcomment2 = new FbComments();
		try {
			fcomment2.parseBytes(fcomment.getBytes());
			System.out.println(fcomment2.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
