package supportingClasses;

import java.util.Random;

public class Utils {
	
	public static String randomStringGenerator(int stringLength){
		
		String rndString="";
		Random charSequence = new Random();
		Random coin = new Random();
		for(int i=0;i<stringLength;i++){
			if(coin.nextInt(3)==1)
				rndString+= Character.toString((char) (charSequence.nextInt(10)+48));
			else if(coin.nextInt(3)==0)
				rndString+= Character.toString((char) (charSequence.nextInt(26)+65));
			else
				rndString+= Character.toString((char) (charSequence.nextInt(26)+97));
		}
		
		return rndString;
		
	}
}
