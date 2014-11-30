package foursquare.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
* Foursquare Checkin Object
* @author giagulei
*/

public class FoursquareCheckin extends SocialNetworkEntity{

	private String id;
	private Date createdAt;
	private String userID;
	private FoursquareVenue venue;
	private LinkedList<String> likes;
	private String shout;
	private int isProcessed; // binary variable. Value 0 indicates unprocessed data
							//	while a value of 1 indicates processed. 
	
	
	public FoursquareCheckin(){
		likes = new LinkedList<String>();
		isProcessed = 0;
	}
	
	public void setID(String id){
		this.id = id;
	}
	
	/*
	 *	foursquare uses unix timestamp in order to represent 
	 *	dates and as such, unix timestamps are used in the
	 *	checkin response json. 
	 */
	public void setDateFromUnixTimestamp(Long timestamp){
		createdAt = new Date((long) timestamp*1000);
	}
	
	public void setCreatedAt(Date createdAt){
		this.createdAt = createdAt;
	}
	
	public void setUserID(String userID){
		this.userID = userID;
	}
	
	public void setVenue(FoursquareVenue venue){
		this.venue = venue;
	}
	
	public void setLikes(LinkedList<String> likes){
		this.likes = likes;
	}
	
	public void setShout(String shout){
		this.shout = shout;
	}
	
	public void setIsProcessed(int isProcessed){
		this.isProcessed = isProcessed;
	}
	
	public String getID(){
		return id;
	}
	
	public Date getCreatedAt(){
		return createdAt;
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
	
	public String getUserID(){
		return userID;
	}
	
	public FoursquareVenue getVenue(){
		return venue;
	}
	
	public LinkedList<String> getLikes(){
		return likes;
	}
	
	public String getShout(){
		return shout;
	}
	
	public int getIsProcessed(){
		return isProcessed;
	}
	
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		int sizeOfID = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.id = new String(bytes, index, sizeOfID);
		index+=this.id.getBytes("UTF-8").length;
		
		int createdDateSize = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		byte[] dateBytes = new byte[createdDateSize];
		for(int i=0;i< createdDateSize;i++)
			dateBytes[i] = bytes[index+i];
		index+=dateBytes.length;
		
		this.createdAt = this.getDateFromDateToStringBytes(dateBytes);
		
		int sizeOfuserID = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.userID = new String(bytes, index, sizeOfuserID);
		index+=this.userID.getBytes("UTF-8").length;
		
		int sizeOfVenue = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		byte[] venueBytes = new byte[sizeOfVenue];
		for(int i=0;i< sizeOfVenue;i++)
			venueBytes[i] = bytes[index+i];
		index+=venueBytes.length;
		
		FoursquareVenue venue = new FoursquareVenue();
		venue.parseBytes(venueBytes);
		this.venue = venue;
		
		this.likes = new LinkedList<String>();
		int sizeOfUserLikesList = buffer.getInt(index);
		index+= Integer.SIZE/8;
		for(int i=0;i<sizeOfUserLikesList;i++) {
			int sizeOfUser = buffer.getInt(index);
			index+= Integer.SIZE/8;
			String userlikeid = new String(bytes, index, sizeOfUser);
			index+= userlikeid.length();
			likes.add(userlikeid);
		}
		
		int sizeOfShout = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.shout = new String(bytes, index, sizeOfShout);
		index+=this.shout.getBytes("UTF-8").length;
		
		this.isProcessed = buffer.getInt(index);
		
	}

	
	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + id.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8 + createdAt.toString().getBytes("UTF-8").length
					+ Integer.SIZE/8 + userID.getBytes("UTF-8").length;
		
		byte[] venueBytes = venue.getBytes();
		totalSize+= Integer.SIZE/8 + venueBytes.length;
		
		totalSize+= Integer.SIZE/8;
		for(String s:likes)
			totalSize+= Integer.SIZE/8 + s.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(shout!=null)
			totalSize+= shout.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.id.getBytes("UTF-8").length));
		buffer.put(this.id.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(createdAt.toString().getBytes("UTF-8").length));
		buffer.put(this.createdAt.toString().getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.userID.getBytes("UTF-8").length));
		buffer.put(this.userID.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(venueBytes.length));
		buffer.put(venueBytes);
		
		buffer.putInt(likes.size());
		for(String s: likes) {
			byte[] likesBytes = s.getBytes("UTF-8");
			buffer.putInt(likesBytes.length);
			buffer.put(likesBytes);
		}
		
		if(shout!=null){
			buffer.put(Bytes.toBytes(this.shout.getBytes("UTF-8").length));
			buffer.put(this.shout.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		
		buffer.putInt(this.isProcessed);
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String checkin="Checkin(id = "+id+"): \n";
		if(userID!=null)
			checkin+="\t -user: "+userID+"\n";
		if(shout!=null)
			checkin+="\t -shout: "+shout+"\n";
		if(createdAt!=null)
			checkin+="\t -createdAt: "+createdAt.toString()+"\n";
		if(venue!=null)
			checkin+="\t -venue: "+venue.toString()+"\n";
		checkin+="\t -liked by:\n";checkin+="\t\t ";
		for(String u:likes)
			checkin+= u +" ";
		
		return checkin;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		FoursquareCheckin ch = new FoursquareCheckin();
		ch.setID("5320147a498ec1db926af3fd");
		ch.setUserID("18880424");
		ch.setDateFromUnixTimestamp(1394611322L);
		ch.setShout("Da best of da best!");
		FoursquareVenue v = new FoursquareVenue();
		v.setID("4b8fb8a9f964a520c85e33e3");
		v.setName("Hilton Brussels City");
		v.setAddress("Place Karel Rogierplein 20");
		v.setCity("Saint-Josse");
		v.setCountry("Belgium");
		v.setLatitude(50.85564690227329);
		v.setLongitude(4.359898567199707);
		
		ch.setVenue(v);
		LinkedList<String> l = new LinkedList<String>();
		l.add("33654718");l.add("27914349");
		ch.setLikes(l);
		System.out.println(ch.toString());
		
		FoursquareCheckin ch2 = new FoursquareCheckin();
		try {
			ch2.parseBytes(ch.getBytes());
			System.out.println(ch2.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
