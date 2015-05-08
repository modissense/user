package sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import net.minidev.json.*;
import net.minidev.json.parser.*;

/**
 * 
 * @author giagulei
 * class to extract text dataset and pois information from crawled
 * TripAdvisor data.
 */
class JsonConverter {
	
	/**
	 * input file with the json we need to parse
	 */
	private String tripAdvisorJSON;
	/**
	 * output file with the texts needed for sentiment analysis training
	 */
	private String trainingSet;
	/**
	 * output file with the pois collected from the dataset.
	 */
	private String collectedPois;
	
	public JsonConverter(String tripAdvisorJSON,String trainingSet,String collectedPois){
		this.tripAdvisorJSON = tripAdvisorJSON;
		this.trainingSet = trainingSet;
		this.collectedPois = collectedPois;
	}
	
	/**
	 * 
	 * @throws IOException
	 * @throws ParseException
	 * processes input json and write the retrieved information to the output files.
	 */
	public void parseTripAdvisorJSON() throws IOException, ParseException{
		
		File input = new File(tripAdvisorJSON);
		JSONParser parser = new JSONParser();
		BufferedWriter poisBufWriter = new BufferedWriter(new FileWriter(collectedPois,
				false));
		
		BufferedWriter textBufWriter = new BufferedWriter(new FileWriter(trainingSet,
				false));

		BufferedReader src = new BufferedReader(new FileReader(input));
		String line = null;

		/*
		 * Since the input json may be in the order of GB and thus does not fit in memory,
		 * we process it line by line.
		 */
		while ((line = src.readLine()) != null) {
			
			// we transform each line of the initial json as to have json format itself.
			line = line.replace("[{", "{");
			line = line.replace("},", "}");
			line = line.replace("}]", "}");
			line = "[" + line + "]";

			Object obj = parser.parse(line);
			JSONArray array = (JSONArray) obj;

			if (array.size() != 1) {
				System.out.println("Error");
				src.close();
				System.exit(0);
			}
			JSONObject jsonLine = (JSONObject) array.get(0);
			String review=null,reviewTitle=null,title = null,rating=null;
			
			JSONArray reviewArray = (JSONArray) jsonLine.get("review");
			if(reviewArray.size()>0){
				review = (String) reviewArray.get(0);
				review = review.substring(0,review.length()-2);
			}
			
			rating = jsonLine.get("rating").toString();
			
			JSONArray reviewTitleArray = (JSONArray) jsonLine.get("rtitle");
			if(reviewTitleArray.size()>0)
				reviewTitle = (String) reviewTitleArray.get(0);
			
			JSONArray titleArray = (JSONArray) jsonLine.get("title");
			if(titleArray.size()>1){
				title = (String) titleArray.get(1);
				//title = title.substring(0,title.length()-2);
			}else title = (String) titleArray.get(0);
			
			double longitude=0, latitude=0;
			if(jsonLine.containsKey("longitude")){
				String longStr = jsonLine.get("longitude").toString();
				if(longStr.contains("E")) continue;
				longitude = Double.parseDouble(longStr);
			}
			if(jsonLine.containsKey("latitude")){
				String latString = jsonLine.get("latitude").toString();
				if(latString.contains("E")) continue;
				latitude = Double.parseDouble(latString);
			}

			if(review==null||rating==null)
				continue;
			
			char finalRating;
			try{
				finalRating = rating.charAt(2);
			}
			catch(Exception e){
				continue;
			}
			
			if(title!=null&&!(latitude==0&&longitude==0)){
				title = title.replaceAll("\n", "");
				String poiOutput = title+"\t"+latitude+"\t"+longitude;
				poisBufWriter.append(poiOutput+"\n");
			}

			String textTrainOutput;
			title = title.replaceAll("\n", "");
			reviewTitle = reviewTitle.replaceAll("\n", "");
			review = review.replaceAll("\n", "");
			if(reviewTitle!=null)
				textTrainOutput = title+"\t"+finalRating+"\t"+reviewTitle+" "+review;
			else
				textTrainOutput = title+"\t"+finalRating+"\t"+review;
			textBufWriter.append(textTrainOutput+"\n");
			
			
		}
		src.close();
		
		poisBufWriter.close();
		textBufWriter.close();
	}
	
	public static void main(String[] args){

		if (args.length != 3) {
			System.err.println("Arguments: [input file] [pois file] [text training file]");
			return;
		}

		String inputFile = args[0];
		String poisFile = args[1];
		String trainingFile = args[2];

		JsonConverter converter = new JsonConverter(inputFile, trainingFile, poisFile);
		try {
			converter.parseTripAdvisorJSON();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
}