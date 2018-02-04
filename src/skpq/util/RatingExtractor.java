package skpq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;

public class RatingExtractor {

	private HashMap<String, String> objetosInteresse;
	private WebContentCache ratingCache;
	private WebContentCache descriptionCache;
	private GooglePlaces googleAPI;
	private Writer ratingBkp;
	private final boolean debug = false;
	private String bkpFileName = "ratings.txt";
	private String ratingMode;

	public RatingExtractor(String ratingMode) throws IOException {

		this.ratingMode = ratingMode;

		objetosInteresse = new HashMap<String, String>();
		ratingBkp = new OutputStreamWriter(new FileOutputStream(bkpFileName, true), "ISO-8859-1");

		ratingCache = new WebContentCache("ratings_dubai.ch");
		ratingCache.load();

		descriptionCache = new WebContentCache("googleDescriptions.ch");
		descriptionCache.load();

		try {
			googleAPI = new GooglePlaces(readGoogleUserKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readGoogleUserKey() throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File("key.info")), "ISO-8859-1")));

		String key = reader.readLine();

		if (key != null) {
			reader.close();
			return key;
		} else {
			System.out.println("WARNING: You did not assign a Google User Key!");
			reader.close();
			return " ";
		}			
	}

	public ArrayList<String> rateLODresult(String fileName) throws IOException{

		ArrayList<String> rateResults = new ArrayList<>();

		importObjectsInterest("hotel.txt", "hotel");

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		// read file until pointer reaches the query results
		while (!line.contains("-->")) {
			line = reader.readLine();
		}

		// line has the first query result
		String queryResult = line;

		while (queryResult != null) {

			String lat = queryResult.substring(queryResult.indexOf("lat=")+4, queryResult.indexOf(", lgt="));
			String lgt = queryResult.substring(queryResult.indexOf("lgt=")+4, queryResult.indexOf(", score="));
			String key = lat + " " + lgt;
			String osmLabel = objetosInteresse.get(key);			
			String score = queryResult.split("score=")[1].split("\\]")[0];

			if(ratingMode.equals("default")){
				rateResults.add(rateObject(osmLabel, lat, lgt));
			}
			else if(ratingMode.equals("cossine")){
				rateResults.add(rateObjectwithCossine(osmLabel, lat, lgt, score));
			}

			queryResult = reader.readLine();
		}

		reader.close();		
		ratingBkp.close();

		return rateResults;
	}

	public ArrayList<String> rateRangeLODresult(String fileName) throws IOException{

		ArrayList<String> rateResults = new ArrayList<>();

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		// read file until pointer reaches the query results
		while (!line.contains("-->")) {
			line = reader.readLine();
		}

		// line has the first query result
		String queryResult = line;

		while (queryResult != null) {

			String lat = queryResult.substring(queryResult.indexOf("lat=")+4, queryResult.indexOf(", lgt="));
			String lgt = queryResult.substring(queryResult.indexOf("lgt=")+4, queryResult.indexOf(", score="));			
			String osmLabel = queryResult.substring(queryResult.indexOf("OSMlabel=")+9, queryResult.indexOf(", lat="));;			
			String score = queryResult.split("score=")[1].split("\\]")[0];

			if(ratingMode.equals("default")){
				rateResults.add(rateObject(osmLabel, lat, lgt));
			}
			else if(ratingMode.equals("cossine")){
				rateResults.add(rateObjectwithCossine(osmLabel, lat, lgt, score));
			}

			queryResult = reader.readLine();
		}

		reader.close();		
		ratingBkp.close();

		return rateResults;
	}

	private String rateObject(String osmLabel, String lat, String lgt) throws IOException{

		String key = lat + " " + lgt;
		String result;
		if (debug) {
			System.out.println("\n\nAvaliando objeto OSM: " + osmLabel + " -- " + key + "\n");
		}

		if (ratingCache.containsKey(osmLabel)) {

			if (debug){
				System.out.println("Pegou do cache: " + osmLabel + " -- " + ratingCache.getDescription(osmLabel));
			}

			result = osmLabel + " " + ratingCache.getDescription(osmLabel);
		} else {

			List<Place> places = null;

			try{
				places = googleAPI.getPlacesByQueryPosition(osmLabel, lat, lgt,
						GooglePlaces.MAXIMUM_RESULTS);

				if (places.size() > 1) {

					String candidate = "Empty";
					double maxRating = 0;

					for (int a = 0; a < places.size(); a++) {

						Place obj = places.get(a);

						String objStr = obj.getName() + " " + obj.getLatitude() + " " + obj.getLongitude() + " "
								+ obj.getRating();

						if (debug)
							System.out.println("Obteve do Google: " + objStr);

						if (lat.regionMatches(0, Double.toString(obj.getLatitude()), 0, 5)
								&& lgt.regionMatches(0, Double.toString(obj.getLongitude()), 0, 5)) {

							double rating = obj.getRating();

							if (rating > maxRating) {
								candidate = objStr;
								maxRating = rating;
							}
						}
					}
					// adiciona escolhido
					ratingCache.putDescription(osmLabel, Double.toString(maxRating));
					// salva backup com mais informações dos ratings
					ratingBkp.append(osmLabel + "[ " + key + " ]" + " -- Google ---> " + candidate + "\n");

					if (debug){
						System.out.println("Salvou no cache: " + osmLabel + " -- " + maxRating);
					}
					result =  osmLabel + " " + maxRating;
				} else {
					ratingCache.putDescription(osmLabel, Double.toString(places.get(0).getRating()));
					String objStr = places.get(0).getName() + " " + places.get(0).getLatitude() + " "
							+ places.get(0).getLongitude() + " " + places.get(0).getRating();
					ratingBkp.append(osmLabel + "[ " + key + " ]" + " -- Google ---> " + objStr + "\n");
					if (debug) {
						System.out.println("Obteve do Google: " + objStr);
						System.out.println("Salvou no cache: " + osmLabel + " -- " + places.get(0).getRating());
					}
					result =  osmLabel + " " + places.get(0).getRating();
				}		

				if(debug)
					System.out.println("\n\n");					

			}catch(se.walkercrou.places.exception.GooglePlacesException e){

				result = osmLabel + " " + "0.0";
				ratingCache.putDescription(osmLabel, "0.0");
				ratingCache.store();
				return result;
			}			
			ratingCache.store();		
		}		
		return result;
	}	

	private String rateObjectwithCossine (String osmLabel, String lat, String lgt, String score) throws IOException{

		String key = lat + " " + lgt;
		String result;

		if (debug) {
			System.out.println("\n\nAvaliando objeto OSM: " + osmLabel + " -- " + key + "\n");
		}

		if (ratingCache.containsKey(osmLabel) && !key.equals("null null") && !key.equals("0.0 0.0")) {

			if (debug){
				System.out.println("Pegou do cache: " + osmLabel + " -- " + ratingCache.getDescription(osmLabel));
			}

			result = "osmLabel=" + osmLabel + " googleDescription=" + descriptionCache.getDescription(osmLabel)
			+ " score=" + score + " rate=" + ratingCache.getDescription(osmLabel);
		} else if(!key.equals("null null") && !key.equals("0.0 0.0")){

			List<Place> places = null;

			try{
				places = googleAPI.getPlacesByQueryPosition(osmLabel, lat, lgt,
						GooglePlaces.MAXIMUM_RESULTS);

				if (places.size() > 1) {

					String candidate = "Empty";
					double maxRating = 0;

					for (int a = 0; a < places.size(); a++) {

						Place obj = places.get(a);

						String objStr = obj.getName() + " " + obj.getLatitude() + " " + obj.getLongitude() + " "
								+ obj.getRating();

						if (debug)
							System.out.println("Obteve do Google: " + objStr);

						if (lat.regionMatches(0, Double.toString(obj.getLatitude()), 0, 5)
								&& lgt.regionMatches(0, Double.toString(obj.getLongitude()), 0, 5)) {

							double rating = obj.getRating();

							if (rating > maxRating) {
								candidate = objStr;
								maxRating = rating;
							}
						}
					}
					// adiciona escolhido
					ratingCache.putDescription(osmLabel, Double.toString(maxRating));

					String description = candidate.split("[0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+")[0].trim();
					descriptionCache.putDescription(osmLabel, description);

					// salva backup com mais informações dos ratings
					ratingBkp.append(osmLabel + "[ " + key + " ]" + " -- Google ---> " + candidate + "\n");

					if (debug){
						System.out.println("Salvou no cache: " + osmLabel + " -- " + maxRating);
					}
					result =  "osmLabel=" + osmLabel + " googleDescription=" + description + " score=" + score + " rate=" + maxRating;
				} else {
					ratingCache.putDescription(osmLabel, Double.toString(places.get(0).getRating()));
					descriptionCache.putDescription(osmLabel, places.get(0).getName());
					String objStr = places.get(0).getName() + " " + places.get(0).getLatitude() + " "
							+ places.get(0).getLongitude() + " " + places.get(0).getRating();
					ratingBkp.append(osmLabel + "[ " + key + " ]" + " -- Google ---> " + objStr + "\n");

					if (debug) {
						System.out.println("Obteve do Google: " + objStr);
						System.out.println("Salvou no cache: " + osmLabel + " -- " + places.get(0).getRating());
					}

					result =  "osmLabel=" + osmLabel + " googleDescription=" + places.get(0).getName() + " score=" + score + 
							" rate=" + places.get(0).getRating();
				}


				if(debug)
					System.out.println("\n\n");					

			}catch(se.walkercrou.places.exception.GooglePlacesException e){

				result =  "osmLabel=" + osmLabel + " googleDescription=empty" + " score=0.1" + "rate=0.0";
				ratingCache.putDescription(osmLabel, "0.1");
				descriptionCache.putDescription(osmLabel, "empty");

				ratingCache.store();
				descriptionCache.store();

				return result;
			}

			ratingCache.store();
			descriptionCache.store();		
		}else{			
			result = "osmLabel=" + osmLabel + " googleDescription=empty" + " score=0.1" + " rate=0.1";
		}
		return result;
	}	

	public ArrayList<String> rateSKPQresults(String fileName) throws IOException {

		ArrayList<String> rateResults = new ArrayList<>();

		importObjectsInterest("hotel.txt", "hotel");

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		// read file until pointer reaches the query results
		while (!line.contains("-->")) {
			line = reader.readLine();
		}

		// line has the first query result
		String queryResult = line;

		while (queryResult != null && queryResult.contains("-->")) {

			String[] qResVec = queryResult.split(" ");

			String lat = qResVec[4].split("=|,")[1];
			String lgt = qResVec[5].split("=|,")[1];
			String key = lat + " " + lgt;
			String osmLabel = objetosInteresse.get(key);		
			String score = qResVec[3].split("=|,")[1];

			if (debug) {
				System.out.println("Avaliando objeto OSM: " + osmLabel + " -- " + key + "\n");
			}					

			if(ratingMode.equals("default")){
				rateResults.add(rateObject(osmLabel, lat, lgt));
			}
			else if(ratingMode.equals("cossine")){
				rateResults.add(rateObjectwithCossine(osmLabel, lat, lgt, score));
			}
			else if(ratingMode.equals("cossinePenalty")){
				System.out.println("Not implemented yet!");
			}

			queryResult = reader.readLine();
		}

		reader.close();
		return rateResults;
	}

	public ArrayList<String> rateRangeResults(String fileName) throws IOException {

		ArrayList<String> rateResults = new ArrayList<>();

		importObjectsInterest("dubai_abu-dhabi.txt");

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		// read file until pointer reaches the query results
		while (!line.contains("-->")) {
			line = reader.readLine();
		}

		// line has the first query result
		String queryResult = line;

		while (queryResult != null && queryResult.contains("-->")) {

			String[] qResVec = queryResult.split(" ");

			String lat = qResVec[4].split("=|,")[1];
			String lgt = qResVec[5].split("=|,")[1];
			String key = lat + " " + lgt;
			String osmLabel = objetosInteresse.get(key);			
			String score = qResVec[3].split("=|,")[1];

			if (debug) {
				System.out.println("Avaliando objeto OSM2: " + osmLabel + " -- " + key + "\n");
			}					

			if(ratingMode.equals("default")){
				rateResults.add(rateObject(osmLabel, lat, lgt));
			}
			else if(ratingMode.equals("cossine")){
				//				System.out.println("cossine");
				//				System.out.println(queryResult);
				rateResults.add(rateObjectwithCossine(osmLabel, lat, lgt, score));
			}
			else if(ratingMode.equals("cossinePenalty")){
				System.out.println("Not implemented yet!");
			}

			queryResult = reader.readLine();
		}
		reader.close();
		return rateResults;
	}

	private void importObjectsInterest(String fileName, String category) throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		while (line != null) {
			String[] lineVec = line.split(" ");
			String[] labelVec = line.split("\\("+category+"\\)");

			String key = lineVec[1] + " " + lineVec[2];
			String label = labelVec[labelVec.length - 1].trim();

			objetosInteresse.put(key, label);

			line = reader.readLine();
		}

		reader.close();
	}

	private void importObjectsInterest(String fileName) throws IOException {

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));

		String line = reader.readLine();

		while (line != null) {
			String[] lineVec = line.split(" ");
			String[] labelVec = line.split("\\)");

			String key = lineVec[1] + " " + lineVec[2];
			String label = labelVec[labelVec.length - 1].trim();

			objetosInteresse.put(key, label);

			line = reader.readLine();
		}
		reader.close();
	}


	/*Create a cache file with <osmLabel, googleDescription> tuples. The information is obtained
	 * from an existing backup file.
	 */
	public void createDescriptionCachefromBKP() throws IOException{

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(bkpFileName)), "ISO-8859-1")));

		String line = reader.readLine();

		while (line != null) {

			if(debug)
				System.out.println(line);

			String[] lineSplited = line.split("-- Google --->");


			String osmLabel = lineSplited[0].split("\\[ [0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+ \\]")[0].trim();
			String googleDescription = lineSplited[1].split("[0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+")[0].trim();

			if(debug){
				System.out.println("OsmLabel: " + osmLabel);
				System.out.println("GoogleDescription: " + googleDescription);
			}

			descriptionCache.putDescription(osmLabel, googleDescription);

			line = reader.readLine();
		}

		descriptionCache.store();
		reader.close();
	}

	public String getGoogleDescription(String osmLabel, String lat, String lgt) throws IOException{

		if (descriptionCache.containsKey(osmLabel)) {

			if (debug)
				System.out.println("Pegou do cache: " + osmLabel + " -- " + ratingCache.getDescription(osmLabel));

			return osmLabel + " " + descriptionCache.getDescription(osmLabel);
		} else {

			List<Place> places = null;

			places = googleAPI.getPlacesByQueryPosition(osmLabel, lat, lgt,
					GooglePlaces.MAXIMUM_RESULTS);

			if (places.size() > 1) {

				String candidate = "Empty";
				double maxRating = 0;

				for (int a = 0; a < places.size(); a++) {

					Place obj = places.get(a);

					String objStr = obj.getName() + " " + obj.getLatitude() + " " + obj.getLongitude() + " "
							+ obj.getRating();

					if (debug)
						System.out.println("Obteve do Google: " + objStr);

					if (lat.regionMatches(0, Double.toString(obj.getLatitude()), 0, 5)
							&& lgt.regionMatches(0, Double.toString(obj.getLongitude()), 0, 5)) {

						double rating = obj.getRating();

						if (rating > maxRating) {
							candidate = objStr;
							maxRating = rating;
						}
					}
				}

				ratingCache.putDescription(osmLabel, Double.toString(maxRating));

				String description = candidate.split("[0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+")[0].trim();
				descriptionCache.putDescription(osmLabel, description);

				ratingBkp.append(osmLabel + "[ " + osmLabel + " ]" + " -- Google ---> " + candidate + "\n");

				if (debug){
					System.out.println("Salvou no cache: " + osmLabel + " -- " + maxRating);
				}
				return  "osmLabel=" + osmLabel + " googleDescription=" + description;
			}
			if(debug)
				System.out.println("\n\n");	
			return null;
		}					
	}

	public static void main(String[] args) throws IOException {

		//String fileName = "SPKQ-LD [k=20, kw=amenity].txt";
		//		String fileName = "SPKQ [k=10, kw=amenity].txt";
		//		
		//		Writer output = new OutputStreamWriter(new FileOutputStream(fileName.split("\\.")[0] + " --- ratings.txt"), "ISO-8859-1");
		//		RatingExtractor obj = new RatingExtractor("default");

		//obj.createDescriptionCachefromBKP();
		//		ArrayList<String> rateResults = obj.rateSKPQresults(fileName);
		//ArrayList<String> rateResults = obj.rateLODresult(fileName);

		//		System.out.println("\n\n --- Resultados ---\n");
		//		
		//		for (String x : rateResults) {
		//									
		//			output.write(x + "\n");	
		//		}		
		//		output.close();
	}
}
