package entities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author giagulei
 *
 */
public class POI extends DatabaseTransaction{
	
	private static final String TABLE_NAME = "poi";
	private static final double DEFAULT_INTEREST = -1.0;
	private static final long DEFAULT_USER = Long.MIN_VALUE;
	private static final long DEFAULT_HOTNESS = Long.MAX_VALUE;
	private static final long FAKE_DEFAULT_ID = -1;
	
	private static final int ST_SRID_GEO = 4326;
	
	private static final double DEFAULT_LAT = -1;
	private static final double DEFAULT_LONG = -1;
	
	public static final long SOCIAL_NETWORK_POI_USER = -2;

	private long id = FAKE_DEFAULT_ID;
	private long userID = DEFAULT_USER;
	private String name;
	private double interest = DEFAULT_INTEREST;
	private long hotness = DEFAULT_HOTNESS;
	private boolean isPublic = true;
	private String keywords; //space separated keywords
	private String description; //TODO: pou gemizei auto?
	private String timestamp;
	//private String deltTimestamp; //TODO: ti sto diaolo einai auto?
	
	private double latitude = DEFAULT_LAT;
	private double longitude = DEFAULT_LONG;
	
	private boolean exists = false;
	
	public POI(){
		super();
	}
	
	public POI(String name,double latitude,double longitude){
		super();
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		List<HashMap<String, String>> res = this.read();
		if(res == null) System.out.println("Giati pairnw null????");
		if(res.size()>0){
			exists = true;
			
			
		}
	}
	
	public boolean exists(){
		return exists;
	}
	
	public long getID(){
		return id;
	}
	
	public void setID(long id){
		this.id = id;
	}
	
	public long getUserID(){
		return userID;
	}
	
	public void setUserID(long userID){
		this.userID = userID;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public double getInterest(){
		return interest;
	}
	
	public void setInterest(double interest){
		this.interest = interest;
	}
	
	public long getHotness(){
		return hotness;
	}
	
	public void setHotness(long hotness){
		this.hotness = hotness;
	}
	
	public boolean getIsPublic(){
		return isPublic;
	}
	
	public void setIsPublic(boolean isPublic){
		this.isPublic = isPublic;
	}
	
	public String getKeywords(){
		return keywords;
	}
	
	public void setKeywords(String keyWords){
		this.keywords = keyWords;
	}
	
	public void addToKeywords(String word){
		this.keywords += " "+word;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getTimestamp(){
		return timestamp;
	}
	
	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}
	
	public double getLat(){
		return latitude;
	}
	
	public void setLat(double latitude){
		this.latitude = latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public boolean create(){
		return this.create(this.serialize());
	}
	
	/**
	 * Delete object from database.
	 */
	public boolean delete(){
		HashMap<String, String> foo = this.serialize();
		return this.delete(foo);
	}
	
	public void close(){
		this.closeConnection();
	}
	/**
	 * 
	 * @return
	 */
	public List<HashMap<String, String>> read(){
		HashMap<String, String> t = this.serialize();
		List<HashMap<String, String>> res = this.read(t);
		if(res.size()==1){
			HashMap<String, String> info = res.get(0);
			this.id=new Long(info.get("poi_id"));
			this.isSynced=true;
		}
		return res;
	}
	
	public boolean update(){
		//TODO: to be implemented
		return true;
	}
	
	/**
	 * Returns a HashMap of the object. Used for database transactions.
	 * @return
	 */
	
	private HashMap<String, String> serialize(){
		HashMap<String, String> foo = new HashMap<String,String>();
		if(this.id!= FAKE_DEFAULT_ID)
			foo.put("poi_id", (new Long(this.id)).toString());
		if(this.name!=null)
			foo.put("name", "'"+this.name+"'");
		if(this.latitude!=DEFAULT_LAT && this.longitude!= DEFAULT_LONG)
			foo.put("geo", "GeomFromText('point("+this.latitude+" "+this.longitude+")',"+ST_SRID_GEO+")");
		if(this.userID != DEFAULT_USER)
			foo.put("user_id", (new Long(this.userID)).toString());
		if(this.interest != DEFAULT_INTEREST)
			foo.put("interest", (new Double(this.interest)).toString());
		if(this.hotness != DEFAULT_HOTNESS)
			foo.put("hotness", (new Long(this.hotness)).toString());
		foo.put("publicity", Boolean.valueOf(isPublic).toString());
		if(this.keywords!=null)
			foo.put("keywords", this.keywords);
		if(this.description!=null)
			foo.put("description", this.description);
		if(this.timestamp!=null)
			foo.put("tmstamp", "TIMESTAMP '"+timestamp+"'");
		
		return foo;
	}
	
	public boolean isSynced(){
		return this.isSynced;
	}
	
	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}
	
	/*
	 * Loads pois from CSV file to postgres. the file format
	 * is the following:
	 * 
	 * dummy dummy long lat name
	 */
	public static void loadPOISFromFile(String filename){
		String poiCSV = filename;
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(poiCSV));
			String line;
			
		    while ((line = br.readLine())!=null){
		    	String[] info = line.split("\t");
		    	String name = info[4];
		    	if(name.contains("'")){
		    		name = name.replace("'", " ");
		    	}
		    	POI p = new POI(name,Double.parseDouble(info[2]),Double.parseDouble(info[3]));
		    	if(!p.exists){
		    		p.setUserID(SOCIAL_NETWORK_POI_USER);
		    		if(p.create())
		    			System.out.println("POI inserted successfully!");
		    		else{
		    			System.out.println("ERROR on POI insertion");
		    		}
		    	}
		    }
		    br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) {
		
		loadPOISFromFile(args[0]);
		
	}
	

}
