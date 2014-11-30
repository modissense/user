package entities;

import java.util.HashMap;
import java.util.List;

public class SNDetails extends DatabaseTransaction{

	private Integer userId=-1;
	private String snName=null, snToken=null, snIdentifier=null, snTokenSecret=null;
	
	
	public SNDetails() {
		super();
	}

	@Override
	protected String getTableName() {
		return "sn_list";
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the snName
	 */
	public String getSnName() {
		return snName;
	}

	/**
	 * @param snName the snName to set
	 */
	public void setSnName(String snName) {
		this.snName = snName;
	}

	/**
	 * @return the snToken
	 */
	public String getSnToken() {
		return snToken;
	}
	
	/**
	 * @return the snToken
	 */
	public String getSnTokenSecret() {
		return snTokenSecret;
	}

	/**
	 * @param snToken the snToken to set
	 */
	public void setSnToken(String snToken) {
		this.snToken = snToken;
	}
	
	/**
	 * @param snToken the snToken to set
	 */
	public void setSnTokenSecret(String snTokenSecret) {
		this.snTokenSecret = snTokenSecret;
	}


	/**
	 * @return the snIdentifier
	 */
	public String getSnIdentifier() {
		return snIdentifier;
	}

	/**
	 * @param snIdentifier the snIdentifier to set
	 */
	public void setSnIdentifier(String snIdentifier) {
		this.snIdentifier = snIdentifier;
	}
	

	/**
	 * Creates a new entry on the SN details table.
	 * @return
	 */
	public boolean create(){
		return this.create(this.serialize());
	}
	
	public void close(){
		this.closeConnection();
	}
	
	// TODO
	public List<HashMap<String, String>> read(){
		List<HashMap<String, String>> results=this.read(this.serialize());
		if(results!=null && results.size()==1){
			this.userId = new Integer(results.get(0).get("user_id"));
			this.snName = results.get(0).get("sn_name");
			this.snToken = results.get(0).get("sn_token");
			this.snTokenSecret = results.get(0).get("sn_token_secret");
			this.snIdentifier= results.get(0).get("sn_identifier");
		}
		return results;
	}
	
	
	/**
	 * Update the specified table.
	 * @return
	 */
	//TODO:prepei na proste8oun ki alla.gia thn wra einai mono update token
	public boolean update(HashMap<String, String> id){
		//HashMap<String, String> id = new HashMap<String, String>();
		//id.put("sn_token", "'"+token+"'");
		return this.update(id,this.serialize());
	}
	
	/**
	 * Delete the specified entry from the table.
	 * @return
	 */
	public boolean delete(){
		return this.delete(this.serialize());
	}
	
	/**
	 * Returns a HashMap of the object. Used for database transactions.
	 * @return
	 */
	private HashMap<String, String> serialize(){
		HashMap<String, String> foo = new HashMap<String, String>();
		if(this.userId!=-1)
			foo.put("user_id", this.userId.toString());
		if(this.snName!=null)
			foo.put("sn_name", "'"+this.snName+"'");
		if(this.snToken!=null)
			foo.put("sn_token", "'"+this.snToken+"'");
		if(this.snTokenSecret!=null)
			foo.put("sn_token_secret", "'"+this.snTokenSecret+"'");
		if(this.snIdentifier!=null)
			foo.put("sn_identifier", "'"+this.snIdentifier+"'");
		;
		

		return foo;
	}
	
	@Override
	public String toString() {
		return this.userId+","+this.snName+","+this.snToken+","+this.snTokenSecret+","+this.snIdentifier;
	}
	public static void main(String[] args) {
		User a = new User("ggian");
		SNDetails d = new SNDetails();
		d.setUserId(a.getId());
		d.setSnName("facebook");
		System.out.println(d.read());
		System.out.println(d);

	}
}
