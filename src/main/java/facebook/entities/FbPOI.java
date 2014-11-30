package facebook.entities;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.hadoop.hbase.util.Bytes;

import snetworks.SocialNetworkEntity;

/**
 * Facebook Place Object
 * @author Giagkos Mytilinis
 */

public class FbPOI extends SocialNetworkEntity{
	
	/*
	 * Info for debugging:
	 * 		8ewrw oti gia ta id,name,latitude, longitude exw panta times
	 * 		enw gia ta upoloipa fields kanw elegxo gia null h keno string
	 */
	
	private String name;
	private String id; //It is long.
	private String street;
	private String city;
	private String state;
	private String country;
	private String zip;
	private double latitude;
	private double longitude;
	
	public FbPOI(){}
	
	public FbPOI(String id){
		this.id = id;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setID(String id){
		this.id = id;
	}
	
	public void setStreet(String street){
		this.street = street;
	}
	
	public void setCity(String city){
		this.city = city;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public void setCountry(String country){
		this.country = country;
	}
	
	public void setZip(String zip){
		this.zip = zip;
	}
	
	public void setLatitude(double lat){
		latitude = lat;
	}
	
	public void setLongitude(double longitude){
		this.longitude = longitude;
	}
	
	public String getID(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getStreet(){
		return street;
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
	
	public String getZip(){
		return zip;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public double getLongitude(){
		return longitude;
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
		
		int sizeOfStreet = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.street = new String(bytes, index, sizeOfStreet);
		index+=this.street.getBytes("UTF-8").length;
		
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
		
		int sizeOfZip = buffer.getInt(index);
		index+= Integer.SIZE/8;
		
		this.zip = new String(bytes, index, sizeOfZip);
		index+=this.zip.getBytes("UTF-8").length;
		
		this.latitude = buffer.getDouble(index);
		index+= Double.SIZE/8;
		
		this.longitude = buffer.getDouble(index);
		index+=Double.SIZE/8;	
	}

	public byte[] getBytes() throws UnsupportedEncodingException {
		
		int totalSize =	Integer.SIZE/8 + id.getBytes("UTF-8").length		
							+ Integer.SIZE/8 + name.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(street!=null&&!street.equals(""))
			totalSize+= street.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(city!=null&&!city.equals(""))
			totalSize+= city.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(state!=null&&!state.equals(""))
			totalSize+= state.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(country!=null&&!country.equals(""))
			totalSize+= country.getBytes("UTF-8").length;
		
		totalSize+= Integer.SIZE/8;
		if(zip!=null&&!zip.equals(""))
			totalSize+= zip.getBytes("UTF-8").length;
							
		totalSize+= Double.SIZE/8 + Double.SIZE/8;
		
		byte[] serializable = new byte[totalSize];
		
		ByteBuffer buffer = ByteBuffer.wrap(serializable);
		buffer.put(Bytes.toBytes(this.id.getBytes("UTF-8").length));
		buffer.put(this.id.getBytes("UTF-8"));
		buffer.put(Bytes.toBytes(this.name.getBytes("UTF-8").length));
		buffer.put(this.name.getBytes("UTF-8"));
		if(street!=null&&!street.equals("")){
			buffer.put(Bytes.toBytes(this.street.getBytes("UTF-8").length));
			buffer.put(this.street.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
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
		if(zip!=null&&!zip.equals("")){
			buffer.put(Bytes.toBytes(this.zip.getBytes("UTF-8").length));
			buffer.put(this.zip.getBytes("UTF-8"));
		}else
			buffer.put(Bytes.toBytes(0));
		buffer.put(Bytes.toBytes(this.latitude));
		buffer.put(Bytes.toBytes(this.longitude));
		
		return serializable;
	}
	
	@Override
	public String toString(){
		String poi="POI(id = "+id+"): \n";
		poi+="\t\t -name: "+name+"\n";
		poi+="\t\t -street: "+street+"\n";
		poi+="\t\t -city: "+city+"\n";
		poi+="\t\t -state: "+state+"\n";
		poi+="\t\t -country: "+country+"\n";
		poi+="\t\t -zip: "+zip+"\n";
		poi+="\t\t -latitude: "+latitude+"\n";
		poi+="\t\t -lonigitude: "+longitude+"\n";
		
		return poi;
	}
	
	/*
	 * main class only for testing purposes
	 */
	
	public static void main(String[] args) {
		
		FbPOI poi = new FbPOI("1234567");
		
		poi.setCity("Athens");
		poi.setStreet("Gkazoxwri");
		poi.setCountry("Greece");
		poi.setZip("11854");
		poi.setLatitude(37.978684398467);
		poi.setLongitude(23.710294884872);
		poi.setName("Gkazoxwri");
		
		System.out.println(poi.toString());
		
		FbPOI poi2 = new FbPOI();
		try {
			poi2.parseBytes(poi.getBytes());
			System.out.println(poi2.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
