package sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

public class AccuracyChecker {
	
	int getCategory(int rate){
		if(rate==4||rate==5)
			return 1;
		else return 0;
	}
	
	public void fixDataSetAccuracy(String inFile) throws IOException{
		BufferedReader src = null;
		src = new BufferedReader(new FileReader(inFile));
		BufferedWriter dst = null;
		dst = new BufferedWriter(new FileWriter(inFile+".new", false));
		String line = null;

		Configuration conf = new Configuration(true);
		Classifier classifier = new Classifier(conf);
		
		while ((line = src.readLine()) != null) {
			String[] tokens = line.split("\\s+",2);
			if(tokens[0].length()==2) tokens[0] = Character.toString(tokens[0].charAt(0));
			int tripAdvisorRate = Integer.parseInt(tokens[0]);
			String comment = tokens[1];
			int score = classifier.classify(comment);
			if(getCategory(tripAdvisorRate)==score)
					dst.append(score+"\t"+comment+"\n");
		}
		src.close();
		dst.close();
	
	}

	public static void main(String[] args) throws IOException {
		AccuracyChecker ac = new AccuracyChecker();
		ac.fixDataSetAccuracy(args[0]);
	}

}
