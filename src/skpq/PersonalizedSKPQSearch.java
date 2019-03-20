package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import predictor.MyFilteredLearner;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;
import se.walkercrou.places.Review;
import skpq.util.RatingExtractor;
import skpq.util.User;
import skpq.util.WebContentCache;
import weka.Predictor;
import xxl.util.StarRTree;

/**
 * Process a Spatial Preference Keyword Query using LOD.
 * 
 * @author João Paulo
 */
public class PersonalizedSKPQSearch extends SpatialQueryLD {

	private double radius;
	GooglePlaces googleAPI;
	private WebContentCache reviewCache;

	public PersonalizedSKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;	
		
		reviewCache = new WebContentCache("reviews.ch");
		reviewCache.load();
		
		try {
			googleAPI = new GooglePlaces(RatingExtractor.readGoogleUserKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public TreeSet<SpatialObject> execute(String queryKeywords, int k) {

		List<SpatialObject> interestObjectSet = new ArrayList<SpatialObject>();
		TreeSet<SpatialObject> topK = new TreeSet<>();
		TreeSet<SpatialObject> pTopK = new TreeSet<>();
		
		try {
			interestObjectSet = loadObjectsInterest("hotel_LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing PSKPQ query...\n");

		if (debug) {
			printQueryName();
		}
		
			topK = findFeaturesLGD(interestObjectSet, keywords, radius);
				
		//Predicting
				try {
//					Predictor p = new Predictor("balanceado.arff");
					MyFilteredLearner p = new MyFilteredLearner("room"); 										
					
					Iterator<SpatialObject> it = topK.iterator();			
					
					ArrayList<String> reviews = new ArrayList<>();
					
					//reviews do usuário?
//					reviews.add("slept like a baby");
//					reviews.add("The hotel was excellent in all aspects. Lobby was warm and friendly, staff was knowledgeable. The room was clean, comfortable and quiet. My one night stay was outstanding. "
//							+ "The location of this best western was right off the interstate highway with plenty of restaurants to choose from. It also had its own restaurant.");
//					reviews.add("horrible place");
//					reviews.add("disgusting");
//					reviews.add("I will not come back to this horrible place");
					
					int count = 0;
					while(it.hasNext()){
						
						SpatialObject obj = it.next();
						
						String hotelName = obj.getName().split("\\(hotel\\)")[1].trim(); 
						
						if(hotelName.equals("novotel")){
							if(count == 0){
								count++;
								hotelName=hotelName+"25";
							}else{
								hotelName=hotelName+"24";
							}						
						}
						double score = p.classifyHotel(hotelName);
						
//						System.out.println("Descrição? " + hotelName);
//						System.out.println("Score antigo: " + obj.getScore());
						System.out.println("Score novo: " + score);
						System.out.println("Score somado: " + (score+obj.getScore()) + "\n");
						
						obj.setScore((score + obj.getScore()));
						
						pTopK.add(obj);
						count++;
					}
				} catch (Exception e1) {
					System.out.println("Error during prediction process");
					e1.printStackTrace();
				}
				
				try {	
					if(!pTopK.isEmpty()){
						saveGroupResults(pTopK);
					}
					//Retirei porque o NDCG não me interessa por agora
//						evaluateQuery(keywords, null, k);									
				} catch (IOException e) {
					e.printStackTrace();
				}		

				return topK;
	}

	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {			

		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("PSKPQ-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {
			SpatialObject obj = it.next();
			System.out.println("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}
	
protected void saveGroupResults(TreeSet<SpatialObject> topK) throws IOException{
		
		/* Imprime 5 */
		Writer outputFile = new OutputStreamWriter(new FileOutputStream("pskpq/PSKPQ-LD [" + "k=" + "5" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		Iterator<SpatialObject> it = topK.descendingIterator();
		
		for(int a = 1; a <= 5; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 10 */
		outputFile = new OutputStreamWriter(new FileOutputStream("pskpq/PSKPQ-LD [" + "k=" + "10" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 10; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 15 */
		outputFile = new OutputStreamWriter(new FileOutputStream("pskpq/PSKPQ-LD [" + "k=" + "15" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 15; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
		
		/* Imprime 20 */
		outputFile = new OutputStreamWriter(new FileOutputStream("pskpq/PSKPQ-LD [" + "k=" + "20" + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");
		
		it = topK.descendingIterator();
		
		for(int a = 1; a <= 20; a++){
			SpatialObject obj = it.next();
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt=" + obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}
		
		outputFile.close();
	}

	@Override
	public void printQueryName(){
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}
