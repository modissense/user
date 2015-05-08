package entities;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import org.postgresql.geometric.PGpoint;


public abstract class DatabaseTransaction {

	public static String CONF_LOCATION="/etc/modissenserc";
	public static boolean DEBUG=true;
	
	
	private Configuration conf;
	protected static Connection connection=null;
	
	protected boolean isSynced=false;
	
	public DatabaseTransaction() {
		this.conf = new Configuration(CONF_LOCATION);
		this.openConnenction();
	}
	
	/**
	 * Opens a connection with the database
	 */
	protected void openConnenction(){
		try {
			if(DatabaseTransaction.connection!=null && !DatabaseTransaction.connection.isClosed()){
				return;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try{
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e ){
			e.printStackTrace();
		}
		if(DEBUG)	System.out.println("Driver registered");
		try {
			DatabaseTransaction.connection = 
					DriverManager.getConnection("jdbc:postgresql://"+
							this.conf.getValue("DATABASE_HOST")+":"+
							this.conf.getValue("DATABASE_PORT")+"/"+
							this.conf.getValue("DATABASE_NAME"),
							this.conf.getValue("DATABASE_USERNAME"),
							this.conf.getValue("DATABASE_PASSWORD"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(DEBUG)	System.out.println("Connection created");
	}
	
	/**
	 * Closes the connection with the database	
	 */
	protected void closeConnection(){
		try {
			DatabaseTransaction.connection.close();
			if(DEBUG)	System.out.println("Connection closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract String getTableName();
	
	/**
	 * Generic "insert statement" method used to insert tuple into the database. It returns true if the 
	 * insertion is done, else false.
	 * @param keyValues
	 * @return
	 */
	protected boolean create(HashMap<String, String> keyValues){
		String statement = "INSERT INTO "+this.getTableName();
		if(!keyValues.isEmpty()){
			statement+=" (";
			String keys="", values="";
			for (Entry<String,String> e : keyValues.entrySet()) {
				keys+=e.getKey()+",";
				values+=e.getValue()+",";
			}
			if(keys.length()==0 || values.length()==0)
				return false;
			statement = statement+ keys.substring(0,keys.length()-1)+") VALUES ("+values.substring(0,values.length()-1)+")";
		}else{
			statement+=" default values";
		}
		return this.executeSQLWithoutResults(statement);
	}

	/**
	 * Generic "read" method used to fetch data from a table. The objects are the parameters.
	 * @param object
	 * @return
	 */
	protected List<HashMap<String,String>> read(Map<String, String> object) {
		String sql="SELECT * FROM "+this.getTableName();
		String temp="";
		for(Entry<String, String> e:object.entrySet()){
			temp+=e.getKey()+"="+e.getValue()+" AND ";
		}
		if(temp.length()>5)
			temp = " WHERE "+temp.substring(0,temp.length()-5);
		sql=sql+temp;
		return this.executeSQLWithResults(sql);
		
	}
	
	
	protected boolean update(Map<String, String> newValues, Map<String, String> id) {
		String sql = "UPDATE "+this.getTableName()+" SET ";
		String temp="";
		for(Entry<String, String> e:newValues.entrySet()){
			temp+=e.getKey()+"="+e.getValue()+", ";
		}
		sql+=temp.substring(0,temp.length()-2);
		if(temp.length()>0)
			temp = " WHERE ";
		for(Entry<String, String> e:id.entrySet()){
			temp+=e.getKey()+"="+e.getValue()+" AND ";
		}
		sql+=temp.substring(0,temp.length()-5);
		System.out.println(sql);
		return this.executeSQLWithoutResults(sql);
	}
	
	/**
	 * Generic "delete statement" used to remove a tuple from the database
	 * @return
	 */
	protected boolean delete(HashMap<String, String> object) {
		String sql="DELETE FROM "+this.getTableName();
		String temp="";
		for(Entry<String, String> e:object.entrySet()){
			temp+=e.getKey()+"="+e.getValue()+" AND ";
		}
		if(temp.length()>0)
			temp = " WHERE "+temp.substring(0,temp.length()-5);
		sql=sql+temp;
		System.out.println(sql);
		return executeSQLWithoutResults(sql);
	}
		
	protected boolean executeSQLWithoutResults(String sql){
		try {
			Statement st = DatabaseTransaction.connection.createStatement();
			st.execute(sql);
			st.close();
			return true;
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
	}

	protected List<HashMap<String, String>> executeSQLWithResults(String sql){
		try {
			Statement st = DatabaseTransaction.connection.createStatement();
			ResultSet rs=st.executeQuery(sql);
			ResultSetMetaData md = rs.getMetaData();
			LinkedList<HashMap<String, String>> results = new LinkedList<HashMap<String,String>>();
			while(rs.next()){
				HashMap<String, String> object =new HashMap<String, String>();
				for(int i=1;i<md.getColumnCount()+1;i++){
						object.put(md.getColumnName(i), rs.getString(i));
				}
				results.add(object);
			}
			rs.close();
			st.close();
			return results;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
