package description;

import java.util.HashMap;

/**
 * This class is used in order to describe a service. Is it inherited by other subclasses which implement 
 * different types of descriptions (like WSDL, WADL, plain text description, json description, etc.). 
 * 
 * @author Giannis Giannakopoulos
 *
 */
public abstract class ServiceDescription {

	/*
	 * <key, value> pairs representing the type and the name of the parameters
	 * that the service expects
	 * key==name, value==type
	 */
	protected HashMap<String, String> parameters;
	// Return value of the service
	protected String returnValue;
	protected String description;
	protected String serviceName;
	
	public ServiceDescription(String name){
		this.serviceName=name;
		this.parameters = new HashMap<String,String>();
	}
	
	public void addParameter(String type, String name){
		this.parameters.put(name, type);
	}
	
	public void setReturnValue(String type){
		this.returnValue=type;
	}
	
	public void setDescription(String description){
		this.description=description;
	}
	
	public abstract String serialize();
	public abstract void deSerialize(String object);
}
