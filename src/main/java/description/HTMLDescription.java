package description;

import java.util.Map.Entry;

/**
 * Subclass of the {@link ServiceDescription} superclass. It is used to print the description
 * as an HTML file.
 * @author Giannis Giannakopoulos
 *
 */
public class HTMLDescription extends ServiceDescription {

	public HTMLDescription(String serviceName) {
		super(serviceName);
	}

	@Override
	public String serialize() {
		String buffer="";
		buffer+="<html>\n<head><title>"+this.serviceName+"</title></head>\n";
		buffer+="<body>\n";
		
		buffer+="<table border=1>\n";
		buffer+="<tr>\n";
		buffer+="<th style='text-align:left'>Service Name</th>\n";
		buffer+="<td colspan=2>"+this.serviceName+"</td>\n";
		buffer+="</tr>\n";
		buffer+="<tr>\n"; 
		buffer+="<th style='text-align:left'>Parameters</th>\n";
		buffer+="<th>Name</th>\n";
		buffer+="<th>Type</th>\n";
		buffer+="</tr>\n";
		
		if(this.parameters.isEmpty()){
			buffer+="<tr>\n";
			buffer+="<td></td>\n";
			buffer+="<td colspan=2>No parameters</td>\n";
			buffer+="</tr>\n";
		}
		for(Entry<String,String> e:this.parameters.entrySet()){
			buffer+="<tr>\n";
			buffer+="<td></td>\n";
			buffer+="<td>"+e.getKey()+"</td>\n";
			buffer+="<td>"+e.getValue()+"</td>\n";
			buffer+="</tr>\n";
		}
		
		buffer+="<tr>\n";
		buffer+="<th>Return value</th>\n";
		buffer+="<td colspan=2>"+this.returnValue+"</td>\n";
		buffer+="</tr>\n";
		
		buffer+="<tr>\n";
		buffer+="<th style='text-align:left'>Service Description</th>\n";
		buffer+="<td colspan=2 style='width:400px'>"+this.description+"</td>\n";
		buffer+="</tr>\n";
		
		buffer+="</table>\n";
		
		buffer+="</body>\n</html>";
		return buffer;
	}

	@Override
	public void deSerialize(String object) {
		//not needed
	}

}
