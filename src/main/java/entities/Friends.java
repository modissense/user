package entities;

import java.util.HashMap;
import java.util.List;

/**
 * Class modeling the relationships between different users
 * @author Giannis Giannakopoulos
 *
 */
public class Friends extends DatabaseTransaction {

	private Integer userA=-1, userB=-1;
	
	public Friends() {
		super();
	}

	@Override
	protected String getTableName() {
		return "friends";
	}

	/**
	 * @return the userA
	 */
	public int getUserA() {
		return userA;
	}

	/**
	 * @param userA the userA to set
	 */
	public void setUserA(int userA) {
		this.userA = userA;
	}

	/**
	 * @return the userB
	 */
	public int getUserB() {
		return userB;
	}

	/**
	 * @param userB the userB to set
	 */
	public void setUserB(int userB) {
		this.userB = userB;
	}

	/**
	 * Creates a new friendship
	 * @return
	 */
	public boolean create(){
		return this.create(this.serialize());
	}
	
	/**
	 * Executes a "SELECT" statement based on the given parameters.
	 * @return
	 */
	public List<HashMap<String, String>> read(){
		List<HashMap<String, String>> results = this.read(this.serialize());
		if(results.size()==1){
			this.userA = new Integer(results.get(0).get("usera"));
			this.userB = new Integer(results.get(0).get("userb"));
		}
		
		return results;
	}
	
	/**
	 * Deletes the current entry from the database.
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
		if(this.userA!=-1)
			foo.put("usera", this.userA.toString());
		if(this.userB!=-1)
			foo.put("userb", this.userB.toString());

		return foo;
	}
	
	public static void main(String[] args) {
		User a = new User("gmytil"), b = new User("ggian");
		Friends f = new Friends();
		f.setUserA(a.getId());
		f.setUserB(b.getId());
//		f.setUserB(b.getId());
//		f.create();
//		f.setUserA(11);
		System.out.println(f.read());
	}

}
