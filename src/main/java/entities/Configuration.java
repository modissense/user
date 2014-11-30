package entities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Class used to read a configuration file (containing database specific parameters). The conf file is organized
 * as a set of key-values and the information are stored as a hash map in memory.
 * @author Giannis Giannakopoulos
 *
 */
public class Configuration {

	private HashMap<String, String> kv;
	/**
	 * Empty constructor
	 */
	public Configuration() {
		this.kv = new HashMap<String, String>();
	}
	
	public Configuration(String conf) {
		this.kv = new HashMap<String, String>();
		this.readFile(conf);
	}
	
	public final boolean readFile(String filename){
		try {
			BufferedReader in =  new BufferedReader(new FileReader(filename));
			while(in.ready()){
				String[] buffer = in.readLine().split("\t");
				if(buffer.length>1 && buffer[0].trim().charAt(0)!='#')
					this.kv.put(buffer[0].trim(), buffer[buffer.length-1].trim());
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String getValue(String key){
		return this.kv.get(key);
	}
	
	@Override
	public String toString() {
		return this.kv.toString();
	}
	
	public static void main(String[] args) {
		Configuration c = new Configuration(args[0]);
		System.out.println(c);
	}

}
