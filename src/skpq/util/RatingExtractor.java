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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.LineEvent;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;

public class RatingExtractor {

	private HashMap<String, String> objetosInteresse;
	private WebContentCache ratingCache, tripCache, uriCache, personalizedCache;
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

		//stores google rates
		ratingCache = new WebContentCache("ratings_dubai.ch");
		ratingCache.load();
		
		//stores hotels uri
		uriCache = new WebContentCache("uri.ch");
		uriCache.load();

		//stores TA ratings
		tripCache = new WebContentCache("trip_dubai.ch");
		tripCache.load();
		
		//relate a osm hotel name to its name in opinrank dataset 
		personalizedCache = new WebContentCache("osm_to_opddb.ch");
		personalizedCache.load();
		
		//hotel description on Google
		descriptionCache = new WebContentCache("googleDescriptions.ch");
		descriptionCache.load();

		try {
			googleAPI = new GooglePlaces(readGoogleUserKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static public String readGoogleUserKey() throws IOException {

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
			else if(ratingMode.equals("tripAdvisor")){
				
				String uriBN = queryResult.substring(queryResult.indexOf("BN=")+3, queryResult.indexOf(", score="));
				String label = null;		
				
				if(uriCache.containsKey(uriBN)){
					label = uriCache.getDescription(uriBN);					
				}
				else{
					BufferedReader uriReader = new BufferedReader(
							(new InputStreamReader(new FileInputStream(new File("hotel_LGD.txt")), "ISO-8859-1")));
					
					String uriLine = uriReader.readLine();
					
					while(uriLine != null){
						
						String uriCheck = uriLine.substring(uriLine.indexOf("http")).trim();						
						
						if(uriBN.equals(uriCheck)){
							
							String[] uriLineVec = uriLine.split("http");							
							label = uriLineVec[0].split("[0-9]+\\.[0-9]+ [0-9]+\\.[0-9]+")[1].trim();							
						}
						uriLine = uriReader.readLine();
					}
					uriCache.putDescription(uriBN, label);
					uriCache.store();
					uriReader.close();
				}
				rateResults.add(rateObjectwithTA(label, lat, lgt, score));
			}else if(ratingMode.equals("personalized")){
				rateResults.add(ratePersonalizedQuery(osmLabel, score, 1));
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
			String osmLabel = queryResult.substring(queryResult.indexOf("OSMlabel=")+9, queryResult.indexOf(", lat="));			
			String score = queryResult.split("score=")[1].split("\\]")[0];

			if(ratingMode.equals("default")){
				rateResults.add(rateObject(osmLabel, lat, lgt));
			}
			else if(ratingMode.equals("cossine")){
				rateResults.add(rateObjectwithCossine(osmLabel, lat, lgt, score));
			}
			else if(ratingMode.equals("tripAdvisor")){
				osmLabel = queryResult.substring(queryResult.indexOf("BN=")+3, queryResult.indexOf(", score="));
				rateResults.add(rateObjectwithTA(osmLabel, lat, lgt, score));
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

	//profiles é obtido da tabela dubai.txt, começa na coluna 11 (contagem iniciando de 0). O numero no profile começa de zero.
	private String ratePersonalizedQuery(String osmLabel, String score, int profile) throws IOException{
		
		String result = "";		
		
		if (debug) {
			System.out.println("\n\nEvaluating object: " + osmLabel + "\n");
		}

		if (personalizedCache.containsKey(osmLabel)) {

			if (debug){
				System.out.println("Got it from cache: " + osmLabel + " -- " + personalizedCache.getDescription(osmLabel));
			}
			String[] profiles = personalizedCache.getDescription(osmLabel).split(",");
			
			result = "osmLabel=" + osmLabel + " score=" + score + " rate=" + profiles[profile];
//			result = osmLabel + " " + profiles[profile];
		} else {
			
			BufferedReader link = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("profiles/hotels/hotel profiles.info")), "ISO-8859-1")));
			
			String line = link.readLine();
//			System.out.println(line);
//			System.out.println(osmLabel);
			String fileID = null;
			System.out.println(osmLabel);
			//Search for the hotel fileID in opinrankdataset 
			while(line != null){
				
				line = line.trim();
				if(line.contains(osmLabel)){
					String[] vec = line.split("->");
					if(vec.length > 1){
						fileID = vec[1].trim();	
					}else{
						fileID="vazio";
					}
					break;
				}else{
					line = link.readLine();
				}
			}			
			link.close();
			
			BufferedReader rates = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("dubai.txt")), "ISO-8859-1")));
			
			line = rates.readLine();
//			System.out.println(line);
//			System.out.println("FileID " + fileID);
			if(fileID.equals("vazio")){
				
				String ratesValues = 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0;
								
				result = "osmLabel=" + osmLabel + " score=" + score + " rate=" + 0;
				
				personalizedCache.putDescription(osmLabel, ratesValues);
				personalizedCache.store();
			}else{					
				while(line != null){
					if(line.contains(fileID)){
						String[] lineVec = line.split(",");
						String ratesValues = lineVec[11] + "," + lineVec[12] + "," + lineVec[13] + "," + lineVec[14]
								+ "," + lineVec[15] + "," + lineVec[16] + "," + lineVec[17];
						
//						result = osmLabel + " " + lineVec[profile+11];
						result = "osmLabel=" + osmLabel + " score=" + score + " rate=" + lineVec[profile+11];
						
						personalizedCache.putDescription(osmLabel, ratesValues);
						personalizedCache.store();
						break;
					}	
					line = rates.readLine();
				}
		}
			rates.close();
		}	
//		System.out.println(result);
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
	
	//Remover
	private String rateObjectwithComments (String osmLabel, String lat, String lgt, String score) throws IOException{

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
	
	//Rate object with TripAdvisor judgments
	private String rateObjectwithTA (String osmLabel, String lat, String lgt, String score) throws IOException{

		String key = lat + " " + lgt;
		String result;

		if (debug) {
			System.out.println("\n\nAvaliando objeto OSM: " + osmLabel + "\n");
		}

		if (tripCache.containsKey(osmLabel) && !key.equals("null null") && !key.equals("0.0 0.0")) {

			if (debug){
				System.out.println("Pegou do cache: " + osmLabel + " -- " + tripCache.getDescription(osmLabel));
			}

			result = "osmLabel=" + osmLabel + " googleDescription=" + descriptionCache.getDescription(osmLabel)
			+ " score=" + score + " rate=" + tripCache.getDescription(osmLabel);
		} else if(!key.equals("null null") && !key.equals("0.0 0.0")){
			
			BufferedReader readerLink = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("osmLinkTrip.txt")), "ISO-8859-1")));
			
			BufferedReader readerRate = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("qdubai_9.q")), "ISO-8859-1")));

			String tripLabel="";
			String rate="";
			String lineLink = readerLink.readLine();
			boolean inserted = false;

			while(lineLink != null){
				
				String[] lineLinkVec = lineLink.split(";");
				
				if(lineLinkVec.length > 1){
					String[] labelVec = lineLinkVec[1].split("\\)");

					String label = labelVec[labelVec.length - 1].trim();
					
					if(label.equals(osmLabel)){
						tripLabel=lineLinkVec[0];
						break;
					}	
				}
				lineLink = readerLink.readLine();
			}
			readerLink.close();
//			System.out.println("Trip = " + tripLabel);
			String lineRate = readerRate.readLine();
			
			while(lineRate != null){
				String[] lineRateVec = lineRate.split(";");
				
				if(lineRateVec[0].equals(tripLabel)){
					rate=lineRateVec[1];
					tripCache.putDescription(osmLabel, rate);
					tripCache.store();
					inserted = true;
					break;
				}
				lineRate = readerRate.readLine();
			}	
			
			readerRate.close();
			
			if(!inserted){
				tripCache.putDescription(osmLabel, "0.0");
				tripCache.store();
			}
			
			result = "osmLabel=" + osmLabel + " googleDescription=" + descriptionCache.getDescription(osmLabel)
			+ " score=" + score + " rate=" + tripCache.getDescription(osmLabel);
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
			else if(ratingMode.equals("tripAdvisor")){
				rateResults.add(rateObjectwithTA(osmLabel, lat, lgt, score));
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
			else if(ratingMode.equals("tripAdvisor")){
				rateResults.add(rateObjectwithTA(osmLabel, lat, lgt, score));
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
	
	@Deprecated
	public void readTripAdvisorJudgments(String fileName) throws IOException{

		BufferedReader reader = new BufferedReader(
				(new InputStreamReader(new FileInputStream(new File(fileName)), "ISO-8859-1")));
		
		HashMap<String, String> tripToOsm = new HashMap<String, String>();
//		TreeSet<Hotel> aux = new TreeSet<>();

		int[] count = new int[162];

		for(int i = 0; i < count.length; i++){
			count[i] = 0;
		}

		String trip = reader.readLine();

		while(trip.contains("#")){
			trip = reader.readLine();
		}
		
		int b = 0;
		while(b == 0){
			System.out.println(trip);
			
			String[] tripVec = trip.split(";")[0].split("_");		
			
			BufferedReader osmHotels = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("hotel.txt")), "ISO-8859-1")));
			
			String hotelLine = osmHotels.readLine();

			int flag = 0;

			for(int i = 2; i < tripVec.length; i++){
				while(hotelLine != null){

					String[] labelVec = hotelLine.split("\\)");

					String REGEX = "\\s*" + tripVec[i].toLowerCase() + "\\s";
					Pattern pattern;
					Matcher matcher;
					pattern = Pattern.compile(REGEX);
				      
					String label = labelVec[labelVec.length - 1].trim();
					matcher = pattern.matcher(label.toLowerCase());
					System.out.println(label.toLowerCase() + " --> " + tripVec[i].toLowerCase() + " --> " + matcher.lookingAt());
					System.out.println(label.toLowerCase() + " --> " + tripVec[i].toLowerCase() + " --> " + label.toLowerCase().matches("\\b"+tripVec[i].toLowerCase()+"\\b"));
					if(matcher.lookingAt()){
//						System.out.println(label.toLowerCasse() + " --> " + tripVec[i] + " --> " + matcher.lookingAt());
						count[flag] = count[flag] + 1;						
					}
					flag++;
					hotelLine = osmHotels.readLine();
				}
				osmHotels = new BufferedReader(
						(new InputStreamReader(new FileInputStream(new File("hotel.txt")), "ISO-8859-1")));
				hotelLine = osmHotels.readLine();
				flag = 0;
			}

			String maxLine="";
			int maxCount = 0;

			osmHotels = new BufferedReader(
					(new InputStreamReader(new FileInputStream(new File("hotel.txt")), "ISO-8859-1")));

			hotelLine = osmHotels.readLine();
			int a = 0;
			while(hotelLine != null){
				if(count[a] >= maxCount){
					maxCount = count[a];
					maxLine = hotelLine;
				}
				a++;
				hotelLine = osmHotels.readLine();
			}

			String[] labelVec = maxLine.split("\\)");

			String label = labelVec[labelVec.length - 1].trim();

			if(maxCount > 0){
				tripToOsm.put(label, trip);
			}
			else{
				tripToOsm.put(label, "Não encontrou");
			}
				
			trip = reader.readLine();
			for(int i = 0; i < count.length; i++){
				count[i] = 0;
			}
			b++;
		}
		System.out.println(tripToOsm.toString());
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
				RatingExtractor obj = new RatingExtractor("personalized");
				
				obj.rateLODresult("pskpq/PSPKQ-LD [k=5, kw=amenity].txt");
				
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
