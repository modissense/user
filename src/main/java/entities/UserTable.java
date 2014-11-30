package entities;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Class used to implement all user's transaction with the database. This class implements method
 * in which the user can fetch a user from the database, update him, delete him, etc. In addition, with this class,
 * a user's friends can be specified, (fetched, pushed, deleted, etc.). Finally, the social network specific details
 * can be retrieved (and set) using this class.
 * 
 * <br/>
 * <b>Tables managed by this class</b>
 * <ul>
 * <li>users </li>
 * <li>friends</li>
 * <li>sn_list</li>
 * </ul>
 * @author Giannis Giannakopoulos
 *
 */
public class UserTable extends DatabaseTransaction {

	// fields of the actual table
	private Integer id=-1;
	private String username;
	private Entry<Integer, Integer> point;
	
	public UserTable() {
		super();
	}

	public UserTable(int id){
		super();
		this.id=id;
		this.read();
	}
	
	public UserTable(String username){
		super();
		this.username=username;
		this.read();
	}
	
	@Override
	public String getTableName() {
		return "users";
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
		this.isSynced=false;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
		this.isSynced=false;
	}

	/**
	 * @return the point
	 */
	public Entry<Integer, Integer> getPoint() {
		return point;
	}

	/**
	 * @param point the point to set
	 */
	public void setPoint(int x, int y) {
		this.point = new SimpleEntry<Integer,Integer>(x,y);
		this.isSynced=false;
	}
	
	/**
	 * Put this user object to database.
	 */
	public boolean create(){
		HashMap<String, String> foo = new HashMap<String,String>();
		if(this.id!=-1)
			foo.put("id", this.id.toString());
		if(this.username!=null)
			foo.put("username", "'"+this.username+"'");
		if(this.point!=null)
			foo.put("map_center", "POINT("+point.getKey()+","+point.getValue()+")");
		return this.create(foo);
	}
	
	/**
	 * Delete object from database.
	 */
	public boolean delete(){
		HashMap<String, String> foo = this.serialize();
		foo.remove("map_center");
		return this.delete(foo);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean read(){
		List<HashMap<String, String>> res =this.read(this.serialize());
		if(res.isEmpty())
			return false;
		HashMap<String, String> info = res.get(0);
		if(info==null)
			return false;
		this.id=new Integer(info.get("id"));
		this.username = info.get("username");
		if(info.get("map_center")!=null){
			String temp[] = info.get("map_center").split(",");
			this.point = new SimpleEntry<Integer, Integer>(new Integer(temp[0].substring(1)), new Integer(temp[1].substring(0,temp[1].length()-1)));
		}
		this.isSynced=true;
		return true;
	}
	
	public boolean update(){
		HashMap<String, String> id = new HashMap<String, String>();
		id.put("id", this.id.toString());
		this.update(this.serialize(),id);
		return true;
	}
	
	/**
	 * Returns a HashMap of the object. Used for database transactions.
	 * @return
	 */
	private HashMap<String, String> serialize(){
		HashMap<String, String> foo = new HashMap<String, String>();
		if(this.id!=-1)
			foo.put("id", this.id.toString());
		if(this.username!=null)
			foo.put("username", "'"+this.username+"'");
		if(this.point!=null)
			foo.put("map_center", "POINT("+point.getKey()+","+point.getValue()+")");
		return foo;
	}
	
	public boolean isSynced(){
		return this.isSynced;
	}
	
	@Override
	public String toString() {
		if(this.point!=null)
			return "("+this.id.toString()+","+this.username+", ["+this.point.getKey()+","+this.point.getValue()+"])";
		else
			return "("+this.id.toString()+","+this.username+", null)";
	}
	
	public static void main(String[] args) {
		UserTable u = new UserTable("gmytil2");
//		u.setPoint(-10, 100);
		if(u.isSynced()){
			u.setUsername("gmytil");
			System.out.println("Writing to DB");
			u.update();
		}
	}
}
