package foursquare.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
* Foursquare Venue Object
* @author giagulei
*/

public class FoursquareVenue extends SocialNetworkEntity{

	private String id;
	private String name;
	private String address;
	private double latitude;
	private double longitude;
	private String city;
	private String state;
	private String country;
	
	public void setID(String id){
		this.id = id;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setAddress(String address){
		this.address = address;
	}
	
	public void setLatitude(double latitude){
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public void setCity(String city){
		this.city = city;
	}
	
	public void setState(String state){
		this.state=state;
	}
	
	public void setCountry(String country){
		this.country = country;
	}
	
	public String getID(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getAddress(){
		return address;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public String getCity(){
		return city;
	}
	
	public String getState(){
		return state;
	}
	
	public String getCountry(){
		return country;
	}
	
	public void parseBytes(byte[] bytes) throws UnsupportedEncodingException {
		int index = 0;
		
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		
		
		int sizeOfID = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.id = new String(bytes, index, sizeOfID);
		index+=this.id.getBytes("UTF-8").length;
		
		int sizeOfName = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.name = new String(bytes, index, sizeOfName);
		index+=this.name.getBytes("UTF-8").length;
		
		int sizeOfAddress = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.address = new String(bytes, index, sizeOfAddress);
		index+=this.address.getBytes("UTF-8").length;
		
		this.latitude = buffer.getDouble(index);
		index+= Double.SIZE/8;
		
		this.longitude = buffer.getDouble(index);
		index+=Double.SIZE/8;	
		
		int sizeOfCity = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.city = new String(bytes, index, sizeOfCity);
		index+=this.city.getBytes("UTF-8").length;
		
		int sizeOfState = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.state = new String(bytes, index, sizeOfState);
		index+=this.state.getBytes("UTF-8").length;
			
		int sizeOfCountry = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.country = new String(bytes, index, sizeOfCountry);
		index+=this.country.getBytes("UTF-8").length;
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + id.getBytes("UTF-8").length		
							+ Integer.SIZE/8 + name.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(address!=null&&!address.equals(""))
			totalSize+= address.getBytes("UTF-8").length;
		
		totalSize+= Double.SIZE/8 + Double.SIZE/8;
		
		totalSize+= Integer.SIZE/8;
		if(city!=null&&!city.equals(""))
			totalSize+= city.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(state!=null&&!state.equals(""))
			totalSize+= state.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(country!=null&&!country.equals(""))
			totalSize+= country.getBytes("UTF-8").length;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.id.getBytes("UTF-8").length));
		buffer.put(this.id.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.name.getBytes("UTF-8").length));
		buffer.put(this.name.getBytes("UTF-8"));
		if(address!=null&&!address.equals("")){
			buffer.put(Bytes.toBytes(this.address.getBytes("UTF-8").length));
			buffer.put(this.address.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		
		buffer.put(Bytes.toBytes(this.latitude));
		buffer.put(Bytes.toBytes(this.longitude));
		
		if(city!=null&&!city.equals("")){
			buffer.put(Bytes.toBytes(this.city.getBytes("UTF-8").length));
			buffer.put(this.city.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		if(state!=null&&!state.equals("")){
			buffer.put(Bytes.toBytes(this.state.getBytes("UTF-8").length));
			buffer.put(this.state.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		if(country!=null&&!country.equals("")){
			buffer.put(Bytes.toBytes(this.country.getBytes("UTF-8").length));
			buffer.put(this.country.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String poi="Venue(id = "+id+"): \n";
		poi+="\t\t -name: "+name+"\n";
		poi+="\t\t -address: "+address+"\n";
		poi+="\t\t -city: "+city+"\n";
		poi+="\t\t -state: "+state+"\n";
		poi+="\t\t -country: "+country+"\n";
		poi+="\t\t -latitude: "+latitude+"\n";
		poi+="\t\t -lonigitude: "+longitude+"\n";
		
		return poi;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		
		FoursquareVenue v = new FoursquareVenue();
		v.setID("4b8fb8a9f964a520c85e33e3");
		v.setName("Hilton Brussels City");
		v.setAddress("Place Karel Rogierplein 20");
		v.setCity("Saint-Josse");
		v.setCountry("Belgium");
		v.setLatitude(50.85564690227329);
		v.setLongitude(4.359898567199707);
		
		System.out.println(v.toString());
		
		FoursquareVenue v2 = new FoursquareVenue();
		try {
			v2.parseBytes(v.getBytes());
			System.out.println(v2.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

}
