package skpq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import predictor.MyLearner;
import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;
import se.walkercrou.places.Review;
import skpq.util.Datasets;
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
//	private WebContentCache reviewCache;

	public PersonalizedSKPQSearch(int k, String keywords, String neighborhood, double radius, StarRTree objectsOfInterest, boolean debug) throws IOException {
		super(k, keywords, objectsOfInterest, debug);
		this.radius = radius;	
		
//		reviewCache = new WebContentCache("reviews.ch");
//		reviewCache.load();
		
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
			interestObjectSet = loadObjectsInterest("hotelLondon_LGD.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Processing PSKPQ query...\n");

		if (debug) {
			printQueryName();
		}
		
//			topK = findFeaturesLGD(interestObjectSet, keywords, radius, "default");
		Datasets data = new Datasets();
		try {
			topK = data.loadResultstoPersonalize("SKPQ", keywords, k);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
				
		//Predicting
				try {
					MyLearner p = new MyLearner("serviceLondon"); 										
					
					Iterator<SpatialObject> it = topK.iterator();			
					
					//Used with Dubai dataset because there is two Novotel in this dataset
//					int count = 0;														
					
					while(it.hasNext()){
						
						SpatialObject obj = it.next();
						
						String hotelName = obj.getName().split("\\(hotel\\)")[1].trim(); 
						
						//Used with Dubai dataset because there is two Novotel in this dataset
//						if(hotelName.equals("Novotel")){
//							if(count == 0){
//								count++;
//								hotelName=hotelName+"25";
//							}else{
//								hotelName=hotelName+"24";
//							}						
//						}

						double score = p.classifyHotel(hotelName);
						
//						System.out.println("Descrição? " + hotelName);
//						System.out.println("Score antigo: " + obj.getScore());
//						System.out.println("Score novo: " + score);
//						System.out.println("Score somado: " + (score+obj.getScore()) + "\n");
						
						obj.setScore((score + obj.getScore()));
												
						pTopK.add(obj);
//						count++;
					}
				} catch (Exception e1) {
					System.out.println("Error during prediction process");
					e1.printStackTrace();
				}
				
				try {	
					if(!pTopK.isEmpty()){
//						saveGroupResults(pTopK);
						saveResults(pTopK);
					}
					//Retirei porque o NDCG não me interessa por agora
//						evaluateQuery(keywords, null, k);									
				} catch (IOException e) {
					e.printStackTrace();
				}		
				return pTopK;
	}

	protected void saveResults(TreeSet<SpatialObject> topK) throws IOException {			

		Writer outputFile = new OutputStreamWriter(
				new FileOutputStream("pskpq/PSKPQ-LD [" + "k=" + k + ", kw=" + getKeywords() + "].txt"), "ISO-8859-1");

		Iterator<SpatialObject> it = topK.descendingIterator();

		for (int a = 1; a <= topK.size(); a++) {
			
			SpatialObject obj = it.next();
			
			if(debug) {
				System.out.println("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
						+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
			}
			
			outputFile.write("-->[" + a + "]  " + "[OSMlabel=" + obj.getName() + ", lat=" + obj.getLat() + ", lgt="
					+ obj.getLgt() + ", score=" + obj.getScore() + "]\n");
		}

		outputFile.close();
	}
	
	//Don't use it! Nao usar quando estiver carregando resultados ja existentes do SKPQ. Esta lendo o 20 e colocando valores que nao deveria estar no top-5 mas que estao no top 20
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
