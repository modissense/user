package facebook.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
 * Structure which encapsulates all the information of a
 * facebook checkin.
 * 
 * Serialization/Deserialization supported.
 * @author giagulei
 *
 */

public class FbCheckinObject extends SocialNetworkEntity{
	
	private String ID; //id is not a long. It also contains an undercore character
	private String message;
	private String userID; // This is a long.
	private LinkedList<String> withUsers;
	private LinkedList<String> usersLikes;
	private Date createdAt; 
	private FbPOI place;
	private LinkedList<FbComments> comments;
	private int isProcessed; // binary variable. Value 0 indicates unprocessed data
	//	while a value of 1 indicates processed. 
	private String entityKind = "checkin";
	 
	public FbCheckinObject(){
		withUsers = new LinkedList<String>();
		usersLikes = new LinkedList<String>();
		comments = new LinkedList<FbComments>();
	} 
	
	public FbCheckinObject(String id){
		this.ID = id;
		withUsers = new LinkedList<String>();
		usersLikes = new LinkedList<String>();
		comments = new LinkedList<FbComments>();
	}
	
	public void setID(String ID){
		this.ID = ID;
	}
	
	public void setMessage(String message){
		this.message = message;
	}
	
	public void setUserID(String userID){
		this.userID = userID;
	}
	
	public void setWithUsers(LinkedList<String> withUsers){
		this.withUsers = withUsers;
	}
	
	public void setUserLikes(LinkedList<String> usersLikes){
		this.usersLikes = usersLikes;
	}
	
	public void setCreatedAt(Date createdAt){
		this.createdAt = createdAt;
	}
	
	public void setFbPOI(FbPOI place){
		this.place = place;
	}
	
	public void setComments(LinkedList<FbComments> comments){
		this.comments = comments;
	}
	
	public void setIsProcessed(int isProcessed){
		this.isProcessed = isProcessed;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getUserID(){
		return userID;
	}
	
	public String getID(){
		return ID;
	}
	
	public LinkedList<String> getWithUsers(){
		return withUsers;
	}
	
	public LinkedList<String> getUserLikes(){
		return usersLikes;
	}
	
	public Date getCreatedAt(){
		return createdAt;
	}
	
	public void setDateFromFacebookString(String fbDate){
		try {
			Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(fbDate);
			this.createdAt = date;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Date getDateFromDateToStringBytes(byte[] dateBytes){
		
		String dateString = new String(dateBytes);
		Date date = null;
		try {
			date = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public FbPOI getPlace(){
		return place;
	}
	
	public LinkedList<FbComments> getComments(){
		return comments;
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
		
		this.ID = new String(bytes, index, sizeOfID);
		index+=this.ID.getBytes("UTF-8").length;
		
		int sizeOfMessage = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.message = new String(bytes, index, sizeOfMessage);
		index+=this.message.getBytes("UTF-8").length;
		
		int sizeOfUserID = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.userID = new String(bytes, index, sizeOfUserID);
		index+=this.userID.getBytes("UTF-8").length;
		
		this.withUsers = new LinkedList<String>();
		int sizeOfWithUsersList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfWithUsersList;i++) {
			int sizeOfUser = buffer.getInt(index);
			index+= Integer.SIZE/8;
			String withuserid = new String(bytes, index, sizeOfUser);
			index+= withuserid.length();
			withUsers.add(withuserid);
		}
		
		this.usersLikes = new LinkedList<String>();
		int sizeOfUserLikesList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfUserLikesList;i++) {
			int sizeOfUser = buffer.getInt(index);
			index+= Integer.SIZE/8;
			String userlikeid = new String(bytes, index, sizeOfUser);
			index+= userlikeid.length();
			usersLikes.add(userlikeid);
		}
		
		int createdDateSize = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		byte[] dateBytes = new byte[createdDateSize];
		for(int i=0;i< createdDateSize;i++)
			dateBytes[i] = bytes[index+i];
		index+=dateBytes.length;
		
		this.createdAt = this.getDateFromDateToStringBytes(dateBytes);
		
		int sizeOfPOI = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		byte[] poiBytes = new byte[sizeOfPOI];
		for(int i=0;i< sizeOfPOI;i++)
			poiBytes[i] = bytes[index+i];
		index+=poiBytes.length;
		
		FbPOI poi = new FbPOI();
		poi.parseBytes(poiBytes);
		
		place = poi;
		
		this.comments = new LinkedList<FbComments>();
		int sizeOfCommentsList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfCommentsList;i++) {
			int sizeOfComment = buffer.getInt(index);
			index+= Integer.SIZE/8;
			
			byte[] commentBytes = new byte[sizeOfComment];
			for(int j=0;j< sizeOfComment;j++)
				commentBytes[j] = bytes[index+j];
			index+=commentBytes.length;
			
			FbComments com = new FbComments();
			com.parseBytes(commentBytes);
			comments.add(com);
		}
		
		this.isProcessed = buffer.getInt(index);
		
	}

	
	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + ID.length();
		
		totalSize+= Integer.SIZE/8;
		if(message!=null&&!message.equals(""))
			totalSize+= message.getBytes("UTF-8").length;
						 
		totalSize+= Integer.SIZE/8 + userID.getBytes("UTF-8").length;
		

		totalSize+= Integer.SIZE/8;
		for(String s:withUsers)
			totalSize+= Integer.SIZE/8 + s.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		for(String s:usersLikes)
			totalSize+= Integer.SIZE/8 + s.getBytes("UTF-8").length;
		
		
		totalSize+= Integer.SIZE/8 + createdAt.toString().getBytes("UTF-8").length;
		
		byte[] placeBytes = place.getBytes();
		totalSize+= Integer.SIZE/8 + placeBytes.length;
		
		totalSize+= Integer.SIZE/8;
		for(FbComments fbcom:comments){
			byte[] commentBytes = fbcom.getBytes();
			totalSize+= Integer.SIZE/8 + commentBytes.length;
		}
		
		totalSize+= Integer.SIZE/8;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.ID.getBytes("UTF-8").length));
		buffer.put(this.ID.getBytes("UTF-8"));
		if(message!=null&&!message.equals("")){
			buffer.put(Bytes.toBytes(this.message.getBytes("UTF-8").length));
			buffer.put(this.message.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		buffer.put(Bytes.toBytes(this.userID.getBytes("UTF-8").length));
		buffer.put(this.userID.getBytes("UTF-8"));
		
		buffer.putInt(withUsers.size());
		for(String s: withUsers) {
			byte[] userBytes = s.getBytes("UTF-8");
			buffer.putInt(userBytes.length);
			buffer.put(userBytes);
		}
		
		buffer.putInt(usersLikes.size());
		for(String s: usersLikes) {
			byte[] likesBytes = s.getBytes("UTF-8");
			buffer.putInt(likesBytes.length);
			buffer.put(likesBytes);
		}
		buffer.put(Bytes.toBytes(createdAt.toString().getBytes("UTF-8").length));
		buffer.put(this.createdAt.toString().getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(placeBytes.length));
		buffer.put(placeBytes);
	
		buffer.putInt(comments.size());
		for(FbComments fbcom: comments) {
			byte[] commentBytes = fbcom.getBytes();
			buffer.putInt(commentBytes.length);
			buffer.put(commentBytes);
		}
		
		buffer.putInt(this.isProcessed);
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String checkin = "Checkin(id= "+ID+"): \n";
		if(message!=null)
			checkin+="\t -title = "+message+"\n";
		if(userID!=null)
			checkin+="\t -user = "+userID+"\n";
		checkin+="\t -with users:\n";checkin+="\t\t ";
		for(String u:withUsers)
			checkin+= u +" ";
		checkin+="\n";
		checkin+="\t -liked by:\n";checkin+="\t\t ";
		for(String u:usersLikes)
			checkin+= u +" ";
		checkin+="\n";
		if(createdAt!=null)
			checkin+="\t -created at = "+createdAt.toString()+"\n";
		if(place!=null)
			checkin+="\t -"+place.toString()+"\n";
		checkin+="\t -comments:\n";
		for(FbComments c:comments){
			checkin+="\t\t -";
			checkin+= c.toString() +"\n";
		}
		checkin+="\n";
		
		return checkin;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		
		FbCheckinObject checkin = new FbCheckinObject("1234567");
		checkin.setDateFromFacebookString("2014-03-11T14:56:38+0000");
		checkin.setMessage("Krasi");
		checkin.setUserID("1149319018");
		LinkedList<String> otherUsers = new LinkedList<String>();
		otherUsers.add("1053452388");otherUsers.add("1292199436");otherUsers.add("1546030454");
		otherUsers.add("1064715291");otherUsers.add("100001144433414");
		checkin.setWithUsers(otherUsers);
		
		FbPOI poi = new FbPOI("233820413323002");
		poi.setCity("Athens");
		poi.setStreet("Gkazoxwri");
		poi.setCountry("Greece");
		poi.setZip("11854");
		poi.setLatitude(37.978684398467);
		poi.setLongitude(23.710294884872);
		poi.setName("Gkazoxwri");
		
		checkin.setFbPOI(poi);
		
		LinkedList<String> likes = new LinkedList<String>();
		likes.add("100000183687651");
		checkin.setUserLikes(likes);
		
		System.out.println(checkin.toString());
		
		FbCheckinObject checkin2 = new FbCheckinObject();
		try {
			System.out.println(checkin.getBytes());
			System.out.println("damn good");
			checkin2.parseBytes(checkin.getBytes());
			
			System.out.println(checkin2.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*Checkin(id= 630483619_10152292450943620): 
	         -user = 630483619
	         -with users:
	                 
	         -liked by:
	                 1257793029 630483619 1302716767 1410122890 1026371164 100004412920272 1073093078 100000269816954 671823846 100001506919824 627853739 1656411064 100007164638464 100001438152814 100002439756045 1484102910 1058963043 1089819316 100000995589737 827093467 1272793262 1366494418 100000521679532 1277643098 100007305485336 
	         -created at = Sun Mar 16 22:42:12 EET 2014
	         -comments:*/

		System.out.println("=====================================================");
		FbCheckinObject ch = new FbCheckinObject("630483619_10152292450943620");
		ch.setUserID("630483619");
		       
		LinkedList<String> l = new LinkedList<String>();
		l.add("1257793029");l.add("630483619");l.add("1302716767");l.add("1410122890");l.add("1026371164");l.add("100004412920272");
		l.add("1073093078");l.add("100000269816954");l.add("671823846");l.add("100001506919824");l.add("627853739 ");l.add("1656411064 ");
		l.add("100007164638464");l.add("100001438152814");l.add("1089819316");l.add("1058963043");l.add("1484102910");l.add("827093467");
		l.add("100002439756045");l.add("100000995589737");l.add("1272793262");
		l.add("100007305485336");l.add("100000521679532");l.add("1277643098");l.add("1366494418");
		
		ch.setUserLikes(l);
		ch.createdAt = ch.getDateFromDateToStringBytes("Sun Mar 16 22:42:12 EET 2014".getBytes());
		FbCheckinObject ch2 = new FbCheckinObject();
		try {
			ch2.parseBytes(ch.getBytes());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(ch2.toString());
	}

}
