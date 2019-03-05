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

		System.out.println("Processing SKPQ query...\n");

		if (debug) {
			printQueryName();
		}

		
			topK = findFeatures(interestObjectSet, keywords);
		

		
		//Predicting
				try {
//					Predictor p = new Predictor("balanceado.arff");
					MyFilteredLearner p = new MyFilteredLearner(); 										
					
					Iterator<SpatialObject> it = topK.iterator();			
					
					ArrayList<String> reviews = new ArrayList<>();
					
					//reviews do usuário?
					reviews.add("slept like a baby");
					reviews.add("The hotel was excellent in all aspects. Lobby was warm and friendly, staff was knowledgeable. The room was clean, comfortable and quiet. My one night stay was outstanding. "
							+ "The location of this best western was right off the interstate highway with plenty of restaurants to choose from. It also had its own restaurant.");
					reviews.add("horrible place");
					reviews.add("disgusting");
					reviews.add("I will not come back to this horrible place");
					
					int count = 0;
					while(it.hasNext()){
						
						SpatialObject obj = it.next();
						
						double score = p.classifyInstance2(count);
						
						System.out.println("Descrição? " + obj.getName());
						System.out.println("Score antigo: " + obj.getScore());
						System.out.println("Score novo: " + score);
						System.out.println("Score somado: " + ((score*0.2)+obj.getScore()));
						
						obj.setScore(((score*0.2)+obj.getScore()));
						
						pTopK.add(obj);
						count++;
					}
				} catch (Exception e1) {
					System.out.println("Error during prediction process");
					e1.printStackTrace();
				}
				
				try {	
					if(!pTopK.isEmpty()){
						saveResults(pTopK);
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

	@Override
	public void printQueryName(){
		System.out.println("\nk = " + k + " | keywords = [ " + keywords + " ]" + " | type = range [radius = " + radius + "]\n\n");
	}
}
